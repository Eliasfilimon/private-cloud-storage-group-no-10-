# AWS Deployment Guide - Secure Cloud Storage

## Prerequisites
- AWS Account (✅ You have this)
- Domain name (optional but recommended)
- SSH key pair for EC2 access

---

## Step 1: Launch EC2 Instance

### 1.1 Go to AWS Console
1. Navigate to **EC2** service
2. Click **Launch Instance**

### 1.2 Configure Instance
```
Name: secure-cloud-storage
AMI: Ubuntu Server 22.04 LTS (Free tier eligible)
Instance Type: t3.medium (2 vCPU, 4GB RAM) or t3.large for production
Key Pair: Create new or select existing (.pem file)
```

### 1.3 Network Settings
```
VPC: Default VPC
Subnet: Any availability zone
Auto-assign public IP: Enable
Security Group: Create new (see below)
```

### 1.4 Security Group Rules
```
Type        Protocol    Port Range    Source
SSH         TCP         22            My IP (your computer's IP)
HTTP        TCP         80            0.0.0.0/0 (Anywhere)
HTTPS       TCP         443           0.0.0.0/0 (Anywhere)
Custom TCP  TCP         8080          My IP (for testing)
Custom TCP  TCP         3002          My IP (for testing)
```

### 1.5 Storage
```
Size: 30 GB (adjust based on expected storage)
Type: gp3 (general purpose SSD)
```

### 1.6 Launch
Click **Launch Instance** and save the .pem file securely!

---

## Step 2: Connect to Your EC2 Instance

### 2.1 Set Permissions on Key File
```bash
# On your local computer (Linux/Mac)
chmod 400 /path/to/your-key.pem

# On Windows (Git Bash or WSL)
chmod 400 C:\path\to\your-key.pem
```

### 2.2 SSH into the Instance
```bash
# Get the Public IP from AWS Console (EC2 > Instances)
# Replace with your actual values:

ssh -i /path/to/your-key.pem ubuntu@YOUR_EC2_PUBLIC_IP

# Example:
ssh -i ~/Downloads/secure-cloud-key.pem ubuntu@54.123.456.789
```

---

## Step 3: Install Docker & Docker Compose

### 3.1 Update System
```bash
sudo apt-get update
sudo apt-get upgrade -y
```

### 3.2 Install Docker
```bash
# Install dependencies
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Add Docker GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker ubuntu
newgrp docker

# Verify installation
docker --version
docker compose version
```

### 3.3 Install Additional Tools
```bash
sudo apt-get install -y git htop unzip
```

---

## Step 4: Prepare SSL Certificates

### Option A: Let's Encrypt (Free - Recommended)
```bash
# Install certbot
sudo apt-get install -y certbot

# If you have a domain pointing to your EC2:
# (Replace your-domain.com with your actual domain)

sudo certbot certonly --standalone -d your-domain.com

# The certificates will be at:
# /etc/letsencrypt/live/your-domain.com/fullchain.pem
# /etc/letsencrypt/live/your-domain.com/privkey.pem

# Copy to project directory:
sudo mkdir -p ~/secure-cloud-storage/nginx/ssl
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ~/secure-cloud-storage/nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ~/secure-cloud-storage/nginx/ssl/key.pem
sudo chown -R ubuntu:ubuntu ~/secure-cloud-storage/nginx/ssl
```

### Option B: Self-Signed Certificate (For Testing)
```bash
mkdir -p ~/secure-cloud-storage/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ~/secure-cloud-storage/nginx/ssl/key.pem \
  -out ~/secure-cloud-storage/nginx/ssl/cert.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
```

---

## Step 5: Deploy the Application

### 5.1 Clone/Upload Your Project
```bash
# Option 1: If using Git
cd ~
git clone YOUR_REPOSITORY_URL secure-cloud-storage

# Option 2: If using SCP from local machine (run on your local computer):
# scp -i /path/to/key.pem -r /path/to/project ubuntu@YOUR_EC2_IP:/home/ubuntu/

# Option 3: If using ZIP upload:
# Upload via SFTP, then:
# unzip secure-cloud-storage.zip
```

### 5.2 Create Environment File
```bash
cd ~/secure-cloud-storage

# Create .env file
sudo tee .env << 'EOF'
# Database
DB_NAME=secure_cloud_storage
DB_USER=postgres
DB_PASSWORD=YOUR_STRONG_PASSWORD_HERE

# JWT Secret (generate: openssl rand -base64 32)
JWT_SECRET=YOUR_JWT_SECRET_HERE

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=YOUR_MINIO_STRONG_PASSWORD
MINIO_BUCKET=secure-cloud-storage

# Grafana
GRAFANA_PASSWORD=YOUR_GRAFANA_ADMIN_PASSWORD

# Deployment
FRONTEND_URL=https://your-domain.com
CORS_ALLOWED_ORIGINS=https://your-domain.com

# Database Mode (validate for production)
DDL_AUTO=validate
EOF
```

### 5.3 Set Secure Encryption Key
```bash
# Generate a new master encryption key
cd ~/secure-cloud-storage
NEW_KEY=$(openssl rand -base64 32)
echo "Generated Master Key: $NEW_KEY"

# Update docker-compose.yml with the new key
# Edit line 45: MASTER_ENCRYPTION_KEY: HaJ2TiGSEIhVEIbN+vb/G9Txh1QIUvG5v9OcCQz8ILY=
# Replace with: MASTER_ENCRYPTION_KEY: $NEW_KEY
```

### 5.4 Build and Deploy
```bash
cd ~/secure-cloud-storage

# Build and start services
docker-compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build

# Wait for services to start (about 2-3 minutes)
watch docker-compose ps

# When all services show "healthy" or "running", press Ctrl+C
```

---

## Step 6: Verify Deployment

### 6.1 Check Service Status
```bash
# All services should be running
docker-compose ps

# Check logs for any errors
docker-compose logs --tail=50 backend
docker-compose logs --tail=50 frontend
```

### 6.2 Access the Application
```
# If using domain with SSL:
https://your-domain.com

# If using IP only (not recommended for production):
http://YOUR_EC2_IP

# Default admin login (create immediately):
# Username: admin
# Password: (set during first run or check backend logs)
```

### 6.3 Create Admin User (First Time)
```bash
# Create admin user via API
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@yourdomain.com",
    "password": "YOUR_STRONG_PASSWORD",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

---

## Step 7: Post-Deployment Configuration

### 7.1 Setup Auto-Renewal for SSL (Let's Encrypt)
```bash
# Edit crontab
sudo crontab -e

# Add this line:
0 12 * * * certbot renew --quiet && cp /etc/letsencrypt/live/your-domain.com/*.pem ~/secure-cloud-storage/nginx/ssl/ && docker-compose restart nginx
```

### 7.2 Configure AWS Security (Optional but Recommended)
1. **Enable CloudWatch** for monitoring
2. **Setup AWS Backup** for automated snapshots
3. **Configure AWS WAF** for additional security
4. **Enable AWS Shield** for DDoS protection

### 7.3 Setup Domain (If you have one)
1. Go to your domain registrar
2. Create an **A Record** pointing to your EC2 public IP
3. Wait for DNS propagation (5-48 hours)

---

## Step 8: Monitoring & Maintenance

### 8.1 View Logs
```bash
cd ~/secure-cloud-storage

# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
```

### 8.2 Check System Resources
```bash
# CPU/Memory usage
htop

# Disk usage
df -h

# Docker stats
docker stats
```

### 8.3 Update Application
```bash
cd ~/secure-cloud-storage

# Pull latest changes (if using git)
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.yml -f docker-compose.aws.yml down
docker-compose -f docker-compose.yml -f docker-compose.aws.yml up -d --build
```

### 8.4 Backup Data
```bash
# Database backup
docker exec scs_postgres pg_dump -U postgres secure_cloud_storage > backup_$(date +%Y%m%d).sql

# MinIO data (files)
# Located in Docker volume: minio_data
```

---

## Troubleshooting

### Issue: Services won't start
```bash
# Check logs
docker-compose logs backend
docker-compose logs db

# Check if ports are already in use
sudo netstat -tulpn | grep -E '8080|3000|5432'

# Restart with fresh volumes (WARNING: Deletes data!)
docker-compose down -v
docker-compose up -d
```

### Issue: SSL certificate error
```bash
# Verify certificates exist
ls -la ~/secure-cloud-storage/nginx/ssl/

# Check certificate validity
openssl x509 -in ~/secure-cloud-storage/nginx/ssl/cert.pem -text -noout
```

### Issue: Cannot access from browser
```bash
# Check security group rules in AWS Console
# Verify EC2 security group allows inbound on 80/443

# Check if services are listening
sudo netstat -tulpn | grep docker

# Test locally on EC2
curl http://localhost:8080/actuator/health
```

### Issue: Out of disk space
```bash
# Check disk usage
df -h

# Clean Docker
docker system prune -a

# Clean old logs
sudo find /var/log -type f -name "*.log" -mtime +7 -delete
```

---

## Security Checklist

- [ ] Changed default passwords (DB, MinIO, Grafana)
- [ ] Generated new MASTER_ENCRYPTION_KEY
- [ ] SSL certificates installed
- [ ] Security groups restrict port 22 to your IP only
- [ ] Disabled password login (SSH key only)
- [ ] Enabled automatic security updates
- [ ] Configured firewall (UFW or AWS Security Groups)
- [ ] Setup regular backups

---

## Quick Commands Reference

```bash
# Start
sudo docker-compose -f docker-compose.yml -f docker-compose.aws.yml up -d

# Stop
sudo docker-compose down

# Restart
sudo docker-compose restart

# View logs
sudo docker-compose logs -f

# Scale (if needed)
sudo docker-compose up -d --scale backend=2
```

---

## Cost Optimization

| Component | t3.medium | t3.large | Storage |
|-----------|-----------|----------|---------|
| Monthly | ~$30 | ~$60 | ~$3/10GB |

**Tips to reduce costs:**
1. Use **t3.micro** for development (free tier eligible)
2. Enable **spot instances** for non-critical workloads
3. Use **S3** instead of EBS for backups (cheaper)
4. Shutdown instance when not in use for dev

---

## Need Help?

If you encounter issues:
1. Check the logs: `docker-compose logs [service-name]`
2. Verify all services: `docker-compose ps`
3. Check AWS Security Group rules
4. Ensure .env file has correct values

**Your Secure Cloud Storage is now deployed on AWS!** 🚀
