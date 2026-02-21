#!/bin/bash

# Secure Cloud Storage - Database Setup Script
# This script creates the PostgreSQL database and initializes the schema

echo "=================================="
echo "Secure Cloud Storage Setup"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Database Configuration
DB_NAME="secure_cloud_storage"
DB_USER="postgres"
DB_PASSWORD="postgres"
SCHEMA_FILE="src/main/resources/database/schema.sql"

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo -e "${RED}PostgreSQL is not installed!${NC}"
    echo "Please install PostgreSQL first:"
    echo "  Ubuntu/Debian: sudo apt-get install postgresql postgresql-contrib"
    echo "  MacOS: brew install postgresql"
    echo "  Fedora/RHEL: sudo dnf install postgresql-server"
    exit 1
fi

# Check if PostgreSQL service is running
if ! sudo systemctl is-active --quiet postgresql 2>/dev/null && ! pgrep -x postgres > /dev/null; then
    echo -e "${YELLOW}PostgreSQL service is not running. Starting...${NC}"
    if command -v systemctl &> /dev/null; then
        sudo systemctl start postgresql
    else
        echo -e "${RED}Please start PostgreSQL manually${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}✓${NC} PostgreSQL is installed and running"
echo ""

# Create database
echo "Creating database..."
sudo -u postgres psql -c "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1 || \
    sudo -u postgres psql -c "CREATE DATABASE $DB_NAME;"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Database '$DB_NAME' created successfully"
else
    echo -e "${RED}✗${NC} Failed to create database"
    exit 1
fi

# Create database schema
echo ""
echo "Creating database schema..."
if [ -f "$SCHEMA_FILE" ]; then
    sudo -u postgres psql -d $DB_NAME -f $SCHEMA_FILE
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Schema created successfully"
        echo -e "${GREEN}✓${NC} Default admin user created (username: admin, password: Admin@123)"
        echo -e "${GREEN}✓${NC} Default lecturer user created (username: lecturer1, password: Lecturer@123)"
    else
        echo -e "${RED}✗${NC} Failed to create schema"
        exit 1
    fi
else
    echo -e "${RED}✗${NC} Schema file not found: $SCHEMA_FILE"
    exit 1
fi

# Create uploads directory
echo ""
echo "Creating file storage directories..."
mkdir -p uploads backups
chmod 755 uploads backups
echo -e "${GREEN}✓${NC} Storage directories created"

echo ""
echo "=================================="
echo -e "${GREEN}Setup completed successfully!${NC}"
echo "=================================="
echo ""
echo "Default credentials:"
echo "  Admin    - Username: admin      Password: Admin@123"
echo "  Lecturer - Username: lecturer1  Password: Lecturer@123"
echo ""
echo "Database connection details:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo ""
echo "Next steps:"
echo "  1. Update database password in src/main/resources/application.properties if needed"
echo "  2. Run: mvn clean install"
echo "  3. Run: mvn spring-boot:run"
echo "  4. Open frontend: cd ../frontend && npm run dev"
echo ""
