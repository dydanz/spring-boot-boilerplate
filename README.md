 # Spring Boot Authentication API

A secure authentication API built with Spring Boot, featuring user registration with email verification, JWT authentication, and rate limiting.

## Features

- User registration with email/phone verification
- OTP-based email verification
- JWT authentication
- Rate limiting for OTP verification
- Secure password storage with BCrypt
- PostgreSQL database
- Docker support

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Gmail account (for sending OTP emails)

## Configuration

1. Update email configuration in `docker-compose.yml`:
   ```yaml
   SMTP_USERNAME=your-email@gmail.com
   SMTP_PASSWORD=your-app-password
   ```
   Note: For Gmail, you'll need to generate an App Password. Go to Google Account > Security > 2-Step Verification > App Passwords

2. (Optional) Update other configuration in `docker-compose.yml`:
   - JWT secret
   - Database credentials
   - Port mappings

## Running the Application

1. Build and start the containers:
   ```bash
   docker-compose up --build
   ```

2. The API will be available at `http://localhost:8080`

## API Endpoints

### Registration Flow
1. Register a new user:
   ```http
   POST /api/v1/auth/register
   Content-Type: application/json

   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "phoneNumber": "+1234567890",
     "password": "securepassword"
   }
   ```

2. Verify OTP:
   ```http
   POST /api/v1/auth/verify-otp
   Content-Type: application/json

   {
     "email": "john@example.com",
     "otp": "123456"
   }
   ```

### Authentication
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securepassword"
}
```

## Security Features

1. Password Hashing: BCrypt is used for secure password storage
2. JWT Authentication: Secure token-based authentication
3. Rate Limiting: Prevents brute force attacks on OTP verification
4. Input Validation: All user inputs are validated
5. Email Verification: Two-step verification process

## Development

To run the application locally without Docker:

1. Start a PostgreSQL instance
2. Update `application.yml` with your database and email configurations
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Testing

Run the tests using:
```bash
./gradlew test
```
 