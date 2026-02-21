import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { dashboardAPI } from '../services/api';
import { toast } from 'react-toastify';
import { FaFolder, FaDatabase, FaShareAlt, FaUpload, FaEye, FaCog, FaChartLine, FaDownload, FaUsers } from 'react-icons/fa';

const Dashboard = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      const response = user.role === 'ADMIN' 
        ? await dashboardAPI.getAdminStats() 
        : await dashboardAPI.getUserStats();
      setStats(response.data);
    } catch (error) {
      toast.error('Failed to load dashboard statistics');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const formatBytes = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const formatQuota = (used, quota) => {
    return `${formatBytes(used)} / ${formatBytes(quota)}`;
  };

  const dashboardStats = stats ? [
    {
      icon: FaFolder,
      label: 'Total Files',
      value: stats.totalFiles || 0,
      color: 'from-udom-blue-500 to-udom-blue-600',
      bgColor: 'bg-blue-50',
      textColor: 'text-udom-blue-600'
    },
    {
      icon: FaDatabase,
      label: 'Storage Used',
      value: formatQuota(stats.storageUsed || 0, stats.storageQuota || 0),
      color: 'from-udom-gold-500 to-udom-gold-600',
      bgColor: 'bg-yellow-50',
      textColor: 'text-udom-gold-600'
    },
    {
      icon: FaUpload,
      label: 'Recent Uploads',
      value: stats.recentUploads || 0,
      color: 'from-green-500 to-green-600',
      bgColor: 'bg-green-50',
      textColor: 'text-green-600',
      subtitle: 'Last 7 days'
    },
    {
      icon: FaDownload,
      label: 'Recent Downloads',
      value: stats.recentDownloads || 0,
      color: 'from-purple-500 to-purple-600',
      bgColor: 'bg-purple-50',
      textColor: 'text-purple-600',
      subtitle: 'Last 7 days'
    },
    ...(user.role === 'ADMIN' ? [{
      icon: FaUsers,
      label: 'Active Users',
      value: stats.totalActiveUsers || 0,
      color: 'from-indigo-500 to-indigo-600',
      bgColor: 'bg-indigo-50',
      textColor: 'text-indigo-600'
    }] : [])
  ] : [];

  const quickActions = [
    {
      icon: FaUpload,
      title: 'Upload File',
      description: 'Upload new files to your storage',
      link: '/files',
      color: 'bg-udom-blue-500 hover:bg-udom-blue-600'
    },
    {
      icon: FaEye,
      title: 'View Files',
      description: 'Browse and manage your files',
      link: '/files',
      color: 'bg-udom-gold-500 hover:bg-udom-gold-600'
    },
    {
      icon: FaCog,
      title: 'Settings',
      description: 'Manage your account settings',
      link: '/profile',
      color: 'bg-gray-600 hover:bg-gray-700'
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <Navbar />
      
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        {/* Welcome Section */}
        <div className="mb-8">
          <div className="bg-gradient-to-r from-udom-blue-500 to-udom-blue-700 rounded-2xl shadow-xl p-8 text-white">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-4xl font-bold mb-2">
                  Welcome back, {user.fullName || 'User'}!
                </h1>
                <p className="text-blue-100 text-lg">
                  University of Dodoma - Secure Cloud Storage System
                </p>
                <div className="mt-4 inline-flex items-center px-4 py-2 bg-white bg-opacity-20 rounded-lg backdrop-blur-sm">
                  <span className="text-sm font-medium">Role: {user.role || 'LECTURER'}</span>
                </div>
              </div>
              <div className="hidden md:block">
                <FaChartLine className="text-8xl opacity-20" />
              </div>
            </div>
          </div>
        </div>

        {/* Loading State */}
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-udom-blue-600"></div>
          </div>
        ) : (
          <>
            {/* Statistics Cards */}
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4 mb-8">
              {dashboardStats.map((stat, index) => {
                const Icon = stat.icon;
                return (
                  <div key={index} className="card hover:shadow-lg transition-shadow duration-200">
                    <div className="flex items-center">
                      <div className={`flex-shrink-0 p-4 rounded-xl bg-gradient-to-br ${stat.color}`}>
                        <Icon className="h-8 w-8 text-white" />
                      </div>
                      <div className="ml-5 flex-1">
                        <dt className="text-sm font-medium text-gray-500 truncate">
                          {stat.label}
                        </dt>
                        <dd className={`text-2xl font-bold ${stat.textColor}`}>
                          {stat.value}
                        </dd>
                        {stat.subtitle && (
                          <p className="text-xs text-gray-500 mt-1">{stat.subtitle}</p>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Storage Progress Bar */}
            {stats && (
              <div className="card mb-8">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Storage Usage</h3>
                <div className="w-full bg-gray-200 rounded-full h-4">
                  <div 
                    className={`h-4 rounded-full transition-all ${
                      (stats.storageUsed / stats.storageQuota * 100) > 90 ? 'bg-red-500' : 
                      (stats.storageUsed / stats.storageQuota * 100) > 70 ? 'bg-yellow-500' : 
                      'bg-green-500'
                    }`}
                    style={{ width: `${Math.min((stats.storageUsed / stats.storageQuota * 100), 100)}%` }}
                  ></div>
                </div>
                <div className="flex justify-between mt-2 text-sm text-gray-600">
                  <span>{formatBytes(stats.storageUsed)}</span>
                  <span>{((stats.storageUsed / stats.storageQuota) * 100).toFixed(1)}%</span>
                  <span>{formatBytes(stats.storageQuota)}</span>
                </div>
              </div>
            )}
          </>
        )}

        {/* Quick Actions */}
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3 mb-8">
        </div>

        {/* Quick Actions */}
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {quickActions.map((action, index) => {
              const Icon = action.icon;
              return (
                <Link
                  key={index}
                  to={action.link}
                  className="card hover:shadow-xl transition-all duration-200 transform hover:-translate-y-1 group"
                >
                  <div className={`w-12 h-12 rounded-xl ${action.color} flex items-center justify-center mb-4 transition-transform group-hover:scale-110`}>
                    <Icon className="text-2xl text-white" />
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-2">{action.title}</h3>
                  <p className="text-gray-600 text-sm">{action.description}</p>
                </Link>
              );
            })}
          </div>
        </div>

        {/* Recent Activity */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-2xl font-bold text-gray-900">Recent Activity</h2>
            <Link to="/files" className="text-udom-blue-600 hover:text-udom-blue-700 font-medium text-sm">
              View All →
            </Link>
          </div>
          <div className="text-center py-12">
            <FaFolder className="mx-auto h-16 w-16 text-gray-300 mb-4" />
            <p className="text-gray-500 text-lg">No recent activity</p>
            <p className="text-gray-400 text-sm mt-2">Upload files to see your activity here</p>
          </div>
        </div>

        {/* Footer */}
        <div className="mt-8 text-center text-gray-500 text-sm">
          <p>© 2026 University of Dodoma. All rights reserved.</p>
          <p className="mt-1">Secure Self-Hosted Private Cloud Storage System</p>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
