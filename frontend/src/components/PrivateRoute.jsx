import React from 'react';
import { Navigate } from 'react-router-dom';
import Sidebar from './layout/Sidebar';

const PrivateRoute = ({ children, adminOnly = false }) => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && user.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  return <Sidebar>{children}</Sidebar>;
};

export default PrivateRoute;
