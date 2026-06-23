package com.filescout.filescout_api.scan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanStatsResponse {
    private Long totalScans;
    private Long totalFiles;
    private Long pdfCount;
    private Long pptCount;
    private Long docCount;
}
