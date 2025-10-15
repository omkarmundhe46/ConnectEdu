# Discussion Service - CampusConnect Smart College Portal

## Overview
Discussion Service is a Spring Boot microservice that manages event-based discussion rooms for the CampusConnect Smart College Portal.

## Tech Stack
- Spring Boot 3.2.0
- Java 21
- Spring Web
- Spring Data JPA
- MySQL 8.0
- Lombok
- Maven
- File Upload Support (Spring Multipart)

## Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE campusconnect_discussiondb;
```

2. Update database credentials in `application.yml` if needed.

## Entities
- **DiscussionMessage**: id, eventId, userId, content, fileUrl, messageType, sentAt

## API Endpoints

### Send Text Message
- **POST** `/api/discussions/events/{eventId}/messages`
- **Body**: 
```json
{
  "userId": 1,
  "content": "Hello team!",
  "messageType": "TEXT"
}
```

### Send File Message
- **POST** `/api/discussions/events/{eventId}/messages/file`
- **Form Data**: 
  - userId: 1
  - file: [file upload]
  - messageType: IMAGE or FILE

### Get Event Messages
- **GET** `/api/discussions/events/{eventId}/messages?userId={userId}`

### Delete Message
- **DELETE** `/api/discussions/messages/{messageId}?userId={userId}`

## Running the Application
```bash
mvn spring-boot:run
```

The service will start on port 8084.

## Features
- Text and file message support
- File upload with 10MB size limit
- Event-based discussion rooms
- Member validation (integration with Club Service)
- Admin deletion privileges
- Clean layered architecture
- Global exception handling

## File Storage
Files are stored in `./uploads/` directory with UUID-based naming.

## Integration
- **Event Service**: Validates event existence
- **Club Service**: Validates user membership
- **User Service**: Retrieves user names