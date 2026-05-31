import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import {
  FaHome, FaFolder, FaUser, FaSignOutAlt, FaCog, FaShieldAlt,
  FaBars, FaTimes, FaUsers, FaTrash, FaCloud, FaBell, FaSearch,
  FaFileAlt, FaHdd, FaExclamationTriangle
} from 'react-icons/fa';
import StorageRequestModal from '../StorageRequestModal';
import { storageRequestAPI } from '../../services/api';

const Sidebar = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || '{}'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [showStorageModal, setShowStorageModal] = useState(false);

  useEffect(() => {
    const refresh = () => setUser(JSON.parse(localStorage.getItem('user') || '{}'));
    window.addEventListener('storage-updated', refresh);
    return () => window.removeEventListener('storage-updated', refresh);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    toast.success('Logged out successfully');
    navigate('/login');
  };

  const storageUsed = user.storageUsed || 0;
  const storageQuota = user.storageQuota || 5368709120;
  const storagePercent = Math.min(100, Math.round((storageUsed / storageQuota) * 100));
  const formatGB = (bytes) => (bytes / 1073741824).toFixed(1) + ' GB';

  const mainLinks = [
    { path: '/dashboard', icon: FaHome, label: 'Dashboard' },
    { path: '/files', icon: FaFolder, label: 'My Files' },
    { path: '/documents', icon: FaFileAlt, label: 'My Documents' },
    { path: '/shared', icon: FaUsers, label: 'Shared Files' },
    { path: '/trash', icon: FaTrash, label: 'Trash' },
    { path: '/storage-requests', icon: FaHdd, label: 'Storage Requests' },
    { path: '/profile', icon: FaUser, label: 'Profile' },
  ];

  const [pendingCount, setPendingCount] = useState(0);

  useEffect(() => {
    if (user.role === 'ADMIN') {
      storageRequestAPI.getPendingCount()
        .then(r => setPendingCount(r.data?.count || 0))
        .catch(() => {});
    }
  }, [user.role]);

  const adminLinks = user.role === 'ADMIN' ? [
    { path: '/admin', icon: FaCog, label: 'Admin Panel', badge: pendingCount > 0 ? pendingCount : null },
    { path: '/backups', icon: FaShieldAlt, label: 'Backups' },
  ] : [];

  const isActive = (path) => location.pathname === path;
  const initials = user.fullName ? user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : 'U';

  const SidebarContent = () => (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="flex items-center justify-between px-5 py-4 border-b border-blue-700">
        <Link to="/dashboard" className="flex items-center gap-3" onClick={() => setMobileOpen(false)}>
          <div className="h-9 w-9 bg-white rounded-xl flex items-center justify-center shadow-md flex-shrink-0">
            <FaCloud className="text-blue-700 text-lg" />
          </div>
          <div>
            <p className="text-white font-bold text-base leading-tight">UDOM Cloud</p>
            <p className="text-blue-200 text-xs">Secure Storage</p>
          </div>
        </Link>
        <button className="md:hidden text-blue-200 hover:text-white" onClick={() => setMobileOpen(false)}>
          <FaTimes />
        </button>
      </div>

      {/* User Card */}
      <div className="mx-3 mt-4 mb-2 p-3 bg-blue-700 rounded-xl">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-full bg-white flex items-center justify-center text-blue-700 font-bold text-sm flex-shrink-0">
            {initials}
          </div>
          <div className="overflow-hidden flex-1">
            <p className="text-white font-semibold text-sm truncate">{user.fullName || 'User'}</p>
            <p className="text-blue-200 text-xs truncate">{user.email || ''}</p>
          </div>
        </div>
        {/* Storage Bar */}
        <div className="mt-3">
          <div className="flex justify-between text-xs text-blue-200 mb-1">
            <span>Storage</span>
            <span>{formatGB(storageUsed)} / {formatGB(storageQuota)}</span>
          </div>
          <div className="h-1.5 bg-blue-600 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all ${storagePercent > 80 ? 'bg-red-400' : storagePercent > 60 ? 'bg-yellow-400' : 'bg-green-400'}`}
              style={{ width: `${storagePercent}%` }}
            />
          </div>
          <div className="flex justify-between items-center mt-1">
            <p className="text-blue-200 text-xs">{storagePercent}% used</p>
            {storagePercent >= 80 && (
              <button 
                onClick={() => setShowStorageModal(true)}
                className="text-xs bg-red-500/20 hover:bg-red-500/30 text-red-200 px-2 py-0.5 rounded flex items-center gap-1 transition-colors"
              >
                <FaHdd className="text-[10px]" />
                {storagePercent >= 95 ? 'Full!' : 'Request'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Nav Links */}
      <nav className="flex-1 overflow-y-auto px-3 py-2 space-y-0.5">
        <p className="text-blue-300 text-xs font-semibold uppercase px-3 py-2 mt-1">Menu</p>
        {mainLinks.map(({ path, icon: Icon, label }) => (
          <Link
            key={path}
            to={path}
            onClick={() => setMobileOpen(false)}
            className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
              isActive(path)
                ? 'bg-white text-blue-700 shadow-sm'
                : 'text-blue-100 hover:bg-blue-700 hover:text-white'
            }`}
          >
            <Icon className={`text-base flex-shrink-0 ${isActive(path) ? 'text-blue-600' : 'text-blue-300'}`} />
            {label}
          </Link>
        ))}
        {adminLinks.length > 0 && (
          <>
            <p className="text-blue-300 text-xs font-semibold uppercase px-3 py-2 mt-3">Administration</p>
            {adminLinks.map(({ path, icon: Icon, label, badge }) => (
              <Link
                key={path}
                to={path}
                onClick={() => setMobileOpen(false)}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                  isActive(path)
                    ? 'bg-white text-blue-700 shadow-sm'
                    : 'text-blue-100 hover:bg-blue-700 hover:text-white'
                }`}
              >
                <Icon className={`text-base flex-shrink-0 ${isActive(path) ? 'text-blue-600' : 'text-blue-300'}`} />
                {label}
                {badge && <span className="ml-auto bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full leading-none">{badge}</span>}
              </Link>
            ))}
          </>
        )}
      </nav>

      {/* Logout */}
      <div className="p-3 border-t border-blue-700">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-red-300 hover:bg-red-900 hover:text-red-200 transition-all"
        >
          <FaSignOutAlt className="text-base flex-shrink-0" />
          Sign Out
        </button>
      </div>
    </div>
  );

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-20 bg-black bg-opacity-50 md:hidden" onClick={() => setMobileOpen(false)} />
      )}

      {/* Sidebar — desktop always visible, mobile slide-in */}
      <aside className={`fixed inset-y-0 left-0 z-30 w-64 bg-blue-800 flex flex-col transition-transform duration-300 md:relative md:translate-x-0 ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <SidebarContent />
      </aside>

      {/* Main area */}
      <div className="flex-1 flex flex-col overflow-hidden min-w-0">
        {/* Top bar */}
        <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4 flex-shrink-0">
          <div className="flex items-center gap-3">
            <button onClick={() => setMobileOpen(true)} className="md:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100">
              <FaBars />
            </button>
            <div className="hidden sm:flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-1.5 w-56">
              <FaSearch className="text-gray-400 text-sm flex-shrink-0" />
              <input className="bg-transparent text-sm text-gray-600 outline-none w-full placeholder-gray-400" placeholder="Search files..." readOnly />
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 relative">
              <FaBell className="text-lg" />
            </button>
            <div className="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-xs font-bold">
              {initials}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>

      {/* Storage Request Modal */}
      <StorageRequestModal 
        isOpen={showStorageModal}
        onClose={() => setShowStorageModal(false)}
        currentQuotaGb={Math.round(storageQuota / (1024 * 1024 * 1024))}
        storageUsedGb={storageUsed / (1024 * 1024 * 1024)}
      />
    </div>
  );
};

export default Sidebar;
