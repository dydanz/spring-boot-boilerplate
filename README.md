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

## API Documentation (Swagger UI)

This project uses Swagger UI for API documentation. The API documentation is automatically generated from the code and can be accessed through a web interface.

### Accessing Swagger UI

1. Start the application
2. Open your browser and navigate to: http://localhost:8080/swagger-ui/index.html

### Using Swagger UI

1. The Swagger UI interface will display all available API endpoints grouped by their controllers.
2. Each endpoint shows:
   - HTTP method (GET, POST, PUT, DELETE)
   - Path
   - Description
   - Request parameters
   - Request body schema (if applicable)
   - Response schema
   - Authentication requirements

### Authentication in Swagger UI

Most endpoints are protected and require authentication. To use these endpoints:

1. First, use the authentication endpoints to get a JWT token:
   - Register a new user (`/api/v1/auth/register`)
   - Login with credentials (`/api/v1/auth/authenticate`)
2. Click the 'Authorize' button (🔒) at the top of the page
3. In the authorization popup:
   - Enter your JWT token in the format: `Bearer your-token-here`
   - Click 'Authorize'
4. You can now access protected endpoints

### Testing Endpoints

1. Click on any endpoint to expand it
2. Click 'Try it out'
3. Fill in the required parameters or request body
4. Click 'Execute'
5. The response will be displayed below

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
 