import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardAPI, fileAPI, adminAPI } from '../services/api';
import { toast } from 'react-toastify';
import {
  FaFolder, FaDatabase, FaUpload, FaDownload, FaUsers, FaFile,
  FaFilePdf, FaFileWord, FaFileExcel, FaFileImage, FaFileArchive,
  FaShareAlt, FaShieldAlt, FaArrowRight, FaHistory, FaClock
} from 'react-icons/fa';

const fmt = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024, s = ['B','KB','MB','GB','TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + s[i];
};

const fileIcon = (name = '') => {
  const e = name.split('.').pop().toLowerCase();
  if (e === 'pdf') return { Icon: FaFilePdf, cls: 'text-red-500 bg-red-50' };
  if (['doc','docx'].includes(e)) return { Icon: FaFileWord, cls: 'text-blue-500 bg-blue-50' };
  if (['xls','xlsx'].includes(e)) return { Icon: FaFileExcel, cls: 'text-green-500 bg-green-50' };
  if (['jpg','jpeg','png','gif','webp'].includes(e)) return { Icon: FaFileImage, cls: 'text-purple-500 bg-purple-50' };
  if (['zip','rar','7z','tar'].includes(e)) return { Icon: FaFileArchive, cls: 'text-yellow-500 bg-yellow-50' };
  return { Icon: FaFile, cls: 'text-gray-400 bg-gray-50' };
};

const getGreeting = () => {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 17) return 'Good afternoon';
  return 'Good evening';
};

const Dashboard = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [recentFiles, setRecentFiles] = useState([]);
  const [activities, setActivities] = useState([]);

  useEffect(() => {
    const isAdmin = user.role === 'ADMIN';
    (isAdmin ? dashboardAPI.getAdminStats() : dashboardAPI.getUserStats())
      .then(r => setStats(r.data))
      .catch(() => toast.error('Failed to load stats'))
      .finally(() => setLoading(false));
    fileAPI.getUserFiles()
      .then(r => setRecentFiles((r.data || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 8)))
      .catch(() => {});
    if (user.role === 'ADMIN') {
      adminAPI.getActivityTimeline()
        .then(r => setActivities((r.data?.recentActivities || []).slice(0, 6)))
        .catch(() => {});
    }
  }, []);

  const storagePercent = stats?.storageQuota
    ? Math.min(100, Math.round(((stats.storageUsed || 0) / stats.storageQuota) * 100)) : 0;

  const statCards = [
    { label: 'Total Files', value: stats?.totalFiles ?? '—', icon: FaFolder, bg: 'bg-blue-600', light: 'bg-blue-50 text-blue-600' },
    { label: 'Storage Used', value: fmt(stats?.storageUsed), icon: FaDatabase, bg: 'bg-indigo-600', light: 'bg-indigo-50 text-indigo-600' },
    { label: 'Uploads (7d)', value: stats?.recentUploads ?? '—', icon: FaUpload, bg: 'bg-emerald-600', light: 'bg-emerald-50 text-emerald-600' },
    { label: 'Downloads (7d)', value: stats?.recentDownloads ?? '—', icon: FaDownload, bg: 'bg-violet-600', light: 'bg-violet-50 text-violet-600' },
    ...(user.role === 'ADMIN' ? [{ label: 'Active Users', value: stats?.activeUsers ?? '—', icon: FaUsers, bg: 'bg-rose-600', light: 'bg-rose-50 text-rose-600' }] : []),
  ];

  const quickLinks = [
    { to: '/files', icon: FaUpload, label: 'Upload Files', desc: 'Add new files to storage', color: 'from-blue-600 to-blue-700' },
    { to: '/shared', icon: FaShareAlt, label: 'Shared With Me', desc: 'Files others shared with you', color: 'from-indigo-600 to-indigo-700' },
    { to: '/profile', icon: FaShieldAlt, label: 'Security Settings', desc: 'Manage your account & password', color: 'from-emerald-600 to-emerald-700' },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome banner */}
      <div className="rounded-2xl bg-gradient-to-r from-blue-700 to-blue-900 p-6 text-white shadow-lg">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold">{getGreeting()}, {user.fullName?.split(' ')[0] || 'User'} 👋</h1>
            <p className="text-blue-200 mt-1 text-sm">University of Dodoma — Secure Cloud Storage</p>
          </div>
          <Link to="/files" className="inline-flex items-center gap-2 bg-white text-blue-700 font-semibold text-sm px-5 py-2.5 rounded-xl hover:bg-blue-50 transition-colors self-start sm:self-auto flex-shrink-0">
            <FaUpload className="text-sm" /> Upload File
          </Link>
        </div>
        {/* Storage bar */}
        <div className="mt-5">
          <div className="flex justify-between text-sm text-blue-200 mb-1.5">
            <span>Storage</span>
            <span>{fmt(stats?.storageUsed || 0)} / {fmt(stats?.storageQuota || 5368709120)}</span>
          </div>
          <div className="h-2 bg-blue-600 rounded-full overflow-hidden">
            <div className={`h-full rounded-full transition-all duration-700 ${storagePercent > 80 ? 'bg-red-400' : storagePercent > 60 ? 'bg-yellow-300' : 'bg-green-400'}`}
              style={{ width: `${storagePercent}%` }} />
          </div>
          <p className="text-xs text-blue-300 mt-1">{storagePercent}% of storage used</p>
        </div>
      </div>

      {/* Stats */}
      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white rounded-2xl p-5 shadow-sm animate-pulse h-24" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-5 gap-4">
          {statCards.map(({ label, value, icon: Icon, bg, light }) => (
            <div key={label} className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex items-center gap-4">
              <div className={`h-11 w-11 rounded-xl ${light} flex items-center justify-center flex-shrink-0`}>
                <Icon className="text-lg" />
              </div>
              <div className="min-w-0">
                <p className="text-2xl font-bold text-gray-800 leading-tight">{value}</p>
                <p className="text-xs text-gray-500 mt-0.5 truncate">{label}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Quick links + Recent files */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Quick Actions */}
        <div className="space-y-3">
          <h2 className="text-base font-semibold text-gray-800">Quick Actions</h2>
          {quickLinks.map(({ to, icon: Icon, label, desc, color }) => (
            <Link key={to} to={to}
              className={`flex items-center gap-4 p-4 rounded-xl bg-gradient-to-r ${color} text-white shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all`}>
              <div className="h-10 w-10 bg-white bg-opacity-20 rounded-xl flex items-center justify-center flex-shrink-0">
                <Icon className="text-lg" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-sm">{label}</p>
                <p className="text-xs text-white text-opacity-80 truncate">{desc}</p>
              </div>
              <FaArrowRight className="text-xs text-white text-opacity-60 flex-shrink-0" />
            </Link>
          ))}
        </div>

        {/* Recent Files */}
        <div className="lg:col-span-2">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-semibold text-gray-800">Recent Files</h2>
            <Link to="/files" className="text-sm text-blue-600 hover:text-blue-700 font-medium">View all →</Link>
          </div>
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            {recentFiles.length > 0 ? (
              <div className="divide-y divide-gray-50">
                {recentFiles.map((file) => {
                  const { Icon, cls } = fileIcon(file.fileName);
                  return (
                    <div key={file.id} className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 transition-colors">
                      <div className={`h-9 w-9 rounded-lg ${cls} flex items-center justify-center flex-shrink-0`}>
                        <Icon className="text-sm" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-800 truncate">{file.fileName}</p>
                        <p className="text-xs text-gray-400">{fmt(file.fileSize)} · {new Date(file.createdAt).toLocaleDateString()}</p>
                      </div>
                      <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-md uppercase flex-shrink-0">
                        {file.fileName.split('.').pop()}
                      </span>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-12 text-center px-4">
                <div className="h-14 w-14 bg-blue-50 rounded-2xl flex items-center justify-center mb-3">
                  <FaFolder className="text-blue-400 text-xl" />
                </div>
                <p className="text-gray-700 font-medium">No files yet</p>
                <p className="text-sm text-gray-400 mt-1">Upload your first file to get started</p>
                <Link to="/files" className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700 transition-colors">
                  Upload File
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Activity Feed (Admin only) */}
      {user.role === 'ADMIN' && activities.length > 0 && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5">
          <div className="flex items-center gap-2 mb-4">
            <FaHistory className="text-blue-500" />
            <h2 className="text-base font-semibold text-gray-800">Recent Activity</h2>
          </div>
          <div className="space-y-3">
            {activities.map((a, i) => (
              <div key={a.id || i} className="flex items-start gap-3 text-sm">
                <div className="h-8 w-8 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
                  <FaClock className="text-blue-400 text-xs" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-gray-700">
                    <span className="font-medium">{a.username || 'System'}</span>
                    {' '}<span className="text-gray-500">{(a.action || '').replace(/_/g, ' ').toLowerCase()}</span>
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5 truncate">{a.details}</p>
                </div>
                <span className="text-xs text-gray-400 flex-shrink-0">
                  {a.createdAt ? new Date(a.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
