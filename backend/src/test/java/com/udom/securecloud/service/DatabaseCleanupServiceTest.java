package com.udom.securecloud.service;

import com.udom.securecloud.model.FileMetadata;
import com.udom.securecloud.model.ShareLink;
import com.udom.securecloud.model.SharedFile;
import com.udom.securecloud.repository.FileMetadataRepository;
import com.udom.securecloud.repository.ShareLinkRepository;
import com.udom.securecloud.repository.SharedFileRepository;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseCleanupServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private ShareLinkRepository shareLinkRepository;

    @Mock
    private SharedFileRepository sharedFileRepository;

    @Mock
    private MinioClient minioClient;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DatabaseCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cleanupService, "bucketName", "securecloud-test-bucket");
    }

    @Test
    void testCleanupExpiredShares_WithExpiredLinksAndShares() {
        // Arrange
        ShareLink link1 = new ShareLink();
        ShareLink link2 = new ShareLink();
        List<ShareLink> expiredLinks = Arrays.asList(link1, link2);

        SharedFile share1 = new SharedFile();
        List<SharedFile> expiredShares = Collections.singletonList(share1);

        when(shareLinkRepository.findByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(expiredLinks);
        when(sharedFileRepository.findByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(expiredShares);

        // Act
        cleanupService.cleanupExpiredShares();

        // Assert
        verify(shareLinkRepository, times(1)).deleteAll(expiredLinks);
        verify(sharedFileRepository, times(1)).deleteAll(expiredShares);
        
        verify(auditLogService).logAction(null, "SYSTEM", "CLEANUP_SHARE_LINKS", "SYSTEM", null, null, null, "SUCCESS", "Removed 2 expired links");
        verify(auditLogService).logAction(null, "SYSTEM", "CLEANUP_INTERNAL_SHARES", "SYSTEM", null, null, null, "SUCCESS", "Removed 1 expired internal shares");
    }

    @Test
    void testCleanupSoftDeletedFiles_Success() throws Exception {
        // Arrange
        FileMetadata metadata = new FileMetadata();
        metadata.setId(100L);
        metadata.setFilePath("user1/test.txt");
        metadata.setOriginalName("test.txt");

        when(fileMetadataRepository.findByIsDeletedTrueAndUpdatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(metadata));

        // Act
        cleanupService.cleanupSoftDeletedFiles();

        // Assert
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient, times(1)).removeObject(captor.capture());
        assertEquals("securecloud-test-bucket", captor.getValue().bucket());
        assertEquals("user1/test.txt", captor.getValue().object());

        verify(fileMetadataRepository, times(1)).delete(metadata);
        verify(auditLogService).logAction(null, "SYSTEM", "CLEANUP_TRASH", "SYSTEM", null, null, null, "SUCCESS", "Permanently deleted 1 files, failed 0");
    }
}
