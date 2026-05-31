import React, { Component } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './styles/udom-theme.css';
import './styles/admin-theme.css';

import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
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
      const err = this.state.error;
      const msg = typeof err === 'string' ? err : (err?.message || String(err));
      const stack = typeof err?.stack === 'string' ? err.stack : '';
      return (
        <div style={{ padding: '2rem', fontFamily: 'monospace', background: '#fff' }}>
          <h2 style={{ color: 'red' }}>Application Error</h2>
          <pre style={{ background: '#f5f5f5', padding: '1rem', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
            {msg}
            {'\n'}
            {stack}
          </pre>
          <button onClick={() => { localStorage.removeItem('token'); localStorage.removeItem('user'); window.location.href = '/'; }} style={{ marginTop: '1rem', padding: '0.5rem 1rem' }}>Back to Login</button>
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

          <SessionTimeout>
            <Routes>
              <Route path="/" element={<Login />} />
              <Route path="/login" element={<Login />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />

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
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
