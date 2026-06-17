import { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';
import { toast } from 'react-toastify';
import api from '../services/api';

const SessionContext = createContext();

export const useSession = () => {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within SessionProvider');
  }
  return context;
};

/**
 * SessionProvider: Centralized session management
 * Handles inactivity timeout, token refresh, and activity tracking
 */
export const SessionProvider = ({ children }) => {
  const [sessionState, setSessionState] = useState({
    isActive: false,
    showWarning: false,
    remainingTime: 60,
    lastActivityTime: Date.now(),
  });

  const timeoutRef = useRef(null);
  const warningRef = useRef(null);
  const countdownRef = useRef(null);
  const activityPingRef = useRef(null);

  const INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes (matches backend session.timeout-minutes)
  const WARNING_TIME = 60 * 1000; // 60 seconds before logout
  const ACTIVITY_PING_INTERVAL = 5 * 60 * 1000; // Ping server every 5 minutes

  /**
   * Logout user
   */
  const logout = useCallback(async () => {
    try {
      // Call logout endpoint to invalidate session on server
      await api.post('/auth/logout').catch(() => {});
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('refreshToken');
      setSessionState(prev => ({ ...prev, isActive: false, showWarning: false }));
      toast.error('Session expired due to inactivity. Please log in again.');
      window.location.href = '/login';
    }
  }, []);

  /**
   * Reset inactivity timer
   */
  const resetTimer = useCallback(() => {
    // Clear existing timers
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    if (warningRef.current) clearTimeout(warningRef.current);
    if (countdownRef.current) clearInterval(countdownRef.current);

    setSessionState(prev => ({
      ...prev,
      showWarning: false,
      remainingTime: 60,
      lastActivityTime: Date.now(),
    }));

    // Set warning timer (9:30 min - 30 sec)
    warningRef.current = setTimeout(() => {
      setSessionState(prev => ({ ...prev, showWarning: true, remainingTime: 60 }));

      // Start countdown
      countdownRef.current = setInterval(() => {
        setSessionState(prev => {
          if (prev.remainingTime <= 1) {
            clearInterval(countdownRef.current);
            return { ...prev, remainingTime: 0 };
          }
          return { ...prev, remainingTime: prev.remainingTime - 1 };
        });
      }, 1000);
    }, INACTIVITY_TIMEOUT - WARNING_TIME);

    // Set logout timer (10 min)
    timeoutRef.current = setTimeout(() => {
      logout();
    }, INACTIVITY_TIMEOUT);
  }, [logout]);

  /**
   * Extend session (called when user clicks "Stay Logged In")
   */
  const extendSession = useCallback(async () => {
    try {
      resetTimer();
      setSessionState(prev => ({ ...prev, showWarning: false }));
      toast.success('Session extended');

      // Ping server to update activity
      await api.post('/auth/activity').catch(() => {});
    } catch (error) {
      console.error('Error extending session:', error);
    }
  }, [resetTimer]);

  /**
   * Ping server with activity update
   */
  const pingActivity = useCallback(async () => {
    try {
      await api.post('/auth/activity').catch(() => {});
    } catch (error) {
      console.error('Error pinging activity:', error);
    }
  }, []);

  /**
   * Handle user activity (debounced)
   */
  const handleActivity = useCallback(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    resetTimer();

    // Ping server every 5 minutes
    if (activityPingRef.current) clearTimeout(activityPingRef.current);
    activityPingRef.current = setTimeout(() => {
      pingActivity();
    }, ACTIVITY_PING_INTERVAL);
  }, [resetTimer, pingActivity]);

  /**
   * Check JWT token expiration periodically
   */
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
          setSessionState(prev => ({ ...prev, isActive: false }));
          toast.error('Your session has expired. Please log in again.');
          window.location.href = '/login';
        } else if (timeLeft <= 5 * 60 * 1000 && timeLeft > 4.5 * 60 * 1000) {
          toast.warn('Your session will expire in 5 minutes. Save your work.', { toastId: 'token-expiry' });
        }
      } catch (_) {}
    };

    const interval = setInterval(checkTokenExpiry, 30000);
    checkTokenExpiry();
    return () => clearInterval(interval);
  }, []);

  /**
   * Setup activity tracking
   */
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setSessionState(prev => ({ ...prev, isActive: false }));
      return;
    }

    setSessionState(prev => ({ ...prev, isActive: true }));

    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

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
      if (activityPingRef.current) clearTimeout(activityPingRef.current);
    };
  }, [handleActivity, resetTimer]);

  const value = {
    ...sessionState,
    logout,
    extendSession,
    resetTimer,
  };

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
};
