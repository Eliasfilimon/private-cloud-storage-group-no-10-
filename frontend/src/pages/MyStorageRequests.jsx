import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { storageRequestAPI } from '../services/api';
import { FaHdd, FaClock, FaCheckCircle, FaTimesCircle, FaTrash, FaPlus } from 'react-icons/fa';
import StorageRequestModal from '../components/StorageRequestModal';

const MyStorageRequests = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showRequestModal, setShowRequestModal] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const storageUsedGb = (user.storageUsed || 0) / (1024 * 1024 * 1024);
  const storageQuotaGb = (user.storageQuota || 5368709120) / (1024 * 1024 * 1024);
  const storagePercent = Math.round((storageUsedGb / storageQuotaGb) * 100);

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    try {
      const response = await storageRequestAPI.getMyRequests();
      setRequests(response.data || []);
    } catch (error) {
      toast.error('Failed to load storage requests');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (requestId) => {
    if (!window.confirm('Cancel this storage request?')) return;
    
    setCancellingId(requestId);
    try {
      await storageRequestAPI.cancelRequest(requestId);
      toast.success('Request cancelled');
      fetchRequests();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to cancel request');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status) => {
    const configs = {
      PENDING: { icon: FaClock, class: 'bg-yellow-100 text-yellow-700', label: 'Pending' },
      APPROVED: { icon: FaCheckCircle, class: 'bg-green-100 text-green-700', label: 'Approved' },
      REJECTED: { icon: FaTimesCircle, class: 'bg-red-100 text-red-700', label: 'Rejected' }
    };
    const config = configs[status] || configs.PENDING;
    const Icon = config.icon;
    return (
      <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold ${config.class}`}>
        <Icon className="text-[10px]" />
        {config.label}
      </span>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-800">My Storage Requests</h1>
          <p className="text-sm text-gray-500 mt-0.5">View and manage your storage quota requests</p>
        </div>
        <button 
          onClick={() => setShowRequestModal(true)}
          className="flex items-center gap-2 bg-blue-600 text-white text-sm font-medium px-4 py-2.5 rounded-xl hover:bg-blue-700 transition-colors"
        >
          <FaPlus className="text-xs" /> New Request
        </button>
      </div>

      {/* Current Storage Card */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-700 rounded-2xl p-5 text-white">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-blue-100 text-sm">Current Storage</p>
            <p className="text-2xl font-bold mt-1">{storageQuotaGb.toFixed(1)} GB</p>
          </div>
          <div className="text-right">
            <p className="text-blue-100 text-sm">Used</p>
            <p className="text-lg font-semibold">{storagePercent}%</p>
          </div>
        </div>
        <div className="mt-4">
          <div className="h-2 bg-blue-900/50 rounded-full overflow-hidden">
            <div 
              className={`h-full rounded-full transition-all ${storagePercent > 80 ? 'bg-red-400' : storagePercent > 60 ? 'bg-yellow-300' : 'bg-green-400'}`}
              style={{ width: `${Math.min(storagePercent, 100)}%` }}
            />
          </div>
          <p className="text-blue-200 text-xs mt-2">
            {storageUsedGb.toFixed(2)} GB used of {storageQuotaGb.toFixed(2)} GB
          </p>
        </div>
      </div>

      {/* Requests List */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        {requests.length === 0 ? (
          <div className="text-center py-16 px-4">
            <div className="h-16 w-16 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-4">
              <FaHdd className="text-2xl text-blue-400" />
            </div>
            <h3 className="text-base font-semibold text-gray-800 mb-1">No storage requests yet</h3>
            <p className="text-sm text-gray-500 max-w-sm mx-auto mb-4">
              Need more storage space? Submit a request and an admin will review it.
            </p>
            <button 
              onClick={() => setShowRequestModal(true)}
              className="inline-flex items-center gap-2 bg-blue-600 text-white text-sm font-medium px-4 py-2 rounded-xl hover:bg-blue-700"
            >
              <FaPlus className="text-xs" /> Request Storage
            </button>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {requests.map((request) => (
              <div key={request.id} className="p-5 hover:bg-gray-50/50 transition-colors">
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      {getStatusBadge(request.status)}
                      <span className="text-xs text-gray-400">
                        {new Date(request.createdAt).toLocaleDateString()} at {new Date(request.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    
                    <div className="flex items-center gap-2 text-sm mb-2">
                      <span className="text-gray-500">{request.previousQuotaGb} GB</span>
                      <span className="text-gray-300">→</span>
                      <span className="font-semibold text-blue-600">{request.requestedQuotaGb} GB</span>
                      <span className="text-xs text-green-600 bg-green-50 px-2 py-0.5 rounded-full">
                        +{request.requestedQuotaGb - request.previousQuotaGb} GB
                      </span>
                    </div>
                    
                    <p className="text-sm text-gray-600 bg-gray-50 rounded-lg p-3">
                      <span className="font-medium text-gray-700">Reason:</span> {request.reason}
                    </p>
                    
                    {request.adminNotes && (
                      <p className="text-sm text-gray-500 mt-2">
                        <span className="font-medium">Admin note:</span> {request.adminNotes}
                      </p>
                    )}
                    
                    {request.status === 'APPROVED' && request.approvedBy && (
                      <p className="text-xs text-gray-400 mt-2">
                        Approved by {request.approvedBy} on {new Date(request.approvedAt).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  
                  {request.status === 'PENDING' && (
                    <button
                      onClick={() => handleCancel(request.id)}
                      disabled={cancellingId === request.id}
                      className="flex items-center gap-1.5 text-red-600 hover:text-red-700 text-sm font-medium px-3 py-1.5 rounded-lg hover:bg-red-50 transition-colors"
                    >
                      <FaTrash className="text-xs" />
                      {cancellingId === request.id ? 'Cancelling...' : 'Cancel'}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Storage Request Modal */}
      <StorageRequestModal 
        isOpen={showRequestModal}
        onClose={() => setShowRequestModal(false)}
        currentQuotaGb={Math.round(storageQuotaGb)}
        storageUsedGb={storageUsedGb}
      />
    </div>
  );
};

export default MyStorageRequests;
