import React, { useState, useEffect } from 'react';
import { FaUsers, FaDownload, FaEye, FaFile, FaFilePdf, FaFileWord, FaFileExcel, FaFileImage, FaFileArchive, FaShieldAlt, FaTimes, FaLock } from 'react-icons/fa';
import { shareAPI, fileAPI } from '../services/api';
import { toast } from 'react-toastify';
import { formatDistanceToNow } from 'date-fns';

const SharedFiles = () => {
  const [sharedWithMe, setSharedWithMe] = useState([]);
  const [sharedByMe, setSharedByMe] = useState([]);
  const [activeTab, setActiveTab] = useState('with-me');
  const [loading, setLoading] = useState(true);
  const [previewFile, setPreviewFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [docxContent, setDocxContent] = useState(null); // For Word document preview
  const [xlsxContent, setXlsxContent] = useState(null); // For Excel spreadsheet preview

  useEffect(() => { fetchSharedFiles(); }, []);

  const fetchSharedFiles = async () => {
    setLoading(true);
    try {
      const [withMeRes, byMeRes] = await Promise.all([
        shareAPI.getFilesSharedWithMe(),
        shareAPI.getFilesSharedByMe(),
      ]);
      setSharedWithMe(withMeRes.data || []);
      setSharedByMe(byMeRes.data || []);
    } catch {
      toast.error('Failed to load shared files');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (file) => {
    try {
      const response = await fileAPI.downloadFile(file.fileId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const a = document.createElement('a');
      a.href = url; a.setAttribute('download', file.fileName);
      document.body.appendChild(a); a.click(); a.remove();
      toast.success('Downloaded!');
    } catch { toast.error('Download failed'); }
  };

  const handlePreview = async (file) => {
    setPreviewFile(file);
    setPreviewLoading(true);
    setDocxContent(null); // Reset DOCX content
    setXlsxContent(null); // Reset XLSX content
    try {
      const response = await fileAPI.previewFile(file.fileId);
      const blob = new Blob([response.data], { type: file.mimeType || 'application/octet-stream' });
      
      const name = (file.fileName || '').toLowerCase();
      
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
      
      setPreviewUrl(window.URL.createObjectURL(blob));
    } catch {
      toast.error('Failed to preview file');
      setPreviewFile(null);
    } finally { setPreviewLoading(false); }
  };

  const closePreview = () => {
    if (previewUrl) window.URL.revokeObjectURL(previewUrl);
    setPreviewFile(null);
    setDocxContent(null);
    setXlsxContent(null);
    setPreviewUrl(null);
  };

  const handleUnshare = async (shareId) => {
    if (!window.confirm('Stop sharing this file?')) return;
    try {
      await shareAPI.unshareFile(shareId);
      toast.success('Unshared');
      fetchSharedFiles();
    } catch { toast.error('Failed to unshare'); }
  };

  const fmt = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024, s = ['B','KB','MB','GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + s[i];
  };

  const permBadge = (p) => {
    const map = { VIEW: 'bg-gray-100 text-gray-600', DOWNLOAD: 'bg-blue-100 text-blue-700', EDIT: 'bg-green-100 text-green-700' };
    return <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${map[p] || 'bg-gray-100 text-gray-600'}`}>{p}</span>;
  };

  const getFileStyle = (name = '') => {
    const ext = name.split('.').pop().toLowerCase();
    if (ext === 'pdf') return { Icon: FaFilePdf, cls: 'text-red-500 bg-red-50' };
    if (['doc','docx'].includes(ext)) return { Icon: FaFileWord, cls: 'text-blue-500 bg-blue-50' };
    if (['xls','xlsx'].includes(ext)) return { Icon: FaFileExcel, cls: 'text-green-500 bg-green-50' };
    if (['jpg','jpeg','png','gif','webp'].includes(ext)) return { Icon: FaFileImage, cls: 'text-purple-500 bg-purple-50' };
    if (['zip','rar','7z','tar'].includes(ext)) return { Icon: FaFileArchive, cls: 'text-yellow-500 bg-yellow-50' };
    return { Icon: FaFile, cls: 'text-gray-400 bg-gray-50' };
  };

  const FileTable = ({ list, isOwner }) => {
    if (!list.length) return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <div className="h-12 w-12 bg-blue-50 rounded-xl flex items-center justify-center mb-3">
          <FaUsers className="text-blue-300 text-xl" />
        </div>
        <p className="text-gray-600 font-medium">No files here</p>
        <p className="text-sm text-gray-400 mt-1">{isOwner ? 'You haven\'t shared any files yet' : 'No files have been shared with you'}</p>
      </div>
    );
    return (
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-100 text-xs text-gray-500 uppercase tracking-wider">
              <th className="px-4 py-3 text-left font-semibold">File</th>
              <th className="px-4 py-3 text-left font-semibold hidden sm:table-cell">{isOwner ? 'Shared With' : 'Shared By'}</th>
              <th className="px-4 py-3 text-left font-semibold hidden md:table-cell">Permission</th>
              <th className="px-4 py-3 text-left font-semibold hidden lg:table-cell">When</th>
              <th className="px-4 py-3 text-right font-semibold">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {list.map((item) => {
              const { Icon: FileIcon, cls: fileCls } = getFileStyle(item.fileName);
              return (
              <tr key={item.id || item.fileId} className="hover:bg-gray-50 transition-colors">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <div className={`h-8 w-8 rounded-lg flex items-center justify-center flex-shrink-0 ${fileCls}`}>
                      <FileIcon className="text-sm" />
                    </div>
                    <div className="min-w-0">
                      <p className="font-medium text-gray-800 truncate max-w-[150px]">{item.fileName}</p>
                      <p className="text-xs text-gray-400">{fmt(item.fileSize || 0)}</p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3 text-gray-500 hidden sm:table-cell">{isOwner ? (item.sharedWithFullName || '—') : (item.ownerFullName || '—')}</td>
                <td className="px-4 py-3 hidden md:table-cell">{permBadge(item.permission)}</td>
                <td className="px-4 py-3 text-gray-400 text-xs hidden lg:table-cell">{formatDistanceToNow(new Date(item.sharedAt || item.createdAt || Date.now()), { addSuffix: true })}</td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-end gap-2">
                    <button onClick={() => handlePreview(item)} className="p-1.5 rounded-lg text-green-500 hover:bg-green-50 transition-colors" title="Preview">
                      <FaEye className="text-sm" />
                    </button>
                    {(isOwner || item.permission === 'DOWNLOAD' || item.permission === 'EDIT') && (
                      <button onClick={() => handleDownload(item)} className="p-1.5 rounded-lg text-blue-500 hover:bg-blue-50 transition-colors" title="Download">
                        <FaDownload className="text-sm" />
                      </button>
                    )}
                    {isOwner && (
                      <button onClick={() => handleUnshare(item.id)} className="p-1.5 rounded-lg text-red-400 hover:bg-red-50 transition-colors" title="Stop sharing">
                        <FaTimes className="text-sm" />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            );
            })}
          </tbody>
        </table>
      </div>
    );
  };

  const tabs = [
    { id: 'with-me', label: 'Shared With Me', count: sharedWithMe.length },
    { id: 'by-me', label: 'Shared By Me', count: sharedByMe.length },
  ];

  return (
    <div className="space-y-5">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-gray-800">Shared Files</h1>
        <p className="text-sm text-gray-500 mt-0.5">Files shared within UDOM — all transfers are encrypted end-to-end</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Shared With Me', value: sharedWithMe.length, color: 'text-blue-600 bg-blue-50' },
          { label: 'Shared By Me', value: sharedByMe.length, color: 'text-indigo-600 bg-indigo-50' },
          { label: 'Download Access', value: sharedWithMe.filter(i => i.permission !== 'VIEW').length, color: 'text-emerald-600 bg-emerald-50' },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-white rounded-2xl p-4 shadow-sm border border-gray-100">
            <p className="text-xs text-gray-500 font-medium">{label}</p>
            <p className={`text-2xl font-bold mt-1 ${color.split(' ')[0]}`}>{value}</p>
          </div>
        ))}
      </div>

      {/* Table card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {/* Tabs */}
        <div className="flex border-b border-gray-100 px-4 pt-1">
          {tabs.map(t => (
            <button key={t.id} onClick={() => setActiveTab(t.id)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === t.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
              {t.label}
              <span className={`text-xs px-1.5 py-0.5 rounded-full font-semibold ${activeTab === t.id ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500'}`}>{t.count}</span>
            </button>
          ))}
        </div>

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          </div>
        ) : (
          <>
            {activeTab === 'with-me' && <FileTable list={sharedWithMe} isOwner={false} />}
            {activeTab === 'by-me' && <FileTable list={sharedByMe} isOwner={true} />}
          </>
        )}
      </div>

      {/* Preview Modal */}
      {previewFile && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4 backdrop-blur-sm" onClick={closePreview}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between p-4 border-b border-gray-100">
              <div className="flex items-center gap-2 min-w-0">
                <FaFile className="text-blue-500 flex-shrink-0" />
                <h2 className="text-sm font-bold text-gray-800 truncate">{previewFile.fileName}</h2>
              </div>
              <div className="flex items-center gap-2">
                {(previewFile.permission !== 'VIEW') && (
                  <button onClick={() => handleDownload(previewFile)} className="p-2 rounded-lg text-blue-500 hover:bg-blue-50" title="Download">
                    <FaDownload className="text-sm" />
                  </button>
                )}
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
                  const name = (previewFile.fileName || '').toLowerCase();
                  if (mime.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp|svg)$/.test(name)) return <img src={previewUrl} alt={name} className="max-w-full max-h-[70vh] object-contain" />;
                  if (mime.startsWith('video/') || /\.(mp4|webm|mov|avi)$/.test(name)) return <video src={previewUrl} controls className="max-w-full max-h-[70vh]" />;
                  if (mime.startsWith('audio/') || /\.(mp3|wav|ogg|m4a)$/.test(name)) return <audio src={previewUrl} controls className="w-full" />;
                  if (mime === 'application/pdf' || name.endsWith('.pdf')) return <iframe src={previewUrl} title="PDF" className="w-full h-[70vh] bg-white" />;
                  if (mime.startsWith('text/') || /\.(txt|csv|json|xml|md|log)$/.test(name)) return <iframe src={previewUrl} title="Text" className="w-full h-[70vh] bg-white" />;
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
                        <p className="text-xs text-gray-400 mt-1">Preview loading or unavailable.</p>
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">Download File</button>
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
                        <p className="text-xs text-gray-400 mt-1">Preview loading or unavailable.</p>
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">Download File</button>
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
                        <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">Download File</button>
                      </div>
                    );
                  }
                  
                  return (
                    <div className="text-center py-12">
                      <FaFile className="text-gray-300 text-5xl mx-auto mb-3" />
                      <p className="text-gray-600 font-medium">Preview not available for this file type</p>
                      <p className="text-xs text-gray-400 mt-1">Supported: Images, Videos, Audio, PDF, Text, Word (DOCX), Excel (XLSX), PowerPoint (PPTX)</p>
                      <button onClick={() => handleDownload(previewFile)} className="mt-4 bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-xl hover:bg-blue-700">Download File</button>
                    </div>
                  );
                })()
              ) : null}
            </div>
          </div>
        </div>
      )}

      {/* Security note */}
      <div className="flex items-start gap-3 bg-blue-50 border border-blue-100 rounded-xl px-4 py-3 text-sm text-blue-700">
        <FaLock className="flex-shrink-0 mt-0.5" />
        <span>All shared files are <strong>end-to-end encrypted</strong>. Only UDOM staff can receive shared files and owners can revoke access at any time.</span>
      </div>
    </div>
  );
};

export default SharedFiles;
