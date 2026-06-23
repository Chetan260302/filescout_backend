package com.filescout.filescout_api.scan.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.filescout.filescout_api.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="scans")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Scan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 2000)
    private String url;

    @Column(nullable = false)
    private LocalDateTime scannedAt;

    private Long durationMs;

    @Column(length = 500)
    private String title;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "scan",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<DetectedFile> detectedFiles=new ArrayList<>();

    @Column(nullable = false)
    private String status;
}
