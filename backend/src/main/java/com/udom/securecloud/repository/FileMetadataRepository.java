package com.udom.securecloud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    List<FileMetadata> findByUserId(Long userId);
    
    List<FileMetadata> findByUserIdAndParentFolderId(Long userId, Long parentFolderId);
    
    List<FileMetadata> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
    
    Long countByUserIdAndIsDeletedFalse(Long userId);
    
    Optional<FileMetadata> findByFilePath(String filePath);
    
    @Query("SELECT f FROM FileMetadata f WHERE f.owner = :owner AND f.isDeleted = false ORDER BY f.createdAt DESC")
    List<FileMetadata> findActiveFilesByOwner(@org.springframework.data.repository.query.Param("owner") User owner);
    
    Long countByOwnerAndIsDeletedFalse(User owner);
    
    Long countByIsDeletedFalse();
    
    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f WHERE f.owner = :owner")
    Long calculateTotalStorageUsed(@org.springframework.data.repository.query.Param("owner") User owner);
}
