# Production image - uses pre-built frontend files
FROM nginx:alpine

# Copy pre-built application
COPY build/ /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 -O /dev/null http://127.0.0.1:3000/ || exit 1

# Run nginx
CMD ["nginx", "-g", "daemon off;"]