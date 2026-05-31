#!/bin/bash

# Production deployment script with security considerations

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_header() {
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}============================================${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Check if running as non-root
if [ "$EUID" -eq 0 ]; then
    print_error "Do not run this script as root. Use sudo if necessary for Docker commands."
    exit 1
fi

# Check prerequisites
check_requirements() {
    print_header "Checking Requirements"
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    
    print_success "Docker and Docker Compose found"
}

# Generate secure keys
generate_secrets() {
    print_header "Generating Secure Secrets"
    
    local jwt_secret=$(openssl rand -base64 32)
    local db_password=$(openssl rand -base64 16)
    local minio_secret=$(openssl rand -base64 24)
    
    cat > .env << EOF
# Database Configuration
DB_NAME=secure_cloud_storage
DB_USER=clouduser
DB_PASSWORD=${db_password}
DDL_AUTO=validate

# Frontend Configuration
FRONTEND_URL=https://your-domain.com

# JWT Secret
JWT_SECRET=${jwt_secret}

# MinIO Configuration
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=${minio_secret}
MINIO_BUCKET=secure-cloud-storage

# Grafana Configuration
GRAFANA_PASSWORD=$(openssl rand -base64 16)

# Prometheus Configuration
PROMETHEUS_RETENTION=30d
EOF
    
    print_success "Secrets generated in .env"
    print_warning "Review and update the .env file as needed"
}

# Security checks
security_check() {
    print_header "Security Pre-flight Checks"
    
    # Check if .env exists
    if [ ! -f ".env" ]; then
        print_error ".env file not found. Creating secure configuration..."
        generate_secrets
    fi
    
    # Check permissions
    if [ -f ".env" ]; then
        local perms=$(stat -f %OLp .env 2>/dev/null || stat -c %a .env 2>/dev/null || echo "unknown")
        if [[ "$perms" != *"600"* && "$perms" != *"400"* ]]; then
            print_warning "Fixing .env file permissions"
            chmod 600 .env
        fi
    fi
    
    print_success "Security checks passed"
}

# Setup firewall rules (for Linux)
setup_firewall() {
    if command -v ufw &> /dev/null; then
        print_header "Setting up Firewall Rules"
        
        echo "Configuring UFW rules..."
        sudo ufw allow 3000/tcp comment "Frontend"
        sudo ufw allow 8080/tcp comment "Backend API"
        sudo ufw allow 9001/tcp comment "MinIO Console"
        
        print_success "Firewall rules configured"
    fi
}

# Deploy services
deploy() {
    print_header "Deploying Services"
    
    print_warning "Using production docker-compose configuration"
    
    # Pull latest images
    docker-compose pull
    print_success "Images pulled"
    
    # Build images
    docker-compose build
    print_success "Images built"
    
    # Start services with production override
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    print_success "Services started"
    
    # Wait for services
    echo "Waiting for services to be healthy..."
    sleep 15
    
    docker-compose ps
}

# Verify deployment
verify_deployment() {
    print_header "Verifying Deployment"
    
    local backend_status=$(docker-compose exec -T backend curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    local db_status=$(docker-compose exec -T db pg_isready -U $(grep DB_USER .env | cut -d= -f2) > /dev/null && echo "200" || echo "500")
    
    if [ "$backend_status" = "200" ]; then
        print_success "Backend API is healthy"
    else
        print_error "Backend API returned status: $backend_status"
    fi
    
    if [ "$db_status" = "200" ]; then
        print_success "Database is healthy"
    else
        print_error "Database is not responding"
    fi
}

# Generate SSL certificates (requires certbot)
setup_ssl() {
    if command -v certbot &> /dev/null; then
        print_header "Setting up SSL Certificates"
        
        read -p "Enter your domain (e.g., example.com): " domain
        
        if [ -z "$domain" ]; then
            print_warning "Skipping SSL setup"
            return
        fi
        
        sudo certbot certonly --standalone -d "$domain"
        print_success "SSL certificate obtained for $domain"
    fi
}

# Final instructions
show_post_deployment() {
    print_header "Deployment Complete!"
    
    echo ""
    echo "Next steps:"
    echo ""
    echo "1. Update your DNS records to point to this server"
    echo ""
    echo "2. Configure reverse proxy (Nginx/Apache) with SSL"
    echo ""
    echo "3. Create the first admin user:"
    echo "   curl -X POST https://your-domain.com/api/auth/signup \\"
    echo "     -H 'Content-Type: application/json' \\"
    echo "     -d '{\"username\":\"admin\",\"password\":\"ChangeMe123\",\"displayName\":\"Administrator\",\"role\":\"ADMIN\"}'"
    echo ""
    echo "4. Access the application:"
    echo "   Frontend:      https://your-domain.com"
    echo "   MinIO:         http://localhost:9001 (port 9001, requires SSH tunnel)"
    echo "   Grafana:       http://localhost:3001 (port 3001, requires SSH tunnel)"
    echo ""
    echo "5. Set up backups:"
    echo "   Make docker-control.sh backup-db"
    echo ""
    echo "6. Configure monitoring and alerts in Grafana"
    echo ""
}

# Main
main() {
    check_requirements
    security_check
    generate_secrets
    
    read -p "Setup firewall rules? (requires sudo) [y/N]: " setup_fw
    if [ "$setup_fw" = "y" ]; then
        setup_firewall
    fi
    
    read -p "Deploy services now? [y/N]: " deploy_now
    if [ "$deploy_now" = "y" ]; then
        deploy
        verify_deployment
        show_post_deployment
    else
        print_warning "Deployment skipped. Run 'docker-compose up -d' to start services."
    fi
}

main "$@"
