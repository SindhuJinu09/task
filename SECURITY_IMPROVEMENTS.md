# Security Improvements for API Key Management

## Overview
This document outlines the security improvements implemented to address concerns about API key management and client registration.

## Security Concerns Addressed

### 1. Multiple Active API Keys
**Issue**: Multiple API keys could be active simultaneously for the same client, increasing the attack surface.

**Solution**: 
- Implemented single active key policy by default
- When a new API key is generated, all existing active keys are automatically revoked
- Configurable via `apikey.allow-multiple-active-keys` property
- Default: `false` (recommended for security)

### 2. API Key Expiry
**Issue**: API keys had no expiry time, posing a long-term security risk.

**Solution**:
- Added expiry time to all API keys (default: 1 year)
- Expired keys are automatically marked as "expired" during validation
- Expiry time is configurable via `apikey.expiry-duration` property
- Expiry information is included in all API responses

### 3. Duplicate Client Registration
**Issue**: Same client could be registered multiple times with the same name and organization.

**Solution**:
- Added unique constraint on `(clientName, organizationUuid)` combination
- Prevents duplicate client registrations
- Returns appropriate error message for duplicate attempts

## Configuration Options

### application.properties
```properties
# API Key Configuration
apikey.expiry-duration=365d                    # Default: 1 year
apikey.allow-multiple-active-keys=false        # Default: false (recommended)
```

### Security Recommendations
1. **Keep `allow-multiple-active-keys=false`** - This ensures only one active key per client
2. **Set appropriate expiry duration** - Consider shorter durations for higher security requirements
3. **Monitor key usage** - The system now tracks `lastUsedAt` for audit purposes

## API Response Changes

### Client Registration Response
```json
{
  "client_id": "uuid",
  "api_key": "generated-key",
  "client_name": "Client Name",
  "status": "active",
  "created_at": "2024-01-01T00:00:00",
  "expires_at": "2025-01-01T00:00:00",
  "message": "Client registered successfully. API key expires in 365 days."
}
```

### Generate API Key Response
```json
{
  "client_id": "uuid",
  "api_key": "generated-key",
  "expires_at": "2025-01-01T00:00:00",
  "message": "API key generated successfully. Previous keys have been revoked. New key expires in 365 days."
}
```

### Get API Keys Response
```json
[
  {
    "api_key_id": "uuid",
    "client_id": "client-uuid",
    "status": "active",
    "created_at": "2024-01-01T00:00:00",
    "expires_at": "2025-01-01T00:00:00",
    "last_used_at": "2024-01-15T10:30:00",
    "created_by": "user-id"
  }
]
```

## Validation Improvements

### Key Validation Process
1. **Format Check**: Validates API key format (32+ alphanumeric characters)
2. **Hash Lookup**: Finds key by hash in database
3. **Status Check**: Ensures key is "active"
4. **Expiry Check**: Validates key hasn't expired
5. **Auto-expire**: Marks expired keys as "expired"
6. **Usage Tracking**: Updates `lastUsedAt` timestamp

### Error Handling
- **Duplicate Client**: Returns 400 Bad Request with descriptive message
- **Expired Key**: Automatically marks as expired and returns 401 Unauthorized
- **Invalid Key**: Returns 401 Unauthorized with appropriate message

## Database Schema Changes

### Client Entity
- Added unique constraint on `(clientName, organizationUuid)`

### ApiKey Entity
- `expiresAt`: Expiry timestamp (used in validation)
- `lastUsedAt`: Last usage timestamp (updated on each validation)

## Migration Notes
- Existing API keys will have `null` expiry times
- These keys will continue to work but should be rotated for security
- New keys will have proper expiry times set
- Database constraints may require migration for existing duplicate clients 