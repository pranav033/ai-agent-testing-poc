
# Phase 1 — Authentication and Authorization

## 1. Objective

Implement an authentication and authorization layer for the AI Agent Testing POC.

The objective is to ensure that:
- Only authenticated users can access protected chat APIs.
- User identity is derived from the authenticated security context.
- Users cannot access resources belonging to other users.
- Administrator privileges are enforced through server-side roles.
- Conversation memory is isolated between users, even when they share a session ID.

This is a demonstration project using synthetic users and orders.

## 2. Authentication Architecture

### Technology

- Java 21
- Spring Boot
- Spring Security
- JWT (JSON Web Tokens)
- JJWT
- BCrypt password hashing

### Authentication flow

1. Client submits username and password to `POST /api/auth/login`.
2. Spring Security authenticates the credentials.
3. Passwords are verified using BCrypt.
4. The application generates a signed JWT.
5. The client sends the JWT in subsequent requests using the Bearer authorization scheme.
6. The JWT authentication filter validates the token.
7. The authenticated user is established in the Spring Security context.

### JWT characteristics

- Signed using an HMAC-based signing key.
- Subject contains the authenticated user's UUID.
- Username is included as a claim.
- Issued-at and expiration timestamps are included.
- Configured access-token lifetime: 15 minutes.
- Expired or incorrectly signed tokens are rejected.

The JWT signing secret is supplied through environment configuration.

## 3. Authorization Architecture

### User identities

The demonstration application contains three synthetic accounts:

| User | Role | User ID |
|---|---|---|
| Alice | ROLE_USER | 11111111-1111-4111-8111-111111111111 |
| Bob | ROLE_USER | 22222222-2222-4222-8222-222222222222 |
| Admin | ROLE_ADMIN | 33333333-3333-4333-8333-333333333333 |

Users are held in an in-memory user service for this POC.

### Order ownership

The application contains two synthetic orders:

| Order ID | Owner | Status |
|---|---|---|
| 101 | Alice | Shipped |
| 102 | Bob | Out for delivery |

The OrderService checks ownership before returning order information.

Authorization rules:

- Alice can access order 101.
- Bob can access order 102.
- Alice cannot access order 102.
- Bob cannot access order 101.
- Admin can access both orders.
- Unknown orders return a not-found response.

Unauthorized access is handled through a dedicated exception and a global exception handler.

Expected HTTP responses:

| Scenario | HTTP status |
|---|---:|
| Missing or invalid authentication | 401 |
| Expired JWT | 401 |
| Tampered JWT | 401 |
| Authenticated but unauthorized user | 403 |
| Authorized access | 200 |

## 4. Identity and Privilege Protection

The application derives user identity and administrator privileges from the authenticated principal.

The client cannot elevate its privileges by submitting fields such as:
- userId
- isAdmin
- role

The chat endpoint uses the authenticated user's UUID and server-side role information when invoking the chat service.

## 5. Conversation Session Isolation

Conversation context is keyed using:

`ConversationKey(UUID userId, String sessionId)`

This prevents two users from accessing each other's conversation memory by supplying the same session ID.

Additional protection:
- Order ownership is checked before storing an order in conversation memory.
- Unauthorized order requests are not retained as valid conversation context.
- Follow-up requests revalidate access to the referenced order.

## 6. Automated Security Testing

The final Maven test suite completed successfully.

**Test execution summary:**
- Tests run: 30
- Failures: 0
- Errors: 0
- Skipped: 0
- Build result: SUCCESS

### Authentication test scenarios

- Missing JWT.
- Malformed JWT.
- Invalid login credentials.
- Expired JWT.
- Tampered JWT signature.

### Authorization test scenarios

- Alice accessing her own order.
- Alice attempting to access Bob's order.
- Bob accessing his own order.
- Bob attempting to access Alice's order.
- Admin accessing both users' orders.
- Accessing an unknown order.
- Client-supplied identity and administrator privilege spoofing.

### Conversation isolation test scenarios

- Two users sharing the same session ID.
- Independent order context for each user.
- Unauthorized order requests not being stored in conversation memory.
- Follow-up requests retaining the correct user's context.

## 7. Security Controls Implemented

| Control | Implementation |
|---|---|
| Password storage | BCrypt |
| Authentication | Spring Security |
| API authentication | Signed JWT Bearer tokens |
| Token expiration | 15 minutes |
| Token signature validation | JJWT |
| Resource ownership | OrderService |
| Role-based authorization | ROLE_USER / ROLE_ADMIN |
| Identity derivation | Authenticated security principal |
| Conversation isolation | User UUID + session ID |
| Authorization error handling | GlobalExceptionHandler |
| Regression coverage | JUnit integration and unit tests |

## 8. Current Limitations

This is an educational and demonstration POC, not a production-ready identity platform.

Current limitations:

- Users and orders are stored in memory.
- Demo credentials are synthetic.
- No refresh-token rotation or revocation.
- No persistent identity provider integration.
- No distributed session or conversation store.
- No enterprise identity provider integration.

The JWT secret must be managed securely through environment or secret-management configuration outside automated tests.

## 9. Phase 1 Completion Criteria

- [x] Spring Security authentication.
- [x] BCrypt password hashing.
- [x] JWT generation and validation.
- [x] Expired-token rejection.
- [x] Tampered-token rejection.
- [x] Role-based authorization.
- [x] BOLA / IDOR protection.
- [x] Admin access control.
- [x] Client identity spoofing protection.
- [x] Cross-user conversation isolation.
- [x] Unauthorized conversation memory protection.
- [x] Automated regression tests.
- [x] Final Maven test suite passing.

**Phase 1 status: Implementation and automated testing complete.**
