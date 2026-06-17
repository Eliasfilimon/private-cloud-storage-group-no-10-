import React, { Component } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './styles/udom-theme.css';
import './styles/admin-theme.css';

import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import FileManager from './pages/FileManager';
import Profile from './pages/Profile';
import AdminPanel from './pages/AdminPanel';
import SharedFiles from './pages/SharedFiles';
import Trash from './pages/Trash';
import Backups from './pages/Backups';
import MyDocuments from './pages/MyDocuments';
import MyStorageRequests from './pages/MyStorageRequests';
import PrivateRoute from './components/PrivateRoute';
import SessionTimeout from './components/SessionTimeout';
import { SessionProvider } from './context/SessionContext';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }
  render() {
    if (this.state.hasError) {
      const isDev = import.meta.env.DEV;
      const err   = this.state.error;
      const msg   = typeof err === 'string' ? err : (err?.message || 'An unexpected error occurred.');
      const stack = typeof err?.stack === 'string' ? err.stack : '';
      return (
        <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif', background: '#0f172a', color: '#f1f5f9', minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h2 style={{ color: '#f87171', marginBottom: '1rem' }}>Something went wrong</h2>
          <p style={{ color: '#94a3b8', marginBottom: '1.5rem', textAlign: 'center', maxWidth: 480 }}>
            An unexpected error occurred. Please try refreshing the page or contact IT support if the problem persists.
          </p>
          {/* M9: Only show technical details in development mode */}
          {isDev && (
            <details style={{ marginBottom: '1rem', maxWidth: 640, width: '100%' }}>
              <summary style={{ cursor: 'pointer', color: '#64748b', marginBottom: '0.5rem' }}>
                Technical details (dev only)
              </summary>
              <pre style={{ background: '#1e293b', padding: '1rem', borderRadius: '0.5rem', whiteSpace: 'pre-wrap', wordBreak: 'break-all', fontSize: '0.75rem', color: '#e2e8f0' }}>
                {msg}{'\n'}{stack}
              </pre>
            </details>
          )}
          <button
            onClick={() => { localStorage.removeItem('token'); localStorage.removeItem('user'); window.location.href = '/'; }}
            style={{ padding: '0.625rem 1.5rem', background: '#3b82f6', color: '#fff', border: 'none', borderRadius: '0.5rem', cursor: 'pointer', fontWeight: 600 }}
          >
            Back to Login
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <div className="App">
          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
          />

          <SessionProvider>
            <SessionTimeout>
              <Routes>
              <Route path="/" element={<Login />} />
              <Route path="/login" element={<Login />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route path="/register" element={<Register />} />

              <Route path="/dashboard" element={
                <PrivateRoute><Dashboard /></PrivateRoute>
              } />
              <Route path="/files" element={
                <PrivateRoute><FileManager /></PrivateRoute>
              } />
              <Route path="/documents" element={
                <PrivateRoute><MyDocuments /></PrivateRoute>
              } />
              <Route path="/shared" element={
                <PrivateRoute><SharedFiles /></PrivateRoute>
              } />
              <Route path="/profile" element={
                <PrivateRoute><Profile /></PrivateRoute>
              } />
              <Route path="/storage-requests" element={
                <PrivateRoute><MyStorageRequests /></PrivateRoute>
              } />
              <Route path="/trash" element={
                <PrivateRoute><Trash /></PrivateRoute>
              } />
              <Route path="/backups" element={
                <PrivateRoute adminOnly><Backups /></PrivateRoute>
              } />
              <Route path="/admin" element={
                <PrivateRoute adminOnly><AdminPanel /></PrivateRoute>
              } />
              <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </SessionTimeout>
          </SessionProvider>
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
