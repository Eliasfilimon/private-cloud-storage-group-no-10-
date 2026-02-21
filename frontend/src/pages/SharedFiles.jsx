import React, { useState, useEffect } from 'react';
import { FaUsers, FaDownload, FaFile, FaUser, FaClock, FaShieldAlt } from 'react-icons/fa';
import { shareAPI, fileAPI } from '../services/api';
import { toast } from 'react-toastify';
import { formatDistanceToNow } from 'date-fns';

const SharedFiles = () => {
  const [sharedWithMe, setSharedWithMe] = useState([]);
  const [sharedByMe, setSharedByMe] = useState([]);
  const [activeTab, setActiveTab] = useState('with-me');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSharedFiles();
  }, []);

  const fetchSharedFiles = async () => {
    setLoading(true);
    try {
      const [withMeRes, byMeRes] = await Promise.all([
        shareAPI.getFilesSharedWithMe(),
        shareAPI.getFilesSharedByMe(),
      ]);
      setSharedWithMe(withMeRes.data);
      setSharedByMe(byMeRes.data);
    } catch (error) {
      toast.error('Failed to load shared files');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (file) => {
    try {
      const response = await fileAPI.downloadFile(file.fileId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('File downloaded successfully!');
    } catch (error) {
      toast.error('Failed to download file');
    }
  };

  const handleUnshare = async (shareId) => {
    if (!window.confirm('Are you sure you want to stop sharing this file?')) {
      return;
    }

    try {
      await shareAPI.unshareFile(shareId);
      toast.success('File unshared successfully');
      fetchSharedFiles();
    } catch (error) {
      toast.error('Failed to unshare file');
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const getPermissionBadge = (permission) => {
    const colors = {
      VIEW: 'bg-gray-100 text-gray-800',
      DOWNLOAD: 'bg-blue-100 text-blue-800',
      EDIT: 'bg-green-100 text-green-800',
    };
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${colors[permission] || colors.VIEW}`}>
        {permission}
      </span>
    );
  };

  const renderFileList = (files, isSharedByMe = false) => {
    if (files.length === 0) {
      return (
        <div className="text-center py-16">
          <FaUsers className="mx-auto text-6xl text-gray-300 mb-4" />
          <p className="text-gray-500 text-lg">
            {isSharedByMe ? 'You haven\'t shared any files yet' : 'No files shared with you'}
          </p>
        </div>
      );
    }

    return (
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b-2 border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                File
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                {isSharedByMe ? 'Shared With' : 'Owner'}
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Permission
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Shared
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Expires
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {files.map((share) => (
              <tr key={share.id} className="hover:bg-gray-50 transition">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <FaFile className="text-udom-blue-500 text-xl mr-3" />
                    <div>
                      <p className="font-medium text-gray-900">{share.fileName}</p>
                      <p className="text-sm text-gray-500">{formatFileSize(share.fileSize)}</p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <FaUser className="text-gray-400 mr-2" />
                    <div>
                      <p className="font-medium text-gray-900">
                        {isSharedByMe ? share.sharedWithFullName : share.ownerFullName}
                      </p>
                      <p className="text-sm text-gray-500">
                        {isSharedByMe ? share.sharedWithUsername : share.ownerUsername}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getPermissionBadge(share.permission)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div className="flex items-center">
                    <FaClock className="mr-2 text-gray-400" />
                    {formatDistanceToNow(new Date(share.sharedAt), { addSuffix: true })}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {share.expiresAt ? (
                    <span className="text-orange-600">
                      {formatDistanceToNow(new Date(share.expiresAt), { addSuffix: true })}
                    </span>
                  ) : (
                    <span className="text-gray-400">Never</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                  {(share.permission === 'DOWNLOAD' || share.permission === 'EDIT') && !isSharedByMe && (
                    <button
                      onClick={() => handleDownload(share)}
                      className="text-udom-blue-600 hover:text-udom-blue-800 font-medium flex items-center space-x-1"
                    >
                      <FaDownload />
                      <span>Download</span>
                    </button>
                  )}
                  {isSharedByMe && (
                    <button
                      onClick={() => handleUnshare(share.id)}
                      className="text-red-600 hover:text-red-800 font-medium"
                    >
                      Unshare
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-8 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-udom-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading shared files...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center space-x-3">
            <FaUsers className="text-udom-blue-600" />
            <span>Shared Files</span>
          </h1>
          <p className="text-gray-600 mt-2">
            Manage files shared with you and files you've shared with others
          </p>
        </div>

        {/* Tabs */}
        <div className="bg-white rounded-lg shadow-sm mb-6">
          <div className="border-b border-gray-200">
            <nav className="flex -mb-px">
              <button
                onClick={() => setActiveTab('with-me')}
                className={`px-6 py-4 text-sm font-medium border-b-2 transition ${
                  activeTab === 'with-me'
                    ? 'border-udom-blue-600 text-udom-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Shared With Me ({sharedWithMe.length})
              </button>
              <button
                onClick={() => setActiveTab('by-me')}
                className={`px-6 py-4 text-sm font-medium border-b-2 transition ${
                  activeTab === 'by-me'
                    ? 'border-udom-blue-600 text-udom-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                Shared By Me ({sharedByMe.length})
              </button>
            </nav>
          </div>

          {/* Content */}
          <div className="p-6">
            {activeTab === 'with-me' && renderFileList(sharedWithMe, false)}
            {activeTab === 'by-me' && renderFileList(sharedByMe, true)}
          </div>
        </div>

        {/* Info Box */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <div className="flex items-start space-x-3">
            <FaShieldAlt className="text-blue-600 text-xl mt-1" />
            <div>
              <h3 className="font-semibold text-blue-900 mb-2">Secure File Sharing</h3>
              <p className="text-blue-800 text-sm">
                All shared files maintain the same encryption and security standards. 
                You can only share files with other staff members within the institution.
                File owners can revoke access at any time.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SharedFiles;
