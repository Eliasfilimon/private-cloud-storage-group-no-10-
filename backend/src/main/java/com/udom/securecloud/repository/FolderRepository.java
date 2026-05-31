package com.udom.securecloud.repository;

import com.udom.securecloud.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    List<Folder> findByUserIdAndParentFolderIsNullAndIsDeletedFalse(Long userId);
    
    List<Folder> findByUserIdAndParentFolderIdAndIsDeletedFalse(Long userId, Long parentFolderId);
    
    Optional<Folder> findByIdAndUserId(Long folderId, Long userId);
    
    Optional<Folder> findByFolderNameAndUserIdAndParentFolderIdAndIsDeletedFalse(String folderName, Long userId, Long parentFolderId);
    
    Optional<Folder> findByFolderNameAndUserIdAndParentFolderIsNullAndIsDeletedFalse(String folderName, Long userId);
    
    List<Folder> findByParentFolderIdAndIsDeletedFalse(Long parentFolderId);
    
    boolean existsByIdAndUserId(Long folderId, Long userId);
}
