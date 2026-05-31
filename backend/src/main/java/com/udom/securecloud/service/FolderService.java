package com.udom.securecloud.service;

import com.udom.securecloud.dto.CreateFolderRequest;
import com.udom.securecloud.dto.FolderResponse;
import com.udom.securecloud.dto.FileResponse;
import com.udom.securecloud.dto.RenameFolderRequest;
import com.udom.securecloud.model.Folder;
import com.udom.securecloud.model.User;
import com.udom.securecloud.repository.FolderRepository;
import com.udom.securecloud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public FolderResponse createFolder(CreateFolderRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findByIdAndUserId(request.getParentFolderId(), userId)
                    .orElseThrow(() -> new RuntimeException("Parent folder not found or does not belong to user"));
        }

        // Check if folder with same name already exists in same parent
        if (request.getParentFolderId() != null) {
            if (folderRepository.findByFolderNameAndUserIdAndParentFolderIdAndIsDeletedFalse(
                    request.getFolderName(), userId, request.getParentFolderId()).isPresent()) {
                throw new RuntimeException("Folder with this name already exists in this location");
            }
        } else {
            if (folderRepository.findByFolderNameAndUserIdAndParentFolderIsNullAndIsDeletedFalse(
                    request.getFolderName(), userId).isPresent()) {
                throw new RuntimeException("Folder with this name already exists");
            }
        }

        Folder folder = new Folder();
        folder.setFolderName(request.getFolderName());
        folder.setUser(user);
        folder.setParentFolder(parentFolder);
        folder.setFolderColor(request.getFolderColor());
        folder.setIsDeleted(false);

        Folder savedFolder = folderRepository.save(folder);
        return mapToFolderResponse(savedFolder);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getRootFolders(Long userId) {
        List<Folder> folders = folderRepository.findByUserIdAndParentFolderIsNullAndIsDeletedFalse(userId);
        return folders.stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FolderResponse getFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        return mapToFolderResponse(folder);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getSubFolders(Long parentFolderId, Long userId) {
        // Verify parent folder belongs to user
        folderRepository.findByIdAndUserId(parentFolderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        List<Folder> folders = folderRepository.findByUserIdAndParentFolderIdAndIsDeletedFalse(userId, parentFolderId);
        return folders.stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FolderResponse updateFolder(Long folderId, RenameFolderRequest request, Long userId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (request.getNewFolderName() != null && !request.getNewFolderName().isEmpty()) {
            folder.setFolderName(request.getNewFolderName());
        }

        if (request.getFolderColor() != null) {
            folder.setFolderColor(request.getFolderColor());
        }

        Folder updatedFolder = folderRepository.save(folder);
        return mapToFolderResponse(updatedFolder);
    }

    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Soft delete
        folder.setIsDeleted(true);
        folderRepository.save(folder);
    }

    @Transactional
    public void deleteAllFolders(Long userId) {
        List<Folder> userFolders = folderRepository.findByUserIdAndParentFolderIsNullAndIsDeletedFalse(userId);
        userFolders.forEach(folder -> {
            folder.setIsDeleted(true);
            folderRepository.save(folder);
        });
    }

    private FolderResponse mapToFolderResponse(Folder folder) {
        long totalSize = folder.getFiles().stream()
                .filter(f -> !f.getIsDeleted())
                .mapToLong(com.udom.securecloud.model.FileMetadata::getFileSize)
                .sum();

        int fileCount = (int) folder.getFiles().stream()
                .filter(f -> !f.getIsDeleted())
                .count();

        List<FolderResponse> subFolders = folder.getSubFolders().stream()
                .filter(f -> !f.getIsDeleted())
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());

        List<FileResponse> files = folder.getFiles().stream()
                .filter(f -> !f.getIsDeleted())
                .map(this::mapFileToResponse)
                .collect(Collectors.toList());

        return FolderResponse.builder()
                .id(folder.getId())
                .folderName(folder.getFolderName())
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .folderColor(folder.getFolderColor())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .subFolders(subFolders)
                .files(files)
                .totalSize(totalSize)
                .fileCount(fileCount)
                .build();
    }

    private FileResponse mapFileToResponse(com.udom.securecloud.model.FileMetadata file) {
        return FileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .mimeType(file.getMimeType())
                .encrypted(file.getIsEncrypted())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}
