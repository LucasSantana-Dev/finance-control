# Migration Alignment Guide

This document tracks the alignment between backend (Flyway) and frontend (Supabase) migrations.

## Overview

The backend and frontend use **different databases** with **incompatible schemas**:
- **Backend**: Traditional PostgreSQL with BIGINT IDs, `users` table, application-level security
- **Frontend**: Supabase PostgreSQL with UUID IDs, `auth.users` table, Row Level Security (RLS)

Therefore, migrations **cannot be unified** into a single folder. However, they should be **logically aligned** to maintain schema consistency.

## Migration Mapping

| Backend Migration | Frontend Migration | Feature | Status |
|------------------|-------------------|---------|--------|
| V1__initial_schema.sql | 20250101000001_initial_schema.sql | Initial schema (transaction tables) | ✅ Aligned |
| V3__add_completion_and_reconciliation_fields.sql | 20250101000002_add_completion_and_reconciliation_fields.sql | Goals completion & transaction reconciliation | ✅ Aligned |
| V4__add_updated_at_to_transaction_responsibilities.sql | 20250101000003_add_updated_at_to_transaction_responsibilities.sql | Updated_at trigger for responsibilities | ✅ Aligned |
| V13__add_status_and_installment_fields.sql | 20251115000000_consolidate_installments_into_transactions.sql | Transaction status & installments | ✅ Aligned |
| V16__create_user_categories_table.sql | 20251114140000_create_goals_and_categories.sql | User categories | ✅ Aligned |
| V17__add_priority_and_status_to_financial_goals.sql | 20251114140000_create_goals_and_categories.sql | Goals with priority/status | ✅ Aligned |
| V21__create_budgets_table.sql | 20251123054400_create_budgets_table.sql | Budgets table | ✅ Aligned |

## Key Differences

### ID Types
- **Backend**: `BIGSERIAL` (BIGINT) - Auto-incrementing integers
- **Frontend**: `UUID` - Universally unique identifiers

### User References
- **Backend**: `users(id)` - Custom users table with BIGINT
- **Frontend**: `auth.users(id)` - Supabase auth table with UUID

### Category References
- **Backend**: `user_categories(id)` - BIGINT foreign key
- **Frontend**: `categories(id)` - UUID foreign key

### Timestamps
- **Backend**: `TIMESTAMP` (without timezone)
- **Frontend**: `TIMESTAMP WITH TIME ZONE` (Supabase standard)

### Security
- **Backend**: Application-level security (Spring Security, JWT)
- **Frontend**: Row Level Security (RLS) policies in Supabase

### Migration Tools
- **Backend**: Flyway (Java/Gradle) - Runs automatically on app startup
- **Frontend**: Supabase CLI - Manual execution via `supabase db push`

## Best Practices

### 1. Keep Migrations Separate
✅ **DO**: Maintain separate migration folders
- Backend: `src/main/resources/db/migration/`
- Frontend: `supabase/migrations/`

❌ **DON'T**: Try to unify migrations into one folder (incompatible schemas/tools)

### 2. Document Alignment
✅ **DO**: Add comments in migrations referencing the corresponding migration:
```sql
-- Backend migration comment:
-- Aligned with frontend: 20251123054400_create_budgets_table.sql

-- Frontend migration comment:
-- Aligned with backend: V21__create_budgets_table.sql
```

### 3. Update This Document
✅ **DO**: Update this document when creating new migrations
✅ **DO**: Mark alignment status (✅ Aligned, ⚠️ Needs Review, ❌ Not Aligned)

### 4. Consistent Naming
✅ **DO**: Use descriptive names that match across both migrations:
- `create_budgets_table.sql` (both)
- `add_priority_and_status_to_financial_goals.sql` (both)

### 5. Schema Equivalence
✅ **DO**: Ensure logical equivalence even if types differ:
- Same columns (with appropriate type conversions)
- Same constraints (CHECK, UNIQUE, etc.)
- Same indexes (for performance)
- Same business logic

## Repository Structure

### Current Structure (Separate Repos)
```
finance-control/                    # Backend repo
├── src/main/resources/db/migration/
│   ├── V1__initial_schema.sql
│   ├── V16__create_user_categories_table.sql
│   └── V21__create_budgets_table.sql
└── docs/
    └── MIGRATION_ALIGNMENT.md

finance-control-front/             # Frontend repo
├── supabase/migrations/
│   ├── 20251112211308_*.sql
│   ├── 20251114140000_create_goals_and_categories.sql
│   └── 20251123054400_create_budgets_table.sql
└── docs/
    └── MIGRATION_ALIGNMENT.md (copy of this file)
```

### Alternative: Monorepo Structure (Optional)
If you decide to move to a monorepo:
```
finance-control-monorepo/
├── backend/
│   ├── src/main/resources/db/migration/
│   └── ...
├── frontend/
│   ├── supabase/migrations/
│   └── ...
└── docs/
    └── MIGRATION_ALIGNMENT.md
```

**Benefits of Monorepo:**
- Single repository to manage
- Easier to keep migrations aligned
- Shared documentation
- Single CI/CD pipeline (optional)

**Drawbacks:**
- More complex setup
- Requires monorepo tooling (optional)
- Still need separate migration folders (cannot be unified)

## Recommendations

### For One-Person Project (Current)
✅ **Recommended**: Keep separate repos (simpler, less overhead)
- Easier to deploy independently
- Clear separation of concerns
- Less tooling complexity

### For Future Scaling
⚠️ **Consider**: Monorepo if team grows
- Easier collaboration
- Shared tooling
- Unified CI/CD

### Migration Management
✅ **Always**: Keep migrations separate (required due to different databases)
✅ **Always**: Document alignment in this file
✅ **Always**: Add cross-references in migration comments

## Migration Execution

### Backend Migrations
```bash
# Migrations run automatically on Spring Boot startup
# Or manually:
./gradlew flywayMigrate
```

### Frontend Migrations
```bash
# Via Supabase CLI
supabase db push

# Or via Supabase Dashboard
# SQL Editor → Run migration SQL
```

## Troubleshooting

### Migration Out of Sync
If migrations become misaligned:
1. Check this document for expected alignment
2. Compare schema structures (ignore type differences)
3. Create missing migrations to restore alignment
4. Update this document

### Schema Drift
If schemas drift apart:
1. Identify the differences
2. Determine if drift is intentional or accidental
3. Create alignment migrations if needed
4. Update this document

## Important Notes

### TypeScript Types Regeneration Required

After running migrations, **regenerate TypeScript types** to ensure type safety:

```bash
# Regenerate Supabase TypeScript types
supabase gen types typescript --project-id YOUR_PROJECT_ID > src/integrations/supabase/types.ts
```

**Critical**: The following tables/columns are missing from current types and need regeneration:
- `categories` table (complete)
- `goals` table (complete)
- `budgets` table (complete)
- `transactions.status` column
- `transactions.installment_group_id` column
- `transactions.installment_number` column
- `transactions.total_installments` column
- `transactions.installment_amount` column

### Migration Idempotency

All migrations have been updated to be idempotent:
- Use `CREATE TABLE IF NOT EXISTS` for table creation
- Use `DROP POLICY IF EXISTS` before `CREATE POLICY`
- Use `DROP TRIGGER IF EXISTS` before `CREATE TRIGGER`
- Use conditional `DO $$` blocks for column additions
- Use conditional checks for data migrations

### Cross-Reference Comments

All migrations now include cross-reference comments:
- Frontend migrations reference backend Flyway migrations
- Backend migrations should reference frontend Supabase migrations (when updated)

## Notes

- Migrations **cannot be unified** due to incompatible database schemas
- Migrations **should be aligned** to maintain logical consistency
- This document helps track alignment and prevent drift
- Update this document whenever creating new migrations
- **Always regenerate TypeScript types after running migrations**
