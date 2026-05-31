import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../services/api';

/**
 * Session Timeout Component
 * Logs out user after 15 minutes of inactivity
 */
const INACTIVITY_TIMEOUT = 15 * 60 * 1000; // 15 minutes in milliseconds
const WARNING_TIME = 30 * 1000; // Show warning 30 seconds before logout

const SessionTimeout = ({ children }) => {
  const navigate = useNavigate();
  const [showWarning, setShowWarning] = useState(false);
  const [remainingTime, setRemainingTime] = useState(30);
  const timeoutRef = useRef(null);
  const warningRef = useRef(null);
  const countdownRef = useRef(null);
  const lastActivityRef = useRef(Date.now());

  const logout = async () => {
    try {
      // Clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      toast.error('Session expired due to inactivity. Please log in again.');
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
      navigate('/login');
    }
  };

  const resetTimer = () => {
    lastActivityRef.current = Date.now();
    
    // Clear existing timers
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    if (warningRef.current) clearTimeout(warningRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);
    
    setShowWarning(false);
    
    // Set warning timer (5 min - 30 sec)
    warningRef.current = setTimeout(() => {
      setShowWarning(true);
      setRemainingTime(30);
      
      // Start countdown
      countdownRef.current = setInterval(() => {
        setRemainingTime(prev => {
          if (prev <= 1) {
            clearInterval(countdownRef.current);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }, INACTIVITY_TIMEOUT - WARNING_TIME);
    
    // Set logout timer (15 min)
    timeoutRef.current = setTimeout(() => {
      logout();
    }, INACTIVITY_TIMEOUT);
  };

  // Check JWT token expiry periodically
  useEffect(() => {
    const checkTokenExpiry = () => {
      const token = localStorage.getItem('token');
      if (!token) return;
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiresAt = payload.exp * 1000;
        const timeLeft = expiresAt - Date.now();
        if (timeLeft <= 0) {
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          toast.error('Your session has expired. Please log in again.');
          navigate('/login');
        } else if (timeLeft <= 5 * 60 * 1000 && timeLeft > 4.5 * 60 * 1000) {
          toast.warn('Your session will expire in 5 minutes. Save your work.', { toastId: 'token-expiry' });
        }
      } catch (_) {}
    };
    const interval = setInterval(checkTokenExpiry, 30000);
    checkTokenExpiry();
    return () => clearInterval(interval);
  }, [navigate]);

  useEffect(() => {
    // Only track if user is logged in
    const token = localStorage.getItem('token');
    if (!token) return;

    // Activity events to track
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
    
    const handleActivity = () => {
      resetTimer();
    };

    // Add listeners
    events.forEach(event => {
      window.addEventListener(event, handleActivity, true);
    });

    // Start initial timer
    resetTimer();

    // Cleanup
    return () => {
      events.forEach(event => {
        window.removeEventListener(event, handleActivity, true);
      });
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      if (warningRef.current) clearTimeout(warningRef.current);
      if (countdownRef.current) clearInterval(countdownRef.current);
    };
  }, [navigate]);

  // Handle stay logged in
  const handleStayLoggedIn = () => {
    resetTimer();
    setShowWarning(false);
    toast.success('Session extended');
    
    // Ping server to update session activity
    api.get('/auth/me').catch(() => {});
  };

  // Handle logout now
  const handleLogoutNow = () => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    if (warningRef.current) clearTimeout(warningRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);
    logout();
  };

  return (
    <>
      {children}
      
      {/* Warning Modal */}
      {showWarning && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <div className="text-center">
              <div className="h-12 w-12 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="h-6 w-6 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-gray-900 mb-2">Session Timeout Warning</h3>
              <p className="text-sm text-gray-500 mb-4">
                Your session will expire in <span className="font-bold text-amber-600">{remainingTime}</span> seconds due to inactivity.
              </p>
              <div className="flex gap-3">
                <button
                  onClick={handleLogoutNow}
                  className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50 transition-colors"
                >
                  Logout Now
                </button>
                <button
                  onClick={handleStayLoggedIn}
                  className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 transition-colors font-medium"
                >
                  Stay Logged In
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default SessionTimeout;
