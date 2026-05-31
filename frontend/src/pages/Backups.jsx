import React, { useState, useEffect } from 'react';
import { FaDatabase, FaUsers, FaClock, FaCheckCircle, FaExclamationTriangle, FaBolt, FaFileAlt, FaShieldAlt } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { adminAPI } from '../services/api';

const fmtGB = (b) => ((b || 0) / 1073741824).toFixed(2) + ' GB';

const Backups = () => {
  const [creating, setCreating] = useState(false);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [lastBackup, setLastBackup] = useState(null);

  useEffect(() => { fetchStatus(); }, []);

  const fetchStatus = async () => {
    try {
      const r = await adminAPI.getBackupStatus();
      setStatus(r.data);
    } catch { /* ignore */ }
    finally { setLoading(false); }
  };

  const handleCreateBackup = async () => {
    setCreating(true);
    try {
      const r = await adminAPI.createBackup();
      if (r.data?.status === 'SUCCESS') {
        toast.success('Backup created successfully');
        setLastBackup(r.data);
        fetchStatus();
      } else {
        toast.error(r.data?.message || 'Backup failed');
      }
    } catch { toast.error('Failed to create backup'); }
    finally { setCreating(false); }
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-800">System Backups</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage backups and restore system data</p>
        </div>
        <button onClick={handleCreateBackup} disabled={creating}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors self-start sm:self-auto">
          <FaBolt className="text-xs" /> {creating ? 'Creating...' : 'Create Backup Now'}
        </button>
      </div>

      {/* Stats */}
      {loading ? (
        <div className="flex justify-center py-8"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-4">
            <div className="h-11 w-11 rounded-xl bg-blue-50 flex items-center justify-center flex-shrink-0">
              <FaUsers className="text-blue-500 text-lg" />
            </div>
            <div>
              <p className="text-xs text-gray-500">Users in System</p>
              <p className="text-lg font-bold text-gray-800">{status?.userCount ?? '—'}</p>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-4">
            <div className="h-11 w-11 rounded-xl bg-green-50 flex items-center justify-center flex-shrink-0">
              <FaFileAlt className="text-green-500 text-lg" />
            </div>
            <div>
              <p className="text-xs text-gray-500">Audit Log Entries</p>
              <p className="text-lg font-bold text-gray-800">{status?.auditLogCount ?? '—'}</p>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-4">
            <div className="h-11 w-11 rounded-xl bg-indigo-50 flex items-center justify-center flex-shrink-0">
              <FaDatabase className="text-indigo-500 text-lg" />
            </div>
            <div>
              <p className="text-xs text-gray-500">Total Storage Used</p>
              <p className="text-lg font-bold text-gray-800">{fmtGB(status?.storageUsed)}</p>
            </div>
          </div>
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex items-center gap-4">
            <div className="h-11 w-11 rounded-xl bg-violet-50 flex items-center justify-center flex-shrink-0">
              <FaClock className="text-violet-500 text-lg" />
            </div>
            <div>
              <p className="text-xs text-gray-500">Backup Schedule</p>
              <p className="text-lg font-bold text-gray-800">Daily 02:00 AM</p>
            </div>
          </div>
        </div>
      )}

      {/* Last backup result */}
      {lastBackup && (
        <div className={`rounded-xl border p-4 flex items-center gap-3 ${lastBackup.status === 'SUCCESS' ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
          {lastBackup.status === 'SUCCESS' ? <FaCheckCircle className="text-green-600 flex-shrink-0" /> : <FaExclamationTriangle className="text-red-600 flex-shrink-0" />}
          <div>
            <p className={`text-sm font-semibold ${lastBackup.status === 'SUCCESS' ? 'text-green-800' : 'text-red-800'}`}>{lastBackup.message}</p>
            <p className="text-xs text-gray-500 mt-0.5">
              {lastBackup.userCount && `${lastBackup.userCount} users`}
              {lastBackup.auditLogCount && ` · ${lastBackup.auditLogCount} audit logs`}
              {lastBackup.backupDate && ` · ${new Date(lastBackup.backupDate).toLocaleString()}`}
            </p>
          </div>
        </div>
      )}

      {/* Backup History */}
      <BackupHistory key={lastBackup?.fileName} />
    </div>
  );
};

const fmtSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024, s = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + s[i];
};

const BackupHistory = () => {
  const [backups, setBackups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchHistory(); }, []);

  const fetchHistory = async () => {
    try {
      const r = await adminAPI.getBackupHistory();
      setBackups(r.data || []);
    } catch { /* ignore */ }
    finally { setLoading(false); }
  };

  const handleDownload = async (backup) => {
    try {
      const r = await adminAPI.downloadBackup(backup.id);
      const url = window.URL.createObjectURL(new Blob([r.data]));
      const a = document.createElement('a');
      a.href = url;
      a.setAttribute('download', backup.fileName);
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success('Download started');
    } catch { toast.error('Failed to download backup'); }
  };

  const handleDelete = async (backup) => {
    if (!window.confirm(`Delete backup "${backup.fileName}"? This cannot be undone.`)) return;
    try {
      await adminAPI.deleteBackup(backup.id);
      toast.success('Backup deleted');
      fetchHistory();
    } catch { toast.error('Failed to delete backup'); }
  };

  if (loading) return <div className="flex justify-center py-8"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>;

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-50">
        <h2 className="text-sm font-semibold text-gray-700">Backup History</h2>
        <span className="text-xs text-gray-400">{backups.length} backup(s)</span>
      </div>
      {backups.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center px-4">
          <div className="h-12 w-12 bg-gray-100 rounded-xl flex items-center justify-center mb-3">
            <FaDatabase className="text-gray-300 text-xl" />
          </div>
          <p className="text-gray-600 font-medium">No backups yet</p>
          <p className="text-sm text-gray-400 mt-1">Create your first backup using the button above</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-5 py-3 text-left font-semibold">File Name</th>
                <th className="px-5 py-3 text-left font-semibold hidden sm:table-cell">Type</th>
                <th className="px-5 py-3 text-left font-semibold hidden md:table-cell">Date</th>
                <th className="px-5 py-3 text-left font-semibold hidden md:table-cell">Size</th>
                <th className="px-5 py-3 text-left font-semibold hidden lg:table-cell">Contents</th>
                <th className="px-5 py-3 text-left font-semibold">Status</th>
                <th className="px-5 py-3 text-right font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {backups.map((b) => (
                <tr key={b.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3">
                    <div className="flex items-center gap-2">
                      <FaDatabase className="text-gray-300 text-xs flex-shrink-0" />
                      <span className="font-medium text-gray-800 truncate">{b.fileName}</span>
                    </div>
                  </td>
                  <td className="px-5 py-3 hidden sm:table-cell">
                    <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-purple-100 text-purple-700">{b.backupType}</span>
                  </td>
                  <td className="px-5 py-3 text-gray-500 text-xs hidden md:table-cell">{new Date(b.createdAt).toLocaleString()}</td>
                  <td className="px-5 py-3 text-gray-500 hidden md:table-cell">{fmtSize(b.fileSize)}</td>
                  <td className="px-5 py-3 text-gray-400 text-xs hidden lg:table-cell">
                    {b.userCount} users · {b.fileCount} files · {b.auditLogCount} logs
                  </td>
                  <td className="px-5 py-3">
                    {b.status === 'SUCCESS' ? (
                      <span className="inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded-full bg-green-100 text-green-700"><FaCheckCircle className="text-xs" /> Success</span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded-full bg-red-100 text-red-600"><FaExclamationTriangle className="text-xs" /> Failed</span>
                    )}
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex items-center justify-end gap-2">
                      {b.status === 'SUCCESS' && (
                        <button onClick={() => handleDownload(b)}
                          className="flex items-center gap-1 text-xs text-blue-600 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg font-medium transition-colors">
                          <FaBolt className="text-xs" /> Download
                        </button>
                      )}
                      <button onClick={() => handleDelete(b)}
                        className="flex items-center gap-1 text-xs text-red-600 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg font-medium transition-colors">
                        <FaExclamationTriangle className="text-xs" /> Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Backups;
