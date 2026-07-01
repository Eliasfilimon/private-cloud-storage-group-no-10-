# Stage 1: Build the React application
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./

# Install dependencies securely
RUN npm install

# Copy source code and build
COPY . .
ENV VITE_API_URL=/api
RUN npm run build

# Stage 2: Production image - serves the built frontend files
FROM nginx:alpine

# Copy built application from builder stage
COPY --from=builder /app/build /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 -O /dev/null http://127.0.0.1:3000/ || exit 1

# Run nginx
CMD ["nginx", "-g", "daemon off;"]