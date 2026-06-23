package com.filescout.filescout_api.scan.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.filescout.filescout_api.scan.entity.DetectedFile;

@Repository
public interface DetectedFileRepository extends JpaRepository<DetectedFile,Long> {
    List<DetectedFile> findByScanId(Long scanId);
    Page<DetectedFile> findByScanId(Long scanId, Pageable pageable);
    Page<DetectedFile> findByScanIdAndFileNameContainingIgnoreCase(Long scanId, String fileName, Pageable pageable);
    
    Long countByScanUserId(Long userId);

    Long countByScanUserIdAndFileType(
        Long userId,
        String fileType
    );
}
