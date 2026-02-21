import React, { useState, useEffect } from 'react';
import { FaTimes, FaUserPlus, FaCheck } from 'react-icons/fa';
import { userAPI, shareAPI } from '../services/api';
import { toast } from 'react-toastify';

const ShareFileModal = ({ file, onClose, onSuccess }) => {
  const [users, setUsers] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [permission, setPermission] = useState('DOWNLOAD');
  const [expiresInDays, setExpiresInDays] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingUsers, setLoadingUsers] = useState(true);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await userAPI.getAllUsers();
      const currentUser = JSON.parse(localStorage.getItem('user'));
      // Filter out current user
      const filteredUsers = response.data.filter(u => u.id !== currentUser.id);
      setUsers(filteredUsers);
    } catch (error) {
      toast.error('Failed to load users');
    } finally {
      setLoadingUsers(false);
    }
  };

  const toggleUserSelection = (userId) => {
    if (selectedUsers.includes(userId)) {
      setSelectedUsers(selectedUsers.filter(id => id !== userId));
    } else {
      setSelectedUsers([...selectedUsers, userId]);
    }
  };

  const handleShare = async () => {
    if (selectedUsers.length === 0) {
      toast.error('Please select at least one user');
      return;
    }

    setLoading(true);
    try {
      const shareData = {
        userIds: selectedUsers,
        permission,
        expiresInDays: expiresInDays ? parseInt(expiresInDays) : null,
      };

      await shareAPI.shareFile(file.id, shareData);
      toast.success(`File shared with ${selectedUsers.length} user(s)`);
      onSuccess();
      onClose();
    } catch (error) {
      toast.error(error.response?.data || 'Failed to share file');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-udom-blue-600 to-udom-blue-700 px-6 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <FaUserPlus className="text-white text-xl" />
            <h2 className="text-xl font-bold text-white">Share File</h2>
          </div>
          <button
            onClick={onClose}
            className="text-white hover:bg-white hover:bg-opacity-20 rounded-full p-2 transition"
          >
            <FaTimes />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {/* File Info */}
          <div className="bg-gray-50 rounded-lg p-4 mb-6">
            <p className="text-sm text-gray-600">Sharing file:</p>
            <p className="font-semibold text-gray-900 truncate">{file.originalName}</p>
          </div>

          {/* Permission Settings */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Permission Level
            </label>
            <select
              value={permission}
              onChange={(e) => setPermission(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-udom-blue-500 focus:border-transparent"
            >
              <option value="VIEW">View Only</option>
              <option value="DOWNLOAD">View & Download</option>
            </select>
          </div>

          {/* Expiration */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Expires In (Days)
            </label>
            <input
              type="number"
              value={expiresInDays}
              onChange={(e) => setExpiresInDays(e.target.value)}
              placeholder="Leave empty for no expiration"
              min="1"
              max="365"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-udom-blue-500 focus:border-transparent"
            />
          </div>

          {/* User Selection */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Share With ({selectedUsers.length} selected)
            </label>
            <div className="border border-gray-300 rounded-lg max-h-64 overflow-y-auto">
              {loadingUsers ? (
                <div className="p-8 text-center text-gray-500">
                  Loading users...
                </div>
              ) : users.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  No other users available
                </div>
              ) : (
                <div className="divide-y divide-gray-200">
                  {users.map((user) => (
                    <div
                      key={user.id}
                      onClick={() => toggleUserSelection(user.id)}
                      className={`p-4 cursor-pointer hover:bg-gray-50 transition ${
                        selectedUsers.includes(user.id) ? 'bg-udom-blue-50' : ''
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-udom-blue-500 to-udom-blue-600 flex items-center justify-center text-white font-bold">
                            {user.fullName?.charAt(0) || user.username?.charAt(0) || 'U'}
                          </div>
                          <div>
                            <p className="font-medium text-gray-900">{user.fullName}</p>
                            <p className="text-sm text-gray-500">{user.email}</p>
                            {user.department && (
                              <p className="text-xs text-gray-400">{user.department}</p>
                            )}
                          </div>
                        </div>
                        {selectedUsers.includes(user.id) && (
                          <FaCheck className="text-udom-blue-600 text-xl" />
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="bg-gray-50 px-6 py-4 flex items-center justify-end space-x-3">
          <button
            onClick={onClose}
            className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-100 transition"
            disabled={loading}
          >
            Cancel
          </button>
          <button
            onClick={handleShare}
            disabled={loading || selectedUsers.length === 0}
            className="px-6 py-2 bg-udom-blue-600 text-white rounded-lg hover:bg-udom-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            <FaUserPlus />
            <span>{loading ? 'Sharing...' : 'Share File'}</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShareFileModal;
