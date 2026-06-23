package com.filescout.filescout_api.scan.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.filescout.filescout_api.scan.entity.Scan;

@Repository
public interface ScanRepository extends JpaRepository<Scan,Long> {
    Optional<Scan> findById(Long id);
    Page<Scan> findByUserId(
        Long userId,
        Pageable pageable);
    Page<Scan> findByUserIdAndUrlContainingIgnoreCase(
        Long userId,
        String keyword,
        Pageable pageable
    );

    Long countByUserId(Long userId);
    
}
