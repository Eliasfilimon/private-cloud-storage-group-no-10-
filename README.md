# Secure Self-Hosted Private Cloud Storage System

A secure, self-hosted private cloud storage solution for academic institutions.

## Project Structure

```
secure-cloud-storage/
├── backend/          # Spring Boot API
├── frontend/         # React.js UI
├── docs/            # Documentation
└── README.md
```

## Team Members - Group 10
- DORECE MAKUHANA (T22-03-06104)
- KULWA MARUBA (T22-03-14638)
- EDWIN MHONDELA YOMBO (T22-03-09887)
- ELIAS MABELE FILIMON (T22-03-11751)
- EMANUEL MAHONA JOHN (T22-03-09888)

## Technology Stack

### Backend
- Java 17+
- Spring Boot 3.x
- Spring Security
- PostgreSQL
- Spring Data JPA

### Frontend
- React.js
- React Router
- Axios
- Tailwind CSS / Material-UI

## Getting Started

### Prerequisites
- Java JDK 17 or higher
- Node.js 18+ and npm
- PostgreSQL 14+
- Git

### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

## Features
- Secure user authentication and authorization
- Admin-only user registration (no public registration)
- **Staff-only system** - Role-based access control (Admin, Lecturer)
- File upload, download, and management
- File versioning
- Encrypted file storage
- Automated backups
- Audit logging
- Storage analytics and reporting
- Secure file sharing

## User Roles
This system is designed exclusively for **university staff**:
- **Admin**: Full system access, user management, system configuration
- **Lecturer**: Personal file storage, file management, secure file sharing

## License
Academic Project - University of Dodoma
