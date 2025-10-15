# Event Service - CampusConnect Smart College Portal

## Overview
Event Service is a Spring Boot microservice that manages events and event registrations for the CampusConnect Smart College Portal.

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
CREATE DATABASE campusconnect_eventdb;
```

2. Update database credentials in `application.yml` if needed.

## Entities
- **Event**: id, title, description, location, startDate, endDate, createdAt
- **EventRegistration**: id, eventId, userId, registeredAt (unique constraint on eventId + userId)

## API Endpoints

### Event Management
- **POST** `/api/events` - Create new event
- **GET** `/api/events` - Get all events
- **GET** `/api/events/{id}` - Get event by ID
- **PUT** `/api/events/{id}` - Update event details
- **DELETE** `/api/events/{id}` - Delete event

### Event Registration
- **POST** `/api/events/{id}/register` - Register user to event
- **GET** `/api/events/{id}/registrations` - Get all registrations for event
- **DELETE** `/api/events/{id}/registrations/{userId}` - Cancel registration

## Example Requests

### Create Event
```json
{
  "title": "Tech Conference 2024",
  "description": "Annual technology conference",
  "location": "Main Auditorium",
  "startDate": "2024-06-15T09:00:00",
  "endDate": "2024-06-15T17:00:00"
}
```

### Register User
```json
{
  "userId": 1
}
```

## Running the Application
```bash
mvn spring-boot:run
```

The service will start on port 8083.

## Features
- Clean layered architecture (Controller → Service → Repository)
- DTO pattern for request/response
- Input validation (date range, required fields)
- Custom exception handling
- Duplicate registration prevention
- Automatic timestamp management