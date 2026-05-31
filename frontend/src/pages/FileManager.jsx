import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ShareFileModal from '../components/ShareFileModal';
import { fileAPI, folderAPI } from '../services/api';
import { toast } from 'react-toastify';
import {
  FaUpload, FaSearch, FaDownload, FaEye, FaTrash, FaFile, FaFilePdf,
  FaFileWord, FaFileExcel, FaFileImage, FaFileVideo, FaFileAudio,
  FaFileArchive, FaLock, FaLockOpen, FaTimes, FaShare,
  FaFolder, FaChevronRight, FaPlus, FaHome, FaEdit, FaBars,
  FaCloud, FaFolderPlus, FaFolderOpen, FaFileAlt, FaEllipsisV
} from 'react-icons/fa';

const FileManager = () => {
  // File states
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadFiles, setUploadFiles] = useState([]);
  // Encryption is mandatory - all files are encrypted with AES-256-GCM
  const [uploading, setUploading] = useState(false);
  const [shareModalFile, setShareModalFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [dragOver, setDragOver] = useState(false);

  // Folder states
  const [folders, setFolders] = useState([]);
  const [currentFolderId, setCurrentFolderId] = useState(null);
  const [folderStack, setFolderStack] = useState([]);
  const [showCreateFolderModal, setShowCreateFolderModal] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [folderColor, setFolderColor] = useState('#0047AB');
  const [editingFolderId, setEditingFolderId] = useState(null);
  const [editFolderName, setEditFolderName] = useState('');
  const [uploadToFolderId, setUploadToFolderId] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const folderColors = ['#0047AB', '#E74C3C', '#2ECC71', '#F39C12', '#9B59B6', '#1ABC9C'];

  useEffect(() => {
    loadFolders();
    loadFiles();
  }, [currentFolderId]);

  const loadFolders = async () => {
    try {
      let response;
      if (currentFolderId) {
        response = await folderAPI.getSubFolders(currentFolderId);
      } else {
        response = await folderAPI.getRootFolders();
      }
      setFolders(response.data || []);
    } catch (error) {
      setFolders([]);
    }
  };

  const loadFiles = async () => {
    try {
      setLoading(true);
      let response;
      if (currentFolderId) {
        response = await fileAPI.getUserFiles(currentFolderId);
      } else {
        response = await fileAPI.getUserFiles();
      }
      setFiles(response.data || []);
    } catch (error) {
      setFiles([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) {
      toast.error('Please enter a folder name');
      return;
    }

    try {
      await folderAPI.createFolder({
        folderName: newFolderName,
        parentFolderId: currentFolderId,
        folderColor: folderColor
      });
      toast.success('Folder created successfully');
      setNewFolderName('');
      setFolderColor('#0047AB');
      setShowCreateFolderModal(false);
      loadFolders();
    } catch (error) {
      toast.error('Failed to create folder');
    }
  };

  const handleDeleteFolder = async (folderId) => {
    if (window.confirm('Are you sure you want to delete this folder?')) {
      try {
        await folderAPI.deleteFolder(folderId);
        toast.success('Folder deleted successfully');
        loadFolders();
      } catch (error) {
        toast.error('Failed to delete folder');
      }
    }
  };

  const handleNavigateToFolder = (folder) => {
    setFolderStack([...folderStack, { id: currentFolderId, name: folderStack.length === 0 && !currentFolderId ? 'Home' : (folders.find(f => f.id === currentFolderId)?.folderName || 'Home') }]);
    setCurrentFolderId(folder.id);
  };

  const handleNavigateBack = (index) => {
    if (index === -1) {
      setCurrentFolderId(null);
      setFolderStack([]);
    } else {
      const newStack = folderStack.slice(0, index);
      setCurrentFolderId(folderStack[index].id);
      setFolderStack(newStack);
    }
  };

  const handleEditFolder = (folderId, currentName) => {
    setEditingFolderId(folderId);
    setEditFolderName(currentName);
  };

  const handleSaveEditFolder = async (folderId) => {
    if (!editFolderName.trim()) {
      toast.error('Folder name cannot be empty');
      return;
    }

    try {
      await folderAPI.updateFolder(folderId, {
        folderName: editFolderName
      });
      toast.success('Folder renamed successfully');
      setEditingFolderId(null);
      setEditFolderName('');
      loadFolders();
    } catch (error) {
      toast.error('Failed to rename folder');
    }
  };

  const MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

  const handleFileSelect = (e) => {
    const selected = Array.from(e.target.files);
    const valid = selected.filter(f => {
      if (f.size > MAX_FILE_SIZE) { toast.error(`"${f.name}" exceeds 500 MB limit`); return false; }
      return true;
    });
    if (valid.length) setUploadFiles(prev => [...prev, ...valid]);
  };

  const handleUpload = async () => {
    if (uploadFiles.length === 0) {
      toast.error('Please select at least one file');
      return;
    }

    try {
      setUploading(true);
      const targetFolderId = uploadToFolderId !== null ? uploadToFolderId : currentFolderId;
      let success = 0;
      for (let i = 0; i < uploadFiles.length; i++) {
        const formData = new FormData();
        formData.append('file', uploadFiles[i]);
        if (targetFolderId) formData.append('folderId', targetFolderId);
        await fileAPI.uploadFile(formData, (e) => {
          if (e.total) setUploadProgress(Math.round(((i + e.loaded / e.total) / uploadFiles.length) * 100));
        });
        success++;
      }
      toast.success(`${success} file${success > 1 ? 's' : ''} uploaded successfully`);
      setUploadFiles([]);
      setUploadToFolderId(null);
      setUploadProgress(0);
      setShowUploadModal(false);
      loadFiles();
      // Refresh storage data in localStorage
      try {
        const me = await (await import('../services/api')).authAPI.getCurrentUser();
        const u = JSON.parse(localStorage.getItem('user') || '{}');
        localStorage.setItem('user', JSON.stringify({ ...u, storageUsed: me.data.storageUsed, storageQuota: me.data.storageQuota }));
        window.dispatchEvent(new Event('storage-updated'));
      } catch (_) {}
    } catch (error) {
      toast.error('Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  const [renamingFileId, setRenamingFileId] = useState(null);
  const [renameValue, setRenameValue] = useState('');
  const [selectedFiles, setSelectedFiles] = useState(new Set());
  const [bulkDeleting, setBulkDeleting] = useState(false);

  const toggleFileSelection = (fileId) => {
    setSelectedFiles(prev => {
      const next = new Set(prev);
      if (next.has(fileId)) next.delete(fileId); else next.add(fileId);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selectedFiles.size === filteredFiles.length) setSelectedFiles(new Set());
    else setSelectedFiles(new Set(filteredFiles.map(f => f.id)));
  };

  const handleBulkDelete = async () => {
    if (selectedFiles.size === 0) return;
    if (!window.confirm(`Delete ${selectedFiles.size} selected file(s)?`)) return;
    setBulkDeleting(true);
    let deleted = 0;
    for (const fileId of selectedFiles) {
      try { await fileAPI.deleteFile(fileId); deleted++; } catch {}
    }
    toast.success(`${deleted} file(s) deleted`);
    setSelectedFiles(new Set());
    setBulkDeleting(false);
    loadFiles();
  };

  const handleRename = async (fileId) => {
    if (!renameValue.trim()) { toast.error('Name cannot be empty'); return; }
    try {
      await fileAPI.renameFile(fileId, renameValue.trim());
      toast.success('File renamed');
      setRenamingFileId(null);
      loadFiles();
    } catch { toast.error('Failed to rename file'); }
  };

  // Move file state and handler
  const [movingFile, setMovingFile] = useState(null);
  const [targetFolderId, setTargetFolderId] = useState(null);

  const handleMove = async () => {
    if (!movingFile) return;
    try {
      await fileAPI.moveFile(movingFile.id, targetFolderId);
      toast.success('File moved successfully');
      setMovingFile(null);
      setTargetFolderId(null);
      loadFiles();
    } catch { toast.error('Failed to move file'); }
  };

  const [previewFile, setPreviewFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [docxContent, setDocxContent] = useState(null); // For Word document preview
  const [xlsxContent, setXlsxContent] = useState(null); // For Excel spreadsheet preview

  const handleDownload = async (file) => {
    try {
      const response = await fileAPI.downloadFile(file.id);
      const blob = new Blob([response.data], { type: file.mimeType || 'application/octet-stream' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = file.originalName || file.fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success('Download started');
    } catch (error) {
      toast.error('Failed to download file');
    }
  };

  const handlePreview = async (file) => {
    setPreviewFile(file);
    setPreviewLoading(true);
    setDocxContent(null); // Reset DOCX content
    setXlsxContent(null); // Reset XLSX content
    
    try {
      const response = await fileAPI.previewFile(file.id);
      const blob = new Blob([response.data], { type: file.mimeType || 'application/octet-stream' });
      const url = window.URL.createObjectURL(blob);
      setPreviewUrl(url);
      
      const name = (file.originalName || file.fileName || '').toLowerCase();
      
      // Handle DOCX files - convert to HTML for preview
      if (name.endsWith('.docx') || file.mimeType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
        try {
          const mammoth = await import('mammoth');
          const arrayBuffer = await blob.arrayBuffer();
          const result = await mammoth.convertToHtml({ arrayBuffer });
          setDocxContent(result.value);
        } catch (docxError) {
          console.warn('DOCX preview failed:', docxError);
        }
      }
      
      // Handle XLSX files - convert to JSON/HTML for preview
      if (name.endsWith('.xlsx') || name.endsWith('.xls') || file.mimeType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
        try {
          const XLSX = await import('xlsx');
          const arrayBuffer = await blob.arrayBuffer();
          const workbook = XLSX.read(arrayBuffer, { type: 'array' });
          const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
          const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 });
          setXlsxContent(jsonData);
        } catch (xlsxError) {
          console.warn('XLSX preview failed:', xlsxError);
        }
      }
      
      // PPTX files - no preview available, will show download prompt in UI
    } catch (error) {
      toast.error('Failed to preview file');
      setPreviewFile(null);
    } finally {
      setPreviewLoading(false);
    }
  };

  const closePreview = () => {
    if (previewUrl) window.URL.revokeObjectURL(previewUrl);
    setPreviewFile(null);
    setPreviewUrl(null);
    setDocxContent(null);
    setXlsxContent(null);
  };

  const handleDeleteFile = async (fileId) => {
    if (window.confirm('Are you sure you want to delete this file?')) {
      try {
        await fileAPI.deleteFile(fileId);
        toast.success('File deleted successfully');
        loadFiles();
      } catch (error) {
        toast.error('Failed to delete file');
      }
    }
  };

  const getFileIcon = (fileName) => {
    const ext = fileName.split('.').pop().toLowerCase();
    if (['jpg', 'jpeg', 'png', 'gif'].includes(ext)) return <FaFileImage />;
    if (['pdf'].includes(ext)) return <FaFilePdf />;
    if (['doc', 'docx'].includes(ext)) return <FaFileWord />;
    if (['xls', 'xlsx'].includes(ext)) return <FaFileExcel />;
    if (['mp4', 'avi', 'mov'].includes(ext)) return <FaFileVideo />;
    if (['mp3', 'wav'].includes(ext)) return <FaFileAudio />;
    if (['zip', 'rar'].includes(ext)) return <FaFileArchive />;
    return <FaFile />;
  };

  const [searchResults, setSearchResults] = useState(null);
  const [isSearching, setIsSearching] = useState(false);

  // Debounced backend search
  useEffect(() => {
    if (!searchTerm.trim()) {
      setSearchResults(null);
      return;
    }
    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const r = await fileAPI.searchFiles(searchTerm);
        setSearchResults(r.data || []);
      } catch {
        toast.error('Search failed');
      } finally {
        setIsSearching(false);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const filteredFiles = searchResults !== null ? searchResults : files;

  const formatBytes = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  };

  const getFileColorClass = (fileName) => {
    const ext = fileName.split('.').pop().toLowerCase();
    if (ext === 'pdf') return 'text-red-500 bg-red-50';
    if (['doc', 'docx'].includes(ext)) return 'text-blue-600 bg-blue-50';
    if (['xls', 'xlsx'].includes(ext)) return 'text-green-600 bg-green-50';
    if (['jpg', 'jpeg', 'png', 'gif'].includes(ext)) return 'text-purple-500 bg-purple-50';
    if (['zip', 'rar'].includes(ext)) return 'text-yellow-600 bg-yellow-50';
    return 'text-gray-500 bg-gray-50';
  };

  return (
    <div className="space-y-5">
      {/* Header + Toolbar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-800">My Files</h1>
          {/* Breadcrumb */}
          <div className="flex items-center gap-1.5 mt-1 text-sm text-gray-500">
            <button onClick={() => { setCurrentFolderId(null); setFolderStack([]); }}
              className="hover:text-blue-600 transition-colors flex items-center gap-1">
              <FaHome className="text-xs" /> Home
            </button>
            {folderStack.map((folder, i) => (
              <React.Fragment key={i}>
                <FaChevronRight className="text-xs text-gray-300" />
                <button onClick={() => handleNavigateBack(i)} className="hover:text-blue-600 transition-colors truncate max-w-[100px]">
                  {folder.name || 'Folder'}
                </button>
              </React.Fragment>
            ))}
          </div>
        </div>
        <div className="flex items-center gap-2 flex-shrink-0">
          <div className="relative">
            <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-xs" />
            <input type="text" placeholder="Search files..." value={searchTerm} onChange={e => setSearchTerm(e.target.value)}
              className={`pl-8 ${searchTerm ? 'pr-8' : 'pr-3'} py-2 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 w-52 transition-all`} />
            {isSearching && <div className="absolute right-2 top-1/2 -translate-y-1/2 h-4 w-4 border-2 border-blue-200 border-t-blue-600 rounded-full animate-spin" />}
            {searchTerm && !isSearching && (
              <button onClick={() => { setSearchTerm(''); setSearchResults(null); }} className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                <FaTimes className="text-xs" />
              </button>
            )}
          </div>
          <button onClick={() => setShowCreateFolderModal(true)}
            className="flex items-center gap-1.5 px-3 py-2 text-sm font-medium border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 transition-colors">
            <FaFolderPlus className="text-xs" /> Folder
          </button>
          <button onClick={() => setShowUploadModal(true)}
            className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors">
            <FaUpload className="text-xs" /> Upload
          </button>
        </div>
      </div>

      {/* Folders */}
      {folders.length > 0 && (
        <div>
          <h2 className="text-sm font-semibold text-gray-600 mb-2">Folders</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-6 gap-3">
            {folders.map(folder => (
              <div key={folder.id} className="bg-white rounded-xl border border-gray-100 p-3 cursor-pointer hover:shadow-md hover:border-blue-100 transition-all group"
                onClick={() => handleNavigateToFolder(folder)}>
                <div className="h-10 w-10 rounded-xl mb-2 flex items-center justify-center"
                  style={{ backgroundColor: (folder.folderColor || '#0047AB') + '20' }}>
                  <FaFolder style={{ color: folder.folderColor || '#0047AB' }} className="text-lg" />
                </div>
                <p className="text-xs font-medium text-gray-700 truncate">{folder.folderName}</p>
                <p className="text-xs text-gray-400 mt-0.5">{new Date(folder.createdAt).toLocaleDateString()}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Files */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div className="flex items-center justify-between px-5 py-3.5 border-b border-gray-50">
          <h2 className="text-sm font-semibold text-gray-700">Files</h2>
          <div className="flex items-center gap-3">
            {selectedFiles.size > 0 && (
              <button onClick={handleBulkDelete} disabled={bulkDeleting}
                className="flex items-center gap-1.5 text-xs font-medium text-red-600 bg-red-50 px-3 py-1.5 rounded-lg hover:bg-red-100 disabled:opacity-50">
                <FaTrash className="text-[10px]" /> {bulkDeleting ? 'Deleting...' : `Delete (${selectedFiles.size})`}
              </button>
            )}
            {searchResults !== null ? (
              <span className="text-xs text-blue-600 font-medium">{filteredFiles.length} results for "{searchTerm}"</span>
            ) : (
              <span className="text-xs text-gray-400">{loading ? 'Loading...' : `${filteredFiles.length} files`}</span>
            )}
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          </div>
        ) : filteredFiles.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-14 text-center px-4">
            <div className="h-14 w-14 bg-blue-50 rounded-2xl flex items-center justify-center mb-3">
              <FaFolderOpen className="text-blue-300 text-2xl" />
            </div>
            <p className="text-gray-700 font-medium">{searchTerm ? 'No matching files' : 'No files yet'}</p>
            <p className="text-sm text-gray-400 mt-1">{searchTerm ? 'Try different search terms' : 'Upload your first file to get started'}</p>
            {!searchTerm && (
              <button onClick={() => setShowUploadModal(true)}
                className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700 transition-colors">
                Upload File
              </button>
            )}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wider">
                  <th className="px-2 py-3 w-8">
                    <input type="checkbox" checked={filteredFiles.length > 0 && selectedFiles.size === filteredFiles.length}
                      onChange={toggleSelectAll} className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                  </th>
                  <th className="px-5 py-3 text-left font-semibold">Name</th>
                  <th className="px-5 py-3 text-left font-semibold hidden sm:table-cell">Size</th>
                  <th className="px-5 py-3 text-left font-semibold hidden md:table-cell">Type</th>
                  <th className="px-5 py-3 text-left font-semibold hidden lg:table-cell">Modified</th>
                  <th className="px-5 py-3 text-right font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {filteredFiles.map(file => (
                  <tr key={file.id} className={`hover:bg-gray-50 transition-colors ${selectedFiles.has(file.id) ? 'bg-blue-50/50' : ''}`}>
                    <td className="px-2 py-3">
                      <input type="checkbox" checked={selectedFiles.has(file.id)} onChange={() => toggleFileSelection(file.id)}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
                    </td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <div className={`h-8 w-8 rounded-lg flex items-center justify-center flex-shrink-0 ${getFileColorClass(file.fileName)}`}>
                          {getFileIcon(file.fileName)}
                        </div>
                        <div className="min-w-0">
                          {renamingFileId === file.id ? (
                            <div className="flex items-center gap-1">
                              <input type="text" value={renameValue} onChange={e => setRenameValue(e.target.value)}
                                onKeyDown={e => { if (e.key === 'Enter') handleRename(file.id); if (e.key === 'Escape') setRenamingFileId(null); }}
                                className="px-2 py-1 text-sm border border-blue-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 w-40" autoFocus />
                              <button onClick={() => handleRename(file.id)} className="text-green-500 text-xs p-1">✓</button>
                              <button onClick={() => setRenamingFileId(null)} className="text-gray-400 text-xs p-1">✕</button>
                            </div>
                          ) : (
                            <p className="font-medium text-gray-800 truncate max-w-[180px]">{file.originalName || file.fileName}</p>
                          )}
                          {file.encrypted && (
                            <div className="flex items-center gap-1 text-xs text-green-600">
                              <FaLock className="text-xs" /> Encrypted
                            </div>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-gray-500 hidden sm:table-cell">{formatBytes(file.fileSize)}</td>
                    <td className="px-5 py-3 hidden md:table-cell">
                      <span className="text-xs bg-blue-50 text-blue-600 font-medium px-2 py-0.5 rounded-md uppercase">
                        {file.fileName.split('.').pop()}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-gray-400 text-xs hidden lg:table-cell">{new Date(file.createdAt).toLocaleDateString()}</td>
                    <td className="px-5 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <button onClick={() => handlePreview(file)} className="p-1.5 rounded-lg text-green-500 hover:bg-green-50 transition-colors" title="Preview">
                          <FaEye className="text-sm" />
                        </button>
                        <button onClick={() => handleDownload(file)} className="p-1.5 rounded-lg text-blue-500 hover:bg-blue-50 transition-colors" title="Download">
                          <FaDownload className="text-sm" />
                        </button>
                        <button onClick={() => { setRenamingFileId(file.id); setRenameValue(file.originalName || file.fileName); }} className="p-1.5 rounded-lg text-amber-500 hover:bg-amber-50 transition-colors" title="Rename">
                          <FaEdit className="text-sm" />
                        </button>
                        <button onClick={() => { setMovingFile(file); setTargetFolderId(file.folderId || null); }} className="p-1.5 rounded-lg text-cyan-500 hover:bg-cyan-50 transition-colors" title="Move to folder">
                          <FaFolder className="text-sm" />
                        </button>
                        <button onClick={() => setShareModalFile(file)} className="p-1.5 rounded-lg text-indigo-500 hover:bg-indigo-50 transition-colors" title="Share">
                          <FaShare className="text-sm" />
                        </button>
                        <button onClick={() => handleDeleteFile(file.id)} className="p-1.5 rounded-lg text-red-400 hover:bg-red-50 transition-colors" title="Delete">
                          <FaTrash className="text-sm" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Upload File</h2>
              <button onClick={() => { setShowUploadModal(false); setUploadFiles([]); setUploadProgress(0); }} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <label
                className={`flex flex-col items-center justify-center border-2 border-dashed rounded-xl py-8 cursor-pointer transition-all ${dragOver ? 'border-blue-500 bg-blue-50 scale-[1.02]' : uploadFiles.length > 0 ? 'border-blue-400 bg-blue-50' : 'border-gray-200 hover:border-blue-300 hover:bg-blue-50'}`}
                onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                onDragLeave={() => setDragOver(false)}
                onDrop={(e) => { e.preventDefault(); setDragOver(false); const dropped = Array.from(e.dataTransfer.files).filter(f => { if (f.size > MAX_FILE_SIZE) { toast.error(`"${f.name}" exceeds 500 MB limit`); return false; } return true; }); if (dropped.length) setUploadFiles(prev => [...prev, ...dropped]); }}>
                <FaUpload className={`text-2xl mb-2 ${dragOver ? 'text-blue-600 animate-bounce' : uploadFiles.length > 0 ? 'text-blue-500' : 'text-gray-300'}`} />
                <p className="text-sm font-medium text-gray-600">{uploadFiles.length > 0 ? `${uploadFiles.length} file${uploadFiles.length > 1 ? 's' : ''} selected` : dragOver ? 'Drop your files here' : 'Click or drag files here'}</p>
                <p className="text-xs text-gray-400 mt-1">{uploadFiles.length > 0 ? `${formatBytes(uploadFiles.reduce((a, f) => a + f.size, 0))} total` : 'Any file type · max 500 MB per file'}</p>
                <input type="file" className="hidden" onChange={handleFileSelect} multiple />
              </label>

              {uploading && uploadProgress > 0 && (
                <div>
                  <div className="flex justify-between text-xs text-gray-500 mb-1">
                    <span>Uploading...</span>
                    <span>{uploadProgress}%</span>
                  </div>
                  <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                    <div className="h-full bg-blue-600 rounded-full transition-all duration-300" style={{ width: `${uploadProgress}%` }} />
                  </div>
                </div>
              )}

              {/* Folder Selector */}
              {folders.length > 0 && (
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1.5">Upload to Folder</label>
                  <select
                    value={uploadToFolderId !== null ? uploadToFolderId : (currentFolderId || '')}
                    onChange={e => setUploadToFolderId(e.target.value ? Number(e.target.value) : null)}
                    className="w-full px-3 py-2 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">Home (Root)</option>
                    {folders.map(folder => (
                      <option key={folder.id} value={folder.id}>{folder.folderName}</option>
                    ))}
                  </select>
                </div>
              )}

              <div className="flex items-center gap-2 bg-green-50 border border-green-100 rounded-lg p-3">
                <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                  <FaLock className="text-green-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-green-800">End-to-End Encryption Enabled</p>
                  <p className="text-xs text-green-600">All files are encrypted with AES-256-GCM before storage. Even admins cannot access your file contents.</p>
                </div>
              </div>
              <div className="flex gap-3">
                <button onClick={() => { setShowUploadModal(false); setUploadFiles([]); setUploadProgress(0); }} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleUpload} disabled={uploadFiles.length === 0 || uploading}
                  className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 disabled:opacity-50 font-medium">
                  {uploading ? `Uploading ${uploadProgress}%` : 'Upload'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Create Folder Modal */}
      {showCreateFolderModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">New Folder</h2>
              <button onClick={() => setShowCreateFolderModal(false)} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <input type="text" value={newFolderName} onChange={e => setNewFolderName(e.target.value)} placeholder="Folder name"
                className="w-full px-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" autoFocus
                onKeyDown={e => e.key === 'Enter' && handleCreateFolder()} />
              <div>
                <p className="text-xs font-medium text-gray-600 mb-2">Color</p>
                <div className="flex gap-2">
                  {folderColors.map(c => (
                    <button key={c} onClick={() => setFolderColor(c)}
                      className={`h-7 w-7 rounded-full transition-all ${folderColor === c ? 'ring-2 ring-offset-2 ring-blue-500 scale-110' : ''}`}
                      style={{ backgroundColor: c }} />
                  ))}
                </div>
              </div>
              <div className="flex gap-3">
                <button onClick={() => setShowCreateFolderModal(false)} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleCreateFolder} className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 font-medium">Create</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Move File Modal */}
      {movingFile && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between p-5 border-b border-gray-100">
              <h2 className="text-base font-bold text-gray-800">Move File</h2>
              <button onClick={() => { setMovingFile(null); setTargetFolderId(null); }} className="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100"><FaTimes /></button>
            </div>
            <div className="p-5 space-y-4">
              <p className="text-sm text-gray-600">Move <span className="font-medium text-gray-800">{movingFile.originalName || movingFile.fileName}</span> to:</p>
              <select
                value={targetFolderId || ''}
                onChange={e => setTargetFolderId(e.target.value ? Number(e.target.value) : null)}
                className="w-full px-3 py-2.5 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Home (Root)</option>
                {folders.map(folder => (
                  <option key={folder.id} value={folder.id}>{folder.folderName}</option>
                ))}
              </select>
              <div className="flex gap-3">
                <button onClick={() => { setMovingFile(null); setTargetFolderId(null); }} className="flex-1 border border-gray-200 text-gray-600 text-sm py-2.5 rounded-xl hover:bg-gray-50">Cancel</button>
                <button onClick={handleMove} className="flex-1 bg-blue-600 text-white text-sm py-2.5 rounded-xl hover:bg-blue-700 font-medium">Move</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Share Modal */}
      {shareModalFile && (
        <ShareFileModal file={shareModalFile} onClose={() => setShareModalFile(null)} />
      )}

      {/* Preview Modal */}
      {previewFile && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4 backdrop-blur-sm" onClick={closePreview}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-4 border-b border-gray-100">
              <div className="flex items-center gap-2 min-w-0">
                <FaFile className="text-blue-500 flex-shrink-0" />
                <h2 className="text-sm font-bold text-gray-800 truncate">{previewFile.originalName || previewFile.fileName}</h2>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => handleDownload(previewFile)} className="p-2 rounded-lg text-blue-500 hover:bg-blue-50" title="Download">
                  <FaDownload className="text-sm" />
                </button>
                <button onClick={closePreview} className="p-2 rounded-lg text-gray-400 hover:bg-gray-100">
                  <FaTimes />
                </button>
              </div>
            </div>
            <div className="flex-1 overflow-auto bg-gray-50 flex items-center justify-center p-4 min-h-[400px]">
              {previewLoading ? (
                <div className="h-12 w-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
              ) : previewUrl ? (
                (() => {
                  const mime = previewFile.mimeType || '';
                  const name = (previewFile.originalName || previewFile.fileName || '').toLowerCase();
                  if (mime.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp|svg)$/.test(name)) {
                    return <img src={previewUrl} alt={previewFile.originalName} className="max-w-full max-h-[70vh] object-contain" />;
                  }
                  if (mime.startsWith('video/') || /\.(mp4|webm|mov|avi)$/.test(name)) {
                    return <video src={previewUrl} controls className="max-w-full max-h-[70vh]" />;
                  }
                  if (mime.startsWith('audio/') || /\.(mp3|wav|ogg|m4a)$/.test(name)) {
                    return <audio src={previewUrl} controls className="w-full" />;
                  }
                  if (mime === 'application/pdf' || name.endsWith('.pdf')) {
                    return <iframe src={previewUrl} title="PDF" className="w-full h-[70vh] bg-white" />;
                  }
                  if (mime.startsWith('text/') || /\.(txt|csv|json|xml|md|log)$/.test(name)) {
                    return <iframe src={previewUrl} title="Text" className="w-full h-[70vh] bg-white" />;
                  }
                  // DOCX/Word documents - rendered as HTML
                  if (name.endsWith('.docx') || mime === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') {
                    if (docxContent) {
                      return (
                        <div 
                          className="w-full h-[70vh] bg-white p-8 overflow-auto prose prose-sm max-w-none"
                          dangerouslySetInnerHTML={{ __html: docxContent }}
                        />
                      );
                    }
                    return (
                      <div className="text-center py-12">
                        <FaFileWord className="text-blue-400 text-5xl mx-auto mb-3" />
                        <p className="text-gray-600 font-medium">Word Document</p>
                        <p className="text-xs text-gray-400 mt-1">Preview loading or unavailable. Download to view.</p>
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">
                          Download File
                        </button>
                      </div>
                    );
                  }
                  
                  // XLSX/Excel files - rendered as table
                  if (name.endsWith('.xlsx') || name.endsWith('.xls') || mime === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
                    if (xlsxContent && xlsxContent.length > 0) {
                      return (
                        <div className="w-full h-[70vh] bg-white overflow-auto">
                          <table className="w-full border-collapse text-sm">
                            <tbody>
                              {xlsxContent.map((row, rowIndex) => (
                                <tr key={rowIndex} className={rowIndex === 0 ? 'bg-gray-100 font-semibold' : 'border-b border-gray-100'}>
                                  {row.map((cell, cellIndex) => (
                                    <td key={cellIndex} className="p-2 border border-gray-200 min-w-[100px]">
                                      {cell}
                                    </td>
                                  ))}
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      );
                    }
                    return (
                      <div className="text-center py-12">
                        <FaFileExcel className="text-green-500 text-5xl mx-auto mb-3" />
                        <p className="text-gray-600 font-medium">Excel Spreadsheet</p>
                        <p className="text-xs text-gray-400 mt-1">Preview loading or unavailable. Download to view.</p>
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">
                          Download File
                        </button>
                      </div>
                    );
                  }
                  
                  // PPTX/PowerPoint files - simplified preview
                  if (name.endsWith('.pptx') || mime === 'application/vnd.openxmlformats-officedocument.presentationml.presentation') {
                    return (
                      <div className="text-center py-12">
                        <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center mx-auto mb-3">
                          <span className="text-orange-500 text-2xl">📊</span>
                        </div>
                        <p className="text-gray-600 font-medium">PowerPoint Presentation</p>
                        <p className="text-xs text-gray-400 mt-1">PPTX preview not available in browser. Please download to view.</p>
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">
                          Download File
                        </button>
                      </div>
                    );
                  }
                  
                  return (
                    <div className="text-center py-12">
                      <FaFile className="text-gray-300 text-5xl mx-auto mb-3" />
                      <p className="text-gray-600 font-medium">Preview not available for this file type</p>
                      <p className="text-xs text-gray-400 mt-1">Supported: Images, Videos, Audio, PDF, Text, Word (DOCX), Excel (XLSX), PowerPoint (PPTX)</p>
                      <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">
                        Download File
                      </button>
                    </div>
                  );
                })()
              ) : null}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default FileManager;
