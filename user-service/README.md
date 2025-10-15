# User Service - CampusConnect Smart College Portal

## Overview
User Service is a Spring Boot microservice that manages user operations for the CampusConnect Smart College Portal.

## Tech Stack
- Spring Boot 3.2.0
- Java 21
- Spring Web
- Spring Data JPA
- MySQL 8.0
- Lombok
- Maven

## Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE campusconnect_userdb;
```

2. Update database credentials in `application.yml` if needed.

## API Endpoints

### Create User
- **POST** `/api/users`
- **Body**: 
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "department": "Computer Science"
}
```

### Get All Users
- **GET** `/api/users`

### Get User by ID
- **GET** `/api/users/{id}`

### Get User by Email
- **GET** `/api/users/email/{email}`

### Update User
- **PUT** `/api/users/{id}`
- **Body**: Same as create user

### Delete User
- **DELETE** `/api/users/{id}`

## Running the Application
```bash
mvn spring-boot:run
```

The service will start on port 8081.

## Features
- Clean layered architecture (Controller → Service → Repository)
- DTO pattern for request/response
- Input validation
- Custom exception handling
- Email uniqueness validation
- Automatic timestamp management