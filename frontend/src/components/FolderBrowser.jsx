import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { FaFolder, FaFolderPlus, FaChevronRight, FaEdit2, FaTrash2, FaArrowLeft } from 'react-icons/fa';
import { toast } from 'react-toastify';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const FolderBrowser = ({ onFolderSelect, onBack }) => {
  const [folderStack, setFolderStack] = useState([{ id: null, name: 'My Drive' }]);
  const [folders, setFolders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showNewFolderInput, setShowNewFolderInput] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [folderColor, setFolderColor] = useState('#4A90E2');

  const folderColors = [
    '#4A90E2', // Blue
    '#E74C3C', // Red
    '#2ECC71', // Green
    '#F39C12', // Orange
    '#9B59B6', // Purple
    '#1ABC9C', // Teal
  ];

  const currentFolderId = folderStack[folderStack.length - 1].id;

  useEffect(() => {
    loadFolders();
  }, [currentFolderId]);

  const loadFolders = async () => {
    try {
      setLoading(true);
      let response;
      if (currentFolderId === null) {
        response = await api.get('/folders');
      } else {
        response = await api.get(`/folders/${currentFolderId}/subfolders`);
      }
      setFolders(response.data);
    } catch (error) {
      toast.error('Failed to load folders');
      console.error(error);
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
      const payload = {
        folderName: newFolderName,
        parentFolderId: currentFolderId,
        folderColor: folderColor,
      };

      await api.post('/folders', payload);
      toast.success('Folder created successfully!');
      setNewFolderName('');
      setShowNewFolderInput(false);
      loadFolders();
    } catch (error) {
      toast.error(typeof error.response?.data?.message === 'string' ? error.response.data.message : 'Failed to create folder');
    }
  };

  const handleNavigateToFolder = (folderId, folderName) => {
    setFolderStack([...folderStack, { id: folderId, name: folderName }]);
  };

  const handleGoBack = () => {
    if (folderStack.length > 1) {
      setFolderStack(folderStack.slice(0, -1));
    }
  };

  const handleDeleteFolder = async (folderId) => {
    if (window.confirm('Are you sure you want to delete this folder and all its contents?')) {
      try {
        await api.delete(`/folders/${folderId}`);
        toast.success('Folder deleted successfully!');
        loadFolders();
      } catch (error) {
        toast.error('Failed to delete folder');
      }
    }
  };

  const handleSelectFolder = () => {
    onFolderSelect(currentFolderId);
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          {folderStack.length > 1 && (
            <button
              onClick={handleGoBack}
              className="p-2 hover:bg-gray-100 rounded-lg transition"
            >
              <FaArrowLeft className="text-gray-600" />
            </button>
          )}
          <div className="flex items-center gap-2">
            {folderStack.map((folder, idx) => (
              <React.Fragment key={idx}>
                {idx > 0 && <FaChevronRight className="text-gray-400 text-sm" />}
                <span className="text-sm font-medium text-gray-700">{folder.name}</span>
              </React.Fragment>
            ))}
          </div>
        </div>
        <button
          onClick={() => setShowNewFolderInput(!showNewFolderInput)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition"
        >
          <FaFolderPlus /> New Folder
        </button>
      </div>

      {showNewFolderInput && (
        <div className="mb-6 p-4 bg-gray-50 rounded-lg">
          <div className="flex gap-2 mb-2">
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              placeholder="Folder name"
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleCreateFolder();
                if (e.key === 'Escape') setShowNewFolderInput(false);
              }}
            />
            <div className="flex gap-1">
              {folderColors.map((color) => (
                <button
                  key={color}
                  onClick={() => setFolderColor(color)}
                  className={`w-8 h-8 rounded-lg transition ${
                    folderColor === color ? 'ring-2 ring-offset-2 ring-gray-400' : ''
                  }`}
                  style={{ backgroundColor: color }}
                />
              ))}
            </div>
          </div>
          <div className="flex gap-2">
            <button
              onClick={handleCreateFolder}
              className="px-4 py-2 bg-green-500 hover:bg-green-600 text-white rounded-lg transition"
            >
              Create
            </button>
            <button
              onClick={() => setShowNewFolderInput(false)}
              className="px-4 py-2 bg-gray-300 hover:bg-gray-400 text-gray-800 rounded-lg transition"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">
          <div className="animate-spin inline-block w-8 h-8 border-4 border-gray-300 border-t-blue-500 rounded-full"></div>
        </div>
      ) : folders.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <FaFolder className="text-4xl mx-auto mb-2 opacity-20" />
          <p>No folders here yet. Create one to get started!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {folders.map((folder) => (
            <div
              key={folder.id}
              className="p-4 border border-gray-200 rounded-lg hover:shadow-lg transition cursor-pointer group"
              onClick={() => handleNavigateToFolder(folder.id, folder.folderName)}
            >
              <div className="flex items-start justify-between mb-2">
                <FaFolder
                  className="text-3xl"
                  style={{ color: folder.folderColor }}
                />
                <div className="opacity-0 group-hover:opacity-100 transition flex gap-2">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteFolder(folder.id);
                    }}
                    className="p-2 text-red-500 hover:bg-red-50 rounded"
                  >
                    <FaTrash2 className="text-sm" />
                  </button>
                </div>
              </div>
              <h3 className="font-medium text-gray-800 truncate">{folder.folderName}</h3>
              <p className="text-xs text-gray-500 mt-1">{folder.fileCount} files</p>
            </div>
          ))}
        </div>
      )}

      <div className="flex gap-2">
        <button
          onClick={handleSelectFolder}
          className="flex-1 px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition"
        >
          Select This Folder
        </button>
        <button
          onClick={onBack}
          className="flex-1 px-4 py-2 bg-gray-300 hover:bg-gray-400 text-gray-800 rounded-lg transition"
        >
          Cancel
        </button>
      </div>
    </div>
  );
};

export default FolderBrowser;
