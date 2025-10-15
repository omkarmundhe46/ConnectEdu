# Club Service - CampusConnect Smart College Portal

## Overview
Club Service is a Spring Boot microservice that manages club operations for the CampusConnect Smart College Portal.

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
CREATE DATABASE campusconnect_clubdb;
```

2. Update database credentials in `application.yml` if needed.

## API Endpoints

### Create Club
- **POST** `/api/clubs`
- **Body**: 
```json
{
  "name": "Computer Science Club",
  "description": "A club for computer science students to collaborate and learn together"
}
```

### Get All Clubs
- **GET** `/api/clubs`

### Get Club by ID
- **GET** `/api/clubs/{id}`

### Update Club
- **PUT** `/api/clubs/{id}`
- **Body**: Same as create club

### Delete Club
- **DELETE** `/api/clubs/{id}`

## Running the Application
```bash
mvn spring-boot:run
```

The service will start on port 8082.

## Features
- Clean layered architecture (Controller → Service → Repository)
- DTO pattern for request/response
- Input validation
- Custom exception handling
- Club name uniqueness validation
- Automatic timestamp management