package com.filescout.filescout_api.scan.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanResponse {
    private Long id;
    private String url;
    private LocalDateTime scannedAt;
    private Integer filesFound;
    private String title;
    private List<DetectedFileResponse> files;
    private String status;
    private Integer totalPages;
    private Long totalElements;
}
