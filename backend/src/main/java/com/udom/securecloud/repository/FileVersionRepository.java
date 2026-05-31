package com.udom.securecloud.repository;

import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {
    List<FileVersion> findByFileOrderByVersionNumberDesc(FileMetadata file);
    int countByFile(FileMetadata file);
}
