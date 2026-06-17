import React, { useState } from 'react';
import { toast } from 'react-toastify';
import { storageRequestAPI } from '../services/api';
import { FaTimes, FaDatabase, FaExclamationTriangle } from 'react-icons/fa';

const StorageRequestModal = ({ isOpen, onClose, currentQuotaGb, storageUsedGb }) => {
  const [requestedQuota, setRequestedQuota] = useState(currentQuotaGb + 5);
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!isOpen) return null;

  const usagePercent = Math.round((storageUsedGb / currentQuotaGb) * 100);
  const isFull = usagePercent >= 95;

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (requestedQuota <= currentQuotaGb) {
      toast.error('Requested quota must be greater than your current quota');
      return;
    }
    
    if (!reason.trim()) {
      toast.error('Please provide a reason for the request');
      return;
    }

    setSubmitting(true);
    try {
      await storageRequestAPI.createRequest({
        requestedQuotaGb: requestedQuota,
        reason: reason
      });
      toast.success('Storage request submitted successfully!');
      onClose();
      setRequestedQuota(currentQuotaGb + 5);
      setReason('');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit request');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div role="dialog" aria-modal="true" className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
        <div className="flex items-center justify-between p-5 border-b border-gray-100">
          <div className="flex items-center gap-2">
            <div className={`p-2 rounded-lg ${isFull ? 'bg-red-100' : 'bg-blue-100'}`}>
              {isFull ? (
                <FaExclamationTriangle className="text-red-600" />
              ) : (
                <FaDatabase className="text-blue-600" />
              )}
            </div>
            <h2 className="text-base font-bold text-gray-800">
              {isFull ? 'Storage Full - Request More' : 'Request Additional Storage'}
            </h2>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100">
            <FaTimes />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {isFull && (
            <div className="bg-red-50 border border-red-100 rounded-xl p-3 text-sm text-red-700">
              <p className="font-medium">⚠️ Your storage is almost full ({usagePercent}% used)</p>
              <p className="text-xs mt-1">Request additional storage to continue uploading files.</p>
            </div>
          )}

          {/* Current Usage */}
          <div className="bg-gray-50 rounded-xl p-4">
            <p className="text-xs text-gray-500 mb-2">Current Storage Usage</p>
            <div className="flex justify-between text-sm mb-1">
              <span className="font-medium">{storageUsedGb.toFixed(2)} GB used</span>
              <span className="text-gray-500">of {currentQuotaGb} GB</span>
            </div>
            <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
              <div 
                className={`h-full rounded-full transition-all ${usagePercent >= 90 ? 'bg-red-500' : usagePercent >= 70 ? 'bg-yellow-500' : 'bg-green-500'}`}
                style={{ width: `${Math.min(usagePercent, 100)}%` }}
              />
            </div>
          </div>

          {/* Requested Quota */}
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Requested Total Storage (GB)
            </label>
            <input
              type="number"
              min={currentQuotaGb + 1}
              max={100}
              value={requestedQuota}
              onChange={(e) => setRequestedQuota(parseInt(e.target.value) || currentQuotaGb)}
              className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="text-xs text-gray-400 mt-1">
              Current: {currentQuotaGb} GB → Requested: {requestedQuota} GB 
              (+{requestedQuota - currentQuotaGb} GB)
            </p>
          </div>

          {/* Reason */}
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Reason for Request *
            </label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Explain why you need additional storage..."
              rows={3}
              className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              maxLength={1000}
            />
            <p className="text-xs text-gray-400 mt-1 text-right">{reason.length}/1000</p>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !reason.trim()}
              className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-50 font-medium"
            >
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default StorageRequestModal;
