# AWS Deployment Checklist

Use this checklist before deploying to AWS.

## Pre-Deployment

### 1. Environment Configuration
- [ ] Copy `.env.example` to `.env`
- [ ] Generate secure JWT_SECRET (`openssl rand -base64 32`)
- [ ] Set strong DB_PASSWORD
- [ ] Set strong MINIO_SECRET_KEY
- [ ] Set strong GRAFANA_PASSWORD
- [ ] Update FRONTEND_URL to your domain
- [ ] Set SMTP credentials (optional)

### 2. Code Changes
- [ ] Update CORS in `backend/src/main/resources/application.properties`
  ```
  cors.allowed.origins=https://your-domain.com
  ```

### 3. AWS Setup
- [ ] Launch EC2 instance (t3.medium or larger)
- [ ] Configure Security Group (ports 22, 80, 443, 8080)
- [ ] Attach Elastic IP (optional but recommended)
- [ ] Create Route53 A record pointing to EC2 IP

### 4. SSL Certificate
- [ ] Install certbot
- [ ] Generate SSL certificate
- [ ] Copy certs to `./ssl/` directory

## Deployment Steps

```bash
# 1. SSH into EC2
ssh -i your-key.pem ubuntu@your-ec2-ip

# 2. Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu

# 3. Clone repository
git clone <repo-url>
cd secure-cloud-storage

# 4. Configure environment
cp .env.example .env
nano .env  # Edit with your values

# 5. Deploy
make start
# OR
docker-compose up -d

# 6. Create admin user
make create-admin

# 7. Verify deployment
make health
```

## Post-Deployment

### Verify Services
- [ ] Frontend accessible at `https://your-domain.com`
- [ ] Backend API responding at `https://your-domain.com/api`
- [ ] Login works with admin credentials
- [ ] File upload works
- [ ] 2FA setup works (if needed)

### Security
- [ ] Security groups properly configured
- [ ] No sensitive ports exposed publicly
- [ ] SSL/HTTPS working
- [ ] Default passwords changed
- [ ] JWT_SECRET is strong and unique

### Monitoring
- [ ] Grafana accessible
- [ ] Prometheus metrics working
- [ ] Logs visible in Grafana/Loki
- [ ] Alerts configured (optional)

### Backup
- [ ] Database backup script configured
- [ ] Automated backup scheduled (cron)
- [ ] Backup files stored securely

## Files Created for AWS Deployment

1. `docker-compose.aws.yml` - AWS-specific Docker override
2. `AWS-DEPLOYMENT.md` - Complete AWS deployment guide
3. `DEPLOYMENT-CHECKLIST.md` - This checklist

## Commands Summary

```bash
# Start services
make start

# Check status
make status

# View logs
make logs

# Health check
make health

# Create admin
make create-admin

# Backup database
make db-backup

# Stop everything
make stop
```

## Troubleshooting Commands

```bash
# Check running containers
docker-compose ps

# View logs for specific service
docker-compose logs -f backend

# Restart service
docker-compose restart backend

# Check resource usage
docker stats

# Shell into container
docker-compose exec backend /bin/sh
```

## Support

- Docker issues: `docker-compose logs`
- AWS issues: Check CloudWatch or EC2 console
- Application issues: Check Grafana logs
