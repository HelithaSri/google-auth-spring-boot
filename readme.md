# Auth Demo — Spring Boot 4.0

A demo project to learn and practice authentication patterns in Spring Boot 4.0, including JWT-based auth and Google OAuth2 login.

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.6**
- **Spring Security + JWT (JJWT 0.12.6)**
- **Spring OAuth2 Client** — Google Login
- **Spring Data JPA + Hibernate 7**
- **PostgreSQL (Neon cloud)**
- **Lombok**
- **Gradle**

## Features

- User registration with BCrypt password hashing
- Login with email and password
- JWT access token + refresh token generation
- Protected endpoints via JWT filter
- Google OAuth2 login — find or create user automatically
- Global exception handler with validation error responses
- Consistent API response wrapper (`ApiResponse<T>`)

## Project Structure

```
src/main/java/com/example/auth/
  ├── common/
  │   ├── response/        → ApiResponse, BaseController
  │   ├── security/        → JwtService, JwtAuthFilter, OAuth2SuccessHandler
  │   └── exceptions/      → GlobalExceptionHandler
  ├── entities/            → User
  ├── repository/          → UserRepository
  ├── requests/            → RegisterRequest, LoginRequest
  ├── responses/           → AuthResponse
  └── config/              → SecurityConfig
```

## API Endpoints

| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login with email + password |
| GET | `/api/auth/me` | Protected | Get current user |
| GET | `/oauth2/authorization/google` | Public | Start Google login |

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL database (or Neon free cloud DB)
- Google OAuth2 credentials

### Setup

1. Clone the repo
```bash
git clone https://github.com/yourusername/auth-demo.git
cd auth-demo
```

2. Configure `application.properties`
```properties
# Database
spring.datasource.url=jdbc:postgresql://your-neon-url/dbname
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# JWT
app.jwt.secret=your-secret-key-at-least-32-characters-long
app.jwt.access-token-expiry=900000
app.jwt.refresh-token-expiry=604800000

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google
```

3. Run the app
```bash
./gradlew bootRun
```

4. App runs on `http://localhost:8080`

## Authentication Flow

### Normal Login
```
POST /api/auth/register  →  creates user, returns JWT tokens
POST /api/auth/login     →  validates password, returns JWT tokens
GET  /api/auth/me        →  send token in Authorization: Bearer <token>
```

### Google OAuth2 Login
```
Visit http://localhost:8080/oauth2/authorization/google
  → Google login page opens
  → User approves
  → Google calls back your backend
  → Backend finds or creates user in DB
  → Backend generates JWT tokens
  → Redirects to frontend with tokens
```

## Token Details

| Token | Expiry | Purpose |
|-------|--------|---------|
| Access Token | 15 minutes | Authenticate API requests |
| Refresh Token | 7 days | Get new access token |

## Notes

- This is a **learning/demo project** — not production ready
- Google OAuth tokens in URL redirect are fine for demo but use Redis code exchange in production
- Passwords are hashed with BCrypt
- Google users have `password_hash = null` and `auth_provider = GOOGLE`