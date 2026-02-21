# Frontend - Secure Cloud Storage UI

React.js frontend for the secure cloud storage system.

## Project Structure

```
frontend/
├── public/                  # Static assets
├── src/
│   ├── components/         # Reusable components
│   │   ├── PrivateRoute.jsx
│   │   ├── Navbar.jsx
│   │   ├── FileList.jsx
│   │   ├── UploadDialog.jsx
│   │   └── ...
│   ├── pages/              # Page components
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Dashboard.jsx
│   │   ├── FileManager.jsx
│   │   ├── Profile.jsx
│   │   └── AdminPanel.jsx
│   ├── services/           # API services
│   │   └── api.js
│   ├── utils/              # Utility functions
│   ├── App.jsx             # Main app component
│   ├── main.jsx            # Entry point
│   └── index.css           # Global styles
├── package.json
├── vite.config.js
└── README.md
```

## Setup Instructions

### 1. Install Dependencies
```bash
npm install
```

### 2. Environment Variables
Create a `.env` file:
```env
VITE_API_URL=http://localhost:8080/api
```

### 3. Run Development Server
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### 4. Build for Production
```bash
npm run build
```

The build output will be in the `build/` directory.

## Features

### User Features
- User authentication (login only - no public registration)
- File upload with progress tracking
- File listing and organization
- File download
- File sharing with other users
- Profile management
- Storage usage tracking

### Admin Features
- User registration and management (Admin only)
- System analytics
- Audit logs
- Storage reports
- System configuration

## Technology Stack
- **React 18** - UI framework
- **React Router** - Navigation
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **Vite** - Build tool
- **React Toastify** - Notifications
- **Recharts** - Analytics charts

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm test` - Run tests

## API Integration

The frontend communicates with the backend API through the `services/api.js` file. All API calls include:
- JWT authentication headers
- Error handling
- Request/response interceptors
- Automatic token refresh

## Component Guidelines

### Creating New Components
1. Use functional components with hooks
2. Follow naming convention: PascalCase for components
3. Place reusable components in `src/components/`
4. Place page components in `src/pages/`

### State Management
- Use React hooks (useState, useEffect, etc.)
- Consider Context API for global state
- Keep component state local when possible

## Styling Guidelines
- Use Tailwind CSS utility classes
- Follow responsive design principles
- Maintain consistent color scheme
- Use provided color palette from tailwind.config.js

## Security Considerations
- Never store sensitive data in localStorage except tokens
- Validate all user inputs
- Sanitize data before rendering
- Use HTTPS in production
- Implement CSRF protection

## Deployment
See deployment guide in `/docs/deployment.md`
