# 💰 Finance Control

A comprehensive financial management system built with Spring Boot, designed to help users track transactions, manage financial goals, and gain insights into their spending patterns.

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.7+-green.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[![CI](https://github.com/LucasSantana/finance-control/actions/workflows/ci.yml/badge.svg)](https://github.com/LucasSantana/finance-control/actions/workflows/ci.yml)
[![SonarQube](https://github.com/LucasSantana/finance-control/actions/workflows/sonarqube.yml/badge.svg)](https://github.com/LucasSantana/finance-control/actions/workflows/sonarqube.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=finance-control&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=finance-control)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=finance-control&metric=coverage)](https://sonarcloud.io/summary/new_code?id=finance-control)

## 🚀 Features

### 📊 Financial Dashboard
- **Interactive Dashboard**: Comprehensive financial overview with key metrics and visualizations
- **Real-time Metrics**: Income, expenses, savings rate, net worth, and monthly balance tracking
- **Spending Analytics**: Top spending categories with percentage breakdowns and transaction counts
- **Monthly Trends**: Historical income/expense trends for chart visualization
- **Goal Progress**: Real-time goal progress monitoring and visualization
- **Financial Metrics**: Detailed metrics including average transaction amounts and largest/smallest transactions
- **Dashboard API**: Complete REST API for dashboard data with caching support

### 🤖 Financial Predictions
- ✅ **NEW**: AI-assisted financial forecasts powered by the OpenAI Responses API
- **Configurable insights**: Tailor forecast horizon, goals, and additional context to refine guidance
- **Structured output**: Deterministic JSON responses with month-by-month projections and recommendations
- **Endpoint**: `POST /dashboard/predictions` (requires `app.ai.openai.enabled=true` and a valid `OPENAI_API_KEY`)

### 💰 Transaction Management
- **Multi-source tracking**: Credit cards, bank accounts, cash, and more
- **Categorization system**: Hierarchical categories and subcategories with full CRUD operations
- **Category management**: Complete REST API for managing transaction categories and subcategories
  - ✅ **NEW**: Full CRUD operations for transaction categories and subcategories
  - ✅ **NEW**: Case-insensitive name validation and duplicate checking
  - ✅ **NEW**: Active/inactive status management for subcategories
  - ✅ **NEW**: Comprehensive test coverage (62 tests) with unit and integration tests
- **Responsibility sharing**: Split transactions between multiple people
- **Installment support**: Track recurring payments and installments
- **Advanced filtering**: Search and filter by date, type, category, and amount
- ✅ **NEW**: Bank statement import (CSV/OFX) with configurable mapping, duplicate detection, and dry-run mode

### 📥 Statement Import
- **Endpoint**: `POST /transactions/import` (multipart form data)
- **Formats supported**: CSV (header-based) and OFX 1.x/2.x
- **Duplicate strategies**: Skip (default) or allow duplicates when importing existing records
- **Dry-run mode**: Validate and simulate imports without persisting transactions
- **Field mapping**: Configure column names, locale, delimiter, and category/source mappings per request

```bash
curl -X POST "https://api.yourdomain.com/transactions/import" \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/statement.csv;type=text/csv" \
  -F 'config={
        "userId": 1,
        "defaultCategoryId": 5,
        "defaultSubtype": "FIXED",
        "defaultSource": "BANK_TRANSACTION",
        "duplicateStrategy": "SKIP",
        "responsibilities": [
          {"responsibleId": 2, "percentage": 100}
        ],
        "csv": {
          "containsHeader": true,
          "delimiter": ";",
          "locale": "pt-BR",
          "dateColumn": "date",
          "descriptionColumn": "description",
          "amountColumn": "amount"
        }
      };type=application/json'
```

> **Tip:** Use the `dryRun` flag to preview results, or provide `categoryMappings` / `sourceEntityMappings` to translate bank labels into Finance Control IDs automatically.

### 🎯 Financial Goals
- **Goal tracking**: Set and monitor financial objectives
- **Progress visualization**: Real-time progress percentage calculation
- **Deadline management**: Track goal completion deadlines
- **Auto-calculation**: Automatic goal progress updates from transactions

### 👥 User Management
- **Secure authentication**: JWT-based authentication system
- **User isolation**: Multi-tenant architecture with data isolation
- **Profile management**: User profile and preferences

### 🔧 Technical Features
- **RESTful API**: Comprehensive REST endpoints with OpenAPI documentation
- **Database migrations**: Flyway-based schema versioning
- **Audit trails**: Automatic creation and update timestamps
- **Validation**: Comprehensive input validation and error handling
- **Testing**: Unit, integration, and E2E test coverage with 80% minimum coverage
- **DTO Mapping**: Type-safe MapStruct mappers for all entities
- **JPA Auditing**: Automatic timestamp management for all entities
- **Redis Caching**: High-performance caching for dashboard and market data
- **Rate Limiting**: API protection with configurable request limits
- **Data Export**: Complete data portability in CSV and JSON formats
- **Monitoring & Alerting**: Enterprise-grade monitoring with Sentry integration
  - ✅ **NEW**: Real-time health checks for database, Redis, and configuration
  - ✅ **NEW**: Comprehensive metrics collection (transactions, users, goals, cache, API errors)
  - ✅ **NEW**: Intelligent alerting system with severity levels and Sentry integration
  - ✅ **NEW**: Performance monitoring with slow operation detection
  - ✅ **NEW**: Custom business metrics and system resource monitoring

### ☁️ Supabase Integration
- **Authentication**: Supabase Auth integration with JWT support
  - ✅ **NEW**: Supabase JWT token validation and user mapping
  - ✅ **NEW**: Dual authentication support (local + Supabase)
  - ✅ **NEW**: Supabase Auth service with signup/login/password reset
  - ✅ **NEW**: REST API endpoints for Supabase authentication
- **Storage**: Supabase Storage for file management
  - ✅ **NEW**: File upload/download/delete operations via WebClient
  - ✅ **NEW**: Avatar uploads with automatic URL generation
  - ✅ **NEW**: Bucket organization (avatars, documents, transactions)
  - ✅ **NEW**: REST API for storage operations
- **Realtime Messaging**: Supabase Realtime for live updates and notifications
  - ✅ **NEW**: WebSocket-based realtime subscriptions (transactions, dashboard, goals)
  - ✅ **NEW**: Real-time dashboard updates and transaction notifications
  - ✅ **NEW**: Goal progress realtime updates
  - ✅ **NEW**: REST API for subscription management
  - ✅ **NEW**: Spring WebSocket integration for client-side updates
- **PostgreSQL Database**: Supabase PostgreSQL integration
  - ✅ **COMPLETE**: Full PostgreSQL integration with Supabase
  - ✅ **WORKING**: Application successfully connects to Supabase database
  - ✅ **MIGRATIONS**: Flyway migrations run automatically on Supabase
  - ✅ **REAL-TIME**: Database change detection and notifications
  - ✅ **CONFIGURED**: Environment variables set for Supabase connection
  - 📋 **STATUS**: See `POSTGRESQL_INTEGRATION_STATUS.md` for details
  - 🧪 **TESTING**: See `TESTING_GUIDE.md` for comprehensive API testing

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.5.3 with Java 24
- **Database**: PostgreSQL 17 with Flyway migrations
- **Cache**: Redis 7 with Spring Cache abstraction
- **Security**: Spring Security with JWT authentication
- **Rate Limiting**: Bucket4j with Redis backend
- **Realtime Messaging**: Supabase Realtime for live updates and notifications
- **Monitoring**: Sentry for error tracking and performance monitoring
- **Health Checks**: Custom health indicators for database, Redis, and configuration
- **Metrics**: Application metrics with performance monitoring and alerting
- **Sentry Integration**: Comprehensive error tracking with custom context and user information
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, TestContainers, Selenium
- **Build Tool**: Gradle 8.7+
- **Code Quality**: Checkstyle, PMD, SpotBugs, SonarQube
- **Coverage**: JaCoCo with 80% minimum requirement
- **Mapping**: MapStruct 1.5.5.Final for type-safe DTO-entity conversion

### Project Structure
```
src/main/java/com/finance_control/
├── auth/                 # Authentication and authorization
├── brazilian_market/    # Brazilian market data integration
├── dashboard/           # Financial dashboard and analytics
├── goals/               # Financial goals management
├── profile/             # User profile management
├── shared/              # Common utilities and base classes
│   ├── config/          # Configuration classes
│   ├── controller/      # Shared controllers
│   ├── monitoring/      # Monitoring and metrics services
│   ├── security/        # Security utilities
│   └── service/         # Shared services
├── transactions/        # Transaction management
│   ├── category/        # Transaction categories
│   ├── responsibles/    # Transaction responsibility sharing
│   ├── source/          # Transaction sources (accounts, cards)
│   └── subcategory/     # Transaction subcategories
└── users/               # User management
```

### Design Patterns
- **Layered Architecture**: Controller → Service → Repository → Entity
- **Base Classes**: Reusable base classes for common operations
- **DTO Pattern**: Data transfer objects for API communication with MapStruct mapping
- **Specification Pattern**: Dynamic query building
- **Audit Pattern**: Automatic timestamp management with JPA auditing
- **Mapper Pattern**: Type-safe DTO-entity conversion with MapStruct

## 🚀 Quick Start

### Prerequisites
- Java 21 or 22
- Gradle 8.7+ (or use the included wrapper)
- PostgreSQL 17
- Docker (optional)

### Environment Setup
Create a `.env` file in the project root:
```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost
DB_PORT=5432
DB_NAME=finance_control
DB_USERNAME=postgres
DB_PASSWORD=your_password

# PostgreSQL Container
POSTGRES_DB=finance_control

# Supabase Configuration (Optional)
SUPABASE_ENABLED=true
SUPABASE_URL=https://your-project-ref.supabase.co

# AI Predictions (Optional)
APP_AI_OPENAI_ENABLED=false
APP_AI_OPENAI_MODEL=gpt-4o-mini
APP_AI_OPENAI_MAX_TOKENS=800
APP_AI_OPENAI_TEMPERATURE=0.2
OPENAI_API_KEY=your-openai-api-key
SUPABASE_ANON_KEY=your-supabase-anon-key
SUPABASE_JWT_SIGNER=your-supabase-jwt-signer
SUPABASE_SERVICE_ROLE_KEY=your-supabase-service-role-key

# Supabase Database (PostgreSQL)
SUPABASE_DATABASE_ENABLED=false                    # Set to true to use Supabase PostgreSQL
SUPABASE_DATABASE_HOST=db.your-project-ref.supabase.co
SUPABASE_DATABASE_PORT=5432
SUPABASE_DATABASE_NAME=postgres
SUPABASE_DATABASE_USERNAME=postgres.your-project-ref
SUPABASE_DATABASE_PASSWORD=your-database-password
SUPABASE_DATABASE_SSL_ENABLED=true
SUPABASE_DATABASE_SSL_MODE=require

# Supabase Storage & Realtime
SUPABASE_STORAGE_ENABLED=true
SUPABASE_REALTIME_ENABLED=true

# Sentry Configuration (Optional)
SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id
SENTRY_ENVIRONMENT=development
SENTRY_RELEASE=1.0.0
```

### Supabase Configuration

#### Getting Supabase Credentials
1. **Create a Supabase project** at [supabase.com](https://supabase.com)
2. **Get your project URL** from Settings → API → Project URL
3. **Get your anon key** from Settings → API → Project API keys → anon public
4. **Get your service role key** from Settings → API → Project API keys → service_role secret
5. **Get your JWT signer** from Settings → API → JWT Secret

#### Environment Variables
```env
SUPABASE_ENABLED=true                           # Enable Supabase integration
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-supabase-anon-key
SUPABASE_JWT_SIGNER=your-supabase-jwt-signer
SUPABASE_SERVICE_ROLE_KEY=your-supabase-service-role-key
SUPABASE_STORAGE_ENABLED=true                   # Enable Storage features
SUPABASE_REALTIME_ENABLED=true                  # Enable Realtime features
```

#### Database Setup
Create the required tables in your Supabase database:
```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable RLS (Row Level Security)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Create policies (adjust based on your needs)
CREATE POLICY "Users can view own record" ON users FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own record" ON users FOR UPDATE USING (auth.uid() = id);
```

#### Storage Buckets
Create these buckets in your Supabase Storage:
- `avatars` - User profile pictures
- `documents` - User documents
- `transactions` - Transaction attachments

#### PostgreSQL Database Integration
The application supports **dual database modes**:

1. **Local PostgreSQL** (default): Uses Docker container or local PostgreSQL instance
2. **Supabase PostgreSQL**: Uses Supabase's hosted PostgreSQL database

##### Switching to Supabase Database

1. **Get Database Credentials** from Supabase Dashboard:
   - Go to Settings → Database
   - Copy the connection parameters (host, database, username, password)

2. **Update Environment Variables**:
   ```env
   SUPABASE_DATABASE_ENABLED=true
   SUPABASE_DATABASE_HOST=db.your-project-ref.supabase.co
   SUPABASE_DATABASE_USERNAME=postgres.your-project-ref
   SUPABASE_DATABASE_PASSWORD=your-database-password
   ```

3. **Database Schema Setup**:
   ```sql
   -- Connect to your Supabase database and run:
   CREATE SCHEMA IF NOT EXISTS finance_control;

   -- The application will automatically run Flyway migrations
   -- to create all required tables, indexes, and constraints
   ```

##### Database Features with Supabase

✅ **Row Level Security (RLS)**: Automatic data isolation per user
✅ **Real-time Subscriptions**: Live database change notifications
✅ **Built-in Backup**: Automatic daily backups
✅ **Connection Pooling**: Optimized connection management
✅ **SSL Encryption**: Secure connections by default

##### Migration Process

1. **Backup Local Data** (if needed):
   ```bash
   # Export local database
   pg_dump -h localhost -U finance_user -d finance_control > backup.sql
   ```

2. **Enable Supabase Database**:
   ```env
   SUPABASE_DATABASE_ENABLED=true
   ```

3. **Run Application**:
   ```bash
   ./gradlew bootRun
   # Flyway will automatically create/update the schema
   ```

4. **Migrate Data** (optional):
   ```bash
   # Import data into Supabase (if needed)
   psql "postgresql://postgres.your-project-ref:password@db.your-project-ref.supabase.co:5432/postgres" < backup.sql
   ```

### Running with Docker Compose
```bash
# Start the application with PostgreSQL and Redis
docker-compose up -d

# The application will be available at:
# - Application: http://localhost:${APPLICATION_PORT}
# - Monitoring: Configure SENTRY_DSN environment variable for error tracking
```

### Running Locally
```bash
# Clone the repository
git clone https://github.com/yourusername/finance-control.git
cd finance-control

# Build the project
./gradlew build

# Run database migrations
./gradlew flywayMigrate

# Start the application
./gradlew bootRun
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test categories
./gradlew test --tests "*UnitTest"
./gradlew test --tests "*IntegrationTest"
./gradlew test --tests "*SeleniumTest"

# Run tests with coverage
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification

# Test Sentry configuration
./scripts/test-sentry.sh
```

### Supabase Integration API

#### Authentication Endpoints
```bash
# Supabase User Registration
curl -X POST http://localhost:8080/supabase/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","data":{"name":"John Doe"}}'

# Supabase User Login
curl -X POST http://localhost:8080/supabase/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Refresh Supabase Token
curl -X POST http://localhost:8080/supabase/auth/refresh-token \
  -H "Authorization: Bearer your-refresh-token"

# Password Reset
curl -X POST http://localhost:8080/supabase/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","redirect_to":"http://localhost:3000/reset-password"}'

# Update Password
curl -X PUT http://localhost:8080/supabase/auth/update-password \
  -H "Authorization: Bearer your-access-token" \
  -H "Content-Type: application/json" \
  -d '{"password":"newpassword123"}'

# Logout
curl -X POST http://localhost:8080/supabase/auth/logout \
  -H "Authorization: Bearer your-access-token"
```

#### Storage Endpoints
```bash
# Upload a file
curl -X POST http://localhost:8080/supabase/storage/upload \
  -F "bucket=avatars" \
  -F "folder=user123" \
  -F "file=@/path/to/avatar.jpg"

# Download a file
curl -X GET http://localhost:8080/supabase/storage/download/avatars/avatar.jpg \
  -o downloaded-avatar.jpg

# Delete a file
curl -X DELETE http://localhost:8080/supabase/storage/delete/avatars/avatar.jpg

# Upload avatar (profile integration)
curl -X POST http://localhost:8080/profile/avatar \
  -H "Authorization: Bearer your-jwt-token" \
  -F "file=@/path/to/avatar.jpg"
```

#### Realtime WebSocket Connection
```javascript
// Connect to Supabase Realtime
const socket = new WebSocket('ws://localhost:8080/supabase/realtime');

// Subscribe to transaction updates
socket.send(JSON.stringify({
  event: 'subscribe',
  channel: 'transactions',
  userId: 'user123'
}));

// Subscribe to goal updates
socket.send(JSON.stringify({
  event: 'subscribe',
  channel: 'goals',
  userId: 'user123'
}));

// Subscribe to dashboard updates
socket.send(JSON.stringify({
  event: 'subscribe',
  channel: 'dashboard',
  userId: 'user123'
}));

// Listen for messages
socket.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('Realtime update:', data);
};
```

#### Dual Authentication Support
The application supports both local JWT and Supabase JWT authentication:

```bash
# Local authentication (existing)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Supabase authentication (new)
curl -X POST http://localhost:8080/api/auth/supabase/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### Integration Examples & Usage Patterns

#### Frontend Integration with Supabase

##### React with Supabase Client
```typescript
// Install dependencies
npm install @supabase/supabase-js @supabase/auth-helpers-react

// Configure Supabase client
import { createClient } from '@supabase/supabase-js'
import { useSupabaseClient } from '@supabase/auth-helpers-react'

const supabase = createClient(
  process.env.NEXT_PUBLIC_SUPABASE_URL!,
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
)

// Authentication example
const signUp = async (email: string, password: string) => {
  const { data, error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        name: 'John Doe',
      }
    }
  })
  return { data, error }
}

// File upload example
const uploadAvatar = async (file: File) => {
  const { data, error } = await supabase.storage
    .from('avatars')
    .upload(`user-${userId}/avatar.jpg`, file)

  if (data) {
    // Update profile with new avatar URL
    const avatarUrl = `${process.env.NEXT_PUBLIC_SUPABASE_URL}/storage/v1/object/public/avatars/${data.path}`
    await fetch('/api/profile/avatar', {
      method: 'PUT',
      body: JSON.stringify({ avatarUrl }),
      headers: { 'Content-Type': 'application/json' }
    })
  }
  return { data, error }
}
```

##### Vue.js with Supabase
```javascript
// Configure Supabase client
import { createClient } from '@supabase/supabase-js'

const supabase = createClient(
  import.meta.env.VITE_SUPABASE_URL,
  import.meta.env.VITE_SUPABASE_ANON_KEY
)

// Real-time subscription example
const subscribeToTransactions = (userId) => {
  return supabase
    .channel('transactions')
    .on('postgres_changes', {
      event: '*',
      schema: 'public',
      table: 'transactions',
      filter: `user_id=eq.${userId}`
    }, (payload) => {
      console.log('Transaction change:', payload)
      // Update UI with new transaction data
      updateTransactionsList(payload.new)
    })
    .subscribe()
}
```

##### Angular with Supabase
```typescript
// Configure Supabase client
import { createClient, SupabaseClient } from '@supabase/supabase-js'

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private supabase: SupabaseClient

  constructor() {
    this.supabase = createClient(
      environment.supabaseUrl,
      environment.supabaseAnonKey
    )
  }

  // Authentication service
  async signIn(email: string, password: string) {
    const { data, error } = await this.supabase.auth.signInWithPassword({
      email,
      password
    })
    return { data, error }
  }

  // Storage service
  async uploadFile(bucket: string, path: string, file: File) {
    const { data, error } = await this.supabase.storage
      .from(bucket)
      .upload(path, file)

    return { data, error }
  }
}
```

#### Backend Integration Patterns

##### Spring Boot Controller Integration
```java
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final SupabaseRealtimeService realtimeService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody @Valid CreateTransactionRequest request) {
        // Create transaction
        TransactionDTO transaction = transactionService.create(request);

        // Send realtime notification
        if (realtimeService != null) {
            realtimeService.notifyTransactionUpdate(
                transaction.getUserId(),
                transaction
            );
        }

        return ResponseEntity.ok(transaction);
    }
}
```

##### Service Layer Integration
```java
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SupabaseRealtimeService realtimeService; // Optional
    private final DashboardService dashboardService; // Optional

    @Autowired(required = false)
    public void setRealtimeService(SupabaseRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @Autowired(required = false)
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public TransactionDTO create(CreateTransactionRequest request) {
        // Business logic...
        Transaction transaction = transactionRepository.save(entity);

        // Notify realtime subscribers
        if (realtimeService != null) {
            realtimeService.notifyTransactionUpdate(
                transaction.getUserId(),
                convertToDTO(transaction)
            );
        }

        // Update dashboard
        if (dashboardService != null) {
            dashboardService.notifyDashboardUpdate(transaction.getUserId());
        }

        return convertToDTO(transaction);
    }
}
```

#### Real-time Dashboard Updates

##### Frontend Real-time Connection
```typescript
// Connect to WebSocket for real-time updates
class DashboardService {
  private socket: WebSocket;
  private subscribers: Map<string, Function[]> = new Map();

  connect(userId: string) {
    this.socket = new WebSocket('ws://localhost:8080/supabase/realtime');

    this.socket.onopen = () => {
      // Subscribe to dashboard updates
      this.socket.send(JSON.stringify({
        event: 'subscribe',
        channel: 'dashboard',
        userId: userId
      }));

      // Subscribe to transaction updates
      this.socket.send(JSON.stringify({
        event: 'subscribe',
        channel: 'transactions',
        userId: userId
      }));

      // Subscribe to goal updates
      this.socket.send(JSON.stringify({
        event: 'subscribe',
        channel: 'goals',
        userId: userId
      }));
    };

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      this.notifySubscribers(data.channel, data);
    };
  }

  subscribe(channel: string, callback: Function) {
    if (!this.subscribers.has(channel)) {
      this.subscribers.set(channel, []);
    }
    this.subscribers.get(channel)!.push(callback);
  }

  private notifySubscribers(channel: string, data: any) {
    const callbacks = this.subscribers.get(channel) || [];
    callbacks.forEach(callback => callback(data));
  }
}

// Usage in React component
const Dashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const dashboardService = new DashboardService();

  useEffect(() => {
    const userId = getCurrentUserId();
    dashboardService.connect(userId);

    // Subscribe to dashboard updates
    dashboardService.subscribe('dashboard', (data) => {
      console.log('Dashboard updated:', data);
      // Refresh dashboard data
      fetchDashboardData().then(setDashboardData);
    });

    // Subscribe to transaction updates
    dashboardService.subscribe('transactions', (data) => {
      console.log('Transaction updated:', data);
      // Update transaction list
      updateTransactionList(data.payload);
    });

    return () => dashboardService.disconnect();
  }, []);
};
```

#### Dual Authentication Flow

##### Backend Dual Auth Support
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Optional Supabase authentication
    @Autowired(required = false)
    private SupabaseAuthService supabaseAuthService;

    // Local authentication (existing)
    public AuthResponse authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getId().toString());
        return AuthResponse.builder()
            .token(token)
            .userId(user.getId())
            .build();
    }

    // Supabase authentication (new)
    public Mono<AuthResponse> authenticateWithSupabase(String email, String password) {
        if (supabaseAuthService == null || !supabaseAuthService.isSupabaseAuthEnabled()) {
            return Mono.error(new IllegalStateException("Supabase authentication is not enabled"));
        }

        return supabaseAuthService.signin(new LoginRequest(email, password))
            .map(response -> {
                // Map Supabase user to local user or create new one
                String supabaseUserId = response.getUser().getId();
                User localUser = findOrCreateUserFromSupabase(response.getUser());

                return AuthResponse.builder()
                    .token(response.getAccessToken())
                    .refreshToken(response.getRefreshToken())
                    .userId(localUser.getId())
                    .build();
            });
    }

    private User findOrCreateUserFromSupabase(AuthResponse.User supabaseUser) {
        // Implementation to map Supabase user to local user
        return userRepository.findByEmail(supabaseUser.getEmail())
            .orElseGet(() -> createUserFromSupabase(supabaseUser));
    }
}
```

##### Frontend Auth Selection
```typescript
// Authentication service with provider selection
class AuthService {
  private provider: 'local' | 'supabase' = 'local';

  setAuthProvider(provider: 'local' | 'supabase') {
    this.provider = provider;
  }

  async login(email: string, password: string) {
    if (this.provider === 'supabase') {
      return this.supabaseLogin(email, password);
    } else {
      return this.localLogin(email, password);
    }
  }

  private async localLogin(email: string, password: string) {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return response.json();
  }

  private async supabaseLogin(email: string, password: string) {
    const response = await fetch('/api/auth/supabase/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    return response.json();
  }
}

// Usage
const authService = new AuthService();

// Switch to Supabase auth
authService.setAuthProvider('supabase');
const result = await authService.login('user@example.com', 'password');
```

### API Testing with Postman
```bash
# Start the application
docker compose up -d

# Test monitoring endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/monitoring/status
curl -X POST http://localhost:8080/api/monitoring/test-alert

# Test security configuration
curl http://localhost:8080/api/transactions  # Should return 403
curl http://localhost:8080/api/monitoring/status  # Should return 500 (not 403)
```

**Collection:** Import `postman/FinanceControl.postman_collection.json` into your API client of choice.

- `{{baseUrl}}` defaults to `http://localhost:8080`
- `{{authToken}}` is the JWT from `/auth/login`
- `{{supabaseToken}}` and `{{supabaseRefreshToken}}` come from Supabase Auth responses
- Requests cover Supabase auth/storage operations, the CSV/OFX `/transactions/import`, and `/dashboard/predictions`

### Monitoring & Health Checks
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Test monitoring endpoints
curl http://localhost:8080/api/monitoring/status
curl http://localhost:8080/api/monitoring/alerts

# Trigger Sentry test alert
curl -X POST http://localhost:8080/api/monitoring/test-alert
```

### Code Quality Checks
```bash
# Run all quality checks
./gradlew qualityCheck

# Individual quality checks
./gradlew checkstyleMain    # Code style validation
./gradlew pmdMain          # Static code analysis
./gradlew spotbugsMain     # Bug detection
./gradlew sonarqube        # SonarQube analysis

# Quality check with enhanced script (includes retry logic)
./scripts/dev.sh quality

# Quality check without tests (faster)
./scripts/dev.sh quality --no-test

# Test Sentry configuration
./scripts/test-sentry.sh
```

### Coverage Requirements
- **Minimum Coverage**: 80% (line and branch coverage)
- **Exclusions**: Configuration classes, DTOs, models, exceptions, enums, utilities, and validation classes
- **Reports**: HTML and XML formats available in `build/reports/jacoco/`

## 📚 API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:${APPLICATION_PORT}/swagger-ui.html
- **OpenAPI JSON**: http://localhost:${APPLICATION_PORT}/v3/api-docs

### API Testing

The project includes comprehensive API testing capabilities:

- **Postman Collection**: Complete "Finance Control - Complete API Testing" collection
  - All major endpoints covered (auth, users, transactions, dashboard, Brazilian market)
  - Pre-configured environment variables for easy testing
  - Authentication flow with JWT token management
  - Ready-to-use requests for all CRUD operations

- **Authentication Flow**:
  - Register new users with secure password hashing
  - Login with JWT token generation
  - Access protected endpoints with Bearer token authentication
  - Complete user management and profile operations

- **Endpoint Coverage**:
  - ✅ Authentication endpoints (login, register, validate)
  - ✅ User management endpoints (CRUD operations)
  - ✅ Transaction endpoints (with filtering and pagination)
  - ✅ Dashboard endpoints (summary, metrics, trends)
  - ✅ Brazilian market data endpoints (stocks, FIIs, indicators)
  - ✅ Financial goals endpoints (CRUD operations)
  - ✅ Category management endpoints (categories and subcategories)
  - ✅ **NEW**: Data export endpoints (CSV and JSON formats)
  - ✅ **NEW**: Redis caching for performance optimization
  - ✅ **NEW**: Rate limiting for API protection
  - ✅ **NEW**: Monitoring endpoints (health, metrics, alerts)

### Key Endpoints

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/validate` - Validate JWT token
- `PUT /auth/password` - Change user password

#### Dashboard
- `GET /api/dashboard/summary` - Complete dashboard overview
- `GET /api/dashboard/metrics` - Detailed financial metrics
- `GET /api/dashboard/spending-categories` - Top spending categories
- `GET /api/dashboard/monthly-trends` - Monthly income/expense trends
- `GET /api/dashboard/current-month-metrics` - Current month data
- `GET /api/dashboard/year-to-date-metrics` - Year-to-date data

#### Transactions
- `GET /transactions` - List transactions with filtering
- `POST /transactions` - Create new transaction
- `PUT /transactions/{id}` - Update transaction
- `DELETE /transactions/{id}` - Delete transaction

#### Financial Goals
- `GET /financial-goals` - List financial goals
- `POST /financial-goals` - Create new goal
- `GET /financial-goals/active` - Get active goals
- `GET /financial-goals/completed` - Get completed goals
- `POST /financial-goals/{id}/progress` - Update goal progress
- `POST /financial-goals/{id}/complete` - Complete goal

#### Transaction Categories
- `GET /transaction-categories` - List transaction categories
- `POST /transaction-categories` - Create new category
- `PUT /transaction-categories/{id}` - Update category
- `DELETE /transaction-categories/{id}` - Delete category

#### Transaction Subcategories
- `GET /transaction-subcategories` - List transaction subcategories
- `POST /transaction-subcategories` - Create new subcategory
- `PUT /transaction-subcategories/{id}` - Update subcategory

#### Data Export
- `GET /api/export/all/csv` - Export all user data as CSV
- `GET /api/export/all/json` - Export all user data as JSON
- `GET /api/export/transactions/csv` - Export transactions as CSV
- `GET /api/export/goals/csv` - Export financial goals as CSV

#### Monitoring & Observability
- `GET /api/monitoring/health` - Detailed system health status
- `GET /api/monitoring/status` - Monitoring system status
- `GET /api/monitoring/alerts` - Active system alerts
- `GET /api/monitoring/metrics/summary` - Application metrics summary
- `POST /api/monitoring/test-alert` - Trigger test alert
- `DELETE /api/monitoring/alerts/{alertId}` - Clear specific alert
- `DELETE /api/monitoring/alerts` - Clear all alerts
- `GET /actuator/health` - Spring Boot Actuator health check
- `GET /actuator/info` - Application information
- `DELETE /transaction-subcategories/{id}` - Delete subcategory
- `GET /transaction-subcategories/category/{categoryId}` - Get subcategories by category
- `GET /transaction-subcategories/category/{categoryId}/usage` - Get subcategories ordered by usage
- `GET /transaction-subcategories/category/{categoryId}/count` - Get subcategory count by category

#### Brazilian Market Data
- `GET /api/brazilian-market/selic` - Get SELIC interest rate
- `GET /api/brazilian-market/cdi` - Get CDI interest rate
- `GET /api/brazilian-market/ipca` - Get IPCA inflation rate
- `GET /api/brazilian-market/indicators` - Get all economic indicators
- `GET /api/brazilian-market/stocks` - Get user's Brazilian stocks
- `GET /api/brazilian-market/fiis` - Get user's FIIs (Real Estate Investment Funds)
- `GET /api/brazilian-market/summary` - Get comprehensive market summary

#### Users & Profile
- `GET /users` - List users
- `POST /users` - Create user
- `GET /users/email/{email}` - Get user by email
- `GET /profile` - Get current user profile
- `PATCH /profile` - Update user profile

#### Other Resources
- `GET /transaction-sources` - List transaction sources
- `GET /transaction-responsibles` - List responsible parties

## 🧪 Testing Strategy

### Test Categories
- **Unit Tests**: Individual component testing with mocked dependencies
- **Integration Tests**: Database and service layer integration testing
- **E2E Tests**: Full application testing with Selenium WebDriver

### Test Organization
```
src/test/java/com/finance_control/
├── unit/           # Unit tests
├── integration/    # Integration tests
├── e2e/           # End-to-end tests
└── selenium/      # Selenium test utilities
```

### Running Tests
```bash
# Unit tests only
./gradlew test --tests "*UnitTest"

# Integration tests only
./gradlew test --tests "*IntegrationTest"

# E2E tests only
./gradlew test --tests "*SeleniumTest"

# All tests with coverage
./gradlew test jacocoTestReport
```

### Test Infrastructure
The project includes comprehensive test coverage with multiple testing strategies:

- **Unit Tests**: Fast, isolated tests for individual components
  - Service layer tests with mocked dependencies
  - Repository tests with in-memory H2 database
  - Controller tests with MockMvc
  - Model validation tests

- **Integration Tests**: Full application context tests
  - TestContainers with real PostgreSQL database
  - End-to-end service integration testing
  - Database transaction testing
  - JPA auditing verification

- **Test Coverage**: 80% minimum coverage requirement
  - JaCoCo coverage reports
  - Branch and line coverage analysis
  - Quality gates enforcement

## 🛠️ Development

### Recent Improvements

The project has recently undergone significant improvements:

- **✅ Transaction Categories Management**: Complete CRUD operations with 62 comprehensive tests
- **✅ MapStruct Integration**: Type-safe DTO-entity mapping with compile-time validation
- **✅ JPA Auditing**: Automatic timestamp management for all entities
- **✅ Enhanced Testing**: Comprehensive unit and integration test coverage (148 tests passing)
- **✅ Quality Gates**: All code quality checks passing consistently (Checkstyle, PMD, SpotBugs)
- **✅ Docker Compatibility**: Full macOS and Docker Compose v2 support
- **✅ Test Isolation**: Fixed optimistic locking issues in integration tests
- **✅ Development Scripts**: Enhanced `dev.sh` with retry logic and better error handling
- **✅ Repository Layer**: Enhanced with NameBasedRepository interface for standardized operations
- **✅ Security Enhancement**: Implemented proper BCrypt password hashing for production security
- **✅ API Testing**: Complete Postman collection for comprehensive endpoint testing
- **✅ Docker Optimization**: Enhanced Docker configuration and environment management
- **✅ Authentication Flow**: Complete JWT authentication system with proper token validation
- **✅ OpenAPI Documentation**: Fixed and enhanced API documentation generation
- **✅ Production Readiness**: All major security and configuration issues resolved
- **✅ Monitoring Infrastructure**: Enterprise-grade monitoring with Sentry integration
- **✅ Test Infrastructure**: Comprehensive test coverage with 20+ enabled test files
- **✅ API Testing**: Enhanced Postman collections with monitoring endpoints
- **✅ Security Configuration**: Properly configured public and protected endpoints
- **✅ Database Optimization**: Performance indexes and concurrent migration support
- **✅ Sentry Integration**: Complete error tracking and performance monitoring setup

### Code Standards
- **Java 24**: Latest LTS version with modern features
- **Lombok**: Reduces boilerplate code
- **Spring Boot**: Latest stable version
- **PostgreSQL**: Primary database with optimized queries
- **Flyway**: Database migration management
- **Code Quality**: Enforced by Checkstyle, PMD, and SpotBugs
- **Coverage**: Minimum 80% test coverage required
- **SonarQube**: Code quality gates and analysis

### Architecture Guidelines
- **Base Classes**: Extend appropriate base classes for consistency
- **DTO Pattern**: Use DTOs for API communication
- **Validation**: Comprehensive input validation
- **Error Handling**: Centralized exception handling
- **Security**: JWT-based authentication with user isolation

### Database Design
- **Normalized Schema**: Proper normalization for data integrity
- **Indexes**: Optimized indexes for query performance
- **Foreign Keys**: Referential integrity constraints
- **Audit Fields**: Automatic timestamp management

## 📖 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) folder:

- **[Architecture Guide](docs/BASE_CLASSES_GUIDE.md)** - Base classes and architecture patterns
- **[API Patterns](docs/API_PATTERNS.md)** - REST API conventions and best practices
- **[Testing Strategy](docs/TESTING_STRATEGY.md)** - Testing guidelines and examples
- **[Naming Conventions](docs/NAMING_EXAMPLES.md)** - Code naming standards
- **[Service Patterns](docs/SERVICE_IMPROVEMENTS.md)** - Service layer improvements
- **[Code Quality Tools](docs/CODE_QUALITY_TOOLS.md)** - Checkstyle, PMD, SpotBugs configuration
- **[Gradle Scripts](docs/GRADLE_SCRIPTS.md)** - Custom Gradle tasks and scripts

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Follow coding standards**: Use the established patterns and conventions
4. **Write tests**: Ensure new features have proper test coverage
5. **Update documentation**: Keep documentation in sync with code changes
6. **Submit a pull request**: Provide clear description of changes

### Development Setup
```bash
# Fork and clone
git clone https://github.com/yourusername/finance-control.git
cd finance-control

# Create feature branch
git checkout -b feature/your-feature

# Make changes and test
./gradlew clean test

# Run quality checks
./gradlew qualityCheck

# Commit with conventional commits
git commit -m "feat: add new transaction filtering feature"

# Push and create PR
git push origin feature/your-feature
```

### Continuous Integration & Quality Gates

This project uses GitHub Actions for automated CI/CD with comprehensive quality gates:

#### CI Pipeline (`ci.yml`)
- **Build & Test**: Clean build, unit tests, integration tests
- **Quality Checks**: Checkstyle, PMD, SpotBugs analysis
- **Security Scan**: OWASP Dependency-Check for vulnerabilities
- **Coverage**: JaCoCo test coverage reports (minimum 80%)
- **Artifacts**: All reports uploaded for review

#### SonarQube Analysis (`sonarqube.yml`)
- **Code Quality**: Automated SonarQube analysis with Docker services
- **Coverage Integration**: JaCoCo reports fed into SonarQube
- **Quality Gates**: Reliability, Security, Maintainability, Coverage metrics
- **Manual Trigger**: Run on-demand via GitHub Actions dispatch

#### Running Locally
```bash
# Run all quality checks
./gradlew qualityCheck

# Run tests with coverage
./gradlew test jacocoTestReport

# Run SonarQube analysis (requires Docker)
docker-compose --profile sonarqube up -d
./gradlew sonarqube

# Run security scan
./gradlew dependencyCheckAnalyze
```

#### Quality Standards
- **Test Coverage**: Minimum 80% required
- **Code Quality**: Zero critical issues in Checkstyle, PMD, SpotBugs
- **Security**: No high/critical vulnerabilities
- **SonarQube**: Quality gate must pass

#### GitHub Secrets Setup
For SonarQube analysis, add these secrets in your repository:
- `SONAR_TOKEN`: Project token from SonarQube UI (Administration > Security > Users > Tokens)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: Report bugs and feature requests via [GitHub Issues](https://github.com/yourusername/finance-control/issues)
- **Discussions**: Join community discussions in [GitHub Discussions](https://github.com/yourusername/finance-control/discussions)
- **Documentation**: Check the [docs/](docs/) folder for detailed guides

## 🗺️ Roadmap

- [ ] **Dashboard**: Interactive financial dashboard with charts
- [ ] **Reports**: Advanced financial reporting and analytics
- [ ] **Budgeting**: Budget planning and tracking features
- [ ] **Mobile App**: React Native mobile application
- [ ] **Export**: Data export to CSV, PDF, and Excel
- [ ] **Notifications**: Email and push notifications
- [ ] **Multi-currency**: Support for multiple currencies
- [ ] **Recurring Transactions**: Automated recurring transaction management

---

**Built with ❤️ using Spring Boot and modern Java technologies**

---

## 📚 Further Reference & Guides

### Reference Documentation
- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
- [Create an OCI image](https://docs.spring.io/spring-boot/3.5.3/maven-plugin/build-image.html)
- [Spring Web](https://docs.spring.io/spring-boot/3.5.3/reference/web/servlet.html)
- [Spring Security](https://docs.spring.io/spring-boot/3.5.3/reference/web/spring-security.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.3/reference/using/devtools.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.3/reference/actuator/index.html)
- [Validation](https://docs.spring.io/spring-boot/3.5.3/reference/io/validation.html)

### Guides
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Validation](https://spring.io/guides/gs/validating-form-input/)

### Code Quality Tools

The project uses several code quality tools to maintain high standards:

- **Checkstyle**: Enforces coding standards and conventions
- **PMD**: Static code analysis for potential bugs and code smells
- **SpotBugs**: Bytecode analysis for bug detection
- **SonarQube**: Comprehensive code quality analysis and reporting
- **JaCoCo**: Test coverage analysis with 80% minimum requirement

### Quality Reports

Quality reports are generated in `build/reports/`:
- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/`
- **SpotBugs**: `build/reports/spotbugs/`
- **JaCoCo**: `build/reports/jacoco/`
- **SonarQube**: Available via SonarQube server



## 🧩 Using MCP servers with Junie

Junie (the AI assistant) can connect to external Model Context Protocol (MCP) servers defined in `.cursor/mcp.json` at the project root.

### What’s already set up
- The repository includes `.cursor/mcp.json` with these servers:
  - `postman-mcp` (HTTPS)
  - `finance-control` (SSE)
  - `Sentry` (HTTPS)
  - `Context7` (HTTPS)
  - `Tinybird` (command via `npx`)
  - `sequential-thinking` (command via `npx`)
  - `playwright` (command via `npx`)

### Prerequisites
- Your Junie/client session must start in the project root so it can find `.cursor/mcp.json`.
- For command-based servers (`npx ...`): install Node.js (LTS) with `npx` available in PATH.
- Outbound HTTPS access must be allowed. For npm-based servers, access to the npm registry is required on first run.

### Secrets via environment variables
To keep secrets out of version control, the config expects environment variables:
- `POSTMAN_MCP_TOKEN` — used by `postman-mcp` via header `Authorization: Bearer ${POSTMAN_MCP_TOKEN}`.
- `TB_TOKEN` — used by `Tinybird` via `?token=${TB_TOKEN}`.
- `NVD_API_KEY` — used by OWASP Dependency Check for faster vulnerability scanning (optional but recommended).

Set them in your shell before starting the client/IDE that hosts Junie:
```bash
export POSTMAN_MCP_TOKEN="<your-postman-token>"
export TB_TOKEN="<your-tinybird-token>"
```

If you use the Docker dev shell for parity:
```bash
./scripts/dev.sh dev
# Inside the container shell:
export POSTMAN_MCP_TOKEN="<your-postman-token>"
export TB_TOKEN="<your-tinybird-token>"
```

### Start/reload
- Restart your Junie session (or the MCP-capable IDE) after setting env vars so it re-loads `.cursor/mcp.json` and reconnects to servers.

### Verify connectivity
- Ask Junie: "What MCP servers are connected?" You should see:
  `postman-mcp`, `finance-control`, `Sentry`, `Context7`, `Tinybird`, `sequential-thinking`, `playwright`.
- Try a simple tool from a server (examples vary by client/server):
  - Sequential Thinking: invoke its planning tool on a trivial prompt.
  - Postman MCP: list collections or run a minimal request (requires `POSTMAN_MCP_TOKEN`).
  - Playwright MCP: basic navigation stub (requires Node.js).

### Troubleshooting
- Server missing in list: ensure `.cursor/mcp.json` is at project root and JSON is valid; restart the session.
- `npx` not found: install Node.js or run only URL-based servers.
- 401/403: verify `POSTMAN_MCP_TOKEN` / `TB_TOKEN` are set in the same environment as the client.
- Corporate proxy: set `HTTP_PROXY`/`HTTPS_PROXY` so the MCP client inherits them.
- SSE drops (for `finance-control`): ensure long-lived connections aren’t blocked by the network.

### Security note
Do not commit real tokens to `.cursor/mcp.json`. Use environment variables as shown above. Keep any local overrides in untracked files or your shell profile.

#### Application Secrets
The application follows security best practices for handling sensitive configuration:

- **Environment Variables**: All secrets (database passwords, JWT keys, API tokens) are externalized via environment variables
- **No Hardcoded Secrets**: Repository scanning confirmed zero hardcoded secrets in committed code
- **Gitignore Protection**: Sensitive files like `docker.env` are properly excluded from version control
- **Development Defaults**: Safe development defaults with clear production override requirements

**Required Environment Variables for Production:**
```bash
# Database
DB_URL=jdbc:postgresql://host:5432/dbname
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_db_password

# JWT Security
JWT_SECRET=your-super-secure-jwt-key-min-256-bits

# External APIs (if used)
ALPHA_VANTAGE_API_KEY=your_api_key
BCB_API_KEY=your_api_key
REDIS_PASSWORD=your_redis_password
```

See `docker.env` for complete list of configurable environment variables.
