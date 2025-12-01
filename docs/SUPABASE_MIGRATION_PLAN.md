# Complete Supabase Migration Plan

## Overview

This document outlines the plan to fully migrate the backend from a separate PostgreSQL database to Supabase, eliminating Flyway migrations and using Supabase migrations exclusively.

## Current State

### Backend (Spring Boot)
- ✅ Already connects to Supabase database (`DatabaseConfig.java`)
- ❌ Still uses Flyway for migrations
- ❌ Uses `Long` (BIGINT) for entity IDs
- ❌ Has separate `users` table (mapped to Supabase via `supabase_user_id`)
- ❌ Uses BIGINT foreign keys

### Frontend (Next.js)
- ✅ Uses Supabase migrations exclusively
- ✅ Uses UUID for all IDs
- ✅ References `auth.users(id)` directly
- ✅ Uses RLS policies

## Migration Goals

1. **Unified Database**: Single Supabase PostgreSQL instance for both frontend and backend
2. **Unified Migrations**: All migrations in `supabase/migrations/` (frontend repo)
3. **UUID IDs**: Convert all entities from `Long` to `UUID`
4. **Direct Auth Integration**: Use `auth.users` directly instead of separate `users` table
5. **RLS Policies**: Add Row Level Security for all tables
6. **Disable Flyway**: Remove Flyway dependency and configuration

## Migration Strategy

### Phase 1: Schema Migration (Supabase Migrations)

#### Step 1.1: Convert Existing Flyway Migrations to Supabase Format

**Action**: Convert all Flyway migrations (`V*.sql`) to Supabase migrations with UUID support.

**Key Changes**:
- Change `BIGSERIAL` → `UUID DEFAULT gen_random_uuid()`
- Change `BIGINT` → `UUID`
- Change `users(id)` references → `auth.users(id)`
- Add RLS policies for all tables
- Add `update_updated_at_column()` trigger function if missing
- Use `TIMESTAMP WITH TIME ZONE` instead of `TIMESTAMP`

**Migration Files to Convert**:
1. `V1__initial_schema.sql` → `YYYYMMDDHHMMSS_initial_schema.sql`
2. `V2__separate_user_profile.sql` → `YYYYMMDDHHMMSS_separate_user_profile.sql`
3. `V3__add_completion_and_reconciliation_fields.sql` → `YYYYMMDDHHMMSS_add_completion_and_reconciliation_fields.sql`
4. ... (all other migrations)

#### Step 1.2: Remove `users` Table Dependency

**Action**: Update all foreign keys to reference `auth.users(id)` directly.

**Changes**:
- Remove `users` table creation
- Update all `user_id BIGINT REFERENCES users(id)` → `user_id UUID REFERENCES auth.users(id)`
- Remove `supabase_user_id` mapping column (no longer needed)

#### Step 1.3: Add RLS Policies

**Action**: Add Row Level Security policies to all tables.

**Pattern**:
```sql
ALTER TABLE table_name ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own records"
ON table_name FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Users can create their own records"
ON table_name FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own records"
ON table_name FOR UPDATE
USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own records"
ON table_name FOR DELETE
USING (auth.uid() = user_id);
```

### Phase 2: Backend Code Migration

#### Step 2.1: Update BaseModel

**File**: `src/main/java/com/finance_control/shared/model/BaseModel.java`

**Changes**:
```java
// FROM:
public abstract class BaseModel<I> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private I id;
    // ...
}

// TO:
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Type(type = "org.hibernate.type.PostgreSQLUUIDType")
    @Column(columnDefinition = "UUID")
    private UUID id;
    // ...
}
```

#### Step 2.2: Update All Entity Classes

**Action**: Change all entity IDs from `Long` to `UUID`.

**Files to Update**:
- `User.java` → Remove (use Supabase Auth directly)
- `Transaction.java` → `UUID id`, `UUID user_id`
- `FinancialGoal.java` → `UUID id`, `UUID user_id`
- `Investment.java` → `UUID id`, `UUID user_id`
- `UserCategory.java` → `UUID id`, `UUID user_id`
- `Notification.java` → `UUID id`, `UUID user_id`
- ... (all entities)

**Pattern**:
```java
// FROM:
@Entity
public class Transaction extends BaseModel<Long> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}

// TO:
@Entity
public class Transaction extends BaseModel {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Remove @ManyToOne relationship (auth.users is external)
}
```

#### Step 2.3: Update Repository Interfaces

**Action**: Change all repository generics from `BaseRepository<Entity, Long>` to `BaseRepository<Entity, UUID>`.

**Files to Update**:
- `TransactionRepository.java`
- `FinancialGoalRepository.java`
- `InvestmentRepository.java`
- `UserCategoryRepository.java`
- ... (all repositories)

**Pattern**:
```java
// FROM:
public interface TransactionRepository extends BaseRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
}

// TO:
public interface TransactionRepository extends BaseRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);
}
```

#### Step 2.4: Update Service Classes

**Action**: Change all service method parameters and return types from `Long` to `UUID`.

**Pattern**:
```java
// FROM:
public TransactionDTO findById(Long id) { ... }
public TransactionDTO create(Long userId, TransactionDTO dto) { ... }

// TO:
public TransactionDTO findById(UUID id) { ... }
public TransactionDTO create(UUID userId, TransactionDTO dto) { ... }
```

#### Step 2.5: Update Controller Classes

**Action**: Change all `@PathVariable Long id` to `@PathVariable UUID id`.

**Pattern**:
```java
// FROM:
@GetMapping("/{id}")
public ResponseEntity<TransactionDTO> getById(@PathVariable Long id) { ... }

// TO:
@GetMapping("/{id}")
public ResponseEntity<TransactionDTO> getById(@PathVariable UUID id) { ... }
```

#### Step 2.6: Update DTOs

**Action**: Change all DTO ID fields from `Long` to `UUID`.

**Pattern**:
```java
// FROM:
public class TransactionDTO {
    private Long id;
    private Long userId;
}

// TO:
public class TransactionDTO {
    private UUID id;
    private UUID userId;
}
```

#### Step 2.7: Remove User Entity and Repository

**Action**: Remove `User.java` entity and `UserRepository.java` since we'll use Supabase Auth directly.

**Files to Remove**:
- `src/main/java/com/finance_control/users/model/User.java`
- `src/main/java/com/finance_control/users/repository/UserRepository.java`
- `src/main/java/com/finance_control/users/service/UserService.java` (or refactor to use Supabase client)

**Alternative**: Keep `UserService` but make it a wrapper around Supabase Auth client.

#### Step 2.8: Update Authentication

**Action**: Update authentication to use Supabase Auth JWT tokens directly.

**Files to Update**:
- `AuthService.java` → Use Supabase Auth client
- `JwtTokenProvider.java` → Remove or adapt for Supabase JWT
- `CustomUserDetailsService.java` → Remove (use Supabase Auth)

### Phase 3: Configuration Changes

#### Step 3.1: Disable Flyway

**File**: `src/main/resources/application.yml`

**Changes**:
```yaml
# FROM:
app:
  flyway:
    enabled: ${FLYWAY_ENABLED:true}

# TO:
app:
  flyway:
    enabled: false
```

#### Step 3.2: Update Hibernate Configuration

**File**: `src/main/resources/application.yml`

**Changes**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Keep validate (migrations handled by Supabase)
    properties:
      hibernate:
        # Add UUID support
        dialect: org.hibernate.dialect.PostgreSQLDialect
        # Ensure UUID types are handled correctly
```

#### Step 3.3: Remove Flyway Dependency

**File**: `build.gradle`

**Changes**:
```gradle
// Remove or comment out:
// implementation 'org.flywaydb:flyway-core'
// implementation 'org.flywaydb:flyway-database-postgresql'
```

### Phase 4: Migration Execution

#### Step 4.1: Create Supabase Migrations

**Action**: Convert all Flyway migrations to Supabase format in `finance-control-front/supabase/migrations/`.

**Naming Convention**: Use timestamp format: `YYYYMMDDHHMMSS_description.sql`

**Order**: Maintain the same logical order as Flyway migrations.

#### Step 4.2: Test Migrations Locally

**Action**: Test all migrations on local Supabase instance.

**Commands**:
```bash
cd finance-control-front
supabase start
supabase db reset  # Apply all migrations
```

#### Step 4.3: Data Migration (if needed)

**Action**: If you have existing data in the backend database, create a migration script to:
1. Export data from old database
2. Transform IDs from BIGINT to UUID
3. Import into Supabase database

**Note**: This is only needed if you have production data. For fresh start, skip this step.

#### Step 4.4: Deploy to Production

**Action**: Apply migrations to production Supabase instance.

**Commands**:
```bash
cd finance-control-front
supabase db push --project-ref YOUR_PROJECT_REF
```

### Phase 5: Testing & Validation

#### Step 5.1: Update Unit Tests

**Action**: Update all tests to use `UUID` instead of `Long`.

**Pattern**:
```java
// FROM:
Long userId = 1L;
Transaction transaction = Transaction.builder().id(1L).build();

// TO:
UUID userId = UUID.randomUUID();
Transaction transaction = Transaction.builder().id(UUID.randomUUID()).build();
```

#### Step 5.2: Update Integration Tests

**Action**: Update integration tests to work with Supabase database and UUID IDs.

#### Step 5.3: End-to-End Testing

**Action**: Test complete user flows:
- User registration/login via Supabase Auth
- Create transactions
- Create goals
- Create budgets
- Dashboard data retrieval

## Migration Checklist

### Schema Migration
- [ ] Convert V1__initial_schema.sql to Supabase format
- [ ] Convert V2__separate_user_profile.sql to Supabase format
- [ ] Convert V3__add_completion_and_reconciliation_fields.sql to Supabase format
- [ ] Convert V4__add_updated_at_to_transaction_responsibilities.sql to Supabase format
- [ ] Convert V5__create_brazilian_market_tables.sql to Supabase format
- [ ] Convert V6__create_dashboard_cache_tables.sql to Supabase format
- [ ] Convert V7_1__create_concurrent_indexes.sql to Supabase format
- [ ] Convert V8__create_unified_investments_table.sql to Supabase format
- [ ] Convert V10__create_open_finance_tables.sql to Supabase format
- [ ] Convert V10_1__open_finance_rls_policies.sql to Supabase format
- [ ] Convert V11__add_email_encryption_and_supabase_mapping.sql to Supabase format
- [ ] Convert V12__enable_rls_for_all_tables.sql to Supabase format
- [ ] Convert V13__add_status_and_installment_fields.sql to Supabase format
- [ ] Convert V14__create_notifications_table.sql to Supabase format
- [ ] Convert V15__create_user_settings_table.sql to Supabase format
- [ ] Convert V16__create_user_categories_table.sql to Supabase format
- [ ] Convert V17__add_priority_and_status_to_financial_goals.sql to Supabase format
- [ ] Convert V18__add_rls_policies_for_new_tables.sql to Supabase format
- [ ] Convert V19__enable_rls_on_missing_tables.sql to Supabase format
- [ ] Convert V20__fix_function_search_path_security.sql to Supabase format
- [ ] Convert V21__create_budgets_table.sql to Supabase format (already done)
- [ ] Add RLS policies to all tables
- [ ] Add `update_updated_at_column()` function if missing
- [ ] Test all migrations locally

### Backend Code Migration
- [ ] Update `BaseModel.java` to use UUID
- [ ] Update `Transaction.java` entity
- [ ] Update `FinancialGoal.java` entity
- [ ] Update `Investment.java` entity
- [ ] Update `UserCategory.java` entity
- [ ] Update `Notification.java` entity
- [ ] Update all other entities
- [ ] Update all repository interfaces
- [ ] Update all service classes
- [ ] Update all controller classes
- [ ] Update all DTOs
- [ ] Remove or refactor `User.java` entity
- [ ] Update authentication to use Supabase Auth
- [ ] Update all tests

### Configuration
- [ ] Disable Flyway in `application.yml`
- [ ] Remove Flyway dependency from `build.gradle`
- [ ] Update Hibernate configuration for UUID support
- [ ] Verify Supabase database connection

### Testing
- [ ] Update unit tests
- [ ] Update integration tests
- [ ] Test user registration/login
- [ ] Test CRUD operations for all entities
- [ ] Test RLS policies
- [ ] End-to-end testing

## Risks & Considerations

### Data Loss Risk
⚠️ **HIGH**: Converting from BIGINT to UUID will break all existing data relationships.

**Mitigation**:
- Create data migration script if production data exists
- Test migration on staging environment first
- Backup database before migration

### Breaking Changes
⚠️ **HIGH**: All API endpoints will change ID types from `Long` to `UUID`.

**Mitigation**:
- Update API documentation
- Version API if needed (`/api/v2/...`)
- Update frontend to handle UUID IDs

### Authentication Changes
⚠️ **MEDIUM**: Moving from JWT to Supabase Auth may require frontend changes.

**Mitigation**:
- Frontend already uses Supabase Auth (good!)
- Backend just needs to validate Supabase JWT tokens

### Migration Complexity
⚠️ **HIGH**: This is a major refactoring affecting all entities.

**Mitigation**:
- Do it incrementally (one entity at a time)
- Test thoroughly after each change
- Keep Flyway enabled until all migrations are complete

## Recommended Approach

### Option A: Big Bang Migration (Fast, Risky)
1. Convert all migrations at once
2. Update all code at once
3. Deploy everything together
4. **Risk**: High chance of breaking things

### Option B: Incremental Migration (Slower, Safer) ✅ **RECOMMENDED**
1. **Phase 1**: Convert migrations for one module (e.g., transactions)
2. **Phase 2**: Update backend code for that module
3. **Phase 3**: Test thoroughly
4. **Phase 4**: Repeat for next module
5. **Risk**: Lower, but takes longer

### Option C: Hybrid Approach (Balanced)
1. Convert all migrations first (schema ready)
2. Update code incrementally (one module at a time)
3. Test each module before moving to next
4. **Risk**: Medium, good balance

## Timeline Estimate

- **Schema Migration**: 2-3 days (converting all Flyway migrations)
- **Backend Code Migration**: 5-7 days (updating all entities, services, controllers)
- **Testing & Validation**: 2-3 days
- **Total**: 9-13 days (2-3 weeks)

## Next Steps

1. **Decide on approach**: Big Bang, Incremental, or Hybrid
2. **Create first Supabase migration**: Convert V1__initial_schema.sql
3. **Test locally**: Ensure migration works
4. **Update one entity**: Start with `Transaction` (most critical)
5. **Test thoroughly**: Ensure everything works
6. **Repeat**: Continue with other entities

## Notes

- Keep Flyway enabled until all migrations are converted and tested
- Use feature flags to gradually enable Supabase-only features
- Maintain backward compatibility during transition if possible
- Document all changes in CHANGELOG.md
