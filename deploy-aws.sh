#!/bin/bash
# AWS One-Command Deployment Script for Secure Cloud Storage
# Usage: chmod +x deploy-aws.sh && ./deploy-aws.sh

set -e  # Exit on any error

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║     Secure Cloud Storage - AWS Deployment Script             ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Get server IP
SERVER_IP=$(curl -s ifconfig.me)
print_status "Detected Server IP: $SERVER_IP"

# Step 1: Update system
print_status "Step 1/10: Updating system packages..."
sudo apt-get update -qq && sudo apt-get upgrade -y -qq
print_success "System updated"

# Step 2: Install dependencies
print_status "Step 2/10: Installing dependencies..."
sudo apt-get install -y -qq ca-certificates curl gnupg lsb-release git htop unzip net-tools
print_success "Dependencies installed"

# Step 3: Install Docker
print_status "Step 3/10: Installing Docker..."
if ! command -v docker &> /dev/null; then
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    sudo usermod -aG docker $USER
    print_success "Docker installed"
else
    print_success "Docker already installed"
fi

# Step 4: Verify Docker
print_status "Step 4/10: Verifying Docker installation..."
docker --version
docker compose version
print_success "Docker verified"

# Step 5: Generate secrets
print_status "Step 5/10: Generating secure secrets..."
DB_PASSWORD=$(openssl rand -base64 16 | tr -dc 'a-zA-Z0-9' | head -c 16)
JWT_SECRET=$(openssl rand -base64 32)
MINIO_SECRET=$(openssl rand -base64 16 | tr -dc 'a-zA-Z0-9' | head -c 16)
GRAFANA_PASSWORD=$(openssl rand -base64 12 | tr -dc 'a-zA-Z0-9' | head -c 12)
MASTER_KEY=$(openssl rand -base64 32)

print_status "Generated passwords:"
echo "  Database Password: $DB_PASSWORD"
echo "  MinIO Secret Key: $MINIO_SECRET"
echo "  Grafana Password: $GRAFANA_PASSWORD"
echo "  Master Encryption Key: $MASTER_KEY"
print_warning "Save these passwords securely!"
echo ""

# Step 6: Create .env file
print_status "Step 6/10: Creating .env file..."
cat > .env << EOF
# Database
DB_NAME=secure_cloud_storage
DB_USER=postgres
DB_PASSWORD=$DB_PASSWORD

# JWT Secret
JWT_SECRET=$JWT_SECRET

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=$MINIO_SECRET
MINIO_BUCKET=secure-cloud-storage

# Grafana
GRAFANA_PASSWORD=$GRAFANA_PASSWORD

# Deployment
FRONTEND_URL=http://$SERVER_IP
CORS_ALLOWED_ORIGINS=http://$SERVER_IP,http://localhost:3002

# Database Mode (validate for production)
DDL_AUTO=validate
EOF
print_success ".env file created"

# Step 7: Create SSL certificates (self-signed for now)
print_status "Step 7/10: Setting up SSL certificates..."
mkdir -p nginx/ssl
if [ ! -f nginx/ssl/cert.pem ]; then
    print_warning "No SSL certificates found. Generating self-signed certificates..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/key.pem \
        -out nginx/ssl/cert.pem \
        -subj "/C=US/ST=State/L=City/O=SecureCloud/CN=$SERVER_IP"
    print_success "Self-signed SSL certificates generated"
    print_warning "For production, replace with valid SSL certificates (Let's Encrypt recommended)"
else
    print_success "SSL certificates already exist"
fi

# Step 8: Update docker-compose.yml with master key
print_status "Step 8/10: Updating encryption key..."
sed -i "s|MASTER_ENCRYPTION_KEY:.*|MASTER_ENCRYPTION_KEY: $MASTER_KEY|" docker-compose.yml
print_success "Encryption key updated"

# Step 9: Build and deploy
print_status "Step 9/10: Building and deploying services..."
print_status "This may take 5-10 minutes..."
echo ""

# Ensure we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found. Please run this script from the project root."
    exit 1
fi

# Pull images and build
docker-compose -f docker-compose.yml -f docker-compose.aws.yml pull
docker-compose -f docker-compose.yml -f docker-compose.aws.yml build --no-cache

# Start services
docker-compose -f docker-compose.yml -f docker-compose.aws.yml up -d

print_success "Services deployed"

# Step 10: Wait and verify
print_status "Step 10/10: Verifying deployment..."
echo ""
print_status "Waiting for services to start (60 seconds)..."
sleep 10

# Check service status
print_status "Service Status:"
docker-compose ps

echo ""
print_status "Checking service health..."

# Health check with retries
MAX_RETRIES=12
RETRY_COUNT=0
BACKEND_HEALTHY=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ] && [ "$BACKEND_HEALTHY" = false ]; do
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        BACKEND_HEALTHY=true
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        print_status "Waiting for backend to be healthy... ($RETRY_COUNT/$MAX_RETRIES)"
        sleep 5
    fi
done

if [ "$BACKEND_HEALTHY" = true ]; then
    print_success "Backend is healthy!"
else
    print_warning "Backend health check timed out. Check logs with: docker-compose logs backend"
fi

# Final summary
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                   DEPLOYMENT COMPLETE!                       ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "📱 Access your application:"
echo "   • Web App: http://$SERVER_IP"
echo "   • API: http://$SERVER_IP:8080"
echo "   • MinIO Console: http://$SERVER_IP:9003"
echo "   • Grafana: http://$SERVER_IP:3001"
echo ""
echo "🔐 Default Credentials:"
echo "   MinIO: minioadmin / $MINIO_SECRET"
echo "   Grafana: admin / $GRAFANA_PASSWORD"
echo ""
echo "⚠️  IMPORTANT: Create an admin user immediately!"
echo "   Visit: http://$SERVER_IP and register your first account."
echo ""
echo "📁 Passwords saved to: ./deployment-secrets.txt"
echo ""

# Save secrets to file
cat > deployment-secrets.txt << EOF
Secure Cloud Storage - Deployment Secrets
Generated: $(date)
Server IP: $SERVER_IP

DATABASE:
  Password: $DB_PASSWORD

MINIO:
  Access Key: minioadmin
  Secret Key: $MINIO_SECRET

GRAFANA:
  Username: admin
  Password: $GRAFANA_PASSWORD

JWT SECRET:
  $JWT_SECRET

MASTER ENCRYPTION KEY:
  $MASTER_KEY

KEEP THIS FILE SECURE! Delete after noting passwords.
EOF

chmod 600 deployment-secrets.txt
print_success "Secrets saved to deployment-secrets.txt"

echo ""
echo "🔧 Useful Commands:"
echo "   View logs:    docker-compose logs -f"
echo "   Stop:         docker-compose down"
echo "   Restart:      docker-compose restart"
echo "   Update:       docker-compose pull && docker-compose up -d"
echo ""
echo "📖 For help, see: AWS-DEPLOYMENT-GUIDE.md"
echo ""
print_success "Deployment script completed successfully!"
