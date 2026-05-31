import React, { useState, useEffect } from 'react';
import { fileAPI } from '../services/api';
import { toast } from 'react-toastify';
import {
  FaFilePdf, FaFileWord, FaFileExcel, FaFilePowerpoint, FaFileAlt,
  FaEye, FaDownload, FaSearch, FaThLarge, FaList, FaClock,
  FaFile, FaSortAmountDown, FaTimes
} from 'react-icons/fa';

const MyDocuments = () => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState('grid');
  const [sortBy, setSortBy] = useState('date');
  const [filterType, setFilterType] = useState('all');
  const [previewFile, setPreviewFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [docxContent, setDocxContent] = useState(null);
  const [xlsxContent, setXlsxContent] = useState(null);

  const documentTypes = {
    pdf: { ext: ['.pdf'], mime: 'application/pdf', icon: FaFilePdf, color: 'text-red-500', bg: 'bg-red-50', label: 'PDF' },
    word: { ext: ['.docx', '.doc'], mime: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', icon: FaFileWord, color: 'text-blue-500', bg: 'bg-blue-50', label: 'Word' },
    excel: { ext: ['.xlsx', '.xls'], mime: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', icon: FaFileExcel, color: 'text-green-500', bg: 'bg-green-50', label: 'Excel' },
    powerpoint: { ext: ['.pptx', '.ppt'], mime: 'application/vnd.openxmlformats-officedocument.presentationml.presentation', icon: FaFilePowerpoint, color: 'text-orange-500', bg: 'bg-orange-50', label: 'PowerPoint' },
    text: { ext: ['.txt', '.csv', '.json', '.xml', '.md', '.log'], mime: 'text/', icon: FaFileAlt, color: 'text-gray-500', bg: 'bg-gray-50', label: 'Text' },
  };

  useEffect(() => { loadDocuments(); }, []);

  const loadDocuments = async () => {
    setLoading(true);
    try {
      const response = await fileAPI.getUserFiles();
      const docFiles = (response.data || []).filter(file => isDocument(file));
      setFiles(docFiles);
    } catch (error) {
      toast.error('Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  const isDocument = (file) => {
    const name = (file.originalName || file.fileName || '').toLowerCase();
    const mime = file.mimeType || '';
    return Object.values(documentTypes).some(type => 
      type.ext.some(ext => name.endsWith(ext)) || 
      (type.mime !== 'text/' && mime.includes(type.mime)) ||
      (type.mime === 'text/' && mime.startsWith('text/'))
    );
  };

  const getFileType = (file) => {
    const name = (file.originalName || file.fileName || '').toLowerCase();
    const mime = file.mimeType || '';
    for (const [key, type] of Object.entries(documentTypes)) {
      if (type.ext.some(ext => name.endsWith(ext)) || 
          (type.mime !== 'text/' && mime.includes(type.mime)) ||
          (type.mime === 'text/' && mime.startsWith('text/'))) {
        return { key, ...type };
      }
    }
    return { key: 'unknown', icon: FaFile, color: 'text-gray-400', bg: 'bg-gray-100', label: 'Unknown' };
  };

  const filteredFiles = files
    .filter(file => {
      const matchesSearch = (file.originalName || file.fileName || '').toLowerCase().includes(searchTerm.toLowerCase());
      const matchesType = filterType === 'all' || getFileType(file).key === filterType;
      return matchesSearch && matchesType;
    })
    .sort((a, b) => {
      if (sortBy === 'date') return new Date(b.createdAt) - new Date(a.createdAt);
      if (sortBy === 'name') return (a.originalName || a.fileName).localeCompare(b.originalName || b.fileName);
      if (sortBy === 'size') return (b.fileSize || 0) - (a.fileSize || 0);
      return 0;
    });

  const recentFiles = files.slice(0, 5);
  const getTypeCount = (type) => files.filter(f => getFileType(f).key === type).length;

  const handlePreview = async (file) => {
    setPreviewFile(file);
    setPreviewLoading(true);
    setDocxContent(null);
    setXlsxContent(null);
    
    try {
      const response = await fileAPI.previewFile(file.id);
      const blob = new Blob([response.data], { type: file.mimeType || 'application/octet-stream' });
      const url = window.URL.createObjectURL(blob);
      setPreviewUrl(url);
      
      const name = (file.originalName || file.fileName || '').toLowerCase();
      const fileType = getFileType(file);
      
      if (fileType.key === 'word') {
        try {
          const mammoth = await import('mammoth');
          const arrayBuffer = await blob.arrayBuffer();
          const result = await mammoth.convertToHtml({ arrayBuffer });
          setDocxContent(result.value);
        } catch (e) { console.warn('DOCX preview failed:', e); }
      }
      
      if (fileType.key === 'excel') {
        try {
          const XLSX = await import('xlsx');
          const arrayBuffer = await blob.arrayBuffer();
          const workbook = XLSX.read(arrayBuffer, { type: 'array' });
          const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
          const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 });
          setXlsxContent(jsonData);
        } catch (e) { console.warn('XLSX preview failed:', e); }
      }
    } catch (error) {
      toast.error('Failed to preview document');
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

  const formatSize = (bytes) => {
    if (!bytes) return '0 B';
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + sizes[i];
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const renderPreviewContent = () => {
    if (!previewFile) return null;
    const mime = previewFile.mimeType || '';
    const name = (previewFile.originalName || previewFile.fileName || '').toLowerCase();
    const fileType = getFileType(previewFile);

    if (previewLoading) {
      return <div className="h-12 w-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />;
    }

    if (fileType.key === 'word' && docxContent) {
      return <div className="w-full h-full bg-white p-8 overflow-auto prose prose-sm max-w-none" dangerouslySetInnerHTML={{ __html: docxContent }} />;
    }

    if (fileType.key === 'excel' && xlsxContent) {
      return (
        <div className="w-full h-full bg-white overflow-auto">
          <table className="w-full border-collapse text-sm">
            <tbody>
              {xlsxContent.map((row, rowIndex) => (
                <tr key={rowIndex} className={rowIndex === 0 ? 'bg-gray-100 font-semibold' : 'border-b border-gray-100'}>
                  {row.map((cell, cellIndex) => (
                    <td key={cellIndex} className="p-2 border border-gray-200 min-w-[100px]">{cell}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    }

    if (mime === 'application/pdf' || name.endsWith('.pdf')) {
      return <iframe src={previewUrl} title="PDF" className="w-full h-full bg-white" />;
    }

    if (mime.startsWith('text/') || fileType.key === 'text') {
      return <iframe src={previewUrl} title="Text" className="w-full h-full bg-white" />;
    }

    return (
      <div className="text-center py-12">
        <fileType.icon className={`text-5xl mx-auto mb-3 ${fileType.color}`} />
        <p className="text-gray-600 font-medium">{fileType.label} Document</p>
        <p className="text-xs text-gray-400 mt-1">Preview not available for this format</p>
        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">
          Download to View
        </button>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">My Documents</h1>
          <p className="text-sm text-gray-500 mt-1">View and manage your PDF, Word, Excel, and other documents</p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">{files.length} documents</span>
        </div>
      </div>

      {/* Recent Documents */}
      {recentFiles.length > 0 && (
        <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
          <div className="flex items-center gap-2 mb-4">
            <FaClock className="text-blue-500" />
            <h2 className="font-semibold text-gray-700">Recent Documents</h2>
          </div>
          <div className="flex gap-4 overflow-x-auto pb-2">
            {recentFiles.map(file => {
              const type = getFileType(file);
              return (
                <button
                  key={file.id}
                  onClick={() => handlePreview(file)}
                  className="flex-shrink-0 w-40 p-4 bg-gray-50 rounded-xl hover:bg-blue-50 hover:border-blue-200 border border-transparent transition-all text-left group"
                >
                  <div className={`h-10 w-10 rounded-lg ${type.bg} flex items-center justify-center mb-3`}>
                    <type.icon className={type.color} />
                  </div>
                  <p className="text-sm font-medium text-gray-700 truncate group-hover:text-blue-600">{file.originalName || file.fileName}</p>
                  <p className="text-xs text-gray-400 mt-1">{formatDate(file.createdAt)}</p>
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Filters & Search */}
      <div className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 space-y-4">
        <div className="relative">
          <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search documents..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setFilterType('all')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${filterType === 'all' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            All ({files.length})
          </button>
          {Object.entries(documentTypes).map(([key, type]) => (
            <button
              key={key}
              onClick={() => setFilterType(key)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${filterType === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              <type.icon className={filterType === key ? 'text-white' : type.color} />
              {type.label} ({getTypeCount(key)})
            </button>
          ))}
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FaSortAmountDown className="text-gray-400" />
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="text-sm border border-gray-200 rounded-lg px-3 py-1.5 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="date">Sort by Date</option>
              <option value="name">Sort by Name</option>
              <option value="size">Sort by Size</option>
            </select>
          </div>
          <div className="flex bg-gray-100 rounded-lg p-1">
            <button onClick={() => setViewMode('grid')} className={`p-2 rounded-md transition-colors ${viewMode === 'grid' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
              <FaThLarge />
            </button>
            <button onClick={() => setViewMode('list')} className={`p-2 rounded-md transition-colors ${viewMode === 'list' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'}`}>
              <FaList />
            </button>
          </div>
        </div>
      </div>

      {/* Documents List */}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : filteredFiles.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-2xl border border-gray-100">
          <FaFile className="text-gray-300 text-5xl mx-auto mb-3" />
          <p className="text-gray-600 font-medium">No documents found</p>
          <p className="text-sm text-gray-400 mt-1">Upload PDF, Word, Excel, or text files to see them here</p>
        </div>
      ) : viewMode === 'grid' ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
          {filteredFiles.map(file => {
            const type = getFileType(file);
            return (
              <div key={file.id} className="bg-white rounded-xl border border-gray-100 p-4 hover:shadow-md hover:border-blue-200 transition-all group">
                <div className={`h-12 w-12 rounded-xl ${type.bg} flex items-center justify-center mb-3 mx-auto`}>
                  <type.icon className={`text-xl ${type.color}`} />
                </div>
                <p className="text-sm font-medium text-gray-700 text-center truncate mb-1" title={file.originalName || file.fileName}>
                  {file.originalName || file.fileName}
                </p>
                <p className="text-xs text-gray-400 text-center">{formatSize(file.fileSize)}</p>
                <p className="text-xs text-gray-400 text-center">{formatDate(file.createdAt)}</p>
                <div className="flex justify-center gap-2 mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => handlePreview(file)} className="p-1.5 rounded-lg text-blue-500 hover:bg-blue-50" title="Preview">
                    <FaEye />
                  </button>
                  <button onClick={() => handleDownload(file)} className="p-1.5 rounded-lg text-green-500 hover:bg-green-50" title="Download">
                    <FaDownload />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 overflow-hidden">
          {filteredFiles.map((file, index) => {
            const type = getFileType(file);
            return (
              <div key={file.id} className={`flex items-center gap-4 p-4 hover:bg-gray-50 transition-colors ${index !== filteredFiles.length - 1 ? 'border-b border-gray-100' : ''}`}>
                <div className={`h-10 w-10 rounded-lg ${type.bg} flex items-center justify-center flex-shrink-0`}>
                  <type.icon className={type.color} />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-700 truncate">{file.originalName || file.fileName}</p>
                  <p className="text-xs text-gray-400">{type.label} • {formatSize(file.fileSize)} • {formatDate(file.createdAt)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <button onClick={() => handlePreview(file)} className="p-2 rounded-lg text-blue-500 hover:bg-blue-50" title="Preview">
                    <FaEye />
                  </button>
                  <button onClick={() => handleDownload(file)} className="p-2 rounded-lg text-green-500 hover:bg-green-50" title="Download">
                    <FaDownload />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Preview Modal */}
      {previewFile && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4 backdrop-blur-sm" onClick={closePreview}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-4 border-b border-gray-100">
              <div className="flex items-center gap-2 min-w-0">
                {(() => {
                  const type = getFileType(previewFile);
                  return <type.icon className={`${type.color} flex-shrink-0`} />;
                })()}
                <h2 className="text-sm font-bold text-gray-800 truncate">{previewFile.originalName || previewFile.fileName}</h2>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => handleDownload(previewFile)} className="p-2 rounded-lg text-blue-500 hover:bg-blue-50" title="Download">
                  <FaDownload />
                </button>
                <button onClick={closePreview} className="p-2 rounded-lg text-gray-400 hover:bg-gray-100">
                  <FaTimes />
                </button>
              </div>
            </div>
            <div className="flex-1 overflow-auto bg-gray-50 flex items-center justify-center p-4 min-h-[400px]">
              {renderPreviewContent()}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyDocuments;
