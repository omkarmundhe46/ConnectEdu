# Stage 1: Build the application using a Maven image
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the .jar file
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create the final, small image using just the Java Runtime
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the .jar file from the 'build' stage
# This wildcard * copes with different version numbers
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Expose the port (we will define the actual port in docker-compose)
# EXPOSE 8089

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]