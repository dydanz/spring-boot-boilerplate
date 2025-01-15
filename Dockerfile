# Use the official OpenJDK image as a base
FROM openjdk:17-jdk-slim AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy the source code
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build --no-daemon

# Use a new stage to create the final image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set the command to run the application
CMD ["java", "-jar", "app.jar"]
ENTRYPOINT ["java","-jar","/app.jar"] 
