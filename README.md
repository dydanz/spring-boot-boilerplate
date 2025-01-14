# Spring Boot Boilerplate API

AI said: "A production-ready Spring Boot RESTful API boilerplate with robust authentication, user management, and event tracking."

But yeah, it's just another (playground) RESTful API service boilerplate built with Java/Spring-boot

#### Disclaimer
As designated for my personal research AI-generated code, ALL OF THESE CODE ARE GENERATED AUTOMATICALLY! 

But feel free to fork, clone or whatever you want at your own risk. 
For questions or professional inquiries: [Linkedin](https://www.linkedin.com/in/dandi-diputra/)

## Features

- 🔐 JWT Authentication
- 👤 User Management
- 📧 Email Verification
- 🔄 Password Reset Flow
- 📝 Event Tracking
- 🚀 Async Processing
- 💾 Redis Caching
- 📊 Kafka Integration
- 🔍 API Documentation
- 🐳 Docker Support

## Technology Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Build Tool:** Gradle
- **Database:** PostgreSQL
- **Cache:** Redis
- **Message Broker:** Kafka
- **Documentation:** OpenAPI (Swagger)
- **Testing:** JUnit 5, Mockito
- **Migration:** Flyway
- **Security:** Spring Security, JWT
- **Generated and assisted by:** Cursor 0.8 + Claude 3.5 Sonnet in VSCode

## Prerequisites

Before you begin, ensure you have installed:

- JDK 17 or later
- Docker and Docker Compose
- Gradle 7.x or later
- Your favorite IDE (IntelliJ IDEA recommended)

## Getting Started

### 1. Clone the Repository
```
$ git clone https://github.com/yourusername/spring-boot-boilerplate.git
$ cd spring-boot-boilerplate
```

### 2. Configure Environment Variables

Create a `.env` file in the project root:
```properties
Database
POSTGRES_DB=boilerplate
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=28800
Mail
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password
```
### 3. Start Dependencies
```
$ gradle wrapper
$ chmod +x gradlew
$ ./gradlew build

$ docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis cache
- Kafka & Zookeeper

### 4. Run the Application
```$ ./gradlew bootRun```

The application will be available at `http://localhost:8080`

### 5. Access API Documentation

Visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## Development

### Database Migrations

Create a new migration:
```$ ./gradlew createMigration -PmigrationName=your_migration_name```

Apply migrations:
```$ ./gradlew flywayMigrate```


### Running Tests
```
Run all tests
$ ./gradlew test
Run specific test
$ ./gradlew test --tests "com.boilerplate.YourTest"
```

### Building for Production
```$ ./gradlew build```

The built artifact will be in `build/libs/`.

## API Endpoints

### Authentication
- POST `/api/v1/auth/register` - Register new user
- POST `/api/v1/auth/login` - Login
- POST `/api/v1/auth/verify-otp` - Verify OTP
- POST `/api/v1/auth/refresh-token` - Refresh token
- POST `/api/v1/auth/logout` - Logout

### User Profile
- GET `/api/v1/profile` - Get user profile
- PUT `/api/v1/profile` - Update profile
- DELETE `/api/v1/profile` - Delete profile

### Password Management
- POST `/api/v1/password/forgot` - Request password reset
- POST `/api/v1/password/reset` - Reset password

## Project Structure
```
src/
├── main/
│ ├── java/
│ │ └── com/
│ │ └── boilerplate/
│ │ ├── config/ # Configuration classes
│ │ ├── controller/ # REST controllers
│ │ ├── dto/ # Data Transfer Objects
│ │ ├── entity/ # JPA entities
│ │ ├── exception/ # Custom exceptions
│ │ ├── repository/ # Data repositories
│ │ ├── security/ # Security configurations
│ │ ├── service/ # Business logic
│ │ └── util/ # Utility classes
│ └── resources/
│ ├── db/migration/ # Flyway migrations
│ └── application.yml # Application configuration
└── test/
└── java/ # Test classes
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
