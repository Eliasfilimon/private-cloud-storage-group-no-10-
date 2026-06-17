# Deployment & Operations Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Development Setup](#development-setup)
3. [Production Deployment](#production-deployment)
4. [Configuration](#configuration)
5. [Monitoring](#monitoring)
6. [Backup & Recovery](#backup--recovery)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance](#maintenance)

---

## Prerequisites

### System Requirements
- **OS**: Linux (Ubuntu 20.04+ recommended)
- **CPU**: 4 cores minimum
- **RAM**: 8 GB minimum
- **Disk**: 100 GB minimum (depends on storage needs)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

### Software Requirements
- **Java**: 17 (for local development)
- **Node.js**: 16+ (for frontend development)
- **Maven**: 3.8+ (for backend builds)
- **PostgreSQL**: 15 (included in Docker)
- **MinIO**: Latest (included in Docker)

---

## Development Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd secure-cloud-storage
```

### 2. Create Environment File
```bash
cat > .env << 'EOF'
# Database
DB_NAME=secure_cloud_storage
DB_USER=postgres
DB_PASSWORD=YourSecurePassword123!

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=YourSecurePassword123!
MINIO_BUCKET=secure-cloud-storage

# Master Encryption Key (generate with: openssl rand -base64 32)
MASTER_ENCRYPTION_KEY=HaJ2TiGSEIhVEIbN+vb/G9Txh1QIUvG5v9OcCQz8ILY=

# JWT Secret
JWT_SECRET=YourJWTSecretKeyChangeInProduction123!

# Grafana
GRAFANA_ADMIN_PASSWORD=YourGrafanaPassword123!

# Initial Admin
APP_INITIAL_ADMIN_EMAIL=admin@udom.ac.tz
APP_INITIAL_ADMIN_PASSWORD=AdminPass123!
EOF
```

### 3. Build Backend
```bash
cd backend
mvn clean package -DskipTests
cd ..
```

### 4. Build Frontend
```bash
cd frontend
npm install
npm run build
cd ..
```

### 5. Start Services
```bash
docker-compose up -d
```

### 6. Verify Services
```bash
# Check all containers are running
docker-compose ps

# Check backend health
curl http://localhost:8080/actuator/health

# Check frontend
curl http://localhost:3002

# Check Prometheus
curl http://localhost:9090

# Check Grafana
curl http://localhost:3001
```

---

## Production Deployment

### 1. Pre-Deployment Checklist
- [ ] All default credentials changed
- [ ] SSL/TLS certificates obtained
- [ ] Firewall rules configured
- [ ] Backup strategy implemented
- [ ] Monitoring configured
- [ ] Disaster recovery plan documented
- [ ] Security audit completed

### 2. Server Setup
```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Create application directory
sudo mkdir -p /opt/secure-cloud-storage
sudo chown $USER:$USER /opt/secure-cloud-storage
```

### 3. Deploy Application
```bash
cd /opt/secure-cloud-storage

# Clone repository
git clone <repository-url> .

# Create .env with production values
nano .env

# Build images
docker-compose build

# Start services
docker-compose up -d

# Verify deployment
docker-compose ps
docker-compose logs backend
```

### 4. SSL/TLS Configuration
```bash
# Option 1: Using Let's Encrypt with Certbot
sudo apt-get install certbot python3-certbot-nginx
sudo certbot certonly --standalone -d yourdomain.com

# Option 2: Using self-signed certificate (development only)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
```

### 5. Reverse Proxy Setup (Nginx)
```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Frontend
    location / {
        proxy_pass http://localhost:3002;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## Configuration

### Environment Variables

#### Database
```
DB_NAME=secure_cloud_storage
DB_USER=postgres
DB_PASSWORD=<strong-password>
```

#### MinIO
```
MINIO_ACCESS_KEY=<strong-key>
MINIO_SECRET_KEY=<strong-password>
MINIO_BUCKET=secure-cloud-storage
```

#### Encryption
```
MASTER_ENCRYPTION_KEY=<base64-encoded-256-bit-key>
```

#### JWT
```
JWT_SECRET=<strong-secret-key>
JWT_EXPIRATION=3600000  # 1 hour in milliseconds
```

#### Grafana
```
GRAFANA_ADMIN_PASSWORD=<strong-password>
GRAFANA_ADMIN_USER=admin
```

#### Frontend
```
VITE_API_URL=/api
```

### Application Properties
Edit `backend/src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://db:5432/secure_cloud_storage
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

# File Upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Logging
logging.level.root=INFO
logging.level.com.udom.securecloud=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus
```

---

## Monitoring

### Prometheus
- **URL**: http://localhost:9090
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Scrape Interval**: 15 seconds
- **Retention**: 15 days (default)

### Grafana
- **URL**: http://localhost:3001
- **Default Credentials**: admin / (from GRAFANA_ADMIN_PASSWORD)
- **Dashboards**: Pre-configured for Spring Boot metrics

### AlertManager
- **URL**: http://localhost:9093
- **Alerts**: Configured in `prometheus/rules/alerts.yml`
- **Notifications**: Email (configure SMTP)

### Key Metrics to Monitor
- Backend service availability (up/down)
- JVM memory usage
- Database connection pool
- HTTP request rate and latency
- Error rate (5xx responses)
- Disk space usage
- File encryption/decryption success rate

---

## Backup & Recovery

### Automated Backups
```bash
# Create backup script
cat > /opt/secure-cloud-storage/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup database
docker-compose exec -T db pg_dump -U postgres secure_cloud_storage | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Backup MinIO data
docker-compose exec -T minio tar czf - /data | gzip > $BACKUP_DIR/minio_$DATE.tar.gz

# Backup application config
tar czf $BACKUP_DIR/config_$DATE.tar.gz .env docker-compose.yml

# Keep only last 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Backup completed: $DATE"
EOF

chmod +x /opt/secure-cloud-storage/backup.sh
```

### Schedule Backups
```bash
# Add to crontab (daily at 2 AM)
crontab -e
# Add: 0 2 * * * /opt/secure-cloud-storage/backup.sh
```

### Restore from Backup
```bash
# Restore database
gunzip < /backups/db_20260604_020000.sql.gz | docker-compose exec -T db psql -U postgres

# Restore MinIO data
docker-compose exec -T minio tar xzf /backups/minio_20260604_020000.tar.gz

# Restore application config
tar xzf /backups/config_20260604_020000.tar.gz
```

---

## Troubleshooting

### Backend Service Won't Start
```bash
# Check logs
docker-compose logs backend

# Common issues:
# 1. Database not ready: Wait 30 seconds and restart
docker-compose restart backend

# 2. Port already in use: Change port in docker-compose.yml
# 3. Out of memory: Increase Docker memory limit
```

### Database Connection Failed
```bash
# Check database service
docker-compose logs db

# Verify credentials in .env
# Restart database
docker-compose restart db
```

### File Upload Fails
```bash
# Check MinIO service
docker-compose logs minio

# Verify MinIO credentials
# Check disk space: df -h
# Check file permissions: ls -la /app/uploads
```

### High Memory Usage
```bash
# Check JVM heap size
docker-compose exec backend jps -l

# Increase heap size in docker-compose.yml:
# environment:
#   JAVA_OPTS: -Xmx2g -Xms1g
```

### Slow API Responses
```bash
# Check database performance
docker-compose exec db psql -U postgres -c "SELECT * FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Check application logs
docker-compose logs backend | grep "took"

# Monitor metrics in Grafana
```

---

## Maintenance

### Regular Tasks

#### Daily
- [ ] Check service health
- [ ] Review error logs
- [ ] Monitor disk space
- [ ] Verify backups completed

#### Weekly
- [ ] Review audit logs
- [ ] Check for failed login attempts
- [ ] Verify backup restoration
- [ ] Update dependencies

#### Monthly
- [ ] Update Docker images
- [ ] Review security patches
- [ ] Performance analysis
- [ ] Capacity planning

#### Quarterly
- [ ] Security audit
- [ ] Disaster recovery drill
- [ ] Encryption key rotation
- [ ] Compliance review

### Updates & Patches
```bash
# Update Docker images
docker-compose pull

# Rebuild images
docker-compose build --no-cache

# Restart services
docker-compose restart

# Verify health
docker-compose ps
curl http://localhost:8080/actuator/health
```

### Log Rotation
```bash
# Configure logrotate
sudo tee /etc/logrotate.d/secure-cloud-storage << EOF
/opt/secure-cloud-storage/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 root root
    sharedscripts
}
EOF
```

---

## Performance Tuning

### Database Optimization
```sql
-- Create indexes for common queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_files_user_id ON file_metadata(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM file_metadata WHERE user_id = 1;
```

### Application Optimization
```properties
# Connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Cache configuration
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

---

**Last Updated**: June 4, 2026
**Version**: 1.0
