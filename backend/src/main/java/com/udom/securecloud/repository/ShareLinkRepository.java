package com.udom.securecloud.repository;

import com.udom.securecloud.model.ShareLink;
import com.udom.securecloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);
    List<ShareLink> findByFileIdAndIsActiveTrue(Long fileId);
    List<ShareLink> findByCreatedByAndIsActiveTrue(User user);
}
