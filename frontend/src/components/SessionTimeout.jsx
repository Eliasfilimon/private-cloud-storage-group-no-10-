import { useSession } from '../context/SessionContext';

/**
 * Session Timeout Component
 * Displays warning modal when session is about to expire
 * Uses centralized SessionContext for session management
 */
const SessionTimeout = ({ children }) => {
  const { showWarning, remainingTime, extendSession, logout } = useSession();

  const handleStayLoggedIn = () => {
    extendSession();
  };

  const handleLogoutNow = () => {
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
