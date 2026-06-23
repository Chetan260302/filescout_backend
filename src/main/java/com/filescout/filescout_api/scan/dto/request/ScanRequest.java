package com.filescout.filescout_api.scan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {
    @NotBlank(message = "URL cannot be empty")
    private String url;
}
