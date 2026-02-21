import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  getCurrentUser: () => api.get('/auth/me'),
  register: (userData) => api.post('/auth/register', userData),
};

// Profile API
export const profileAPI = {
  updateProfile: (data) => api.put('/profile', data),
  changePassword: (data) => api.put('/profile/change-password', data),
};

// Dashboard API
export const dashboardAPI = {
  getUserStats: () => api.get('/dashboard/stats'),
  getAdminStats: () => api.get('/dashboard/admin/stats'),
};

// File API
export const fileAPI = {
  uploadFile: (formData, onUploadProgress) => 
    api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    }),
  getUserFiles: (folderId = null) => 
    api.get('/files', { params: { folderId } }),
  getFileMetadata: (fileId) => api.get(`/files/${fileId}`),
  downloadFile: (fileId) => 
    api.get(`/files/${fileId}/download`, { responseType: 'blob' }),
  deleteFile: (fileId) => api.delete(`/files/${fileId}`),
  shareFile: (fileId, shareData) => 
    api.post(`/files/${fileId}/share`, shareData),
};

// File Share API
export const shareAPI = {
  shareFile: (fileId, shareData) => api.post(`/shares/files/${fileId}`, shareData),
  getFilesSharedWithMe: () => api.get('/shares/shared-with-me'),
  getFilesSharedByMe: () => api.get('/shares/shared-by-me'),
  getFileShares: (fileId) => api.get(`/shares/files/${fileId}`),
  unshareFile: (shareId) => api.delete(`/shares/${shareId}`),
  canAccessFile: (fileId) => api.get(`/shares/can-access/${fileId}`),
};

// User API
export const userAPI = {
  getAllUsers: () => api.get('/users'),
  getUserById: (userId) => api.get(`/users/${userId}`),
  updateUser: (userId, userData) => api.put(`/users/${userId}`, userData),
  deleteUser: (userId) => api.delete(`/users/${userId}`),
  updateProfile: (userData) => api.put('/users/profile', userData),
  changePassword: (passwordData) => api.put('/users/password', passwordData),
};

// Admin API
export const adminAPI = {
  getAllUsers: () => api.get('/admin/users'),
  getUserById: (userId) => api.get(`/admin/users/${userId}`),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
};

// Analytics API
export const analyticsAPI = {
  getStorageReport: () => api.get('/reports/storage'),
  getActivityReport: (startDate, endDate) => 
    api.get('/reports/activity', { params: { startDate, endDate } }),
  getAuditLogs: (userId = null) => 
    api.get('/audit-logs', { params: { userId } }),
};

export default api;
