import React, { useState, useEffect } from 'react';
import { FaTimes, FaUserPlus, FaCheck, FaFile } from 'react-icons/fa';
import { userAPI, shareAPI } from '../services/api';
import { toast } from 'react-toastify';

const ShareFileModal = ({ file, onClose, onSuccess }) => {
  const [users, setUsers] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [permission, setPermission] = useState('DOWNLOAD');
  const [expiresInDays, setExpiresInDays] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    try {
      const response = await userAPI.getAllUsers();
      const currentUser = JSON.parse(localStorage.getItem('user'));
      setUsers((response.data || []).filter(u => u.id !== currentUser.id));
    } catch { toast.error('Failed to load users'); }
    finally { setLoadingUsers(false); }
  };

  const toggleUser = (id) => {
    setSelectedUsers(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const handleShare = async () => {
    if (selectedUsers.length === 0) { toast.error('Select at least one user'); return; }
    setLoading(true);
    try {
      await shareAPI.shareFile(file.id, {
        userIds: selectedUsers,
        permission,
        expiresInDays: expiresInDays ? parseInt(expiresInDays) : null,
      });
      toast.success(`File shared with ${selectedUsers.length} user(s)`);
      if (onSuccess) onSuccess();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to share file');
    } finally { setLoading(false); }
  };

  const filtered = users.filter(u =>
    !search || u.fullName?.toLowerCase().includes(search.toLowerCase()) || u.email?.toLowerCase().includes(search.toLowerCase())
  );

  const inputCls = "w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all";

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[85vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-gray-100 flex-shrink-0">
          <div>
            <h2 className="text-base font-bold text-gray-800">Share File</h2>
            <div className="flex items-center gap-2 mt-1">
              <FaFile className="text-blue-400 text-xs" />
              <p className="text-xs text-gray-500 truncate max-w-[250px]">{file.fileName || file.originalName}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors"><FaTimes /></button>
        </div>

        {/* Body */}
        <div className="p-5 space-y-4 overflow-y-auto flex-1">
          {/* Permission + Expiry */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Permission</label>
              <select value={permission} onChange={e => setPermission(e.target.value)} className={inputCls}>
                <option value="VIEW">View Only</option>
                <option value="DOWNLOAD">Download</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Expires (days)</label>
              <input type="number" value={expiresInDays} onChange={e => setExpiresInDays(e.target.value)} placeholder="Never" min="1" max="365" className={inputCls} />
            </div>
          </div>

          {/* User selection */}
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Share with {selectedUsers.length > 0 && <span className="text-blue-600">({selectedUsers.length} selected)</span>}
            </label>
            <input type="text" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search users..." className={`${inputCls} mb-2`} />
            <div className="border border-gray-200 rounded-xl max-h-48 overflow-y-auto">
              {loadingUsers ? (
                <div className="flex justify-center py-8"><div className="h-6 w-6 border-3 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>
              ) : filtered.length === 0 ? (
                <p className="text-center text-sm text-gray-400 py-6">No users found</p>
              ) : (
                <div className="divide-y divide-gray-50">
                  {filtered.map(u => {
                    const selected = selectedUsers.includes(u.id);
                    return (
                      <div key={u.id} onClick={() => toggleUser(u.id)}
                        className={`flex items-center justify-between px-3 py-2.5 cursor-pointer transition-colors ${selected ? 'bg-blue-50' : 'hover:bg-gray-50'}`}>
                        <div className="flex items-center gap-3 min-w-0">
                          <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                            <span className="text-blue-600 font-bold text-xs">{u.fullName?.charAt(0) || 'U'}</span>
                          </div>
                          <div className="min-w-0">
                            <p className="text-sm font-medium text-gray-800 truncate">{u.fullName}</p>
                            <p className="text-xs text-gray-400 truncate">{u.email}</p>
                          </div>
                        </div>
                        {selected && <FaCheck className="text-blue-600 text-sm flex-shrink-0" />}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-5 border-t border-gray-100 flex gap-3 flex-shrink-0">
          <button onClick={onClose} disabled={loading} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50 transition-colors">Cancel</button>
          <button onClick={handleShare} disabled={loading || selectedUsers.length === 0}
            className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-50 font-medium flex items-center justify-center gap-2 transition-colors">
            <FaUserPlus className="text-xs" /> {loading ? 'Sharing...' : 'Share'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShareFileModal;
