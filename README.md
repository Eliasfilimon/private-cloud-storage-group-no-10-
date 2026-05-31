<div align="center">

# 🔐 UDOM Secure Cloud Storage

### Enterprise-Grade Self-Hosted Private Cloud Storage for Academic Institutions

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![MinIO](https://img.shields.io/badge/MinIO-Storage-C72E49?style=for-the-badge&logo=minio&logoColor=white)](https://min.io/)
[![License](https://img.shields.io/badge/License-Academic-orange?style=for-the-badge)](LICENSE)

<p align="center">
  <strong>A secure, scalable, and feature-rich cloud storage solution designed for universities and academic institutions.</strong>
</p>

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [API Reference](#-api-reference)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Deployment](#-deployment)
- [API Reference](#-api-reference)
- [Security](#-security)
- [Monitoring](#-monitoring)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**UDOM Secure Cloud Storage** is a comprehensive, self-hosted private cloud storage system built specifically for academic institutions. It provides enterprise-grade security with **AES-256-GCM encryption**, role-based access control, and seamless file management capabilities.

### Why UDOM Cloud?

- **🔒 Military-Grade Security** — All files encrypted at rest with AES-256-GCM
- **🏛️ Academic-Focused** — Designed for university workflows and compliance
- **🚀 Self-Hosted** — Complete data sovereignty, no third-party dependencies
- **📊 Full Observability** — Built-in monitoring with Prometheus & Grafana
- **🐳 Container-Ready** — One-command deployment with Docker Compose

---

## ✨ Features

### Core Functionality
| Feature | Description |
|---------|-------------|
| **File Management** | Upload, download, preview, rename, move, and delete files |
| **Folder Organization** | Create color-coded folders with hierarchical navigation |
| **File Sharing** | Share files with other users or via secure public links |
| **File Versioning** | Automatic version history with restore capability |
| **Trash & Recovery** | Soft delete with 30-day recovery window |
| **Document Preview** | In-browser preview for PDF, images, Word, Excel, and text files |
| **Multi-File Upload** | Drag-and-drop with progress tracking (up to 500MB per file) |
| **File Search** | Real-time search across all user files |

### Security & Access Control
| Feature | Description |
|---------|-------------|
| **AES-256-GCM Encryption** | End-to-end encryption with authenticated encryption |
| **JWT Authentication** | Secure token-based authentication with 24-hour expiry |
| **Role-Based Access** | Admin and Staff roles with granular permissions |
| **Two-Factor Auth (TOTP)** | Optional 2FA with authenticator app support |
| **Session Management** | Auto-logout after 15 minutes of inactivity |
| **Audit Logging** | Complete activity trail for compliance |
| **Rate Limiting** | Protection against brute-force attacks |

### Administration
| Feature | Description |
|---------|-------------|
| **User Management** | Create, edit, activate/deactivate users |
| **Bulk User Import** | CSV upload for batch user creation |
| **HR System Integration** | Fetch users from external HR APIs |
| **Storage Quotas** | Per-user storage limits with request workflow |
| **System Health Dashboard** | Real-time system metrics and status |
| **Backup Management** | Scheduled and manual backup capabilities |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UDOM Cloud Storage                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────────────────┐ │
│  │   Frontend   │────▶│   Backend    │────▶│      PostgreSQL DB       │ │
│  │  React 18    │     │ Spring Boot  │     │   (Users, Metadata)      │ │
│  │  Vite + TW   │     │   Java 17    │     └──────────────────────────┘ │
│  └──────────────┘     └──────┬───────┘                                  │
│                              │                                          │
│                              ▼                                          │
│                    ┌──────────────────┐                                 │
│                    │   MinIO Storage  │                                 │
│                    │ (Encrypted Files)│                                 │
│                    └──────────────────┘                                 │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    Observability Stack                            │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐  │   │
│  │  │ Prometheus │  │  Grafana   │  │    Loki    │  │  Promtail  │  │   │
│  │  │  Metrics   │  │ Dashboards │  │    Logs    │  │ Collector  │  │   │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 LTS | Runtime environment |
| Spring Boot | 3.2.2 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM |
| PostgreSQL | 15+ | Relational database |
| MinIO | Latest | Object storage (S3-compatible) |
| JWT (jjwt) | 0.12.3 | Token-based authentication |
| Bouncy Castle | 1.70 | Cryptographic operations |
| Resilience4j | 2.1.0 | Rate limiting & circuit breaker |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.2 | UI framework |
| Vite | 5.x | Build tool & dev server |
| Tailwind CSS | 3.4 | Utility-first styling |
| React Router | 6.x | Client-side routing |
| Axios | 1.6 | HTTP client |
| React Icons | 5.x | Icon library |
| Recharts | 2.10 | Data visualization |
| React Toastify | 10.x | Toast notifications |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker & Docker Compose | Containerization |
| Nginx | Reverse proxy & static serving |
| Prometheus | Metrics collection |
| Grafana | Metrics visualization |
| Loki + Promtail | Log aggregation |

---

## 🚀 Quick Start

### Prerequisites

- **Docker** 20.10+ and **Docker Compose** 2.x
- **Git** for cloning the repository
- 4GB RAM minimum (8GB recommended)

### One-Command Deployment

```bash
# Clone the repository
git clone https://github.com/udom/secure-cloud-storage.git
cd secure-cloud-storage

# Copy environment template
cp .env.example .env

# Start all services
docker-compose up -d

# Check service status
docker-compose ps
```

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:3002 | — |
| **Backend API** | http://localhost:8080/api | — |
| **API Docs** | http://localhost:8080/swagger-ui.html | — |
| **MinIO Console** | http://localhost:9003 | minioadmin / minioadmin123 |
| **Grafana** | http://localhost:3001 | admin / admin |
| **Prometheus** | http://localhost:9090 | — |

### Default Admin Account

```
Email: admin@udom.ac.tz
Password: ADMIN (must change on first login)
```

---

## 📁 Project Structure

```
secure-cloud-storage/
├── backend/                    # Spring Boot API
│   ├── src/main/java/         # Java source code
│   │   └── com/udom/securecloud/
│   │       ├── config/        # Configuration classes
│   │       ├── controller/    # REST API endpoints
│   │       ├── dto/           # Data transfer objects
│   │       ├── model/         # JPA entities
│   │       ├── repository/    # Data access layer
│   │       ├── security/      # Security components
│   │       └── service/       # Business logic
│   ├── src/main/resources/    # Configuration files
│   ├── Dockerfile             # Backend container
│   └── pom.xml                # Maven dependencies
│
├── frontend/                   # React SPA
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── pages/             # Page components
│   │   ├── services/          # API client
│   │   └── styles/            # CSS themes
│   ├── Dockerfile             # Frontend container
│   └── package.json           # NPM dependencies
│
├── grafana/                    # Grafana dashboards
├── nginx/                      # Nginx configuration
├── docker-compose.yml          # Production compose
├── docker-compose.dev.yml      # Development compose
└── Makefile                    # Build automation
```

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
DB_NAME=secure_cloud_storage
DB_USER=postgres
DB_PASSWORD=your_secure_password

# JWT Security
JWT_SECRET=your-256-bit-secret-key

# MinIO Object Storage
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=your_minio_password
MINIO_BUCKET=secure-cloud-storage

# Encryption
MASTER_ENCRYPTION_KEY=your-base64-encoded-32-byte-key

# Email (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Monitoring
GRAFANA_PASSWORD=admin
```

---

## 🚢 Deployment

### Docker Compose (Recommended)

```bash
# Production deployment
docker-compose -f docker-compose.yml up -d

# Development with hot-reload
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose logs -f backend frontend

# Stop all services
docker-compose down
```

### Manual Build

```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/secure-cloud-storage-1.0.0.jar

# Frontend
cd frontend
npm install
npm run build
npm run preview
```

---

## 📚 API Reference

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/logout` | User logout |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password |

### Files
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/files` | List user files |
| POST | `/api/files/upload` | Upload file |
| GET | `/api/files/{id}/download` | Download file |
| GET | `/api/files/{id}/preview` | Preview file |
| PUT | `/api/files/{id}/rename` | Rename file |
| PUT | `/api/files/{id}/move` | Move file to folder |
| DELETE | `/api/files/{id}` | Delete file |
| GET | `/api/files/search` | Search files |

### Folders
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/folders` | List folders |
| POST | `/api/folders` | Create folder |
| PUT | `/api/folders/{id}` | Update folder |
| DELETE | `/api/folders/{id}` | Delete folder |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| POST | `/api/admin/users` | Create user |
| PUT | `/api/admin/users/{id}/storage` | Assign storage quota |
| POST | `/api/admin/users/bulk-upload` | Bulk import users |
| GET | `/api/admin/system-health` | System health status |

Full API documentation available at `/swagger-ui.html`

---

## 🔒 Security

### Encryption
- **Algorithm**: AES-256-GCM (Galois/Counter Mode)
- **Key Management**: Master key wrapping with per-file keys
- **At-Rest**: All files encrypted before storage in MinIO
- **In-Transit**: HTTPS/TLS for all communications

### Authentication
- **JWT Tokens**: 24-hour expiry with secure signing
- **Password Hashing**: BCrypt with strength 12
- **Session Timeout**: 15-minute inactivity auto-logout
- **2FA Support**: TOTP-based two-factor authentication

### Access Control
- **RBAC**: Admin and Staff roles
- **File Ownership**: Users can only access their own files
- **Share Permissions**: Granular view/download permissions
- **Audit Trail**: All actions logged for compliance

---

## 📊 Monitoring

### Grafana Dashboards
- **System Overview**: CPU, memory, disk usage
- **API Metrics**: Request rates, latencies, error rates
- **Storage Metrics**: Upload/download volumes, quota usage
- **User Activity**: Active users, login patterns

### Health Endpoints
```bash
# Application health
curl http://localhost:8080/actuator/health

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed for academic use at the University of Dodoma (UDOM).

---

<div align="center">

**Built with ❤️ for UDOM**

[Report Bug](https://github.com/udom/secure-cloud-storage/issues) • [Request Feature](https://github.com/udom/secure-cloud-storage/issues)

</div>
