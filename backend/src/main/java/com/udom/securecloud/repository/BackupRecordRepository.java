package com.udom.securecloud.repository;

import com.udom.securecloud.model.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {

    List<BackupRecord> findAllByOrderByCreatedAtDesc();

    List<BackupRecord> findByStatusOrderByCreatedAtDesc(String status);
}
