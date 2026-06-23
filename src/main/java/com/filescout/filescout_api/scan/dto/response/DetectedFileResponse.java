package com.filescout.filescout_api.scan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedFileResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Boolean accessible;
}
