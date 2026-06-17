package com.udom.securecloud.repository;

import com.udom.securecloud.model.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    List<SharedFile> findBySharedWithIdAndIsActiveTrue(Long userId);

    List<SharedFile> findByOwnerIdAndIsActiveTrue(Long ownerId);

    List<SharedFile> findByFileIdAndIsActiveTrue(Long fileId);

    Optional<SharedFile> findByFileIdAndSharedWithIdAndIsActiveTrue(Long fileId, Long sharedWithId);

    @Query("SELECT COUNT(sf) FROM SharedFile sf WHERE sf.ownerId = ?1 AND sf.isActive = true")
    long countByOwnerId(Long ownerId);

    @Query("SELECT COUNT(sf) FROM SharedFile sf WHERE sf.sharedWithId = ?1 AND sf.isActive = true")
    long countBySharedWithId(Long sharedWithId);

    void deleteByFileId(Long fileId);
    
    List<SharedFile> findByExpiresAtBefore(java.time.LocalDateTime date);
}
