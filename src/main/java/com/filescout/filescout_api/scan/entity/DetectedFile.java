package com.filescout.filescout_api.scan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="detected_files")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetectedFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileType;

    @Column(length = 2000)
    private String fileUrl;

    private Long fileSize;

    @Column(name = "is_accessible")
    private Boolean accessible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id",nullable = false)
    private Scan scan;
}
