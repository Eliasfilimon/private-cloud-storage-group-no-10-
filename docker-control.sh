#!/bin/bash

# Docker Control Script for Secure Cloud Storage
# Usage: ./docker-control.sh [command]

set -e

COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="secure-cloud-storage"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}========================================${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_info() {
    echo -e "${YELLOW}INFO: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Check if docker and docker-compose are installed
check_requirements() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
}

# Start services
start() {
    print_header "Starting Secure Cloud Storage Services"
    
    if [ ! -f ".env" ]; then
        print_info "Creating .env file from .env.example"
        cp .env.example .env
        print_info "Please review and update .env as needed"
    fi
    
    docker-compose up -d
    print_success "All services started"
    
    print_info "Waiting for services to be healthy..."
    sleep 10
    
    docker-compose ps
    
    echo ""
    echo -e "${GREEN}Services are ready at:${NC}"
    echo "  Frontend:      ${GREEN}http://localhost:3000${NC}"
    echo "  Backend API:   ${GREEN}http://localhost:8080/api${NC}"
    echo "  MinIO Console: ${GREEN}http://localhost:9001${NC}"
    echo "  Prometheus:    ${GREEN}http://localhost:9090${NC}"
    echo "  Grafana:       ${GREEN}http://localhost:3001${NC}"
    echo ""
}

# Stop services
stop() {
    print_header "Stopping Services"
    docker-compose down
    print_success "All services stopped"
}

# Restart services
restart() {
    print_header "Restarting Services"
    stop
    sleep 2
    start
}

# View logs
logs() {
    local service="${1:-}"
    if [ -z "$service" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$service"
    fi
}

# Build images
build() {
    print_header "Building Docker Images"
    docker-compose build --no-cache
    print_success "Images built successfully"
}

# Rebuild and restart
rebuild() {
    print_header "Rebuilding and Restarting"
    build
    print_info "Restarting services..."
    docker-compose up -d
    print_success "Services restarted"
}

# Show status
status() {
    print_header "Service Status"
    docker-compose ps
}

# Create first admin user
create_admin() {
    print_header "Creating First Admin User"
    
    local username="${1:-admin}"
    local password="${2:-Admin@123}"
    
    print_info "Creating admin user: $username"
    
    curl -s -X POST http://localhost:8080/api/auth/signup \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$username\",
            \"password\": \"$password\",
            \"displayName\": \"Administrator\",
            \"role\": \"ADMIN\"
        }" | jq '.' || print_error "Failed to create admin user. Check backend logs."
    
    print_success "Admin user creation request sent"
    echo ""
    echo "Try logging in with:"
    echo "  Username: $username"
    echo "  Password: $password"
}

# Clean up (remove volumes)
clean() {
    print_header "Cleaning Up (Removing Volumes)"
    
    echo -e "${RED}WARNING: This will delete all data in volumes!${NC}"
    read -p "Are you sure? (yes/no): " response
    
    if [ "$response" = "yes" ]; then
        docker-compose down -v
        print_success "Cleanup completed"
    else
        print_info "Cleanup cancelled"
    fi
}

# Database operations
backup_db() {
    print_header "Backing Up Database"
    
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local filename="backup_${timestamp}.sql"
    
    docker-compose exec -T db pg_dump -U postgres secure_cloud_storage > "$filename"
    
    print_success "Database backed up to: $filename"
}

restore_db() {
    local file="${1:-}"
    
    if [ -z "$file" ]; then
        print_error "Please provide backup file: $0 restore-db <filename>"
        exit 1
    fi
    
    if [ ! -f "$file" ]; then
        print_error "File not found: $file"
        exit 1
    fi
    
    print_header "Restoring Database"
    print_info "Restoring from: $file"
    
    docker-compose exec -T db psql -U postgres secure_cloud_storage < "$file"
    print_success "Database restored"
}

# Database shell
db_shell() {
    print_header "Connecting to PostgreSQL"
    docker-compose exec db psql -U postgres -d secure_cloud_storage
}

# Show service endpoints
show_endpoints() {
    print_header "Service Endpoints"
    
    echo ""
    echo -e "${GREEN}Web Applications:${NC}"
    echo "  Frontend:      http://localhost:3000"
    echo "  Backend API:   http://localhost:8080"
    echo ""
    echo -e "${GREEN}Admin Consoles:${NC}"
    echo "  MinIO:         http://localhost:9001 (minioadmin/minioadmin123)"
    echo "  Grafana:       http://localhost:3001 (admin/admin)"
    echo ""
    echo -e "${GREEN}Monitoring:${NC}"
    echo "  Prometheus:    http://localhost:9090"
    echo "  Loki:          http://localhost:3100"
    echo ""
    echo -e "${GREEN}Databases:${NC}"
    echo "  PostgreSQL:    localhost:5432"
    echo ""
}

# Display help
show_help() {
    cat << EOF
Secure Cloud Storage - Docker Control Script

Usage: ./docker-control.sh [command] [options]

Commands:
    start              Start all services
    stop               Stop all services
    restart            Restart all services
    status             Show service status
    logs [service]     View logs (optional: specific service)
    build              Build Docker images
    rebuild            Rebuild images and restart services
    
    create-admin       Create first admin user
    endpoints          Show service endpoints
    
    backup-db          Backup PostgreSQL database
    restore-db [file]  Restore PostgreSQL database from backup
    db-shell           Connect to PostgreSQL shell
    
    clean              Remove all volumes (WARNING: deletes all data)
    help               Show this help message

Examples:
    ./docker-control.sh start
    ./docker-control.sh logs backend
    ./docker-control.sh create-admin myuser MyPassword123
    ./docker-control.sh backup-db
    ./docker-control.sh restore-db backup_20240506_120000.sql

EOF
}

# Main script logic
check_requirements

case "${1:-start}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs "${2:-}"
        ;;
    build)
        build
        ;;
    rebuild)
        rebuild
        ;;
    create-admin)
        create_admin "${2:-}" "${3:-}"
        ;;
    endpoints)
        show_endpoints
        ;;
    backup-db)
        backup_db
        ;;
    restore-db)
        restore_db "${2:-}"
        ;;
    db-shell)
        db_shell
        ;;
    clean)
        clean
        ;;
    help)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
