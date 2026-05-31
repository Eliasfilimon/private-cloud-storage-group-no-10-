# Docker Setup Guide for Secure Cloud Storage

## Overview

This project is fully dockerized with support for:
- **Backend**: Java Spring Boot API
- **Frontend**: React/Vite static site with Nginx reverse proxy
- **Database**: PostgreSQL 15
- **Storage**: MinIO S3-compatible object storage
- **Monitoring**: Prometheus, Grafana, Loki, and Promtail stack

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB RAM allocated to Docker
- 2GB free disk space

## Quick Start

### 1. Clone and Navigate
```bash
cd secure-cloud-storage
```

### 2. Create Environment File
```bash
cp .env.example .env
# Edit .env with your configuration (optional for development)
```

### 3. Build and Start Services
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 4. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **MinIO Console**: http://localhost:9001 (minioadmin / minioadmin123)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin / admin)
- **Loki**: http://localhost:3100

### 5. Initialize First Admin User

Once the application is running, create the first admin user:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123",
    "displayName": "Admin User",
    "role": "ADMIN"
  }'
```

Then login at http://localhost:3000 with the credentials.

## Service Details

### Backend Service (Port 8080)
- **Image**: Built from `./backend/Dockerfile`
- **Database**: Connects to PostgreSQL on port 5432
- **Features**:
  - Multi-stage Maven build for optimized image size
  - Runs as non-root user for security
  - Health checks enabled
  - Auto-schema migration on startup

**Environment Variables**:
- `SPRING_DATASOURCE_URL`: PostgreSQL connection string
- `SPRING_DATASOURCE_USERNAME`: Database user
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing key
- `CORS_ALLOWED_ORIGINS`: CORS configuration
- `MINIO_URL`: MinIO service URL

### Frontend Service (Port 3000)
- **Image**: Built from `./frontend/Dockerfile`
- **Server**: Nginx with SPA routing
- **Features**:
  - Multi-stage Node.js build for minimal image
  - API proxy to backend at `/api`
  - Security headers (X-Frame-Options, X-Content-Type-Options, etc.)
  - Cache busting for static assets
  - Health checks enabled

**Nginx Configuration**: See `./frontend/nginx.conf`

### PostgreSQL (Port 5432)
- **Image**: `postgres:15-alpine`
- **Volume**: `postgres_data`
- **Schema**: Auto-initialized from `./backend/src/main/resources/database/schema.sql`
- **Health Check**: Built-in

### MinIO (Ports 9000, 9001)
- **Image**: `minio/minio:latest`
- **API Port**: 9000 (internal use by backend)
- **Console Port**: 9001 (web dashboard)
- **Volume**: `minio_data`
- **Default Credentials**: minioadmin / minioadmin123

### Monitoring Stack
- **Prometheus**: Metrics collection (port 9090)
- **Grafana**: Visualization (port 3001)
- **Loki**: Log aggregation (port 3100)
- **Promtail**: Log collector (no exposed port)

## Common Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### Rebuild Images
```bash
docker-compose build --no-cache
docker-compose up -d
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db

# Last 100 lines
docker-compose logs --tail 100
```

### Access Database
```bash
docker-compose exec db psql -U postgres -d secure_cloud_storage
```

### Restart Services
```bash
docker-compose restart backend frontend
```

### Clean Up
```bash
# Stop and remove containers
docker-compose down

# Also remove volumes (WARNING: deletes all data)
docker-compose down -v

# Remove unused images
docker image prune -a
```

## Production Deployment

### Security Considerations

1. **Change Default Passwords**:
   ```bash
   # Update .env before deploying
   DB_PASSWORD=<secure-password>
   JWT_SECRET=<generate-secure-random-string>
   MINIO_SECRET_KEY=<secure-password>
   GRAFANA_PASSWORD=<secure-password>
   ```

2. **Environment Variables**: Never commit `.env` to version control

3. **Reverse Proxy**: Use a reverse proxy (Nginx, Apache, Traefik) with SSL/TLS

4. **Database Backups**: Regularly backup PostgreSQL data:
   ```bash
   docker-compose exec db pg_dump -U postgres secure_cloud_storage > backup.sql
   ```

### Docker Production Settings

Update `docker-compose.yml` for production:

```yaml
backend:
  environment:
    SPRING_JPA_HIBERNATE_DDL_AUTO: validate  # Don't auto-migrate in production
    SERVER_SERVLET_SESSION_TIMEOUT: 30m
```

### Scaling

For multiple backend instances behind a load balancer:

```bash
docker-compose up -d --scale backend=3
```

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 3000
lsof -i :3000

# Kill process (Linux/Mac)
kill -9 <PID>

# Or map to different port in docker-compose.yml
ports:
  - "3001:3000"  # Use 3001 instead of 3000
```

### Backend Cannot Connect to Database
```bash
# Check database logs
docker-compose logs db

# Verify database is healthy
docker-compose ps

# Manual test
docker-compose exec db pg_isready -U postgres
```

### Frontend Cannot Connect to API
1. Check backend is running: `docker-compose ps`
2. Check backend logs: `docker-compose logs backend`
3. Verify CORS configuration in backend
4. Check Firefox/Chrome Network tab for failed requests

### Build Failures
```bash
# Clean and rebuild
docker-compose down
docker system prune -a -f
docker-compose build --no-cache
docker-compose up -d
```

### Permission Issues
```bash
# Fix file ownership (if needed)
sudo chown -R $USER:$USER ./backend ./frontend
```

## Monitoring and Logs

### Access Logs
- **Application Logs**: `docker-compose logs -f backend`
- **Nginx Logs**: `docker-compose logs -f frontend`
- **Database Logs**: `docker-compose logs -f db`

### View Metrics
1. Open Prometheus: http://localhost:9090
2. Open Grafana: http://localhost:3001
3. Add Prometheus and Loki data sources
4. Create dashboards for monitoring

### Database Backups
```bash
# Backup
docker-compose exec db pg_dump -U postgres secure_cloud_storage > backup.sql

# Restore
docker-compose exec -T db psql -U postgres secure_cloud_storage < backup.sql
```

## Performance Optimization

### Increase Resource Limits
In `docker-compose.yml`, add resource limits:

```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 1G
```

### Enable Docker BuildKit
```bash
export DOCKER_BUILDKIT=1
docker-compose build --no-cache
```

## Troubleshooting Network Issues

### Check Network
```bash
# Inspect network
docker network inspect secure-cloud-storage_scs_network

# Test connectivity between containers
docker-compose exec backend ping db
docker-compose exec frontend ping backend
```

### DNS Resolution
If containers can't resolve each other, restart Docker daemon:
```bash
sudo systemctl restart docker
docker-compose down && docker-compose up -d
```

## Next Steps

1. **Configure monitoring**: Set up Grafana dashboards
2. **Set up SSL/TLS**: Use Let's Encrypt with Certbot
3. **Enable backups**: Schedule automated PostgreSQL backups
4. **Deploy to production**: Use Docker Swarm or Kubernetes
5. **Set up CI/CD**: Use GitHub Actions or GitLab CI

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Nginx Documentation](https://nginx.org/en/docs/)

## Support

For issues or questions:
1. Check Docker logs: `docker-compose logs`
2. Review this guide
3. Check individual service documentation
