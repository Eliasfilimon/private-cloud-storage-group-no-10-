.PHONY: help start stop restart logs build rebuild clean \
        create-admin db-backup db-restore db-shell status \
        up down health test

# Default target
help:
	@echo "Secure Cloud Storage - Docker Makefile"
	@echo ""
	@echo "Available targets:"
	@echo "  make start        - Start all services"
	@echo "  make stop         - Stop all services"
	@echo "  make restart      - Restart all services"
	@echo "  make up           - Alias for start"
	@echo "  make down         - Alias for stop"
	@echo "  make status       - Show service status"
	@echo "  make logs         - View all logs"
	@echo "  make build        - Build Docker images"
	@echo "  make rebuild      - Rebuild images and restart"
	@echo "  make clean        - Remove all volumes (data loss!)"
	@echo "  make create-admin - Create first admin user"
	@echo "  make db-backup    - Backup PostgreSQL database"
	@echo "  make db-restore   - Restore PostgreSQL database"
	@echo "  make db-shell     - Connect to PostgreSQL shell"
	@echo "  make health       - Check service health"
	@echo "  make endpoints    - Show service endpoints"

# Service management
start:
	docker-compose up -d
	@echo "✓ Services started"

stop:
	docker-compose down
	@echo "✓ Services stopped"

restart: stop
	docker-compose up -d
	@echo "✓ Services restarted"

up: start

down: stop

status:
	docker-compose ps

logs:
	docker-compose logs -f

logs-backend:
	docker-compose logs -f backend

logs-frontend:
	docker-compose logs -f frontend

logs-db:
	docker-compose logs -f db

# Building
build:
	docker-compose build --no-cache
	@echo "✓ Images built"

rebuild: build restart

# Database operations
db-backup:
	@timestamp=$$(date +%Y%m%d_%H%M%S); \
	docker-compose exec -T db pg_dump -U postgres secure_cloud_storage > backup_$$timestamp.sql; \
	echo "✓ Database backed up to backup_$$timestamp.sql"

db-shell:
	docker-compose exec db psql -U postgres -d secure_cloud_storage

db-stats:
	docker-compose exec db psql -U postgres -d secure_cloud_storage \
		-c "SELECT datname, pg_size_pretty(pg_database.datsize) as size FROM pg_database;"

# Admin operations
create-admin:
	@read -p "Enter admin username [admin]: " username; \
	read -p "Enter admin password [Admin@123]: " password; \
	username=$${username:-admin}; \
	password=$${password:-Admin@123}; \
	curl -s -X POST http://localhost:8080/api/auth/signup \
		-H "Content-Type: application/json" \
		-d '{"username":"'$$username'","password":"'$$password'","displayName":"Administrator","role":"ADMIN"}' | jq '.'

# Health checks
health:
	@echo "Checking service health..."
	@docker-compose exec -T backend curl -f http://localhost:8080/actuator/health > /dev/null && echo "✓ Backend: healthy" || echo "✗ Backend: unhealthy"
	@docker-compose exec -T db pg_isready -U postgres > /dev/null && echo "✓ Database: healthy" || echo "✗ Database: unhealthy"
	@wget --quiet --spider http://localhost:3000/ > /dev/null && echo "✓ Frontend: healthy" || echo "✗ Frontend: unhealthy"

# Information
endpoints:
	@echo "Service Endpoints:"
	@echo ""
	@echo "Web Applications:"
	@echo "  Frontend:      http://localhost:3000"
	@echo "  Backend API:   http://localhost:8080"
	@echo ""
	@echo "Admin Consoles:"
	@echo "  MinIO:         http://localhost:9001"
	@echo "  Grafana:       http://localhost:3001"
	@echo ""
	@echo "Monitoring:"
	@echo "  Prometheus:    http://localhost:9090"
	@echo "  Loki:          http://localhost:3100"

# Cleanup
clean:
	@read -p "WARNING: This will delete all data. Continue? [y/N]: " response; \
	if [ "$$response" = "y" ]; then \
		docker-compose down -v; \
		echo "✓ Cleanup completed"; \
	else \
		echo "Cleanup cancelled"; \
	fi

prune:
	docker system prune -a -f
	@echo "✓ Docker system pruned"

# Development helpers
shell-backend:
	docker-compose exec backend /bin/bash

shell-frontend:
	docker-compose exec frontend /bin/bash

shell-db:
	docker-compose exec db /bin/bash

# Testing and validation
test-api:
	@echo "Testing backend API..."
	@curl -s http://localhost:8080/api/auth/me -H "Authorization: Bearer invalid" | jq '.'

test-frontend:
	@echo "Testing frontend..."
	@wget --quiet --spider http://localhost:3000/ && echo "✓ Frontend is accessible" || echo "✗ Frontend is not accessible"

# All targets are phony (not real files)
.PHONY: $(MAKECMDGOALS)
