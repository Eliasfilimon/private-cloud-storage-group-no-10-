import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { authAPI, getErrorMessage } from '../services/api';
import { FaLock, FaEnvelope, FaCloud, FaShieldAlt, FaFolder, FaUsers, FaEye, FaEyeSlash, FaKey } from 'react-icons/fa';
import { Link } from 'react-router-dom';

const Login = () => {
  const [formData, setFormData]           = useState({ email: '', password: '' });
  const [loading, setLoading]             = useState(false);
  const [showPassword, setShowPassword]   = useState(false);
  // G1/M7: 2FA pending state
  const [totpPending, setTotpPending]     = useState(false);
  const [pendingToken, setPendingToken]   = useState('');
  const [totpCode, setTotpCode]           = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user  = localStorage.getItem('user');
    if (token && user) navigate('/dashboard');
  }, [navigate]);

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const saveSession = (data) => {
    localStorage.setItem('token', data.token);
    if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      id: data.id, username: data.username, email: data.email,
      firstName: data.firstName, lastName: data.lastName, fullName: data.fullName,
      role: data.role, department: data.department,
      storageQuota: data.storageQuota, storageUsed: data.storageUsed,
      mustChangePassword: data.mustChangePassword,
    }));
    toast.success(`Welcome back, ${data.fullName}!`);
    navigate(
      data.mustChangePassword ? '/profile' : '/dashboard',
      data.mustChangePassword ? { state: { forcePasswordChange: true } } : undefined
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email.trim()) { toast.error('Please enter your email'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) { toast.error('Please enter a valid email'); return; }
    if (!formData.password.trim()) { toast.error('Please enter your password'); return; }

    setLoading(true);
    try {
      const response = await authAPI.login({ username: formData.email, password: formData.password });
      const data = response.data;

      if (data.pendingTotp) {
        // G1/M7: Server requires 2FA — show the TOTP input form
        setPendingToken(data.pendingToken);
        setTotpPending(true);
        toast.info('Please enter your 6-digit authenticator code');
      } else {
        saveSession(data);
      }
    } catch (error) {
      if (error.response?.status === 401) toast.error('Invalid email or password');
      else if (error.code === 'ERR_NETWORK') toast.error('Network error. Please check your connection.');
      else toast.error(getErrorMessage(error, 'Login failed'));
    } finally {
      setLoading(false);
    }
  };

  const handleTotpSubmit = async (e) => {
    e.preventDefault();
    if (!totpCode || totpCode.length !== 6) { toast.error('Please enter a valid 6-digit code'); return; }
    setLoading(true);
    try {
      const response = await authAPI.verifyTotpLogin(pendingToken, totpCode);
      saveSession(response.data);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Invalid code. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  const features = [
    { icon: FaShieldAlt, title: 'AES-256 Encryption', desc: 'Military-grade file encryption at rest and in transit', color: 'from-amber-400 to-orange-500' },
    { icon: FaCloud, title: 'Self-Hosted Cloud', desc: 'Full institutional control over your data', color: 'from-cyan-400 to-blue-500' },
    { icon: FaFolder, title: 'Smart File Management', desc: 'Organize, version, and share academic documents', color: 'from-emerald-400 to-green-500' },
    { icon: FaUsers, title: 'Secure Collaboration', desc: 'Share files safely with role-based access control', color: 'from-violet-400 to-purple-500' },
  ];

  return (
    <div className="min-h-screen flex bg-gray-50">
      {/* Left hero */}
      <div className="hidden lg:flex lg:w-[55%] hero-udom flex-col justify-center items-center px-12 py-16">
        <div className="relative z-10 max-w-lg text-center">
          <div className="h-16 w-16 bg-white/15 backdrop-blur-sm rounded-2xl flex items-center justify-center mx-auto mb-6 border border-white/20">
            <FaCloud className="text-white text-3xl" />
          </div>
          <h1 className="text-3xl font-extrabold text-white mb-2">UDOM Secure Cloud</h1>
          <p className="text-blue-200 text-lg font-medium">University of Dodoma</p>
          <p className="text-blue-300 text-sm mt-1 mb-10">Private Cloud Storage for Academic Institutions</p>

          <div className="grid grid-cols-2 gap-4 text-left">
            {features.map(({ icon: Icon, title, desc, color }) => (
              <div key={title} className="bg-white/10 backdrop-blur-sm rounded-xl p-4 border border-white/10 hover:bg-white/15 transition-all">
                <div className={`h-9 w-9 rounded-lg bg-gradient-to-br ${color} flex items-center justify-center mb-3`}>
                  <Icon className="text-white text-sm" />
                </div>
                <h3 className="text-white font-semibold text-sm">{title}</h3>
                <p className="text-blue-200 text-xs mt-1 leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>

          <p className="text-blue-300/60 text-xs mt-10">College of Informatics and Virtual Education &bull; Department of Computer Science</p>
        </div>
      </div>

      {/* Right form */}
      <div className="flex-1 flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm animate-fadeInUp">
          {/* Mobile logo */}
          <div className="lg:hidden text-center mb-8">
            <div className="h-14 w-14 bg-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-3 shadow-lg">
              <FaCloud className="text-white text-2xl" />
            </div>
            <h2 className="text-2xl font-bold text-gray-800">UDOM Secure Cloud</h2>
            <p className="text-sm text-gray-500 mt-1">University of Dodoma</p>
          </div>

          {/* Form card */}
          <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
            <div className="bg-gradient-to-r from-blue-700 to-blue-800 px-6 py-5">
              <h2 className="text-xl font-bold text-white">
                {totpPending ? 'Two-Factor Authentication' : 'Welcome Back'}
              </h2>
              <p className="text-blue-200 text-sm mt-0.5">
                {totpPending ? 'Enter the 6-digit code from your authenticator app' : 'Sign in to your academic account'}
              </p>
            </div>

            {totpPending ? (
              /* G1/M7: 2FA code entry form */
              <form onSubmit={handleTotpSubmit} noValidate className="p-6 space-y-5">
                <div>
                  <label htmlFor="totpCode" className="block text-xs font-semibold text-gray-600 mb-1.5 uppercase tracking-wide">
                    Authenticator Code
                  </label>
                  <div className="relative">
                    <FaKey className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm" />
                    <input
                      id="totpCode" name="totpCode" type="text" inputMode="numeric" pattern="[0-9]{6}"
                      maxLength={6} required disabled={loading} placeholder="000000"
                      value={totpCode} onChange={(e) => setTotpCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      autoFocus autoComplete="one-time-code"
                      className="input-field pl-9 text-center tracking-widest text-lg disabled:opacity-50"
                    />
                  </div>
                </div>
                <button type="submit" disabled={loading || totpCode.length !== 6}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-xl shadow-md transition-all disabled:opacity-50 disabled:cursor-not-allowed text-sm">
                  {loading ? 'Verifying...' : 'Verify & Sign In'}
                </button>
                <button type="button" onClick={() => { setTotpPending(false); setPendingToken(''); setTotpCode(''); }}
                  className="w-full text-xs text-gray-400 hover:text-gray-600 py-1">
                  ← Back to login
                </button>
              </form>
            ) : (
              /* Regular login form */
              <form onSubmit={handleSubmit} noValidate className="p-6 space-y-5">
                <div>
                  <label htmlFor="email" className="block text-xs font-semibold text-gray-600 mb-1.5 uppercase tracking-wide">Email Address</label>
                  <div className="relative">
                    <FaEnvelope className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm" />
                    <input id="email" name="email" type="email" required disabled={loading} placeholder="name@udom.ac.tz" value={formData.email} onChange={handleChange} autoComplete="email"
                      className="input-field pl-9 disabled:opacity-50" />
                  </div>
                </div>
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label htmlFor="password" className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Password</label>
                    <Link to="/forgot-password" className="text-xs text-blue-600 hover:text-blue-700 font-medium">Forgot Password?</Link>
                  </div>
                  <div className="relative">
                    <FaLock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm" />
                    <input id="password" name="password" type={showPassword ? 'text' : 'password'} required disabled={loading} placeholder="Enter your password" value={formData.password} onChange={handleChange} autoComplete="current-password"
                      className="input-field pl-9 pr-10 disabled:opacity-50" />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} disabled={loading} aria-label={showPassword ? 'Hide password' : 'Show password'}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded">
                      {showPassword ? <FaEyeSlash className="text-sm" /> : <FaEye className="text-sm" />}
                    </button>
                  </div>
                </div>
                <button type="submit" disabled={loading}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-xl shadow-md hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed text-sm">
                  {loading ? (
                    <span className="flex items-center justify-center gap-2">
                      <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" /></svg>
                      Signing in...
                    </span>
                  ) : 'Sign In'}
                </button>
                <p className="text-center text-xs text-gray-400 pt-1">
                  Don't have an account? Contact your administrator.
                </p>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
