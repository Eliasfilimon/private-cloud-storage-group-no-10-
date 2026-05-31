# Docker Quick Reference

## Installation Check
```bash
docker --version
docker-compose --version
```

## Start Everything
```bash
make start
# or
./docker-control.sh start
# or
docker-compose up -d
```

## View Status
```bash
make status
# Shows all running containers and their status
```

## Create Admin User
```bash
make create-admin
# Follow prompts or provide username and password:
# ./docker-control.sh create-admin admin MyPassword123
```

## Access Services

| Service | URL | Default Credentials |
|---------|-----|-------------------|
| Frontend | http://localhost:3000 | Your admin user |
| Backend API | http://localhost:8080/api | Bearer token required |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin123 |
| Grafana | http://localhost:3001 | admin / admin |
| Prometheus | http://localhost:9090 | No auth |

## View Logs
```bash
make logs              # All services
make logs-backend      # Backend only
make logs-frontend     # Frontend only
make logs-db          # Database only
```

## Stop Services
```bash
make stop
# or
docker-compose down
```

## Database Operations
```bash
make db-backup        # Backup PostgreSQL
make db-shell         # Connect to PostgreSQL
make health           # Check all service health
```

## Rebuild Images
```bash
make rebuild          # Rebuild and restart everything
# or
docker-compose build --no-cache && docker-compose up -d
```

## Clean Up
```bash
make clean            # Remove all volumes (WARNING: deletes data)
make prune            # Prune unused Docker resources
```

## Troubleshooting
```bash
# Check if port is in use
lsof -i :3000

# Full system logs
docker-compose logs

# Check service health
docker-compose ps

# Restart specific service
docker-compose restart backend
```

## Development

### Backend Changes
```bash
make rebuild          # Auto-rebuilds and restarts
```

### Frontend Changes
Frontend auto-reloads in development mode. If issues:
```bash
make rebuild
```

### Database Schema Changes
```bash
make db-shell
# Then use psql commands
```

## Production Deployment
```bash
./deploy.sh           # Run production deployment script
# or
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Useful Commands
```bash
# Check Docker disk usage
docker system df

# Remove all unused images
docker system prune -a

# Export PostgreSQL
docker-compose exec -T db pg_dump -U postgres secure_cloud_storage > backup.sql

# Import PostgreSQL
docker-compose exec -T db psql -U postgres secure_cloud_storage < backup.sql

# SSH into container
docker-compose exec backend bash
docker-compose exec frontend bash
docker-compose exec db bash
```

## Environment Setup
```bash
# Create .env from template
cp .env.example .env

# Edit .env with your settings
nano .env
```

## Common Issues

**Port 3000 already in use:**
Edit docker-compose.yml and change `"3000:3000"` to `"3001:3000"`

**Database connection refused:**
```bash
docker-compose logs db
docker-compose exec db pg_isready -U postgres
```

**Frontend can't reach API:**
Check backend is running: `make status`
Check CORS in backend logs: `make logs-backend`

**Out of disk space:**
```bash
docker system prune -a --volumes
```

## Next Steps
- Read [DOCKER-SETUP.md](./DOCKER-SETUP.md) for full documentation
- Read [DOCKER-DEPLOYMENT.md](./DOCKER-DEPLOYMENT.md) for deployment details
- Configure monitoring in Grafana
- Set up automated backups
