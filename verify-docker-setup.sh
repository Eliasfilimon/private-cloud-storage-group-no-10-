#!/bin/bash

# Docker Setup Verification Script
# Run this script to verify all Docker setup components

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS=0
FAIL=0
WARN=0

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}   Docker Setup Verification - Secure Cloud Storage${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

# Function to check file exists
check_file() {
    local file="$1"
    local description="$2"
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $description - FILE NOT FOUND: $file"
        ((FAIL++))
    fi
}

# Function to check command exists
check_command() {
    local cmd="$1"
    local description="$2"
    
    if command -v "$cmd" &> /dev/null; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASS++))
    else
        echo -e "${YELLOW}⚠${NC} $description - NOT INSTALLED"
        ((WARN++))
    fi
}

# Function to check directory
check_dir() {
    local dir="$1"
    local description="$2"
    
    if [ -d "$dir" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $description - DIRECTORY NOT FOUND: $dir"
        ((FAIL++))
    fi
}

echo -e "${BLUE}1. System Requirements${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_command docker "Docker installed"
check_command docker-compose "Docker Compose installed"
check_command curl "curl installed"
check_command make "Make installed"
check_command chmod "chmod available"

echo ""
echo -e "${BLUE}2. Core Docker Files${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_file "docker-compose.yml" "Main docker-compose.yml"
check_file "docker-compose.dev.yml" "Development overrides"
check_file "docker-compose.prod.yml" "Production overrides"
check_file "backend/Dockerfile" "Backend Dockerfile"
check_file "frontend/Dockerfile" "Frontend Dockerfile"
check_file "frontend/nginx.conf" "Nginx configuration"

echo ""
echo -e "${BLUE}3. Build Optimization${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_file "backend/.dockerignore" "Backend .dockerignore"
check_file "frontend/.dockerignore" "Frontend .dockerignore"

echo ""
echo -e "${BLUE}4. Management Scripts${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_file "docker-control.sh" "Docker control script"
check_file "deploy.sh" "Production deployment script"
check_file "Makefile" "Makefile with targets"

# Check if scripts are executable
if [ -x "docker-control.sh" ]; then
    echo -e "${GREEN}✓${NC} docker-control.sh is executable"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} docker-control.sh is not executable (chmod +x required)"
    ((WARN++))
fi

if [ -x "deploy.sh" ]; then
    echo -e "${GREEN}✓${NC} deploy.sh is executable"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} deploy.sh is not executable (chmod +x required)"
    ((WARN++))
fi

echo ""
echo -e "${BLUE}5. Documentation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_file "DOCKER-README.md" "Docker README (overview)"
check_file "DOCKER-SETUP.md" "Docker setup guide"
check_file "DOCKER-DEPLOYMENT.md" "Docker deployment guide"
check_file "DOCKER-QUICK-START.md" "Docker quick start"
check_file "COMPLETION-SUMMARY.md" "Completion summary"

echo ""
echo -e "${BLUE}6. Configuration Files${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_file ".env.example" "Environment template"

# Check if .env exists
if [ -f ".env" ]; then
    echo -e "${GREEN}✓${NC} .env file exists (configuration ready)"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} .env file not found (will be created on first run)"
    ((WARN++))
fi

echo ""
echo -e "${BLUE}7. CI/CD Pipeline${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_dir ".github/workflows" "GitHub workflows directory"
check_file ".github/workflows/docker.yml" "Docker CI/CD pipeline"

echo ""
echo -e "${BLUE}8. Project Structure${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

check_dir "backend" "Backend directory"
check_dir "frontend" "Frontend directory"
check_file "backend/pom.xml" "Backend pom.xml"
check_file "frontend/package.json" "Frontend package.json"

echo ""
echo -e "${BLUE}9. Docker Image Readiness${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check for key Dockerfile features
if grep -q "FROM.*maven" backend/Dockerfile; then
    echo -e "${GREEN}✓${NC} Backend Dockerfile has Maven build stage"
    ((PASS++))
else
    echo -e "${RED}✗${NC} Backend Dockerfile missing Maven build stage"
    ((FAIL++))
fi

if grep -q "FROM.*eclipse-temurin" backend/Dockerfile; then
    echo -e "${GREEN}✓${NC} Backend uses Alpine JRE"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} Backend should use Alpine JRE"
    ((WARN++))
fi

if grep -q "HEALTHCHECK" backend/Dockerfile; then
    echo -e "${GREEN}✓${NC} Backend has health check"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} Backend missing health check"
    ((WARN++))
fi

if grep -q "FROM node" frontend/Dockerfile && grep -q "FROM nginx" frontend/Dockerfile; then
    echo -e "${GREEN}✓${NC} Frontend has multi-stage build"
    ((PASS++))
else
    echo -e "${YELLOW}⚠${NC} Frontend should use multi-stage build"
    ((WARN++))
fi

echo ""
echo -e "${BLUE}10. Docker Compose Configuration${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Count services in docker-compose.yml
service_count=$(grep -c "^  [a-z].*:" docker-compose.yml || echo "0")
echo -e "${GREEN}✓${NC} Found $service_count services in docker-compose.yml"
((PASS++))

# Check for key services
for service in "backend" "frontend" "db" "minio" "prometheus" "grafana" "loki" "promtail"; do
    if grep -q "^  $service:" docker-compose.yml; then
        echo -e "${GREEN}✓${NC} Service '$service' configured"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} Service '$service' missing"
        ((FAIL++))
    fi
done

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                      VERIFICATION RESULTS${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

echo -e "  ${GREEN}Passed:${NC}  $PASS"
echo -e "  ${YELLOW}Warnings:${NC} $WARN"
echo -e "  ${RED}Failed:${NC}  $FAIL"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ All critical checks passed!${NC}"
    if [ $WARN -gt 0 ]; then
        echo -e "${YELLOW}⚠ $WARN warning(s) - review recommended${NC}"
    fi
else
    echo -e "${RED}✗ $FAIL critical issue(s) - please resolve${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}                    NEXT STEPS${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

echo "1. Create .env configuration:"
echo -e "   ${YELLOW}cp .env.example .env${NC}"
echo ""

echo "2. Make scripts executable (if needed):"
echo -e "   ${YELLOW}chmod +x docker-control.sh deploy.sh${NC}"
echo ""

echo "3. Start Docker services:"
echo -e "   ${YELLOW}make start${NC}"
echo -e "   or"
echo -e "   ${YELLOW}docker-compose up -d${NC}"
echo ""

echo "4. Create admin user:"
echo -e "   ${YELLOW}make create-admin${NC}"
echo ""

echo "5. Access the application:"
echo -e "   Frontend: ${GREEN}http://localhost:3000${NC}"
echo -e "   Backend:  ${GREEN}http://localhost:8080/api${NC}"
echo ""

echo "For more information, see: DOCKER-QUICK-START.md"
echo ""
