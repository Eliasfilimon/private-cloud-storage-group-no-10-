import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaEnvelope, FaArrowLeft, FaCheckCircle } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { authAPI } from '../services/api';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) {
      toast.error('Please enter your email');
      return;
    }

    setLoading(true);
    try {
      await authAPI.forgotPassword(email);
      setSent(true);
      toast.success('Password reset link sent to your email');
    } catch (error) {
      toast.error('Failed to send reset link. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaCheckCircle className="text-green-600 text-2xl" />
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Check Your Email</h2>
            <p className="text-gray-600 mb-6">
              If an account exists with <strong>{email}</strong>, you will receive a password reset link shortly.
            </p>
            <Link to="/login" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 font-medium">
              <FaArrowLeft className="text-sm" /> Back to Login
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
              <FaEnvelope className="text-blue-600 text-2xl" />
            </div>
            <h2 className="text-xl font-bold text-gray-800">Forgot Password?</h2>
            <p className="text-gray-500 text-sm mt-1">
              Enter your email and we'll send you a link to reset your password.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                className="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 rounded-xl font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Sending...' : 'Send Reset Link'}
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

export default ForgotPassword;
