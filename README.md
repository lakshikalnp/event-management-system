# Scalable Event Management System (Backend)

## Objective

Design and implement a production-ready RESTful API for an Event Management Platform. The system allows users to manage events and attendance with secure access, robust filtering capabilities, and performance optimization. It emphasizes scalability, clean architecture, and best practices.

## Core Requirements

### Entities

#### User
- `id`: UUID
- `name`: String
- `email`: String
- `role`: USER | ADMIN
- `createdAt`, `updatedAt`

#### Event
- `id`: UUID
- `title`: String
- `description`: String
- `hostId`: UUID (Foreign Key to User)
- `startTime`, `endTime`: Timestamp
- `location`: String
- `visibility`: PUBLIC | PRIVATE
- `createdAt`, `updatedAt`

#### Attendance
- `eventId`: UUID
- `userId`: UUID
- `status`: GOING | MAYBE | DECLINED
- `respondedAt`: Timestamp

## API Endpoints

- `POST /api/v1/events`: Create an event (Authenticated users only)
- `PUT /api/v1/events/{id}`: Update event (Only host or admin)
- `DELETE /api/v1/events/{id}`: Delete event (Only host or admin)
- `GET /api/v1/events`: List events with filters (date, location, visibility)
- `GET /api/v1/eventsupcoming`: List upcoming events (paginated)
- `GET /api/v1/events/{id}/status`: Get event status
- `GET /api/v1/events/user/{userId}/all`: Events a user is hosting or attending
- `GET /api/v1/events/{id}`: Event details with attendee count

## Value-Added Features

- JWT Authentication & Role-Based Authorization
- Pagination, Sorting, and Advanced Filtering
- Optional Caching with Redis or Caffeine
- Rate Limiting / Throttling (per user/IP)
- Soft Deletes for Events (archiving instead of hard delete)

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven
- Optional: Redis or Caffeine

## Testing

- Unit Testing: JUnit 5
- Integration Testing: H2 database (or Testcontainers)
- Authentication Simulation: `@WithMockUser`, `@WithSecurityContext`
- Code Coverage: Ensure high coverage on services and controllers

## Getting Started

### Prerequisites

- Java 17+
- Maven
- PostgreSQL