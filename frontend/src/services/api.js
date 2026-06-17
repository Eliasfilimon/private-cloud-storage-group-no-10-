import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const getErrorMessage = (error, fallback = 'An error occurred') => {
  if (!error) return fallback;

  const data = error.response?.data;

  if (typeof data === 'string') return data;
  // Handle validation errors with field-specific messages
  if (data && data.errors) {
    const errorMessages = Object.values(data.errors);
    if (errorMessages.length > 0) {
      return errorMessages.join(', ');
    }
  }
  if (data && typeof data.message === 'string') return data.message;
  if (typeof error.message === 'string') return error.message;

  return fallback;
};

const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Flag to prevent infinite refresh loops
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  failedQueue = [];
};

const forceLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login';
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401) {
      const isLoginRequest = originalRequest?.url?.includes('/auth/login');
      const isRefreshRequest = originalRequest?.url?.includes('/auth/refresh');
      const isLoginPage =
        window.location.pathname === '/login' || window.location.pathname === '/';

      // Don't intercept login or refresh failures, or if already on login page
      if (isLoginRequest || isRefreshRequest || isLoginPage) {
        return Promise.reject(error);
      }

      // If already retried this request, force logout
      if (originalRequest._retry) {
        forceLogout();
        return Promise.reject(error);
      }

      // If a refresh is already in progress, queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      // No refresh token available — force logout
      if (!refreshToken) {
        isRefreshing = false;
        forceLogout();
        return Promise.reject(error);
      }

      try {
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { token: newToken } = response.data;

        localStorage.setItem('token', newToken);
        if (response.data.refreshToken) {
          localStorage.setItem('refreshToken', response.data.refreshToken);
        }

        // Update user data if provided
        if (response.data.username) {
          localStorage.setItem('user', JSON.stringify({
            id: response.data.id,
            username: response.data.username,
            email: response.data.email,
            fullName: response.data.fullName,
            role: response.data.role,
            department: response.data.department,
            storageQuota: response.data.storageQuota,
            storageUsed: response.data.storageUsed,
          }));
        }

        api.defaults.headers.common.Authorization = `Bearer ${newToken}`;
        originalRequest.headers.Authorization = `Bearer ${newToken}`;

        processQueue(null, newToken);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        forceLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  getCurrentUser: () => api.get('/auth/me'),
  register: (userData) => api.post('/auth/register', userData),
  changePassword: (passwordData) => api.post('/auth/change-password', passwordData),
  // Password Reset
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  validateResetToken: (token) => api.get('/auth/reset-password/validate', { params: { token } }),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword }),
  // 2FA setup
  setup2fa: () => api.post('/auth/2fa/setup'),
  enable2fa: (code) => api.post('/auth/2fa/enable', { code }),
  disable2fa: (code) => api.post('/auth/2fa/disable', { code }),
  // G1/M7: 2FA login second step — called when server returns pendingTotp: true
  verifyTotpLogin: (pendingToken, code) =>
    api.post('/auth/2fa/verify-login', { pendingToken, code }),
};

export const profileAPI = {
  updateProfile: (data) => api.put('/profile', data),
  changePassword: (data) => api.put('/profile/change-password', data),
};

export const dashboardAPI = {
  getUserStats: () => api.get('/dashboard/stats'),
  getAdminStats: () => api.get('/dashboard/admin/stats'),
};

export const fileAPI = {
  uploadFile: (formData, onUploadProgress) =>
    api.post('/files/upload', formData, {
      onUploadProgress,
    }),

  getUserFiles: (folderId = null) =>
    api.get('/files', { params: folderId != null ? { folderId } : {} }),

  getFileMetadata: (fileId) => api.get(`/files/${fileId}`),

  downloadFile: (fileId) =>
    api.get(`/files/${fileId}/download`, { responseType: 'blob' }),

  previewFile: (fileId) =>
    api.get(`/files/${fileId}/preview`, { responseType: 'blob' }),

  renameFile: (fileId, name) => api.put(`/files/${fileId}/rename`, null, { params: { name } }),

  moveFile: (fileId, folderId) => api.put(`/files/${fileId}/move`, null, { params: { folderId } }),

  deleteFile: (fileId) => api.delete(`/files/${fileId}`),

  shareFile: (fileId, shareData) =>
    api.post(`/files/${fileId}/share`, shareData),
};

export const shareAPI = {
  shareFile: (fileId, shareData) => api.post(`/shares/files/${fileId}`, shareData),
  getFilesSharedWithMe: () => api.get('/shares/shared-with-me'),
  getFilesSharedByMe: () => api.get('/shares/shared-by-me'),
  getFileShares: (fileId) => api.get(`/shares/files/${fileId}`),
  unshareFile: (shareId) => api.delete(`/shares/${shareId}`),
  canAccessFile: (fileId) => api.get(`/shares/can-access/${fileId}`),
};

// L7: Fixed — userAPI now correctly targets /admin/users; non-admin users should use profileAPI
export const userAPI = {
  getAllUsers:   () => api.get('/admin/users'),
  getUserById:  (userId) => api.get(`/admin/users/${userId}`),
  updateUser:   (userId, userData) => api.put(`/admin/users/${userId}/details`, userData),
  deleteUser:   (userId) => api.delete(`/admin/users/${userId}`),
  updateProfile: (userData) => api.put('/profile', userData),
  changePassword: (passwordData) => api.put('/profile/change-password', passwordData),
};

export const storageRequestAPI = {
  createRequest: (data) => api.post('/storage-requests', data),
  getMyRequests: () => api.get('/storage-requests/my-requests'),
  cancelRequest: (requestId) => api.delete(`/storage-requests/${requestId}`),
  
  // Admin endpoints
  getAllRequests: (status, page = 0, size = 20) => 
    api.get('/storage-requests', { params: { status, page, size } }),
  getPendingCount: () => api.get('/storage-requests/pending-count'),
  approveRequest: (requestId, data) => api.put(`/storage-requests/${requestId}/approve`, data),
};

export const adminAPI = {
  getAllUsers: () => api.get('/admin/users'),
  getUserById: (userId) => api.get(`/admin/users/${userId}`),
  // G8: Admin can now list files belonging to a specific user
  getUserFiles: (userId, page = 0, size = 20) =>
    api.get(`/admin/users/${userId}/files`, { params: { page, size } }),
  toggleUserStatus: (userId) => api.put(`/admin/users/${userId}/toggle-status`),
  deleteUser: (userId) => api.delete(`/admin/users/${userId}`),
  updateUserRole: (userId, role) => api.put(`/admin/users/${userId}/role`, null, { params: { role } }),
  updateUserStorage: (userId, quotaGb) => api.put(`/admin/users/${userId}/storage`, null, { params: { quotaGb } }),
  updateUserDetails: (userId, data) => api.put(`/admin/users/${userId}/details`, data),
  createUser: (userData) => api.post('/admin/users', userData),

  bulkUploadUsers: (formData) =>
    api.post('/admin/users/bulk-upload', formData),

  // HR System Integration
  fetchStaffFromExternalApi: (params = {}) =>
    api.get('/admin/users/fetch-from-hr', { params }),
  registerUsersFromExternalApi: (staffList) =>
    api.post('/admin/users/register-from-hr', staffList),

  getStatistics: () => api.get('/admin/statistics'),

  getAuditLogs: (page = 0, size = 20, action = null) =>
    api.get('/admin/audit-logs', { params: { page, size, action } }),

  getSystemHealth: () => api.get('/admin/system-health'),
  getActivityTimeline: () => api.get('/admin/activity-timeline'),
  createBackup: () => api.post('/admin/backup/create'),
  getBackupStatus: () => api.get('/admin/backup/status'),
  getBackupHistory: () => api.get('/admin/backup/history'),

  downloadBackup: (backupId) =>
    api.get(`/admin/backup/${backupId}/download`, { responseType: 'blob' }),

  deleteBackup: (backupId) => api.delete(`/admin/backup/${backupId}`),

  // Admin password reset for users
  resetUserPassword: (userId) =>
    api.post(`/admin/users/${userId}/reset-password`),
};

export const trashAPI = {
  getTrashedFiles: () => api.get('/trash'),
  restoreFile: (fileId) => api.put(`/trash/${fileId}/restore`),
  permanentDelete: (fileId) => api.delete(`/trash/${fileId}`),
  emptyTrash: () => api.delete('/trash/empty'),
};

export const folderAPI = {
  getRootFolders: () => api.get('/folders'),
  getSubFolders: (folderId) => api.get(`/folders/${folderId}/subfolders`),
  getFolder: (folderId) => api.get(`/folders/${folderId}`),
  createFolder: (data) => api.post('/folders', data),
  updateFolder: (folderId, data) => api.put(`/folders/${folderId}`, data),
  deleteFolder: (folderId) => api.delete(`/folders/${folderId}`),
};

export const analyticsAPI = {
  getStorageReport: () => api.get('/reports/storage'),

  getActivityReport: (startDate, endDate) =>
    api.get('/reports/activity', { params: { startDate, endDate } }),

  getAuditLogs: (userId = null) =>
    api.get('/audit-logs', { params: userId != null ? { userId } : {} }),
};

export default api;