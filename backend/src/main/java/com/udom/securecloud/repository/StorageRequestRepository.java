package com.udom.securecloud.repository;

import com.udom.securecloud.model.StorageRequest;
import com.udom.securecloud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRequestRepository extends JpaRepository<StorageRequest, Long> {

    List<StorageRequest> findByUserOrderByCreatedAtDesc(User user);

    Page<StorageRequest> findByStatusOrderByCreatedAtDesc(StorageRequest.Status status, Pageable pageable);

    Page<StorageRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(StorageRequest.Status status);

    boolean existsByUserAndStatus(User user, StorageRequest.Status status);
}
