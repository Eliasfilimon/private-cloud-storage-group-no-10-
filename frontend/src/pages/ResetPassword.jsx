import React, { useState, useEffect } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import { FaLock, FaArrowLeft, FaCheckCircle, FaExclamationTriangle } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { authAPI } from '../services/api';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(true);
  const [isValid, setIsValid] = useState(false);
  const [resetSuccess, setResetSuccess] = useState(false);

  useEffect(() => {
    const validateToken = async () => {
      if (!token) {
        setValidating(false);
        return;
      }
      try {
        const response = await authAPI.validateResetToken(token);
        setIsValid(response.data?.valid || false);
      } catch (error) {
        setIsValid(false);
      } finally {
        setValidating(false);
      }
    };
    validateToken();
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (password.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }
    
    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      await authAPI.resetPassword(token, password);
      setResetSuccess(true);
      toast.success('Password reset successfully');
      setTimeout(() => navigate('/login'), 3000);
    } catch (error) {
      toast.error('Failed to reset password. The link may have expired.');
    } finally {
      setLoading(false);
    }
  };

  if (validating) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center p-4">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Validating reset link...</p>
        </div>
      </div>
    );
  }

  if (!token || !isValid) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaExclamationTriangle className="text-red-600 text-2xl" />
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Invalid or Expired Link</h2>
            <p className="text-gray-600 mb-6">
              This password reset link is invalid or has expired. Please request a new one.
            </p>
            <Link to="/forgot-password" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium">
              Request New Link
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (resetSuccess) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaCheckCircle className="text-green-600 text-2xl" />
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Password Reset Successful</h2>
            <p className="text-gray-600 mb-6">
              Your password has been reset. Redirecting to login...
            </p>
            <Link to="/login" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium">
              Go to Login
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="text-center mb-6">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaLock className="text-blue-600 text-2xl" />
            </div>
            <h2 className="text-xl font-bold text-gray-800">Reset Password</h2>
            <p className="text-gray-500 text-sm mt-1">
              Enter your new password below.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter new password"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
                minLength={8}
              />
              <p className="text-xs text-gray-500 mt-1">Minimum 8 characters</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm new password"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 rounded-xl font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Resetting...' : 'Reset Password'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <Link to="/login" className="inline-flex items-center gap-2 text-gray-600 hover:text-gray-800 text-sm font-medium">
              <FaArrowLeft className="text-xs" /> Back to Login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
