import React, { useState, useEffect } from 'react';
import { FaTrash, FaFile, FaFilePdf, FaFileImage, FaFileWord, FaFileExcel, FaFileArchive, FaUndo, FaExclamationTriangle } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { trashAPI } from '../services/api';

const formatBytes = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024, s = ['B','KB','MB','GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return (bytes / Math.pow(k, i)).toFixed(1) + ' ' + s[i];
};

const getFileIcon = (name = '') => {
  const ext = name.split('.').pop().toLowerCase();
  if (ext === 'pdf') return <FaFilePdf className="text-red-400 text-lg" />;
  if (['doc','docx'].includes(ext)) return <FaFileWord className="text-blue-400 text-lg" />;
  if (['xls','xlsx'].includes(ext)) return <FaFileExcel className="text-green-400 text-lg" />;
  if (['jpg','jpeg','png','gif','webp'].includes(ext)) return <FaFileImage className="text-purple-400 text-lg" />;
  if (['zip','rar','7z','tar'].includes(ext)) return <FaFileArchive className="text-yellow-400 text-lg" />;
  return <FaFile className="text-gray-400 text-lg" />;
};

const Trash = () => {
  const [trashedFiles, setTrashedFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchTrash(); }, []);

  const fetchTrash = async () => {
    setLoading(true);
    try { const r = await trashAPI.getTrashedFiles(); setTrashedFiles(r.data || []); }
    catch { toast.error('Failed to load trash'); }
    finally { setLoading(false); }
  };

  const handleRestore = async (fileId) => {
    try { await trashAPI.restoreFile(fileId); toast.success('File restored'); fetchTrash(); }
    catch { toast.error('Failed to restore file'); }
  };

  const handlePermanentDelete = async (fileId) => {
    if (!window.confirm('Permanently delete this file? This cannot be undone.')) return;
    try { await trashAPI.permanentDelete(fileId); toast.success('File permanently deleted'); fetchTrash(); }
    catch { toast.error('Failed to delete file'); }
  };

  const handleEmptyTrash = async () => {
    if (!window.confirm('Permanently delete ALL items in trash? This cannot be undone.')) return;
    try { await trashAPI.emptyTrash(); toast.success('Trash emptied'); setTrashedFiles([]); }
    catch { toast.error('Failed to empty trash'); }
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-800">Trash</h1>
          <p className="text-sm text-gray-500 mt-0.5">Deleted files are permanently removed after 30 days</p>
        </div>
        {trashedFiles.length > 0 && (
          <button onClick={handleEmptyTrash}
            className="flex items-center gap-2 px-4 py-2 bg-red-50 border border-red-200 text-red-600 text-sm font-semibold rounded-xl hover:bg-red-100 transition-colors self-start sm:self-auto">
            <FaTrash className="text-xs" /> Empty Trash
          </button>
        )}
      </div>

      {/* Warning */}
      <div className="flex items-center gap-3 bg-amber-50 border border-amber-200 text-amber-700 rounded-xl px-4 py-3 text-sm">
        <FaExclamationTriangle className="flex-shrink-0" />
        <span>Items in Trash are <strong>permanently deleted</strong> after 30 days. Restore files you still need.</span>
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16">
            <div className="h-8 w-8 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          </div>
        ) : trashedFiles.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100 text-xs text-gray-500 uppercase tracking-wider">
                  <th className="px-5 py-3 text-left font-semibold">Name</th>
                  <th className="px-5 py-3 text-left font-semibold hidden sm:table-cell">Deleted</th>
                  <th className="px-5 py-3 text-left font-semibold hidden md:table-cell">Size</th>
                  <th className="px-5 py-3 text-left font-semibold hidden lg:table-cell">Type</th>
                  <th className="px-5 py-3 text-right font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {trashedFiles.map((file) => (
                  <tr key={file.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className="h-8 w-8 rounded-lg bg-gray-100 flex items-center justify-center flex-shrink-0">
                          {getFileIcon(file.fileName)}
                        </div>
                        <span className="font-medium text-gray-400 line-through truncate max-w-[180px]">{file.fileName}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500 text-xs hidden sm:table-cell">
                      {file.updatedAt ? new Date(file.updatedAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="px-5 py-3.5 text-gray-500 hidden md:table-cell">{formatBytes(file.fileSize)}</td>
                    <td className="px-5 py-3.5 hidden lg:table-cell">
                      <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-md uppercase">
                        {file.fileName?.split('.').pop()}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => handleRestore(file.id)}
                          className="flex items-center gap-1.5 text-xs text-blue-600 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg font-medium transition-colors">
                          <FaUndo className="text-xs" /> Restore
                        </button>
                        <button onClick={() => handlePermanentDelete(file.id)}
                          className="flex items-center gap-1.5 text-xs text-red-500 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg font-medium transition-colors">
                          <FaTrash className="text-xs" /> Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-16 text-center px-4">
            <div className="h-16 w-16 bg-gray-100 rounded-2xl flex items-center justify-center mb-4">
              <FaTrash className="text-gray-300 text-2xl" />
            </div>
            <p className="text-gray-700 font-semibold">Trash is empty</p>
            <p className="text-sm text-gray-400 mt-1">Deleted files will appear here</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Trash;
