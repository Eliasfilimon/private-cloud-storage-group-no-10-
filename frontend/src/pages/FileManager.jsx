import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import ShareFileModal from '../components/ShareFileModal';
import { fileAPI } from '../services/api';
import { toast } from 'react-toastify';
import { 
  FaUpload, FaSearch, FaDownload, FaTrash, FaFile, FaFilePdf, 
  FaFileWord, FaFileExcel, FaFileImage, FaFileVideo, FaFileAudio,
  FaFileArchive, FaLock, FaLockOpen, FaTimes, FaSort, FaSortUp, FaSortDown, FaFilter, FaShare
} from 'react-icons/fa';

const FileManager = () => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadFile, setUploadFile] = useState(null);
  const [encrypt, setEncrypt] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [sortBy, setSortBy] = useState('uploadDate'); // name, uploadDate, size
  const [sortOrder, setSortOrder] = useState('desc'); // asc, desc
  const [filterType, setFilterType] = useState('all'); // all, image, document, video, audio, archive
  const [shareModalFile, setShareModalFile] = useState(null);

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      setLoading(true);
      const response = await fileAPI.getUserFiles();
      setFiles(response.data);
    } catch (error) {
      toast.error('Failed to load files');
      console.error('Error fetching files:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setUploadFile(file);
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) {
      toast.error('Please select a file');
      return;
    }

    const formData = new FormData();
    formData.append('file', uploadFile);
    formData.append('encrypt', encrypt);

    try {
      setUploading(true);
      await fileAPI.uploadFile(formData);
      toast.success('File uploaded successfully!');
      setShowUploadModal(false);
      setUploadFile(null);
      fetchFiles();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (file) => {
    try {
      const response = await fileAPI.downloadFile(file.id);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.originalName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('File downloaded successfully!');
    } catch (error) {
      toast.error('Failed to download file');
    }
  };

  const handleDelete = async (fileId) => {
    if (window.confirm('Are you sure you want to delete this file?')) {
      try {
        await fileAPI.deleteFile(fileId);
        toast.success('File deleted successfully!');
        fetchFiles();
      } catch (error) {
        toast.error('Failed to delete file');
      }
    }
  };

  const getFileIcon = (mimeType) => {
    if (!mimeType) return <FaFile className="text-gray-500 text-3xl" />;
    
    if (mimeType.includes('pdf')) return <FaFilePdf className="text-red-500 text-3xl" />;
    if (mimeType.includes('word') || mimeType.includes('document')) 
      return <FaFileWord className="text-blue-600 text-3xl" />;
    if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) 
      return <FaFileExcel className="text-green-600 text-3xl" />;
    if (mimeType.includes('image')) return <FaFileImage className="text-purple-500 text-3xl" />;
    if (mimeType.includes('video')) return <FaFileVideo className="text-pink-500 text-3xl" />;
    if (mimeType.includes('audio')) return <FaFileAudio className="text-yellow-500 text-3xl" />;
    if (mimeType.includes('zip') || mimeType.includes('rar') || mimeType.includes('compressed')) 
      return <FaFileArchive className="text-orange-500 text-3xl" />;
    
    return <FaFile className="text-gray-500 text-3xl" />;
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getFileType = (filename) => {
    const ext = filename.split('.').pop().toLowerCase();
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'].includes(ext)) return 'image';
    if (['pdf', 'doc', 'docx', 'txt', 'xls', 'xlsx', 'ppt', 'pptx'].includes(ext)) return 'document';
    if (['mp4', 'avi', 'mkv', 'mov', 'wmv'].includes(ext)) return 'video';
    if (['mp3', 'wav', 'flac', 'aac', 'm4a'].includes(ext)) return 'audio';
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'archive';
    return 'other';
  };

  const toggleSort = (field) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('desc');
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) return <FaSort className="inline ml-1 text-gray-400" />;
    return sortOrder === 'asc' ? 
      <FaSortUp className="inline ml-1 text-udom-blue-600" /> : 
      <FaSortDown className="inline ml-1 text-udom-blue-600" />;
  };

  const filteredFiles = files
    .filter(file => {
      // Search filter
      if (!file.originalName.toLowerCase().includes(searchTerm.toLowerCase())) {
        return false;
      }
      // Type filter
      if (filterType !== 'all' && getFileType(file.originalName) !== filterType) {
        return false;
      }
      return true;
    })
    .sort((a, b) => {
      let comparison = 0;
      switch (sortBy) {
        case 'name':
          comparison = a.originalName.localeCompare(b.originalName);
          break;
        case 'size':
          comparison = a.fileSize - b.fileSize;
          break;
        case 'uploadDate':
        default:
          comparison = new Date(a.uploadDate) - new Date(b.uploadDate);
          break;
      }
      return sortOrder === 'asc' ? comparison : -comparison;
    });

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <Navbar />
      <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-gray-900">File Manager</h1>
          <button 
            onClick={() => setShowUploadModal(true)}
            className="btn-primary flex items-center space-x-2"
          >
            <FaUpload />
            <span>Upload File</span>
          </button>
        </div>

        {/* Search and Filter Bar */}
        <div className="card mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="relative">
              <FaSearch className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="Search files..."
                className="input-field pl-12"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <div className="flex items-center space-x-2">
              <FaFilter className="text-gray-400" />
              <select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                className="input-field"
              >
                <option value="all">All Files</option>
                <option value="image">Images</option>
                <option value="document">Documents</option>
                <option value="video">Videos</option>
                <option value="audio">Audio</option>
                <option value="archive">Archives</option>
              </select>
            </div>
          </div>
        </div>

        {/* Files Table */}
        <div className="card">
          <div className="overflow-x-auto">
            {loading ? (
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-udom-blue-600"></div>
              </div>
            ) : filteredFiles.length === 0 ? (
              <div className="text-center py-12">
                <FaFile className="mx-auto h-12 w-12 text-gray-400" />
                <h3 className="mt-2 text-sm font-medium text-gray-900">No files found</h3>
                <p className="mt-1 text-sm text-gray-500">
                  {searchTerm || filterType !== 'all' ? 'Try adjusting your filters' : 'Get started by uploading a file'}
                </p>
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th 
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => toggleSort('name')}
                    >
                      File Name {getSortIcon('name')}
                    </th>
                    <th 
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => toggleSort('size')}
                    >
                      Size {getSortIcon('size')}
                    </th>
                    <th 
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                      onClick={() => toggleSort('uploadDate')}
                    >
                      Upload Date {getSortIcon('uploadDate')}
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Encrypted
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredFiles.map((file) => (
                    <tr key={file.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center space-x-3">
                          <div className="text-xl">
                            {getFileIcon(file.mimeType)}
                          </div>
                          <div className="text-sm font-medium text-gray-900 truncate max-w-xs">
                            {file.originalName}
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        {formatFileSize(file.fileSize)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        {formatDate(file.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        {file.encrypted ? (
                          <span className="inline-flex items-center space-x-1 text-green-600">
                            <FaLock className="text-sm" />
                            <span>Yes</span>
                          </span>
                        ) : (
                          <span className="text-gray-500">No</span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2 flex justify-end">
                        <buttonsetShareModalFile(file)}
                          className="text-udom-blue-600 hover:text-udom-blue-900 transition-colors"
                          title="Share"
                        >
                          <FaShare />
                        </button>
                        <button
                          onClick={() => handleDownload(file)}
                          className="text-green-600 hover:text-green
                          className="text-udom-blue-600 hover:text-udom-blue-900 transition-colors"
                          title="Download"
                        >
                          <FaDownload />
                        </button>
                        <button
                          onClick={() => handleDelete(file.id)}
                          className="text-red-600 hover:text-red-900 transition-colors"
                          title="Delete"
                        >
                          <FaTrash />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-gray-900">Upload File</h2>
              <button
                onClick={() => {
                  setShowUploadModal(false);
                  setUploadFile(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <FaTimes className="text-2xl" />
              </button>
            </div>

            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Select File
                </label>
                <input
                  type="file"
                  onChange={handleFileSelect}
                  className="block w-full text-sm text-gray-500
                    file:mr-4 file:py-2 file:px-4
                    file:rounded-lg file:border-0
                    file:text-sm file:font-semibold
                    file:bg-udom-blue-50 file:text-udom-blue-700
                    hover:file:bg-udom-blue-100 file:cursor-pointer"
                />
                {uploadFile && (
                  <p className="mt-2 text-sm text-gray-600">
                    Selected: {uploadFile.name} ({formatFileSize(uploadFile.size)})
                  </p>
                )}
              </div>

              <div className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id="encrypt"
                  checked={encrypt}
                  onChange={(e) => setEncrypt(e.target.checked)}
                  className="w-4 h-4 text-udom-blue-600 rounded focus:ring-udom-blue-500"
                />
                <label htmlFor="encrypt" className="flex items-center text-sm font-medium text-gray-700">
                  {encrypt ? <FaLock className="mr-2 text-green-500" /> : <FaLockOpen className="mr-2 text-gray-400" />}
                  Encrypt file (AES-256)
                </label>
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    setShowUploadModal(false);
                    setUploadFile(null);
                  }}
                  className="flex-1 btn-secondary"
                  disabled={uploading}
                >
                  Cancel
                </button>
                <button
                  onClick={handleUpload}
                  className="flex-1 btn-primary"
                  disabled={!uploadFile || uploading}
                >

      {/* Share Modal */}
      {shareModalFile && (
        <ShareFileModal
          file={shareModalFile}
          onClose={() => setShareModalFile(null)}
          onSuccess={() => {
            setShareModalFile(null);
            fetchFiles();
          }}
        />
      )}
                  {uploading ? 'Uploading...' : 'Upload'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FileManager;
