#!/bin/bash

# Secure Cloud Storage - Quick Start Script
# Run this script to start both backend and frontend in one command

echo "=================================="
echo "Secure Cloud Storage - Quick Start"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check if PostgreSQL is running
if ! sudo systemctl is-active --quiet postgresql 2>/dev/null && ! pgrep -x postgres > /dev/null; then
    echo -e "${RED}PostgreSQL is not running!${NC}"
    echo "Please start PostgreSQL first:"
    echo "  sudo systemctl start postgresql"
    exit 1
fi

echo -e "${GREEN}✓${NC} PostgreSQL is running"
echo ""

# Check if database exists
DB_EXISTS=$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='secure_cloud_storage'")

if [ "$DB_EXISTS" != "1" ]; then
    echo -e "${YELLOW}Database not found. Running setup...${NC}"
    cd backend
    chmod +x setup-database.sh
    ./setup-database.sh
    cd ..
    echo ""
fi

echo "Starting services..."
echo ""

# Start backend in background
echo -e "${YELLOW}Starting backend...${NC}"
cd backend
gnome-terminal -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -e "mvn spring-boot:run" 2>/dev/null || \
konsole -e "mvn spring-boot:run" 2>/dev/null || \
(echo -e "${RED}Could not open terminal. Please run manually:${NC}"
 echo "  cd backend && mvn spring-boot:run")
cd ..

sleep 3

# Start frontend in background
echo -e "${YELLOW}Starting frontend...${NC}"
cd frontend

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

gnome-terminal -- bash -c "npm run dev; exec bash" 2>/dev/null || \
xterm -e "npm run dev" 2>/dev/null || \
konsole -e "npm run dev" 2>/dev/null || \
(echo -e "${RED}Could not open terminal. Please run manually:${NC}"
 echo "  cd frontend && npm run dev")

cd ..

echo ""
echo "=================================="
echo -e "${GREEN}Services are starting!${NC}"
echo "=================================="
echo ""
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo ""
echo "Default login:"
echo "  Admin:    username: admin      password: Admin@123"
echo "  Lecturer: username: lecturer1  password: Lecturer@123"
echo ""
echo "Press Ctrl+C in each terminal to stop the services"
echo ""
