# Docker Deployment Summary

This guide provides a complete Docker setup for the Secure Cloud Storage project.

## Files Added

### Core Docker Files
- **backend/Dockerfile** - Multi-stage build for Java Spring Boot
- **frontend/Dockerfile** - Multi-stage build for React/Vite with Nginx
- **frontend/nginx.conf** - Nginx configuration with API proxy
- **docker-compose.yml** - Complete service orchestration
- **.dockerignore** - Build optimization (backend and frontend)

### Configuration & Environment
- **.env.example** - Environment variables template
- **docker-compose.dev.yml** - Development override configuration
- **docker-compose.prod.yml** - Production override configuration with resource limits

### Scripts & Automation
- **docker-control.sh** - Comprehensive Docker management script
- **deploy.sh** - Production deployment with security setup
- **Makefile** - Convenient make targets for common operations

### Documentation
- **DOCKER-SETUP.md** - Complete Docker setup guide
- **.gitignore** - Git ignore patterns (updated)

## Quick Start

### Option 1: Using Docker Control Script
```bash
cd secure-cloud-storage
./docker-control.sh start
./docker-control.sh create-admin admin MyPassword123
```

### Option 2: Using Make
```bash
cd secure-cloud-storage
make start
make create-admin
```

### Option 3: Using Docker Compose Directly
```bash
cd secure-cloud-storage
docker-compose up -d
```

## Architecture

The Docker setup includes:

```
┌─────────────────────────────────────────────────┐
│          Secure Cloud Storage Stack             │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │ Frontend │  │ Backend  │  │ MinIO    │     │
│  │ (Nginx)  │  │ (Spring) │  │ (S3)     │     │
│  │ :3000    │  │ :8080    │  │ :9000-01 │     │
│  └──────────┘  └──────────┘  └──────────┘     │
│       │              │              │          │
│  ┌────────────────────────────────────────┐   │
│  │    PostgreSQL Database                 │   │
│  │    (:5432)                             │   │
│  └────────────────────────────────────────┘   │
│                                                 │
│  ┌──────────────────────────────────────┐     │
│  │    Monitoring Stack                  │     │
│  │  Prometheus (9090)                   │     │
│  │  Grafana (3001)                      │     │
│  │  Loki (3100) + Promtail              │     │
│  └──────────────────────────────────────┘     │
│                                                 │
└─────────────────────────────────────────────────┘
```

## Services

| Service | Port(s) | Container | Purpose |
|---------|---------|-----------|---------|
| Frontend | 3000 | Nginx | React SPA with API proxy |
| Backend | 8080 | Spring Boot | REST API |
| PostgreSQL | 5432 | Postgres 15 | Primary database |
| MinIO | 9000, 9001 | MinIO | Object storage |
| Prometheus | 9090 | Prometheus | Metrics collection |
| Grafana | 3001 | Grafana | Metrics visualization |
| Loki | 3100 | Loki | Log aggregation |
| Promtail | - | Promtail | Log collector |

## Key Features

✅ **Production-Ready**
- Multi-stage Docker builds for optimized images
- Health checks on all services
- Resource limits configured
- Non-root users for security

✅ **Development-Friendly**
- Development and production override configs
- Easy logging and debugging
- Quick admin user creation
- Database backup/restore

✅ **Scalable**
- Database replication support
- Backend horizontal scaling
- Monitoring and alerting

✅ **Secure**
- Environment variable configuration
- Secret management via .env
- Nginx security headers
- Non-root container execution

## Common Commands

```bash
# Start services
make start

# View logs
make logs
make logs-backend

# Create admin user
make create-admin

# Backup database
make db-backup

# Check health
make health

# Access database shell
make db-shell

# Stop services
make stop

# Clean up (deletes data)
make clean
```

## Deployment Options

### Local Development
```bash
docker-compose up -d
```

### Production with Overrides
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Automated Deployment
```bash
./deploy.sh
```

## Volume Mounts

| Volume | Mount Point | Purpose |
|--------|------------|---------|
| postgres_data | /var/lib/postgresql/data | Database persistence |
| minio_data | /data | MinIO storage |
| backend_uploads | /app/uploads | File uploads |
| backend_backups | /app/backups | Database backups |
| backend_logs | /app/logs | Application logs |
| prometheus_data | /prometheus | Metrics storage |
| grafana_data | /var/lib/grafana | Grafana settings |
| loki_data | /loki | Loki logs |

## Environment Variables

Key environment variables:
- `DB_NAME` - PostgreSQL database name
- `DB_USER` - Database user (not root)
- `DB_PASSWORD` - Secure database password
- `JWT_SECRET` - JWT signing key (generate for production)
- `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` - MinIO credentials
- `GRAFANA_PASSWORD` - Grafana admin password
- `DDL_AUTO` - Set to `validate` in production

## Security Considerations

1. **Change Default Passwords**: Update all passwords in .env
2. **Generate JWT Secret**: Use `openssl rand -base64 32`
3. **Use HTTPS**: Deploy behind a reverse proxy with SSL/TLS
4. **Firewall**: Restrict access to ports 9001, 3001, 9090
5. **Backups**: Regular PostgreSQL backups
6. **Updates**: Keep base images updated

## Troubleshooting

### Port Already in Use
Map to different port in docker-compose.yml:
```yaml
ports:
  - "3001:3000"  # Use 3001 instead of 3000
```

### Services Won't Start
```bash
docker-compose down -v
docker system prune -a
docker-compose up -d
```

### Database Connection Issues
```bash
docker-compose logs db
docker-compose exec db pg_isready -U postgres
```

### Frontend Can't Reach API
Check backend logs and CORS configuration in application.properties

## Next Steps

1. Read [DOCKER-SETUP.md](./DOCKER-SETUP.md) for detailed instructions
2. Customize `.env` for your environment
3. Set up SSL/TLS with a reverse proxy
4. Configure automated backups
5. Set up monitoring and alerts
6. Deploy to production

## Files Structure

```
secure-cloud-storage/
├── docker-compose.yml          # Main Docker Compose config
├── docker-compose.dev.yml      # Development overrides
├── docker-compose.prod.yml     # Production overrides
├── docker-control.sh           # Management script
├── deploy.sh                   # Production deployment
├── Makefile                    # Make targets
├── .env.example               # Environment template
├── DOCKER-SETUP.md            # This guide
├── backend/
│   ├── Dockerfile             # Backend image definition
│   └── .dockerignore          # Build optimization
└── frontend/
    ├── Dockerfile             # Frontend image definition
    ├── nginx.conf             # Nginx configuration
    └── .dockerignore          # Build optimization
```

## Support & References

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Nginx Docs](https://nginx.org/en/docs/)

---

**Last Updated**: May 2026
**Status**: Production Ready ✓
