# Notification Service - CampusConnect Smart College Portal

## Overview
Notification Service is a Spring Boot microservice that handles email notifications using SMTP and integrates with other services using OpenFeign.

## Tech Stack
- Spring Boot 3.2.0
- Java 21
- Spring Web
- Spring Data JPA
- Spring Mail (Jakarta Mail)
- Spring Cloud OpenFeign
- Thymeleaf (Template Engine)
- MySQL 8.0
- Lombok
- Maven

## Database Setup
1. Create MySQL database:
```sql
CREATE DATABASE campusconnect_notificationdb;
```

2. Update database credentials in `application.yml` if needed.

## SMTP Configuration
Configure your email provider in `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## Entities
- **EmailTemplate**: id, code, subjectTemplate, bodyTemplate, createdAt, updatedAt
- **NotificationLog**: id, templateId, userId, toEmail, subject, body, status, errorMessage, createdAt, updatedAt

## API Endpoints

### Template Management
- **POST** `/api/notify/templates` - Create email template
- **GET** `/api/notify/templates` - Get all templates
- **GET** `/api/notify/templates/{code}` - Get template by code
- **PUT** `/api/notify/templates/{code}` - Update template
- **DELETE** `/api/notify/templates/{code}` - Delete template

### Email Operations
- **POST** `/api/notify/email/render` - Preview rendered template
- **POST** `/api/notify/email/send` - Send email

## Example Requests

### Create Template
```json
{
  "code": "EVENT_CREATED",
  "subjectTemplate": "New Event: [(${eventName})]",
  "bodyTemplate": "<h2>Hello [(${userName})]!</h2><p>Event: <strong>[(${eventName})]</strong></p>"
}
```

### Send Email with Template
```json
{
  "templateCode": "EVENT_CREATED",
  "toEmail": "user@example.com",
  "variables": {
    "userName": "Alice",
    "eventName": "Hackathon 2025"
  }
}
```

### Send Email with Raw Content
```json
{
  "toEmail": "user@example.com",
  "rawSubject": "Welcome",
  "rawBody": "<h2>Welcome to CampusConnect!</h2>"
}
```

## Running the Application
```bash
mvn spring-boot:run
```

The service will start on port 8085.

## Features
- Template-based email system with Thymeleaf
- Asynchronous email sending
- User email resolution via Feign clients
- Comprehensive logging and error handling
- HTML email support
- Variable substitution in templates

## Integration
- **User Service**: Resolves user emails by ID
- **Future**: Club Service and Event Service integration for notifications

## Template Variables
Templates use Thymeleaf syntax: `[(${variableName})]`
Common variables:
- `userName` - User's display name
- `eventName` - Event title
- `eventDate` - Event date/time
- `eventLocation` - Event location
- `clubName` - Club name