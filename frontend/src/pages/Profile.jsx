import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { authAPI, profileAPI, getErrorMessage } from '../services/api';
import { toast } from 'react-toastify';
import { FaUser, FaEnvelope, FaKey, FaSave, FaShieldAlt, FaFile, FaTimes, FaQrcode, FaLock, FaDatabase } from 'react-icons/fa';

const Profile = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const forcePasswordChange = location.state?.forcePasswordChange || false;

  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    department: ''
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [showPasswordForm, setShowPasswordForm] = useState(false);

  // 2FA states
  const [show2faModal, setShow2faModal] = useState(false);
  const [showDisable2faModal, setShowDisable2faModal] = useState(false);
  const [totpSecret, setTotpSecret] = useState('');
  const [qrCodeUri, setQrCodeUri] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [disableCode, setDisableCode] = useState('');

  useEffect(() => {
    fetchUserData();
    // Auto-show password form if forced
    if (forcePasswordChange) {
      setShowPasswordForm(true);
    }
  }, [forcePasswordChange]);

  const fetchUserData = async () => {
    try {
      const response = await authAPI.getCurrentUser();
      setUser(response.data);
      setProfileData({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        email: response.data.email || '',
        department: response.data.department || ''
      });
    } catch (error) {
      toast.error('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileChange = (e) => {
    setProfileData({
      ...profileData,
      [e.target.name]: e.target.value
    });
  };

  const handlePasswordChange = (e) => {
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value
    });
  };

  const getPasswordStrength = (pass) => {
    let score = 0;
    if (!pass) return { score, color: 'bg-gray-200', text: '' };
    if (pass.length > 7) score += 25;
    if (/[A-Z]/.test(pass)) score += 25;
    if (/[0-9]/.test(pass)) score += 25;
    if (/[^A-Za-z0-9]/.test(pass)) score += 25;
    
    if (score <= 25) return { score, color: 'bg-red-500', text: 'Weak' };
    if (score <= 50) return { score, color: 'bg-amber-400', text: 'Fair' };
    if (score <= 75) return { score, color: 'bg-blue-500', text: 'Good' };
    return { score, color: 'bg-green-500', text: 'Strong' };
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setUpdating(true);

    try {
      const response = await profileAPI.updateProfile(profileData);
      setUser(response.data);
      // Update local storage user data
      const currentStorageUser = JSON.parse(localStorage.getItem('user') || '{}');
      localStorage.setItem('user', JSON.stringify({ ...currentStorageUser, ...response.data }));
      window.dispatchEvent(new Event('storage-updated'));
      toast.success('Profile updated successfully!');
      fetchUserData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to update profile'));
    } finally {
      setUpdating(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }

    setUpdating(true);

    try {
      await authAPI.changePassword(passwordData);
      
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      
      toast.success('Password changed successfully. Please log in again.');
      
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to change password'));
    } finally {
      setUpdating(false);
    }
  };

  // 2FA handlers
  const handleSetup2fa = async () => {
    try {
      const response = await authAPI.setup2fa();
      setTotpSecret(response.data.secret);
      setQrCodeUri(response.data.qrCodeUri);
      setShow2faModal(true);
    } catch (error) {
      toast.error('Failed to setup 2FA');
    }
  };

  const handleEnable2fa = async () => {
    if (!verificationCode || verificationCode.length !== 6) {
      toast.error('Please enter a valid 6-digit code');
      return;
    }

    setUpdating(true);
    try {
      await authAPI.enable2fa(verificationCode);
      toast.success('2FA enabled successfully!');
      setShow2faModal(false);
      setVerificationCode('');
      fetchUserData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to enable 2FA'));
    } finally {
      setUpdating(false);
    }
  };

  const handleDisable2fa = async () => {
    if (!disableCode || disableCode.length !== 6) {
      toast.error('Please enter a valid 6-digit code');
      return;
    }

    setUpdating(true);
    try {
      await authAPI.disable2fa(disableCode);
      toast.success('2FA disabled successfully!');
      setShowDisable2faModal(false);
      setDisableCode('');
      fetchUserData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to disable 2FA'));
    } finally {
      setUpdating(false);
    }
  };

  const formatBytes = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  };

  const storagePercentage = user?.storageQuota
    ? Math.round(((user.storageUsed || 0) / user.storageQuota) * 100)
    : 0;

  const initials = (user?.fullName || 'U').split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2);
  const inputCls = "w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all";

  return (
    <div className="space-y-5">
      {/* Page header */}
      <div>
        <h1 className="text-xl font-bold text-gray-800">Account Settings</h1>
        <p className="text-sm text-gray-500 mt-0.5">Manage your profile, security, and storage</p>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : (
        <div className="space-y-5">
          {/* Profile card */}
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
              <div className="h-16 w-16 rounded-2xl bg-blue-600 flex items-center justify-center text-white font-bold text-2xl flex-shrink-0 shadow-md">
                {initials}
              </div>
              <div className="flex-1 min-w-0">
                <h2 className="text-lg font-bold text-gray-900">{user?.fullName || 'User'}</h2>
                <p className="text-sm text-gray-500">{user?.email}</p>
                <div className="flex flex-wrap items-center gap-2 mt-2">
                  <span className="text-xs bg-blue-100 text-blue-700 font-semibold px-2.5 py-0.5 rounded-full">{user?.role}</span>
                  {user?.department && <span className="text-xs bg-gray-100 text-gray-600 px-2.5 py-0.5 rounded-full">{user?.department}</span>}
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {/* Personal information */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
              <div className="px-5 py-4 border-b border-gray-100 flex items-center gap-2">
                <FaUser className="text-blue-500 text-sm" />
                <h3 className="font-semibold text-gray-800 text-sm">Personal Information</h3>
              </div>
              <div className="p-5">
                <form onSubmit={handleUpdateProfile} className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label htmlFor="firstName" className="block text-xs font-medium text-gray-600 mb-1.5">First Name</label>
                      <input id="firstName" type="text" name="firstName" value={profileData.firstName} onChange={handleProfileChange} className={inputCls} required />
                    </div>
                    <div>
                      <label htmlFor="lastName" className="block text-xs font-medium text-gray-600 mb-1.5">Last Name</label>
                      <input id="lastName" type="text" name="lastName" value={profileData.lastName} onChange={handleProfileChange} className={inputCls} required />
                    </div>
                  </div>
                  <div>
                    <label htmlFor="email" className="block text-xs font-medium text-gray-600 mb-1.5">Email Address</label>
                    <input id="email" type="email" name="email" value={profileData.email} onChange={handleProfileChange} className={inputCls} required />
                  </div>
                  <div>
                    <label htmlFor="department" className="block text-xs font-medium text-gray-600 mb-1.5">Department</label>
                    <input id="department" type="text" name="department" value={profileData.department} onChange={handleProfileChange} className={inputCls} placeholder="e.g., Computer Science" />
                  </div>
                  <button type="submit" disabled={updating}
                    className="flex items-center gap-2 bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors">
                    <FaSave className="text-xs" /> {updating ? 'Saving...' : 'Save Changes'}
                  </button>
                </form>
              </div>
            </div>

            {/* Storage usage */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
              <div className="px-5 py-4 border-b border-gray-100 flex items-center gap-2">
                <FaDatabase className="text-blue-500 text-sm" />
                <h3 className="font-semibold text-gray-800 text-sm">Storage Usage</h3>
              </div>
              <div className="p-5">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-gray-600"><strong className="text-gray-800">{formatBytes(user?.storageUsed || 0)}</strong> used</span>
                  <span className="text-gray-500">{storagePercentage}% of {formatBytes(user?.storageQuota || 0)}</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden mb-5">
                  <div className={`h-full rounded-full transition-all ${storagePercentage > 80 ? 'bg-red-500' : storagePercentage > 60 ? 'bg-yellow-400' : 'bg-blue-500'}`}
                    style={{ width: `${storagePercentage}%` }} />
                </div>
                <div className="grid grid-cols-3 gap-3">
                  {[
                    { label: 'Used', val: formatBytes(user?.storageUsed || 0), color: 'text-blue-600 bg-blue-50', icon: FaDatabase },
                    { label: 'Free', val: formatBytes((user?.storageQuota || 0) - (user?.storageUsed || 0)), color: 'text-green-600 bg-green-50', icon: FaFile },
                    { label: 'Quota', val: formatBytes(user?.storageQuota || 0), color: 'text-amber-600 bg-amber-50', icon: FaShieldAlt },
                  ].map(({ label, val, color, icon: Icon }) => (
                    <div key={label} className="text-center p-3 bg-gray-50 rounded-xl border border-gray-100">
                      <div className={`h-8 w-8 rounded-lg ${color} flex items-center justify-center mx-auto mb-2`}>
                        <Icon className="text-sm" />
                      </div>
                      <p className="text-xs text-gray-500">{label}</p>
                      <p className="text-sm font-bold text-gray-700 mt-0.5">{val}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Security */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden lg:col-span-2">
              <div className="px-5 py-4 border-b border-gray-100 flex items-center gap-2">
                <FaShieldAlt className="text-blue-500 text-sm" />
                <h3 className="font-semibold text-gray-800 text-sm">Security</h3>
              </div>
              <div className="divide-y divide-gray-50">
                {/* Change password row */}
                <div className="p-5">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                      <p className="text-sm font-semibold text-gray-800">Password</p>
                      <p className="text-xs text-gray-500 mt-0.5">Use a strong password with uppercase, numbers and special characters</p>
                    </div>
                    {!showPasswordForm && (
                      <button onClick={() => setShowPasswordForm(true)}
                        className="flex items-center gap-2 text-sm font-medium border border-gray-200 text-gray-700 px-4 py-2 rounded-xl hover:bg-gray-50 transition-colors self-start sm:self-auto flex-shrink-0">
                        <FaKey className="text-xs" /> Change Password
                      </button>
                    )}
                  </div>
                  {showPasswordForm && (
                    <form onSubmit={handleChangePassword} className="mt-4 space-y-3 max-w-md">
                      <input aria-label="Current password" type="password" name="currentPassword" value={passwordData.currentPassword} onChange={handlePasswordChange} placeholder="Current password" className={inputCls} required />
                      <div className="space-y-1">
                        <input aria-label="New password" type="password" name="newPassword" value={passwordData.newPassword} onChange={handlePasswordChange} placeholder="New password (min 8 chars)" className={inputCls} minLength="8" required />
                        {passwordData.newPassword && (() => {
                          const { score, color, text } = getPasswordStrength(passwordData.newPassword);
                          return (
                            <div className="mt-2 mb-2">
                              <div className="flex justify-between items-center mb-1 text-xs">
                                <span className="text-gray-500">Password strength</span>
                                <span className={`font-medium ${color.replace('bg-', 'text-')}`}>{text}</span>
                              </div>
                              <div className="w-full bg-gray-200 rounded-full h-1.5">
                                <div className={`${color} h-1.5 rounded-full transition-all duration-300`} style={{ width: `${score}%` }}></div>
                              </div>
                            </div>
                          );
                        })()}
                      </div>
                      <input aria-label="Confirm new password" type="password" name="confirmPassword" value={passwordData.confirmPassword} onChange={handlePasswordChange} placeholder="Confirm new password" className={inputCls} required />
                      <div className="flex gap-3">
                        <button type="button" onClick={() => { setShowPasswordForm(false); setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' }); }}
                          className="flex-1 border border-gray-200 text-gray-600 text-sm font-medium py-2 rounded-xl hover:bg-gray-50 transition-colors">Cancel</button>
                        <button type="submit" disabled={updating}
                          className="flex-1 bg-blue-600 text-white text-sm font-medium py-2 rounded-xl hover:bg-blue-700 disabled:opacity-50 transition-colors">{updating ? 'Updating...' : 'Update Password'}</button>
                      </div>
                    </form>
                  )}
                </div>
                {/* 2FA row */}
                <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-semibold text-gray-800">Two-Factor Authentication</p>
                      <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${user?.totpEnabled ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {user?.totpEnabled ? 'Enabled' : 'Disabled'}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 mt-0.5">Add an extra layer of security with an authenticator app</p>
                  </div>
                  {user?.totpEnabled ? (
                    <button onClick={() => setShowDisable2faModal(true)}
                      className="flex items-center gap-2 text-sm font-medium bg-red-50 border border-red-200 text-red-600 px-4 py-2 rounded-xl hover:bg-red-100 transition-colors self-start sm:self-auto flex-shrink-0">
                      <FaTimes className="text-xs" /> Disable 2FA
                    </button>
                  ) : (
                    <button onClick={handleSetup2fa}
                      className="flex items-center gap-2 text-sm font-medium bg-blue-600 text-white px-4 py-2 rounded-xl hover:bg-blue-700 transition-colors self-start sm:self-auto flex-shrink-0">
                      <FaQrcode className="text-xs" /> Enable 2FA
                    </button>
                  )}
                </div>
                {/* Encryption row */}
                <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold text-gray-800">Encryption Keys</p>
                    <p className="text-xs text-gray-500 mt-0.5">AES-256 encryption is applied to all your uploaded files</p>
                  </div>
                  <button className="flex items-center gap-2 text-sm font-medium bg-blue-50 border border-blue-100 text-blue-600 px-4 py-2 rounded-xl hover:bg-blue-100 transition-colors self-start sm:self-auto flex-shrink-0">
                    <FaLock className="text-xs" /> View Keys
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2FA Setup Modal */}
      {show2faModal && (
        <div role="dialog" aria-modal="true" aria-labelledby="2fa-setup-title" className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full">
            <div className="p-5 border-b border-gray-100 flex items-center justify-between">
              <div>
                <h2 id="2fa-setup-title" className="text-base font-bold text-gray-800">Enable Two-Factor Authentication</h2>
                <p className="text-xs text-gray-500 mt-0.5">Scan the QR code with any authenticator app</p>
              </div>
              <button aria-label="Close dialog" onClick={() => setShow2faModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-300"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              {/* Single QR Code - Works with all apps */}
              <div className="flex justify-center">
                <img
                  src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrCodeUri)}`}
                  alt="2FA QR Code"
                  className="w-48 h-48 rounded-xl border-2 border-blue-100"
                />
              </div>

              {/* Compatible Apps Info */}
              <div className="bg-blue-50 rounded-xl p-3 text-center">
                <p className="text-xs text-blue-700 font-medium mb-1">Compatible with all authenticator apps:</p>
                <p className="text-xs text-blue-600">
                  Google Authenticator • Microsoft Authenticator • Authy • Ngao Authenticator • 1Password • LastPass
                </p>
              </div>

              <div className="bg-gray-50 rounded-xl p-3">
                <p className="text-xs font-medium text-gray-600 mb-1">Manual key:</p>
                <p className="text-xs font-mono text-gray-700 break-all">{totpSecret}</p>
              </div>
              <input aria-label="Verification code" type="text" value={verificationCode} onChange={e => setVerificationCode(e.target.value)} placeholder="000000" maxLength="6"
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-center text-2xl font-bold tracking-widest focus:outline-none focus:ring-2 focus:ring-blue-500" />
              <div className="flex gap-3">
                <button onClick={() => setShow2faModal(false)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleEnable2fa} disabled={updating} className="flex-1 bg-blue-600 text-white text-sm py-2 rounded-xl hover:bg-blue-700 disabled:opacity-50">{updating ? 'Enabling...' : 'Enable 2FA'}</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2FA Disable Modal */}
      {showDisable2faModal && (
        <div role="dialog" aria-modal="true" aria-labelledby="2fa-disable-title" className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full">
            <div className="p-5 border-b border-gray-100 flex items-center justify-between">
              <div>
                <h2 id="2fa-disable-title" className="text-base font-bold text-gray-800">Disable 2FA</h2>
                <p className="text-xs text-gray-500 mt-0.5">Enter your authenticator code to confirm</p>
              </div>
              <button aria-label="Close dialog" onClick={() => setShowDisable2faModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-300"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 text-sm text-amber-700 flex items-center gap-2">
                <FaShieldAlt className="flex-shrink-0" /> Disabling 2FA will reduce your account security.
              </div>
              <input aria-label="Disable code" type="text" value={disableCode} onChange={e => setDisableCode(e.target.value)} placeholder="000000" maxLength="6"
                className="w-full border border-gray-200 rounded-xl px-4 py-3 text-center text-2xl font-bold tracking-widest focus:outline-none focus:ring-2 focus:ring-red-400" />
              <div className="flex gap-3">
                <button onClick={() => setShowDisable2faModal(false)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleDisable2fa} disabled={updating} className="flex-1 bg-red-600 text-white text-sm py-2 rounded-xl hover:bg-red-700 disabled:opacity-50">{updating ? 'Disabling...' : 'Disable 2FA'}</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;