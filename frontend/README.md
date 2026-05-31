<div align="center">

# 🎨 UDOM Secure Cloud Storage — Frontend

### Modern React SPA for Enterprise Cloud Storage

[![React](https://img.shields.io/badge/React-18.2-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.0-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![React Router](https://img.shields.io/badge/Router-6.x-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white)](https://reactrouter.com/)

</div>

---

A responsive, feature-rich React application providing the user interface for the UDOM Secure Cloud Storage system. Built with modern tooling for optimal performance and developer experience.

---

## Table of Contents

- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Environment Setup](#environment-setup)
- [Available Scripts](#available-scripts)
- [Component Architecture](#component-architecture)
- [API Integration](#api-integration)
- [Styling](#styling)
- [State Management](#state-management)
- [Authentication Flow](#authentication-flow)
- [Docker](#docker)
- [Troubleshooting](#troubleshooting)

---

## Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Framework | React | 18.2+ | UI library |
| Build Tool | Vite | 5.0+ | Development & bundling |
| Styling | Tailwind CSS | 3.4+ | Utility-first CSS |
| HTTP Client | Axios | 1.6+ | API communication |
| Routing | React Router | 6.21+ | Navigation |
| Charts | Recharts | 2.10+ | Data visualization |
| Notifications | React Toastify | 10.0+ | Toast messages |
| Icons | React Icons | 5.0+ | Icon library |
| Date Utils | date-fns | 3.3+ | Date formatting |

---

## Project Structure

```
frontend/
├── public/                          # Static assets
│   ├── logo.svg
│   └── favicon.ico
├── src/
│   ├── components/                  # Reusable UI components
│   │   ├── common/                 # Shared components
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── LoadingSpinner.jsx
│   │   │   └── Toast.jsx
│   │   ├── layout/                 # Layout components
│   │   │   ├── Navbar.jsx          # Top navigation
│   │   │   ├── Sidebar.jsx         # Side navigation
│   │   │   ├── PrivateRoute.jsx    # Protected route wrapper
│   │   │   └── Layout.jsx          # Main layout wrapper
│   │   ├── files/                  # File-related components
│   │   │   ├── FileList.jsx        # File listing
│   │   │   ├── FileCard.jsx        # Single file display
│   │   │   ├── UploadDialog.jsx    # File upload modal
│   │   │   ├── FolderTree.jsx      # Folder navigation
│   │   │   └── FilePreview.jsx     # File preview
│   │   └── dashboard/              # Dashboard components
│   │       ├── StorageChart.jsx    # Storage usage chart
│   │       ├── ActivityFeed.jsx    # Recent activity
│   │       └── StatsCard.jsx       # Statistics cards
│   ├── pages/                       # Page components (routes)
│   │   ├── auth/                   # Authentication pages
│   │   │   └── Login.jsx
│   │   ├── user/                   # User pages
│   │   │   ├── Dashboard.jsx       # Main dashboard
│   │   │   ├── FileManager.jsx     # File management
│   │   │   ├── Trash.jsx           # Trash/recycle bin
│   │   │   ├── Shared.jsx          # Shared files
│   │   │   ├── Backups.jsx         # Backup management
│   │   │   ├── Profile.jsx         # User profile
│   │   │   └── Settings.jsx        # User settings
│   │   └── admin/                  # Admin-only pages
│   │       ├── AdminDashboard.jsx  # Admin dashboard
│   │       ├── UserManagement.jsx  # User CRUD
│   │       ├── AuditLogs.jsx       # System logs
│   │       └── SystemSettings.jsx  # Global settings
│   ├── hooks/                       # Custom React hooks
│   │   ├── useAuth.js              # Authentication hook
│   │   ├── useFiles.js             # File operations hook
│   │   ├── useFolders.js           # Folder operations hook
│   │   └── useStorage.js           # Storage metrics hook
│   ├── services/                    # API service layer
│   │   ├── api.js                  # Axios instance & interceptors
│   │   ├── authService.js          # Authentication API
│   │   ├── fileService.js          # File operations API
│   │   ├── folderService.js        # Folder operations API
│   │   ├── userService.js          # User management API
│   │   └── adminService.js         # Admin operations API
│   ├── context/                     # React Context providers
│   │   ├── AuthContext.jsx         # Authentication state
│   │   └── ThemeContext.jsx        # Theme preferences
│   ├── utils/                       # Utility functions
│   │   ├── formatters.js           # Data formatting (bytes, dates)
│   │   ├── validators.js           # Input validation
│   │   ├── constants.js              # App constants
│   │   └── helpers.js              # Helper functions
│   ├── styles/                      # Global styles
│   │   └── globals.css
│   ├── App.jsx                      # Main application component
│   ├── main.jsx                     # Application entry point
│   └── index.css                    # Global CSS & Tailwind imports
├── .env                             # Environment variables
├── .env.example                     # Environment template
├── vite.config.js                   # Vite configuration
├── tailwind.config.js               # Tailwind CSS configuration
├── postcss.config.js                # PostCSS configuration
├── package.json                     # Dependencies & scripts
├── Dockerfile                       # Container image
└── README.md                        # This file
```

---

## Quick Start

### Option 1: Docker (Recommended for Deployment)

```bash
# From project root
docker compose up -d frontend
```

The frontend will be available at `http://localhost:3002`

### Option 2: Local Development

#### Prerequisites
- Node.js 18+ 
- npm 9+ (or yarn/pnpm)
- Backend API running at `http://localhost:8080`

#### Step 1: Install Dependencies

```bash
npm install
```

#### Step 2: Configure Environment

Create `.env` file:

```env
VITE_API_URL=http://localhost:8080/api
```

#### Step 3: Start Development Server

```bash
npm run dev
```

The app will be available at `http://localhost:5173` (or `http://localhost:3000`)

---

## Environment Setup

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `VITE_API_URL` | Backend API base URL | `http://localhost:8080/api` | Yes |
| `VITE_APP_NAME` | Application name | `Secure Cloud Storage` | No |
| `VITE_MAX_FILE_SIZE` | Max upload size (bytes) | `104857600` (100MB) | No |
| `VITE_CHUNK_SIZE` | Upload chunk size (bytes) | `1048576` (1MB) | No |

**Note:** Vite requires environment variables to be prefixed with `VITE_` to be exposed to the client.

### OS-Specific Setup Commands

**Linux/macOS:**
```bash
# Install dependencies
npm install

# Copy environment file
cp .env.example .env

# Start development
npm run dev
```

**Windows (Command Prompt):**
```cmd
:: Install dependencies
npm install

:: Copy environment file
copy .env.example .env

:: Start development
npm run dev
```

**Windows (PowerShell):**
```powershell
# Install dependencies
npm install

# Copy environment file
Copy-Item .env.example .env

# Start development
npm run dev
```

---

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm install` | Install all dependencies |
| `npm run dev` | Start development server with hot reload |
| `npm run build` | Build for production (outputs to `dist/`) |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint on all files |
| `npm run lint:fix` | Run ESLint and fix auto-fixable issues |
| `npm test` | Run tests (if configured) |

### Development Server

```bash
npm run dev
```

**Features:**
- Hot Module Replacement (HMR) - instant updates on file change
- Source maps for debugging
- Proxy API requests to backend
- Automatic browser refresh

### Production Build

```bash
npm run build
```

**Output:** `dist/` directory containing:
- Optimized JavaScript bundles
- Minified CSS
- Compressed assets
- Source maps (optional)

---

## Component Architecture

### Component Types

#### 1. Common Components (`src/components/common/`)
Reusable UI primitives used across the application.

```jsx
// Example: Button.jsx
export const Button = ({ variant, size, children, onClick, disabled }) => {
  const baseStyles = "rounded font-medium transition-colors";
  const variants = {
    primary: "bg-blue-600 text-white hover:bg-blue-700",
    secondary: "bg-gray-200 text-gray-800 hover:bg-gray-300",
    danger: "bg-red-600 text-white hover:bg-red-700"
  };
  
  return (
    <button 
      className={`${baseStyles} ${variants[variant]}`}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
};
```

#### 2. Layout Components (`src/components/layout/`)
Structural components that define page layout.

- **Navbar** - Top navigation bar with user menu
- **Sidebar** - Side navigation for main sections
- **PrivateRoute** - Route wrapper that requires authentication
- **Layout** - Main layout combining Navbar + Sidebar + content

#### 3. Feature Components (`src/components/files/`, `src/components/dashboard/`)
Domain-specific components for features.

### Component Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Components | PascalCase | `FileList.jsx`, `UploadDialog.jsx` |
| Hooks | camelCase, prefix `use` | `useAuth.js`, `useFiles.js` |
| Utilities | camelCase | `formatters.js`, `validators.js` |
| Constants | UPPER_SNAKE_CASE | `MAX_FILE_SIZE`, `API_ENDPOINTS` |

---

## API Integration

### Axios Configuration (`src/services/api.js`)

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor - add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Service Functions

```javascript
// src/services/fileService.js
import api from './api';

export const fileService = {
  getFiles: (folderId) => api.get(`/files?folderId=${folderId}`),
  uploadFile: (formData, onProgress) => api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: onProgress
  }),
  downloadFile: (id) => api.get(`/files/download/${id}`, { responseType: 'blob' }),
  deleteFile: (id) => api.delete(`/files/${id}`),
  restoreFile: (id) => api.post(`/files/restore/${id}`)
};
```

### Using Services in Components

```jsx
import { useState, useEffect } from 'react';
import { fileService } from '../services/fileService';

const FileManager = () => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFiles();
  }, []);

  const loadFiles = async () => {
    try {
      setLoading(true);
      const response = await fileService.getFiles();
      setFiles(response.data);
    } catch (error) {
      toast.error('Failed to load files');
    } finally {
      setLoading(false);
    }
  };

  // ... render logic
};
```

---

## Styling

### Tailwind CSS

This project uses Tailwind CSS for utility-first styling.

**Key Configuration:** `tailwind.config.js`

```javascript
module.exports = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8'
        }
      }
    }
  }
};
```

### Common Utility Classes

| Purpose | Classes |
|---------|---------|
| Container | `container mx-auto px-4` |
| Card | `bg-white rounded-lg shadow-md p-6` |
| Button Primary | `bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700` |
| Input | `border border-gray-300 rounded px-3 py-2 focus:ring-2 focus:ring-blue-500` |
| Text Header | `text-2xl font-bold text-gray-800` |
| Flex Center | `flex items-center justify-center` |

### Responsive Design

```jsx
// Mobile-first responsive classes
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {/* Content adapts: 1 col mobile, 2 col tablet, 3 col desktop */}
</div>
```

**Breakpoints:**
- `sm:` - 640px+
- `md:` - 768px+
- `lg:` - 1024px+
- `xl:` - 1280px+
- `2xl:` - 1536px+

---

## State Management

### Local State (useState)

For component-specific state:

```jsx
const [isOpen, setIsOpen] = useState(false);
const [files, setFiles] = useState([]);
```

### Global State (Context API)

**AuthContext** (`src/context/AuthContext.jsx`):

```jsx
import { createContext, useState, useContext } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const login = async (credentials) => {
    const response = await authService.login(credentials);
    localStorage.setItem('token', response.token);
    setUser(response.user);
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### Custom Hooks

**useFiles** (`src/hooks/useFiles.js`):

```jsx
import { useState, useEffect, useCallback } from 'react';
import { fileService } from '../services/fileService';

export const useFiles = (folderId) => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadFiles = useCallback(async () => {
    try {
      setLoading(true);
      const response = await fileService.getFiles(folderId);
      setFiles(response.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [folderId]);

  useEffect(() => {
    loadFiles();
  }, [loadFiles]);

  return { files, loading, error, refresh: loadFiles };
};
```

---

## Authentication Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Login     │────▶│   Backend    │────▶│   JWT Token │
│   Page      │     │   /api/auth  │     │   Received  │
└─────────────┘     └──────────────┘     └─────────────┘
                                                │
                                                ▼
                                        ┌─────────────┐
                                        │  Store in   │
                                        │ localStorage│
                                        └─────────────┘
                                                │
                                                ▼
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Authenticated│◀───│  API Calls   │◀────│  Include    │
│   Routes    │     │  with Token  │     │  in Header  │
└─────────────┘     └──────────────┘     └─────────────┘
```

**Token Storage:**
- Access token stored in `localStorage`
- Token automatically included in API requests via Axios interceptor
- 401 responses trigger logout and redirect to login

---

## Docker

### Build Image

```bash
# From project root
docker build -t secure-cloud-storage-frontend ./frontend
```

### Run Container

```bash
docker run -d \
  --name scs_frontend \
  -p 3002:3000 \
  -e VITE_API_URL=/api \
  secure-cloud-storage-frontend
```

### Docker Compose (Recommended)

See root `docker-compose.yml` for orchestrated setup with all services.

---

## Troubleshooting

### Common Issues

#### Port Already in Use

```bash
# Find process using port 5173
lsof -i :5173

# Kill process or use different port
npm run dev -- --port 3000
```

#### API Connection Errors

```bash
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check environment variable
cat .env | grep VITE_API_URL
```

#### Node Modules Issues

```bash
# Clean install
rm -rf node_modules package-lock.json
npm install
```

**Windows:**
```cmd
rmdir /s /q node_modules
del package-lock.json
npm install
```

#### Build Failures

```bash
# Clear Vite cache
rm -rf node_modules/.vite

# Restart dev server
npm run dev
```

### Development Tips

| Issue | Solution |
|-------|----------|
| Changes not reflecting | Check console for HMR errors, restart dev server |
| API 401 errors | Check token in localStorage, verify backend JWT secret |
| CORS errors | Ensure backend CORS allows `http://localhost:5173` |
| Slow build | Check for circular imports, optimize bundle size |
| Tailwind not working | Restart dev server after config changes |

### Browser Developer Tools

**Recommended Extensions:**
- React Developer Tools - Component inspection
- Redux DevTools - State inspection (if using Redux)

**Console Commands:**
```javascript
// Check auth state
JSON.parse(localStorage.getItem('user'))

// Check token
localStorage.getItem('token')

// Clear auth
clearAuth()
```

---

## Performance Optimization

### Code Splitting

```jsx
// Lazy load routes
import { lazy, Suspense } from 'react';

const AdminPanel = lazy(() => import('./pages/admin/AdminPanel'));

<Suspense fallback={<LoadingSpinner />}>
  <AdminPanel />
</Suspense>
```

### Memoization

```jsx
import { memo, useMemo, useCallback } from 'react';

// Memoize component
const FileCard = memo(({ file, onDelete }) => {
  // Component logic
});

// Memoize expensive calculations
const sortedFiles = useMemo(() => {
  return files.sort((a, b) => b.createdAt - a.createdAt);
}, [files]);

// Memoize callbacks
const handleDelete = useCallback((id) => {
  deleteFile(id);
}, []);
```

---

## Production Deployment

### Production Build Optimization

#### Build Configuration

`vite.config.js` production settings:

```javascript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig({
  plugins: [
    react(),
    visualizer({ open: false, gzipSize: true }) // Bundle analysis
  ],
  build: {
    target: 'es2015', // Browser compatibility
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,  // Remove console.log in production
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          // Code splitting by vendor
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-charts': ['recharts'],
          'vendor-utils': ['axios', 'date-fns']
        }
      }
    },
    chunkSizeWarningLimit: 500, // kB
    sourcemap: false  // Disable in production for security
  }
});
```

#### Environment Variables for Production

`.env.production`:

```env
# API endpoint (use relative path for same-origin deployment)
VITE_API_URL=/api

# Production optimizations
VITE_APP_NAME=Secure Cloud Storage
VITE_MAX_FILE_SIZE=104857600
VITE_CHUNK_SIZE=5242880

# Feature flags
VITE_ENABLE_ANALYTICS=true
VITE_ENABLE_SERVICE_WORKER=true
```

### Production Build Process

```bash
# Clean install
rm -rf node_modules dist
npm ci  # Use ci for reproducible builds

# Build for production
npm run build

# Verify build output
ls -la dist/

# Test production build locally
npm run preview
```

### Docker Production Build

```bash
# Build production image
docker build -t secure-cloud-storage-frontend:prod .

# Multi-stage build benefits:
# - Smaller final image (nginx:alpine vs node:20)
# - No build tools in production image
# - Security: non-root user
```

### Nginx Configuration (Production)

```nginx
# /etc/nginx/conf.d/default.conf
server {
    listen 3000;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
    gzip_min_length 1000;

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        try_files $uri =404;
    }

    # API proxy
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }

    # React Router - serve index.html for all routes
    location / {
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache";
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
}
```

### CDN Integration

For global deployments, use a CDN for static assets:

```javascript
// vite.config.js with CDN
export default defineConfig({
  base: 'https://cdn.yourdomain.com/scs/', // CDN URL
  build: {
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        entryFileNames: 'js/[name]-[hash].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.');
          const ext = info[info.length - 1];
          return `assets/[name]-[hash][extname]`;
        }
      }
    }
  }
});
```

### Performance Optimization

#### Code Splitting

```jsx
// Lazy load heavy components
import { lazy, Suspense } from 'react';

const AdminPanel = lazy(() => import('./pages/admin/AdminPanel'));
const AnalyticsDashboard = lazy(() => import('./pages/admin/AnalyticsDashboard'));

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/admin" element={<AdminPanel />} />
        <Route path="/admin/analytics" element={<AnalyticsDashboard />} />
      </Routes>
    </Suspense>
  );
}
```

#### Image Optimization

```jsx
// Use WebP format with fallback
<picture>
  <source srcSet="/images/logo.webp" type="image/webp" />
  <img src="/images/logo.png" alt="Logo" loading="lazy" />
</picture>

// Responsive images
<img 
  srcSet="/img-400.jpg 400w, /img-800.jpg 800w, /img-1200.jpg 1200w"
  sizes="(max-width: 600px) 400px, (max-width: 1000px) 800px, 1200px"
  src="/img-800.jpg" 
  alt="Responsive"
/>
```

#### Service Worker (PWA)

```javascript
// Register service worker for offline support
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js');
  });
}
```

### Security Hardening

#### Content Security Policy

```html
<!-- index.html -->
<meta http-equiv="Content-Security-Policy" content="
  default-src 'self';
  script-src 'self' 'unsafe-inline';
  style-src 'self' 'unsafe-inline';
  img-src 'self' data: https:;
  connect-src 'self' https://api.yourdomain.com;
  font-src 'self';
  frame-ancestors 'none';
">
```

#### Environment Security

```bash
# Never commit .env files with secrets
echo ".env.production" >> .gitignore
echo ".env.local" >> .gitignore

# Verify no secrets in build
grep -r "password\|secret\|key" dist/ || echo "No secrets found"
```

### Monitoring & Error Tracking

#### Error Boundary

```jsx
// components/ErrorBoundary.jsx
import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // Log to monitoring service (Sentry, LogRocket, etc.)
    console.error('Error caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return <h1>Something went wrong. Please refresh the page.</h1>;
    }
    return this.props.children;
  }
}
```

#### Performance Monitoring

```javascript
// utils/performance.js
export const measurePerformance = (name, fn) => {
  const start = performance.now();
  const result = fn();
  const end = performance.now();
  
  console.log(`${name} took ${end - start} milliseconds`);
  
  // Send to analytics
  if (window.gtag) {
    window.gtag('event', 'timing_complete', {
      name: name,
      value: Math.round(end - start)
    });
  }
  
  return result;
};
```

### Testing Production Build

```bash
# Preview production build
npm run preview

# Lighthouse audit
npx lighthouse http://localhost:4173 --output=html --output-path=./report.html

# Bundle analysis
npm run build -- --mode analyze
```

### Troubleshooting Production Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| **Blank page after deploy** | Routing issue | Ensure nginx serves index.html for all routes |
| **API 404 errors** | Wrong API URL | Check `VITE_API_URL` is `/api` not full URL |
| **Assets not loading** | Wrong base path | Set correct `base` in vite.config.js |
| **Slow initial load** | Large bundle | Enable code splitting, lazy loading |
| **CORS errors** | API config | Ensure backend allows frontend origin |
| **Old version cached** | Aggressive caching | Add cache-busting hashes to filenames |

### Deployment Checklist

- [ ] Environment variables configured
- [ ] Build completes without errors
- [ ] `dist/` folder contains all assets
- [ ] Nginx config tested
- [ ] API proxy working
- [ ] SSL/TLS configured
- [ ] Security headers added
- [ ] Gzip compression enabled
- [ ] Cache headers configured
- [ ] Health check endpoint responding
- [ ] Error tracking configured
- [ ] Performance metrics monitored

## Related Documentation

- [Backend README](../backend/README.md) - Spring Boot API documentation
- [Root README](../README.md) - Project overview and Docker setup
- [Environment Config](../.env.example) - Environment variables template

---

<p align="center">
  <b>Production-Ready React Frontend</b><br>
  <sub>Optimized | Secure | Responsive</sub>
</p>
