import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { FaUsers, FaServer, FaShieldAlt, FaUserPlus, FaTimes, FaEdit, FaTrash, FaToggleOn, FaToggleOff, FaSearch, FaCog, FaDatabase, FaFileAlt, FaHistory, FaDownload, FaCheckCircle, FaExclamationTriangle, FaPlus, FaArrowRight, FaUpload, FaFileCsv, FaCloud, FaHdd, FaKey } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { adminAPI, storageRequestAPI } from '../services/api';

const AdminPanel = () => {
  const [activeSection, setActiveSection] = useState('overview');

  const tabs = [
    { id: 'overview', label: 'Overview', icon: FaDatabase },
    { id: 'users', label: 'Users', icon: FaUsers },
    { id: 'storage', label: 'Storage Requests', icon: FaHdd },
    { id: 'system', label: 'System Health', icon: FaServer },
    { id: 'audit', label: 'Audit Logs', icon: FaHistory },
    { id: 'backup', label: 'Backups', icon: FaDatabase },
    { id: 'security', label: 'Security', icon: FaShieldAlt },
    { id: 'settings', label: 'Settings', icon: FaCog },
  ];

  return (
    <div className="space-y-5">
      {/* Page header */}
      <div>
        <h1 className="text-xl font-bold text-gray-800">Admin Panel</h1>
        <p className="text-sm text-gray-500 mt-0.5">Manage users, monitor system health, and configure settings</p>
      </div>

      {/* Tab navigation */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm">
        <div className="flex overflow-x-auto border-b border-gray-100 px-2">
          {tabs.map(({ id, label, icon: Icon }) => (
            <button key={id} onClick={() => setActiveSection(id)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 whitespace-nowrap transition-colors ${
                activeSection === id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              <Icon className="text-xs" /> {label}
            </button>
          ))}
        </div>

        <div className="p-5">
          {activeSection === 'overview' && <StatisticsOverview />}
          {activeSection === 'users' && <UserManagementSection />}
          {activeSection === 'storage' && <StorageRequestsSection />}
          {activeSection === 'system' && <SystemHealthSection />}
          {activeSection === 'audit' && <AuditLogsSection />}
          {activeSection === 'backup' && <BackupSection />}
          {activeSection === 'security' && <SecuritySection />}
          {activeSection === 'settings' && <SettingsSection />}
        </div>
      </div>
    </div>
  );
};

/* ────────────── Statistics Overview ────────────── */
const StatisticsOverview = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getStatistics()
      .then(r => setStats(r.data))
      .catch(() => toast.error('Failed to load statistics'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-12"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>;

  const fmtGB = (b) => ((b || 0) / 1073741824).toFixed(2) + ' GB';
  const pct = stats?.storageUsagePercentage || 0;

  const cards = [
    { label: 'Total Users', value: stats?.totalUsers ?? 0, icon: FaUsers, color: 'bg-blue-50 text-blue-600' },
    { label: 'Active Users', value: stats?.activeUsers ?? 0, icon: FaCheckCircle, color: 'bg-green-50 text-green-600' },
    { label: 'Inactive Users', value: stats?.inactiveUsers ?? 0, icon: FaExclamationTriangle, color: 'bg-amber-50 text-amber-600' },
    { label: 'Storage Used', value: fmtGB(stats?.totalStorageUsed), icon: FaDatabase, color: 'bg-indigo-50 text-indigo-600' },
  ];

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(({ label, value, icon: Icon, color }) => (
          <div key={label} className="bg-white border border-gray-100 rounded-xl p-4 flex items-center gap-3">
            <div className={`h-10 w-10 rounded-xl ${color} flex items-center justify-center flex-shrink-0`}><Icon className="text-base" /></div>
            <div>
              <p className="text-xl font-bold text-gray-800">{value}</p>
              <p className="text-xs text-gray-500">{label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Storage usage bar */}
      <div className="border border-gray-100 rounded-xl p-5">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-semibold text-gray-800">Total Storage Usage</p>
          <span className="text-sm text-gray-500">{fmtGB(stats?.totalStorageUsed)} / {fmtGB(stats?.totalStorageQuota)}</span>
        </div>
        <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
          <div className={`h-full rounded-full transition-all ${pct > 80 ? 'bg-red-500' : pct > 60 ? 'bg-yellow-500' : 'bg-blue-600'}`}
            style={{ width: `${Math.min(pct, 100)}%` }} />
        </div>
        <p className="text-xs text-gray-400 mt-1">{pct}% of total quota used</p>
      </div>

      {/* Users by role */}
      {stats?.usersByRole && (
        <div className="border border-gray-100 rounded-xl p-5">
          <p className="text-sm font-semibold text-gray-800 mb-3">Users by Role</p>
          <div className="flex gap-4">
            {Object.entries(stats.usersByRole).map(([role, count]) => (
              <div key={role} className="flex items-center gap-2">
                <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${role === 'ADMIN' ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700'}`}>{role}</span>
                <span className="text-sm font-bold text-gray-700">{count}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

/* ────────────── User Management ────────────── */
const UserManagementSection = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newUser, setNewUser] = useState({ firstName: '', lastName: '', email: '', role: 'STAFF', department: '' });
  const [creating, setCreating] = useState(false);
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [bulkFile, setBulkFile] = useState(null);
  const [bulkUploading, setBulkUploading] = useState(false);
  const [bulkResult, setBulkResult] = useState(null);

  //  sr2 System Integration states
  const [showHrModal, setShowHrModal] = useState(false);
  const [hrLoading, setHrLoading] = useState(false);
  const [hrStaffList, setHrStaffList] = useState([]);
  const [selectedStaff, setSelectedStaff] = useState(new Set());
  const [hrResult, setHrResult] = useState(null);
  const [hrFilters, setHrFilters] = useState({ department: '', college: '' });

  // Role editing state
  const [editingUserId, setEditingUserId] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);

  // Password reset state
  const [resetPasswordUser, setResetPasswordUser] = useState(null);
  const [resetResult, setResetResult] = useState(null);
  const [resetting, setResetting] = useState(false);
  const USERS_PER_PAGE = 20;

  // Storage assignment state
  const [storageModalUser, setStorageModalUser] = useState(null);
  const [storageQuotaGb, setStorageQuotaGb] = useState(5);

  // Edit user details state
  const [editDetailsUser, setEditDetailsUser] = useState(null);
  const [editDetailsForm, setEditDetailsForm] = useState({ firstName: '', lastName: '', department: '' });
  const [editDetailsSaving, setEditDetailsSaving] = useState(false);

  const fetchUsers = useCallback(async () => {
    try { const r = await adminAPI.getAllUsers(); setUsers(r.data || []); }
    catch { toast.error('Failed to load users'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { fetchUsers(); }, []);

  const handleToggleStatus = async (userId) => {
    try { await adminAPI.toggleUserStatus(userId); toast.success('Status updated'); fetchUsers(); }
    catch { toast.error('Failed to update status'); }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Permanently delete this user?')) return;
    try { await adminAPI.deleteUser(userId); toast.success('User deleted'); fetchUsers(); }
    catch { toast.error('Failed to delete user'); }
  };

  const handleUpdateRole = async (userId, newRole) => {
    try {
      await adminAPI.updateUserRole(userId, newRole);
      toast.success(`Role updated to ${newRole}`);
      setEditingUserId(null);
      fetchUsers();
    } catch {
      toast.error('Failed to update role');
    }
  };

  const handleUpdateStorage = async () => {
    if (!storageModalUser) return;
    try {
      await adminAPI.updateUserStorage(storageModalUser.id, storageQuotaGb);
      toast.success(`Storage updated to ${storageQuotaGb} GB`);
      setStorageModalUser(null);
      fetchUsers();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update storage');
    }
  };

  const openEditDetails = (u) => {
    setEditDetailsUser(u);
    setEditDetailsForm({ firstName: u.firstName || '', lastName: u.lastName || '', department: u.department || '' });
  };

  const handleUpdateUserDetails = async () => {
    if (!editDetailsForm.firstName.trim() || !editDetailsForm.lastName.trim()) {
      toast.error('First name and last name are required');
      return;
    }
    setEditDetailsSaving(true);
    try {
      await adminAPI.updateUserDetails(editDetailsUser.id, editDetailsForm);
      toast.success('User details updated successfully');
      setEditDetailsUser(null);
      fetchUsers();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update user details');
    } finally {
      setEditDetailsSaving(false);
    }
  };

  const handleCreateUser = async () => {
    if (!newUser.firstName.trim() || !newUser.lastName.trim() || !newUser.email.trim()) { toast.error('First name, last name and email required'); return; }
    setCreating(true);
    try {
      await adminAPI.createUser(newUser);
      toast.success('User created');
      setShowCreateModal(false);
      setNewUser({ firstName: '', lastName: '', email: '', role: 'STAFF', department: '' });
      fetchUsers();
    } catch (e) { toast.error(typeof e.response?.data?.message === 'string' ? e.response.data.message : 'Failed to create user'); }
    finally { setCreating(false); }
  };

  const handleBulkUpload = async () => {
    if (!bulkFile) { toast.error('Please select a CSV file'); return; }
    setBulkUploading(true);
    setBulkResult(null);
    try {
      const formData = new FormData();
      formData.append('file', bulkFile);
      const r = await adminAPI.bulkUploadUsers(formData);
      setBulkResult(r.data);
      const successCount = r.data?.successUsers?.length || 0;
      const errorCount = r.data?.errors?.length || 0;
      if (successCount > 0) toast.success(`Created ${successCount} user(s)`);
      if (errorCount > 0) toast.warn(`${errorCount} row(s) had errors`);
      fetchUsers();
    } catch (e) {
      toast.error(typeof e.response?.data?.message === 'string' ? e.response.data.message : 'Bulk upload failed');
    } finally { setBulkUploading(false); }
  };

  const downloadCsvTemplate = () => {
    const csv = 'email,firstname,lastname,role,department\njane@udom.ac.tz,Jane,Doe,STAFF,IT\njohn@udom.ac.tz,John,Smith,ADMIN,Finance\n';
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = 'users-template.csv';
    document.body.appendChild(a); a.click(); a.remove();
    window.URL.revokeObjectURL(url);
  };

  // HR System Integration handlers
  const handleFetchFromHr = async () => {
    setHrLoading(true);
    setHrResult(null);
    try {
      const params = {};
      if (hrFilters.department) params.department = hrFilters.department;
      if (hrFilters.college) params.college = hrFilters.college;

      const r = await adminAPI.fetchStaffFromExternalApi(params);
      const staff = r.data?.staff || [];
      setHrStaffList(staff);
      setSelectedStaff(new Set(staff.map((s, i) => i))); // Select all by default
      toast.success(`Fetched ${staff.length} staff from HR system`);
    } catch (e) {
      toast.error('Failed to fetch from HR system');
    } finally {
      setHrLoading(false);
    }
  };

  const handleRegisterFromHr = async () => {
    if (selectedStaff.size === 0) { toast.error('Please select at least one staff member'); return; }
    setHrLoading(true);
    setHrResult(null);
    try {
      const selected = Array.from(selectedStaff).map(i => hrStaffList[i]);
      const r = await adminAPI.registerUsersFromExternalApi(selected);
      setHrResult(r.data);
      const successCount = r.data?.successCount || 0;
      const errorCount = r.data?.errorCount || 0;
      if (successCount > 0) toast.success(`Registered ${successCount} user(s) from HR system`);
      if (errorCount > 0) toast.warn(`${errorCount} user(s) failed to register`);
      fetchUsers();
    } catch (e) {
      toast.error('Registration from HR system failed');
    } finally {
      setHrLoading(false);
    }
  };

  const toggleStaffSelection = (index) => {
    const newSelected = new Set(selectedStaff);
    if (newSelected.has(index)) newSelected.delete(index);
    else newSelected.add(index);
    setSelectedStaff(newSelected);
  };

  const selectAllStaff = () => setSelectedStaff(new Set(hrStaffList.map((_, i) => i)));
  const deselectAllStaff = () => setSelectedStaff(new Set());

  // Password reset handler — shows temp password in a copy-able dialog (Issue 4b fix)
  const handleResetPassword = async (user) => {
    setResetPasswordUser(user);
    setResetResult(null);
    setResetting(true);
    try {
      const r = await adminAPI.resetUserPassword(user.id);
      setResetResult(r.data);
      // Don't close the modal — admin must see and copy the temp password
      toast.success(`Password reset for ${user.firstName} ${user.lastName} — copy the password below`);
    } catch (e) {
      toast.error('Failed to reset password');
      setResetPasswordUser(null);
    } finally {
      setResetting(false);
    }
  };

  const filtered = users.filter(u => {
    if (roleFilter && u.role !== roleFilter) return false;
    const fullName = (u.firstName + ' ' + u.lastName).toLowerCase();
    if (search && !fullName.includes(search.toLowerCase()) && !u.email?.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const totalPages = Math.ceil(filtered.length / USERS_PER_PAGE);
  const paginatedUsers = filtered.slice(currentPage * USERS_PER_PAGE, (currentPage + 1) * USERS_PER_PAGE);

  // Reset page when filters change
  useEffect(() => { setCurrentPage(0); }, [search, roleFilter]);

  const fmtGB = (b) => (b / 1073741824).toFixed(2) + ' GB';

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="relative">
            <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs" />
            <input type="text" placeholder="Search users..." value={search} onChange={e => setSearch(e.target.value)}
              className="pl-8 pr-3 py-2 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 w-48" />
          </div>
          <select value={roleFilter} onChange={e => setRoleFilter(e.target.value)}
            className="text-sm border border-gray-200 rounded-xl px-3 py-2 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Roles</option>
            <option value="ADMIN">Admin</option>
            <option value="STAFF">Staff</option>
          </select>
        </div>
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button onClick={() => { setShowHrModal(true); setHrStaffList([]); setSelectedStaff(new Set()); setHrResult(null); setHrFilters({ department: '', college: '' }); }}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-green-200 text-green-600 text-sm font-medium rounded-xl hover:bg-green-50 transition-colors"
            title="Fetch staff from HR system">
            <FaCloud className="text-xs" /> From HR System
          </button>
          <button onClick={() => { setShowBulkModal(true); setBulkResult(null); setBulkFile(null); }}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-blue-200 text-blue-600 text-sm font-medium rounded-xl hover:bg-blue-50 transition-colors">
            <FaUpload className="text-xs" /> Bulk Upload
          </button>
          <button onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-xl hover:bg-blue-700 transition-colors">
            <FaUserPlus className="text-xs" /> Add User
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-12"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12">
          <div className="h-12 w-12 bg-gray-100 rounded-xl flex items-center justify-center mx-auto mb-3"><FaUsers className="text-gray-300 text-xl" /></div>
          <p className="text-gray-600 font-medium">No users found</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-100">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-4 py-3 text-left font-semibold">User</th>
                <th className="px-4 py-3 text-left font-semibold hidden sm:table-cell">Role</th>
                <th className="px-4 py-3 text-left font-semibold hidden md:table-cell">Department</th>
                <th className="px-4 py-3 text-left font-semibold hidden lg:table-cell">Storage</th>
                <th className="px-4 py-3 text-left font-semibold">Status</th>
                <th className="px-4 py-3 text-right font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {paginatedUsers.map((u) => (
                <tr key={u.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                        <span className="text-blue-600 font-bold text-xs">{(u.firstName?.[0] || '') + (u.lastName?.[0] || '')}</span>
                      </div>
                      <div className="min-w-0">
                        <p className="font-medium text-gray-800 truncate">{u.firstName} {u.lastName}</p>
                        <p className="text-xs text-gray-400 truncate">{u.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 hidden sm:table-cell">
                    {editingUserId === u.id ? (
                      <div className="flex items-center gap-2">
                        <select
                          value={u.role}
                          onChange={(e) => handleUpdateRole(u.id, e.target.value)}
                          onBlur={() => setEditingUserId(null)}
                          autoFocus
                          className="text-xs border border-gray-200 rounded-lg px-2 py-1 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                          <option value="STAFF">STAFF</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        <button onClick={() => setEditingUserId(null)} className="text-gray-400 hover:text-gray-600">
                          <FaTimes className="text-xs" />
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => setEditingUserId(u.id)}
                        className={`text-xs font-semibold px-2 py-0.5 rounded-full hover:opacity-80 transition-opacity flex items-center gap-1 ${u.role === 'ADMIN' ? 'bg-red-100 text-red-700' : 'bg-blue-100 text-blue-700'}`}
                        title="Click to change role"
                      >
                        {u.role}
                        <FaEdit className="text-[10px]" />
                      </button>
                    )}
                  </td>
                  <td className="px-4 py-3 text-gray-500 hidden md:table-cell">{u.department || '—'}</td>
                  <td className="px-4 py-3 text-gray-500 text-xs hidden lg:table-cell">{fmtGB(u.storageUsed || 0)}</td>
                  <td className="px-4 py-3">
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${u.isActive ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'}`}>
                      {u.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <button aria-label="Edit user details" onClick={() => openEditDetails(u)} className="p-1.5 rounded-lg text-indigo-400 hover:bg-indigo-50 transition-colors" title="Edit Details">
                        <FaEdit className="text-xs" />
                      </button>
                      <button aria-label={u.isActive ? 'Deactivate user' : 'Activate user'} onClick={() => handleToggleStatus(u.id)} className="p-1.5 rounded-lg hover:bg-gray-100 transition-colors" title={u.isActive ? 'Deactivate' : 'Activate'}>
                        {u.isActive ? <FaToggleOn className="text-green-500" /> : <FaToggleOff className="text-gray-400" />}
                      </button>
                      <button aria-label="Reset user password" onClick={() => handleResetPassword(u)} className="p-1.5 rounded-lg text-amber-400 hover:bg-amber-50 transition-colors" title="Reset Password">
                        <FaKey className="text-xs" />
                      </button>
                      <button aria-label="Assign storage quota" onClick={() => { setStorageModalUser(u); setStorageQuotaGb(Math.round((u.storageQuota || 5368709120) / 1073741824)); }} className="p-1.5 rounded-lg text-blue-400 hover:bg-blue-50 transition-colors" title="Assign Storage">
                        <FaHdd className="text-xs" />
                      </button>
                      <button aria-label="Delete user" onClick={() => handleDeleteUser(u.id)} className="p-1.5 rounded-lg text-red-400 hover:bg-red-50 transition-colors" title="Delete">
                        <FaTrash className="text-xs" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t border-gray-100 bg-gray-50">
              <p className="text-xs text-gray-500">Showing {currentPage * USERS_PER_PAGE + 1}–{Math.min((currentPage + 1) * USERS_PER_PAGE, filtered.length)} of {filtered.length}</p>
              <div className="flex gap-1">
                <button onClick={() => setCurrentPage(p => Math.max(0, p - 1))} disabled={currentPage === 0}
                  className="px-3 py-1 text-xs font-medium rounded-lg border border-gray-200 hover:bg-white disabled:opacity-40">Prev</button>
                {[...Array(totalPages)].map((_, i) => (
                  <button key={i} onClick={() => setCurrentPage(i)}
                    className={`px-3 py-1 text-xs font-medium rounded-lg ${currentPage === i ? 'bg-blue-600 text-white' : 'border border-gray-200 hover:bg-white'}`}>{i + 1}</button>
                ))}
                <button onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))} disabled={currentPage === totalPages - 1}
                  className="px-3 py-1 text-xs font-medium rounded-lg border border-gray-200 hover:bg-white disabled:opacity-40">Next</button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Create User Modal */}
      {showCreateModal && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Add New User</h2>
              <button onClick={() => setShowCreateModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">First Name</label>
                  <input type="text" value={newUser.firstName} onChange={e => setNewUser({...newUser, firstName: e.target.value})} placeholder="e.g. John"
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Last Name</label>
                  <input type="text" value={newUser.lastName} onChange={e => setNewUser({...newUser, lastName: e.target.value})} placeholder="e.g. Doe"
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Email</label>
                <input type="email" value={newUser.email} onChange={e => setNewUser({...newUser, email: e.target.value})} placeholder="name@udom.ac.tz"
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Role</label>
                  <select value={newUser.role} onChange={e => setNewUser({...newUser, role: e.target.value})}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="STAFF">Staff</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Department</label>
                  <input type="text" value={newUser.department} onChange={e => setNewUser({...newUser, department: e.target.value})} placeholder="Optional"
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>
              </div>
              <div className="flex gap-3 pt-1">
                <button onClick={() => setShowCreateModal(false)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleCreateUser} disabled={creating}
                  className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-50 font-medium">
                  {creating ? 'Creating...' : 'Create User'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Storage Assignment Modal */}
      {storageModalUser && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Assign Storage</h2>
              <button onClick={() => setStorageModalUser(null)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <p className="text-sm text-gray-600">Set storage quota for <span className="font-medium text-gray-800">{storageModalUser.firstName} {storageModalUser.lastName}</span></p>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1.5">Storage Quota (GB)</label>
                <input
                  type="number"
                  min="1"
                  max="1000"
                  value={storageQuotaGb}
                  onChange={e => setStorageQuotaGb(Number(e.target.value))}
                  className="w-full px-3 py-2.5 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-400 mt-1">Current: {Math.round((storageModalUser.storageQuota || 0) / 1073741824)} GB</p>
              </div>
              <div className="flex gap-3">
                <button onClick={() => setStorageModalUser(null)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleUpdateStorage} className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 font-medium">Update</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit User Details Modal */}
      {editDetailsUser && (
        <div role="dialog" aria-modal="true" aria-labelledby="edit-user-title" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <div>
                <h2 id="edit-user-title" className="text-base font-bold text-gray-800">Edit User Details</h2>
                <p className="text-xs text-gray-500 mt-0.5">{editDetailsUser.email}</p>
              </div>
              <button aria-label="Close dialog" onClick={() => setEditDetailsUser(null)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label htmlFor="edit-firstName" className="block text-xs font-medium text-gray-600 mb-1">First Name <span className="text-red-500">*</span></label>
                  <input
                    id="edit-firstName"
                    type="text"
                    value={editDetailsForm.firstName}
                    onChange={e => setEditDetailsForm({ ...editDetailsForm, firstName: e.target.value })}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="First name"
                  />
                </div>
                <div>
                  <label htmlFor="edit-lastName" className="block text-xs font-medium text-gray-600 mb-1">Last Name <span className="text-red-500">*</span></label>
                  <input
                    id="edit-lastName"
                    type="text"
                    value={editDetailsForm.lastName}
                    onChange={e => setEditDetailsForm({ ...editDetailsForm, lastName: e.target.value })}
                    className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="Last name"
                  />
                </div>
              </div>
              <div>
                <label htmlFor="edit-department" className="block text-xs font-medium text-gray-600 mb-1">Department</label>
                <input
                  id="edit-department"
                  type="text"
                  value={editDetailsForm.department}
                  onChange={e => setEditDetailsForm({ ...editDetailsForm, department: e.target.value })}
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="e.g. Computer Science"
                />
              </div>
              <div className="bg-blue-50 border border-blue-100 rounded-xl p-3">
                <p className="text-xs text-blue-600">Changes will be reflected immediately in the user's profile and sidebar.</p>
              </div>
              <div className="flex gap-3 pt-1">
                <button onClick={() => setEditDetailsUser(null)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button
                  onClick={handleUpdateUserDetails}
                  disabled={editDetailsSaving}
                  className="flex-1 bg-indigo-600 text-white text-sm py-2.5 rounded-xl hover:bg-indigo-700 disabled:opacity-50 font-medium flex items-center justify-center gap-2"
                >
                  {editDetailsSaving ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Bulk Upload Modal */}
      {showBulkModal && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] flex flex-col">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <div>
                <h2 className="text-base font-bold text-gray-800">Bulk Upload Users</h2>
                <p className="text-xs text-gray-500 mt-0.5">Upload a CSV to create multiple users at once</p>
              </div>
              <button onClick={() => setShowBulkModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4 overflow-y-auto">
              <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 text-xs text-blue-700">
                <p className="font-semibold mb-1">CSV Format:</p>
                <code className="block bg-white px-2 py-1 rounded text-[11px] mb-2">email,firstname,lastname,role,department</code>
                <p>• <strong>email</strong>, <strong>firstname</strong>, <strong>lastname</strong>, <strong>role</strong> are required (role: ADMIN or STAFF)</p>
                <p>• <strong>department</strong> is optional</p>
                <p>• Default password: user's <strong>last name in UPPERCASE</strong> (e.g. "Jane Doe" → password: DOE)</p>
                <p>• Users must change password on first login</p>
                <button onClick={downloadCsvTemplate} className="mt-2 text-blue-600 hover:underline font-medium flex items-center gap-1">
                  <FaDownload className="text-[10px]" /> Download template
                </button>
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">CSV File</label>
                <label className="flex flex-col items-center justify-center border-2 border-dashed border-gray-200 rounded-xl p-6 cursor-pointer hover:bg-gray-50 transition-colors">
                  <FaFileCsv className="text-3xl text-blue-400 mb-2" />
                  <span className="text-sm font-medium text-gray-700">{bulkFile ? bulkFile.name : 'Click to choose CSV file'}</span>
                  {bulkFile && <span className="text-xs text-gray-400 mt-1">{(bulkFile.size / 1024).toFixed(1)} KB</span>}
                  <input type="file" accept=".csv" className="hidden" onChange={e => setBulkFile(e.target.files?.[0] || null)} />
                </label>
              </div>

              {bulkResult && (
                <div className="space-y-2">
                  {bulkResult.successUsers?.length > 0 && (
                    <div className="bg-green-50 border border-green-100 rounded-xl p-3">
                      <p className="text-xs font-semibold text-green-700 flex items-center gap-1"><FaCheckCircle /> Created {bulkResult.successUsers.length} user(s):</p>
                      <ul className="text-xs text-green-700 mt-1 max-h-24 overflow-y-auto">
                        {bulkResult.successUsers.map((u, i) => <li key={i}>• {u}</li>)}
                      </ul>
                    </div>
                  )}
                  {bulkResult.errors?.length > 0 && (
                    <div className="bg-red-50 border border-red-100 rounded-xl p-3">
                      <p className="text-xs font-semibold text-red-700 flex items-center gap-1"><FaExclamationTriangle /> {bulkResult.errors.length} error(s):</p>
                      <ul className="text-xs text-red-700 mt-1 max-h-32 overflow-y-auto space-y-0.5">
                        {bulkResult.errors.map((e, i) => <li key={i}>• Line {e.line} ({e.email}): {e.error}</li>)}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
            <div className="p-5 border-t border-gray-100 flex gap-3">
              <button onClick={() => setShowBulkModal(false)} disabled={bulkUploading}
                className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50 disabled:opacity-50">Close</button>
              <button onClick={handleBulkUpload} disabled={bulkUploading || !bulkFile}
                className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-50 font-medium flex items-center justify-center gap-2">
                <FaUpload className="text-xs" /> {bulkUploading ? 'Uploading...' : 'Upload CSV'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* HR System Integration Modal */}
      {showHrModal && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] flex flex-col">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <div>
                <h2 className="text-base font-bold text-gray-800">Register Users from HR System</h2>
                <p className="text-xs text-gray-500 mt-0.5">Fetch staff information from external HR system and create user accounts</p>
              </div>
              <button onClick={() => setShowHrModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="flex-1 overflow-y-auto p-5 space-y-4">
              {/* Filters */}
              <div className="flex gap-3">
                <input type="text" placeholder="Department (optional)" value={hrFilters.department} onChange={e => setHrFilters({...hrFilters, department: e.target.value})}
                  className="flex-1 text-sm border border-gray-200 rounded-xl px-3 py-2 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <input type="text" placeholder="College (optional)" value={hrFilters.college} onChange={e => setHrFilters({...hrFilters, college: e.target.value})}
                  className="flex-1 text-sm border border-gray-200 rounded-xl px-3 py-2 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <button onClick={handleFetchFromHr} disabled={hrLoading}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-xl hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2">
                  <FaCloud className="text-xs" /> {hrLoading ? 'Fetching...' : 'Fetch'}
                </button>
              </div>

              {/* Note */}
              <div className="bg-amber-50 border border-amber-100 rounded-xl p-3 text-xs text-amber-700">
                <strong>Note:</strong> This currently shows mock data. When HR API credentials are configured, this will fetch real staff data. Passwords will be generated from last names in uppercase.
              </div>

              {/* Staff List */}
              {hrStaffList.length > 0 && (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-gray-700">{hrStaffList.length} staff member(s) found</p>
                    <div className="flex gap-2">
                      <button onClick={selectAllStaff} className="text-xs text-blue-600 hover:underline">Select All</button>
                      <span className="text-gray-300">|</span>
                      <button onClick={deselectAllStaff} className="text-xs text-blue-600 hover:underline">Deselect All</button>
                    </div>
                  </div>
                  <div className="border border-gray-100 rounded-xl overflow-hidden max-h-64 overflow-y-auto">
                    <table className="w-full text-sm">
                      <thead className="bg-gray-50 text-xs text-gray-500">
                        <tr>
                          <th className="px-3 py-2 text-left w-10"><input type="checkbox" checked={selectedStaff.size === hrStaffList.length && hrStaffList.length > 0} onChange={e => e.target.checked ? selectAllStaff() : deselectAllStaff()} className="rounded" /></th>
                          <th className="px-3 py-2 text-left">Staff</th>
                          <th className="px-3 py-2 text-left hidden sm:table-cell">Department</th>
                          <th className="px-3 py-2 text-left hidden md:table-cell">College</th>
                          <th className="px-3 py-2 text-left">Phone</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-50">
                        {hrStaffList.map((staff, index) => (
                          <tr key={index} className={`hover:bg-gray-50 ${selectedStaff.has(index) ? 'bg-blue-50' : ''}`}>
                            <td className="px-3 py-2"><input type="checkbox" checked={selectedStaff.has(index)} onChange={() => toggleStaffSelection(index)} className="rounded" /></td>
                            <td className="px-3 py-2">
                              <div className="font-medium text-gray-800">{staff.firstName} {staff.lastName}</div>
                              <div className="text-xs text-gray-500">{staff.email}</div>
                            </td>
                            <td className="px-3 py-2 text-gray-600 hidden sm:table-cell">{staff.department || '-'}</td>
                            <td className="px-3 py-2 text-gray-600 hidden md:table-cell">{staff.college || '-'}</td>
                            <td className="px-3 py-2 text-gray-600 text-xs">{staff.phoneNumber || '-'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* Results */}
              {hrResult && (
                <div className="space-y-2">
                  <div className={`p-3 rounded-xl text-sm ${hrResult.successCount > 0 ? 'bg-green-50 border border-green-100 text-green-700' : 'bg-red-50 border border-red-100 text-red-700'}`}>
                    <p className="font-semibold flex items-center gap-2">
                      {hrResult.successCount > 0 ? <FaCheckCircle /> : <FaExclamationTriangle />}
                      {hrResult.message}
                    </p>
                  </div>
                  {hrResult.errors?.length > 0 && (
                    <div className="bg-red-50 border border-red-100 rounded-xl p-3">
                      <p className="text-xs font-semibold text-red-700 flex items-center gap-1"><FaExclamationTriangle /> {hrResult.errors.length} error(s):</p>
                      <ul className="text-xs text-red-700 mt-1 max-h-24 overflow-y-auto space-y-0.5">
                        {hrResult.errors.map((e, i) => <li key={i}>• {e.email}: {e.error}</li>)}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
            <div className="p-5 border-t border-gray-100 flex gap-3">
              <button onClick={() => setShowHrModal(false)} disabled={hrLoading}
                className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50 disabled:opacity-50">Close</button>
              <button onClick={handleRegisterFromHr} disabled={hrLoading || selectedStaff.size === 0 || hrStaffList.length === 0}
                className="flex-1 bg-green-600 text-white text-sm py-2.5 rounded-xl hover:bg-green-700 disabled:opacity-50 font-medium flex items-center justify-center gap-2">
                <FaUserPlus className="text-xs" /> {hrLoading ? 'Registering...' : `Register ${selectedStaff.size} User(s)`}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Password Reset Result Modal */}
      {resetResult && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Password Reset Successful</h2>
              <button onClick={() => { setResetResult(null); setResetPasswordUser(null); }} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
                <p className="text-sm text-amber-800">
                  <span className="font-medium">{resetResult.firstName} {resetResult.lastName}</span>'s password has been reset.
                </p>
              </div>

              <div className="bg-gray-50 rounded-xl p-4 space-y-3">
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1">Username</p>
                  <p className="text-sm font-medium text-gray-800">{resetResult.username}</p>
                </div>
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1">Temporary Password</p>
                  <div className="flex items-center gap-2">
                    <code className="flex-1 bg-white border border-gray-200 rounded-lg px-3 py-2 text-sm font-mono text-gray-800">{resetResult.tempPassword}</code>
                    <button
                      onClick={() => {
                        navigator.clipboard.writeText(resetResult.tempPassword);
                        toast.success('Password copied to clipboard');
                      }}
                      className="px-3 py-2 bg-blue-600 text-white text-xs font-medium rounded-lg hover:bg-blue-700 transition-colors"
                    >
                      Copy
                    </button>
                  </div>
                </div>
                <div className="flex items-start gap-2">
                  <FaExclamationTriangle className="text-amber-500 text-xs mt-0.5" />
                  <p className="text-xs text-gray-600">
                    User must change this password on next login. 2FA has been disabled if it was enabled.
                  </p>
                </div>
              </div>

              <div className="flex gap-3 pt-1">
                <button onClick={() => { setResetResult(null); setResetPasswordUser(null); }}
                  className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 font-medium">
                  Done
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

/* ────────────── System Health ────────────── */
const SystemHealthSection = () => {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminAPI.getSystemHealth()
      .then(r => setHealth(r.data))
      .catch(() => toast.error('Failed to load system health'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex justify-center py-12"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>;

  const dbStatus = typeof health?.database === 'object' ? health.database.status : (health?.database || 'Unknown');
  const overallStatus = health?.overallStatus || 'Unknown';

  const services = [
    { name: 'Application Server', desc: 'Spring Boot API', status: overallStatus === 'HEALTHY' ? 'UP' : overallStatus, icon: FaServer, color: 'bg-green-50 text-green-600' },
    { name: 'Database', desc: 'PostgreSQL', status: dbStatus, icon: FaDatabase, color: 'bg-blue-50 text-blue-600' },
    { name: 'Object Storage', desc: 'MinIO', status: 'Available', icon: FaFileAlt, color: 'bg-purple-50 text-purple-600' },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {services.map(({ name, desc, status, icon: Icon, color }) => (
        <div key={name} className="border border-gray-100 rounded-xl p-4">
          <div className="flex items-center gap-3 mb-3">
            <div className={`h-10 w-10 rounded-xl ${color} flex items-center justify-center`}><Icon /></div>
            <div>
              <p className="text-sm font-semibold text-gray-800">{name}</p>
              <p className="text-xs text-gray-400">{desc}</p>
            </div>
          </div>
          <span className={`inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full ${status === 'UP' || status === 'Available' || status === 'HEALTHY' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'}`}>
            {status === 'UP' || status === 'Available' || status === 'HEALTHY' ? <FaCheckCircle className="text-xs" /> : <FaExclamationTriangle className="text-xs" />} {status}
          </span>
        </div>
      ))}
    </div>
  );
};

/* ────────────── Audit Logs ────────────── */
const AuditLogsSection = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionFilter, setActionFilter] = useState('');

  useEffect(() => { fetchLogs(); }, [actionFilter]);

  const fetchLogs = async () => {
    setLoading(true);
    try { const r = await adminAPI.getAuditLogs(0, 50, actionFilter); setLogs(r.data.content || []); }
    catch { toast.error('Failed to load audit logs'); }
    finally { setLoading(false); }
  };

  return (
    <div className="space-y-4">
      <select value={actionFilter} onChange={e => setActionFilter(e.target.value)}
        className="text-sm border border-gray-200 rounded-xl px-3 py-2 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500">
        <option value="">All Actions</option>
        <option value="FILE_UPLOAD">File Uploads</option>
        <option value="FILE_DOWNLOAD">File Downloads</option>
        <option value="FILE_DELETE">File Deletions</option>
        <option value="USER_LOGIN">User Logins</option>
      </select>

      {loading ? (
        <div className="flex justify-center py-12"><div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" /></div>
      ) : logs.length === 0 ? (
        <div className="text-center py-12">
          <div className="h-12 w-12 bg-gray-100 rounded-xl flex items-center justify-center mx-auto mb-3"><FaHistory className="text-gray-300 text-xl" /></div>
          <p className="text-gray-600 font-medium">No audit logs found</p>
          <p className="text-sm text-gray-400 mt-1">Activity will appear here as users interact with the system</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-100">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider">
                <th className="px-4 py-3 text-left font-semibold">Timestamp</th>
                <th className="px-4 py-3 text-left font-semibold">User</th>
                <th className="px-4 py-3 text-left font-semibold">Action</th>
                <th className="px-4 py-3 text-left font-semibold hidden md:table-cell">Details</th>
                <th className="px-4 py-3 text-left font-semibold hidden lg:table-cell">IP</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {logs.map(log => (
                <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-xs text-gray-500">{new Date(log.createdAt).toLocaleString()}</td>
                  <td className="px-4 py-3 text-gray-700">{log.username || 'System'}</td>
                  <td className="px-4 py-3">
                    <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-blue-700">{log.action}</span>
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs hidden md:table-cell truncate max-w-[200px]">{log.details || '—'}</td>
                  <td className="px-4 py-3 text-gray-400 text-xs hidden lg:table-cell">{log.ipAddress || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

/* ────────────── Backup ────────────── */
const BackupSection = () => {
  const [creating, setCreating] = useState(false);
  const [status, setStatus] = useState(null);

  useEffect(() => {
    adminAPI.getBackupStatus()
      .then(r => setStatus(r.data))
      .catch(() => {});
  }, []);

  const handleCreateBackup = async () => {
    setCreating(true);
    try {
      const r = await adminAPI.createBackup();
      if (r.data?.status === 'SUCCESS') toast.success('Backup created');
      else toast.error(r.data?.message || 'Backup failed');
      adminAPI.getBackupStatus().then(r2 => setStatus(r2.data)).catch(() => {});
    }
    catch { toast.error('Failed to create backup'); }
    finally { setCreating(false); }
  };

  const fmtSize = (b) => { if (!b) return '0 B'; const k=1024,s=['B','KB','MB','GB']; const i=Math.floor(Math.log(b)/Math.log(k)); return (b/Math.pow(k,i)).toFixed(1)+' '+s[i]; };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-semibold text-gray-800">Backup & Recovery</p>
          <p className="text-xs text-gray-500 mt-0.5">Create and manage system backups</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={handleCreateBackup} disabled={creating}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors">
            <FaDownload className="text-xs" /> {creating ? 'Creating...' : 'Create Backup'}
          </button>
          <Link to="/backups"
            className="flex items-center gap-1 px-3 py-2 border border-gray-200 text-gray-700 text-sm font-medium rounded-xl hover:bg-gray-50 transition-colors">
            Manage <FaArrowRight className="text-xs" />
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="border border-gray-100 rounded-xl p-4">
          <p className="text-xs text-gray-500">Total Backups</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{status?.totalBackups ?? '—'}</p>
        </div>
        <div className="border border-gray-100 rounded-xl p-4">
          <p className="text-xs text-gray-500">Last Backup</p>
          <p className="text-sm font-semibold text-gray-800 mt-1">{status?.lastBackupDate ? new Date(status.lastBackupDate).toLocaleString() : 'Never'}</p>
          {status?.lastBackupFileName && <p className="text-xs text-gray-400 mt-0.5">{status.lastBackupFileName}</p>}
        </div>
        <div className="border border-gray-100 rounded-xl p-4">
          <p className="text-xs text-gray-500">Last Backup Size</p>
          <p className="text-xl font-bold text-gray-800 mt-1">{status?.lastBackupSize ? fmtSize(status.lastBackupSize) : '—'}</p>
        </div>
      </div>
    </div>
  );
};

/* ────────────── Security ────────────── */
const SecuritySection = () => (
  <div className="space-y-4">
    {[
      { title: 'Encryption', desc: 'AES-256 encryption for all stored files', status: 'Active', icon: FaShieldAlt, color: 'bg-green-50 text-green-600' },
      { title: 'JWT Authentication', desc: 'Token-based secure authentication', status: 'Enabled', icon: FaShieldAlt, color: 'bg-blue-50 text-blue-600' },
      { title: 'RBAC', desc: 'Role-based access control (Admin / Staff)', status: 'Enforced', icon: FaUsers, color: 'bg-purple-50 text-purple-600' },
    ].map(({ title, desc, status, icon: Icon, color }) => (
      <div key={title} className="flex items-center justify-between border border-gray-100 rounded-xl p-4">
        <div className="flex items-center gap-3">
          <div className={`h-10 w-10 rounded-xl ${color} flex items-center justify-center`}><Icon /></div>
          <div>
            <p className="text-sm font-semibold text-gray-800">{title}</p>
            <p className="text-xs text-gray-400">{desc}</p>
          </div>
        </div>
        <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-green-100 text-green-700">{status}</span>
      </div>
    ))}
  </div>
);

/* ────────────── Settings ────────────── */
const SettingsSection = () => (
  <div className="space-y-4">
    {[
      { label: 'Default Storage Quota', value: '5 GB', desc: 'Storage allocated to each new user' },
      { label: 'Max Upload Size', value: '100 MB', desc: 'Maximum single file upload size' },
      { label: 'Auto-Delete Trash', value: '30 days', desc: 'Time before trashed files are permanently removed' },
      { label: 'Session Timeout', value: '15 minutes', desc: 'JWT token validity period' },
    ].map(({ label, value, desc }) => (
      <div key={label} className="flex items-center justify-between border border-gray-100 rounded-xl p-4">
        <div>
          <p className="text-sm font-semibold text-gray-800">{label}</p>
          <p className="text-xs text-gray-400">{desc}</p>
        </div>
        <span className="text-sm font-bold text-gray-700 bg-gray-100 px-3 py-1 rounded-lg">{value}</span>
      </div>
    ))}
  </div>
);

/* ────────────── Storage Requests ────────────── */
const StorageRequestsSection = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [processingId, setProcessingId] = useState(null);
  const [showApproveModal, setShowApproveModal] = useState(null);
  const [adminNotes, setAdminNotes] = useState('');
  const [approvedQuota, setApprovedQuota] = useState(0);

  useEffect(() => { fetchRequests(); }, [statusFilter]);

  const fetchRequests = async () => {
    try { 
      const r = await storageRequestAPI.getAllRequests(statusFilter); 
      setRequests(r.data?.content || []); 
    }
    catch { toast.error('Failed to load storage requests'); }
    finally { setLoading(false); }
  };

  const handleApprove = async (requestId, approved) => {
    setProcessingId(requestId);
    try {
      await storageRequestAPI.approveRequest(requestId, {
        approved,
        adminNotes,
        approvedQuotaGb: approved ? approvedQuota : undefined
      });
      toast.success(approved ? 'Request approved' : 'Request rejected');

      // Issue 6 fix: If this storage request belongs to the currently logged-in user,
      // update their localStorage so their dashboard reflects the new quota immediately
      // without needing to re-login.
      if (approved && showApproveModal) {
        const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
        if (currentUser.id && showApproveModal.userId === currentUser.id) {
          const newQuotaBytes = (approvedQuota || showApproveModal.requestedQuotaGb) * 1073741824;
          const updated = { ...currentUser, storageQuota: newQuotaBytes };
          localStorage.setItem('user', JSON.stringify(updated));
        }
      }

      setShowApproveModal(null);
      setAdminNotes('');
      fetchRequests();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to process request');
    } finally {
      setProcessingId(null);
    }
  };

  const openApproveModal = (request) => {
    setShowApproveModal(request);
    setApprovedQuota(request.requestedQuotaGb);
    setAdminNotes('');
  };

  const getStatusBadge = (status) => {
    const styles = {
      PENDING: 'bg-yellow-100 text-yellow-700',
      APPROVED: 'bg-green-100 text-green-700',
      REJECTED: 'bg-red-100 text-red-700'
    };
    return <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${styles[status] || 'bg-gray-100'}`}>{status}</span>;
  };

  if (loading) return <div className="text-center py-10 text-gray-400">Loading...</div>;

  return (
    <div className="space-y-4">
      {/* Filter */}
      <div className="flex items-center gap-3">
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="text-sm border border-gray-200 rounded-xl px-3 py-2">
          <option value="">All Status</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Requests Table */}
      <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              <th className="text-left px-4 py-3 font-medium text-gray-600">User</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Current → Requested</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Reason</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
              <th className="text-left px-4 py-3 font-medium text-gray-600">Date</th>
              <th className="text-center px-4 py-3 font-medium text-gray-600">Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.length === 0 ? (
              <tr><td colSpan="6" className="px-4 py-8 text-center text-gray-400">No storage requests found</td></tr>
            ) : requests.map(r => (
              <tr key={r.id} className="border-b border-gray-50 hover:bg-gray-50/50">
                <td className="px-4 py-3">
                  <p className="font-medium text-gray-800">{r.userFullName}</p>
                  <p className="text-xs text-gray-400">{r.userEmail}</p>
                </td>
                <td className="px-4 py-3">
                  <span className="text-gray-500">{r.previousQuotaGb} GB</span>
                  <span className="mx-1 text-gray-300">→</span>
                  <span className="font-medium text-blue-600">{r.requestedQuotaGb} GB</span>
                </td>
                <td className="px-4 py-3 max-w-xs">
                  <p className="truncate" title={r.reason}>{r.reason}</p>
                </td>
                <td className="px-4 py-3">{getStatusBadge(r.status)}</td>
                <td className="px-4 py-3 text-gray-500">{new Date(r.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3 text-center">
                  {r.status === 'PENDING' && (
                    <div className="flex justify-center gap-2">
                      <button onClick={() => openApproveModal(r)} disabled={processingId === r.id}
                        className="text-xs bg-green-100 text-green-700 px-3 py-1.5 rounded-lg hover:bg-green-200 font-medium">
                        Approve
                      </button>
                      <button onClick={() => handleApprove(r.id, false)} disabled={processingId === r.id}
                        className="text-xs bg-red-100 text-red-700 px-3 py-1.5 rounded-lg hover:bg-red-200 font-medium">
                        Reject
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Approve Modal */}
      {showApproveModal && (
        <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Approve Storage Request</h2>
              <button onClick={() => setShowApproveModal(null)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <div className="bg-blue-50 rounded-xl p-4">
                <p className="text-sm text-blue-800">
                  <span className="font-medium">{showApproveModal.userFullName}</span> requests 
                  <span className="font-medium"> {showApproveModal.requestedQuotaGb} GB</span> storage
                  (currently {showApproveModal.previousQuotaGb} GB)
                </p>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Approved Quota (GB)</label>
                <input type="number" min={showApproveModal.previousQuotaGb + 1} value={approvedQuota}
                  onChange={e => setApprovedQuota(parseInt(e.target.value))}
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Admin Notes (optional)</label>
                <textarea value={adminNotes} onChange={e => setAdminNotes(e.target.value)}
                  placeholder="Add notes about this approval..." rows={3}
                  className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm resize-none" />
              </div>
              <div className="flex gap-3 pt-2">
                <button onClick={() => setShowApproveModal(null)} className="flex-1 border border-gray-200 text-gray-600 py-2.5 rounded-xl">Cancel</button>
                <button onClick={() => handleApprove(showApproveModal.id, true)} disabled={processingId === showApproveModal.id}
                  className="flex-1 bg-green-600 text-white py-2.5 rounded-xl hover:bg-green-700 disabled:opacity-50 font-medium">
                  {processingId === showApproveModal.id ? 'Processing...' : 'Confirm Approval'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminPanel;
