package com.udom.securecloud.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    List<FileMetadata> findByUserIdAndFolderId(Long userId, Long folderId);
    
    List<FileMetadata> findByUserAndFolderIsNullAndIsDeletedFalseOrderByCreatedAtDesc(User user);
    
    List<FileMetadata> findByUserAndFolderIdAndIsDeletedFalseOrderByCreatedAtDesc(User user, Long folderId);
    
    List<FileMetadata> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    /** Paginated variant used by the admin per-user file listing endpoint (G8). */
    Page<FileMetadata> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    Long countByUserIdAndIsDeletedFalse(Long userId);
    
    Optional<FileMetadata> findByFilePath(String filePath);
    
    @Query("SELECT f FROM FileMetadata f WHERE f.user.id = :userId AND f.isDeleted = false ORDER BY f.createdAt DESC")
    List<FileMetadata> findActiveFilesByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
    
    Long countByIsDeletedFalse();

    List<FileMetadata> findByUserIdAndIsDeletedTrueOrderByUpdatedAtDesc(Long userId);
    
    @Query("SELECT SUM(f.fileSize) FROM FileMetadata f WHERE f.user.id = :userId")
    Long calculateTotalStorageUsed(@org.springframework.data.repository.query.Param("userId") Long userId);

    @Query("SELECT f FROM FileMetadata f WHERE f.user = :user AND f.isDeleted = false " +
           "AND (LOWER(f.originalName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(f.mimeType) LIKE LOWER(CONCAT('%', :query, '%')))" +
           "ORDER BY f.createdAt DESC")
    List<FileMetadata> searchByUser(
        @org.springframework.data.repository.query.Param("user") User user,
        @org.springframework.data.repository.query.Param("query") String query
    );
}

