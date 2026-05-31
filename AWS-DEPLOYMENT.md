# AWS Deployment Guide

Complete guide for deploying Secure Cloud Storage on Amazon Web Services.

## Prerequisites

- AWS Account
- EC2 instance (t3.medium or larger recommended)
- Domain name (optional but recommended)
- SSL certificate (Let's Encrypt or AWS Certificate Manager)

## Quick Start

### 1. Launch EC2 Instance

```bash
# Recommended: Ubuntu 22.04 LTS
# Instance type: t3.medium (2 vCPU, 4 GB RAM)
# Storage: 50 GB SSD
# Security Group: Allow ports 22, 80, 443, 8080, 9001, 3001
```

### 2. Install Docker on EC2

```bash
# SSH into your EC2 instance
ssh -i your-key.pem ubuntu@your-ec2-ip

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker ubuntu

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again for group changes
echo "Logout and SSH back in: exit"
```

### 3. Clone and Configure

```bash
# Clone repository
git clone https://github.com/your-org/secure-cloud-storage.git
cd secure-cloud-storage

# Create production environment file
cp .env.example .env

# Edit .env with your AWS configuration
nano .env
```

### 4. Required .env Configuration

```bash
# Database Configuration
DB_NAME=secure_cloud_storage
DB_USER=clouduser
DB_PASSWORD=<generate-strong-password>
DDL_AUTO=validate

# Frontend URL (your domain or EC2 IP)
FRONTEND_URL=https://your-domain.com

# JWT Secret (generate with: openssl rand -base64 32)
JWT_SECRET=<your-generated-secret>

# MinIO Configuration
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=<generate-strong-password>
MINIO_BUCKET=secure-cloud-storage

# Grafana Configuration
GRAFANA_PASSWORD=<admin-password>

# SMTP (optional - for password reset emails)
SMTP_USERNAME=
SMTP_PASSWORD=
```

### 5. Deploy with AWS Configuration

```bash
# Start all services with AWS override
docker-compose -f docker-compose.yml -f docker-compose.aws.yml up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### 6. Create Admin User

```bash
# Create first admin user
make create-admin
# OR manually:
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123","displayName":"Administrator","role":"ADMIN"}'
```

### 7. Configure SSL (Let's Encrypt)

```bash
# Install certbot
sudo apt-get update
sudo apt-get install -y certbot

# Get certificate (standalone mode)
sudo certbot certonly --standalone -d your-domain.com

# Copy certificates to project
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ./ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ./ssl/key.pem
sudo chown ubuntu:ubuntu ./ssl/*.pem

# Restart nginx container
docker-compose restart nginx
```

### 8. Access Your Application

```
Frontend:    https://your-domain.com
Backend API: https://your-domain.com/api
MinIO:       https://your-domain.com:9001 (or use SSH tunnel)
Grafana:     https://your-domain.com:3001 (or use SSH tunnel)
```

## AWS-Specific Configurations

### Using AWS RDS (PostgreSQL)

1. Create RDS PostgreSQL instance
2. Update `.env`:
```bash
DB_HOST=your-rds-endpoint.region.rds.amazonaws.com
DB_PORT=5432
```
3. Update `docker-compose.aws.yml`:
```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
```
4. Comment out the `db` service in docker-compose.yml

### Using AWS S3 (Instead of MinIO)

1. Create S3 bucket
2. Update backend to use AWS SDK (requires code changes)
3. Or keep MinIO with S3 gateway mode

### Using AWS Application Load Balancer

1. Create ALB in EC2 console
2. Target group pointing to EC2 instance port 80
3. HTTPS listener with ACM certificate
4. Security group allowing 80/443 from ALB only
5. EC2 security group only allows 80 from ALB

### Auto-Scaling (ECS/EKS)

For production with auto-scaling, consider:
- **Amazon ECS** with Fargate (serverless containers)
- **Amazon EKS** (Kubernetes)
- **AWS Elastic Beanstalk** (simpler option)

## Security Checklist

- [ ] Change all default passwords
- [ ] Generate strong JWT_SECRET
- [ ] Enable AWS Security Groups (restrict ports)
- [ ] Configure SSL/HTTPS
- [ ] Set up automated backups
- [ ] Enable CloudWatch monitoring
- [ ] Configure log retention
- [ ] Disable unused services

## Maintenance

### Backup Database

```bash
# Manual backup
docker-compose exec -T db pg_dump -U postgres secure_cloud_storage > backup_$(date +%Y%m%d_%H%M%S).sql

# Automated backup (add to crontab)
0 2 * * * cd /home/ubuntu/secure-cloud-storage && make db-backup
```

### Update SSL Certificate

```bash
# Auto-renewal (certbot)
sudo certbot renew

# Restart nginx after renewal
docker-compose restart nginx
```

### Monitor Resources

```bash
# Check Docker stats
docker stats

# Check logs
docker-compose logs -f backend

# Health check
make health
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose logs <service-name>

# Check disk space
df -h

# Check memory
free -h
```

### Database connection issues
```bash
# Verify database is running
docker-compose ps db

# Test connection
docker-compose exec db pg_isready -U postgres
```

### SSL certificate issues
```bash
# Verify certificate
openssl x509 -in ./ssl/cert.pem -text -noout

# Check nginx logs
docker-compose logs nginx
```

## Support

For issues specific to this project:
- Check logs: `docker-compose logs`
- Review documentation in README.md
- Check GitHub Issues

For AWS-specific issues:
- AWS Support Center
- AWS Documentation
- AWS Forums
