# Security Guide

> **Comprehensive security documentation for Finance Control**

## Table of Contents
- [Security Configuration](#security-configuration)
- [Authentication & Authorization](#authentication--authorization)
- [Best Practices](#best-practices)
- [Credential Management](#credential-management)
- [Incident Response](#incident-response)
- [Code Quality & Security](#code-quality--security)

---

## Security Configuration

### CSRF Protection
- **Status**: Disabled (acceptable for stateless JWT API)
- **Reason**: JWT tokens in Authorization header, not cookies
- **Note**: If switching to cookie-based auth, enable CSRF protection

### CORS
- **Status**: Configured with header wildcard mitigation
- **Action Required**: Update production origins in `.env` (remove localhost)
- **Config**: Restrict to allowed origins only in production

### Cookies
- **Status**: Secure utility implemented
- **Settings**:
  - `httpOnly`: true
  - `secure`: true (production only)
  - `sameSite`: strict/lax

### XSS Protection
- **Status**: All HTML rendering is sanitized and validated
- **Policy**: Never render untrusted user content directly
- **Validation**: All inputs are validated before processing

---

## Authentication & Authorization

### JWT Authentication
- JWT tokens in Authorization header (stateless)
- CSRF disabled (acceptable for JWT API)
- Token validation on every request
- Secure cookie settings when needed

### Password Policy

**Current Requirements**:
- Minimum 8 characters
- At least one lowercase letter
- At least one uppercase letter
- At least one digit
- Cannot reuse current password

**Recommended Enhancements**:
- Password strength meter (frontend)
- Password history tracking (prevent reuse of last N passwords)
- Special character requirement
- Configurable password expiration

### Database Security
- **RLS Policies**: Enabled on Supabase
- **Data Isolation**: User data isolated by `user_id`
- **Connections**: SSL/TLS required
- **Encryption**: Sensitive fields encrypted at rest

---

## Best Practices

### Secrets Management
- ✅ **Never commit** `.env` files to version control
- ✅ Use `.env.example` as template
- ✅ Rotate credentials if exposed (see [Credential Rotation](#credential-rotation))
- ✅ Pre-commit hooks configured to prevent accidental commits

### Logging Security
**Utilities**: `LoggingUtils` provides:
- `maskSensitiveValue()` - Masks passwords/secrets/tokens
- `maskEmail()` - Masks email addresses
- `sanitizeForLogging()` - Prevents log injection

**Rules**:
- ❌ Never log passwords, secrets, or full tokens
- ✅ Always use masking utilities for sensitive data
- ✅ Use generic error messages for users
- ✅ Detailed logs for debugging (server-side only)

### Input Validation
- ✅ Validate all user input
- ✅ Use Zod schemas (frontend)
- ✅ Use Bean Validation (backend)
- ✅ Sanitize data before processing

### Network Security
- ✅ HTTPS only in production
- ✅ CORS restricted to allowed origins
- ✅ Rate limiting enabled (100 req/min)
- ✅ Security headers configured (HSTS, CSP, X-Frame-Options, etc.)

### Dependency Security
- 🔄 Run `npm audit` regularly (frontend)
- 🔄 Run `./gradlew dependencyCheckAnalyze` (backend)
- ✅ Auto-fix when possible
- ⚠️ Review and address high/critical vulnerabilities immediately

---

## Credential Management

### Credentials Requiring Rotation

All sensitive credentials in `.env` files:

1. **Database Credentials**
   - Database password
   - Connection strings

2. **Application Secrets**
   - JWT secret (⚠️ default - MUST change)
   - Encryption key
   - Session secrets

3. **Third-Party Services**
   - Supabase keys (anon, service role)
   - OpenAI API key
   - NVD API key
   - Sentry DSN
   - SonarQube token

### Credential Rotation Steps

#### 1. Supabase Keys
```bash
# Dashboard → Settings → API → Reset keys
# Update .env with new keys
SUPABASE_ANON_KEY=new_anon_key
SUPABASE_SERVICE_ROLE_KEY=new_service_role_key
```

#### 2. JWT Secret
```bash
# Generate new secret
openssl rand -base64 32

# Update .env
JWT_SECRET=new_generated_secret
```

**⚠️ Warning**: Rotating JWT secret invalidates all existing tokens

#### 3. Encryption Key
```bash
# Generate new key
openssl rand -base64 32

# Update .env
ENCRYPTION_KEY=new_generated_key
```

**⚠️ Warning**: Rotating encryption key invalidates all encrypted data

#### 4. API Keys
- Revoke old keys in respective service dashboards
- Generate new keys
- Update `.env` file
- Restart application

#### 5. Database Password
```sql
-- Generate new strong password
-- Update database user
ALTER USER finance_control_user WITH PASSWORD 'new_secure_password';

-- Update .env
DB_PASSWORD=new_secure_password
```

---

## Incident Response

### If Credentials Are Exposed

**Immediate Actions (within 1 hour)**:
1. ✅ Rotate all exposed credentials immediately
2. ✅ Review access logs for unauthorized access
3. ✅ Revoke all active sessions/tokens
4. ✅ Deploy updated credentials

**Follow-up (within 24 hours)**:
1. Review security measures
2. Document incident
3. Update security procedures
4. Implement additional safeguards

### If Data Breach Detected

**Immediate Actions**:
1. Assess scope and affected data
2. Patch vulnerability immediately
3. Revoke unauthorized access
4. Preserve logs for investigation

**Follow-up**:
1. Notify affected users (if required by law)
2. Document breach details
3. Update security measures
4. Conduct post-mortem review

### If Vulnerability Exploited

**Immediate Actions**:
1. Patch vulnerability immediately
2. Review logs for affected systems
3. Assess damage and data integrity
4. Block malicious actors

**Follow-up**:
1. Update security measures
2. Implement additional monitoring
3. Document lessons learned
4. Review and update incident response procedures

---

## Code Quality & Security

### Security Scanning Status

**Frontend**:
- ✅ ESLint configured with security rules
- ⚠️ 1 high severity vulnerability in `glob` (indirect dependency)
  - **Fix**: Run `npm audit fix`
- ✅ Auto-fix applied to most issues

**Backend**:
- ✅ Checkstyle configured
- ✅ SpotBugs enabled
- ✅ PMD configured (some config warnings)
- ✅ OWASP Dependency Check enabled
- **Action**: Run `./gradlew dependencyCheckAnalyze` and review

### Current Security Posture

✅ **Completed**:
- Secrets management (`.env.example`, pre-commit hooks)
- Security configs reviewed (CSRF, CORS, cookies, XSS)
- Authentication verified (JWT validation, password policy)
- Logging enhanced (masking utilities)
- Dependencies scanned
- Code quality tools configured

⚠️ **Action Items**:
1. Rotate all credentials (see [Credential Rotation](#credential-rotation-steps))
2. Update dependencies (`npm audit fix`)
3. Fix PMD configuration XML validation errors
4. Verify test coverage meets 85% threshold

### Security Checklist

- [ ] All `.env` files excluded from version control
- [ ] Production CORS origins configured (no localhost)
- [ ] All credentials rotated from defaults
- [ ] HTTPS enabled in production
- [ ] Database RLS policies verified
- [ ] Rate limiting configured
- [ ] Security headers enabled
- [ ] Logging sanitization in place
- [ ] Dependency scanning automated
- [ ] Incident response procedures documented

---

## Additional Resources

- [Environment Setup](./SETUP.md)
- [CI/CD Security](./CI_CD_SETUP.md)
- [Testing Security](./TESTING.md)
- [Architecture](./ARCHITECTURE_DIAGRAM.md)

---

**Last Updated**: 2025-11-23
**Review Frequency**: Monthly or after security incidents
