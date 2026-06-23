package com.filescout.filescout_api.scan.service;

// import java.util.List;

import org.springframework.data.domain.Page;

import com.filescout.filescout_api.common.dto.ApiResponse;
import com.filescout.filescout_api.scan.dto.request.ScanRequest;
import com.filescout.filescout_api.scan.dto.response.ScanResponse;
import com.filescout.filescout_api.scan.dto.response.ScanStatsResponse;

public interface ScanService {
    ApiResponse<ScanResponse> scanUrl(ScanRequest request,String email);
    
    ApiResponse<Page<ScanResponse>> getUserScans(
        String email,
        int page,
        int size
    );

    ApiResponse<ScanResponse> getScanById(Long scanId, String email, String query, int page, int size);
    ApiResponse<Page<ScanResponse>> searchScans(
        String email,
        String keyword,
        int page,
        int size
    );

    ApiResponse<ScanStatsResponse> getStats(String email);

    ApiResponse<Void> deleteScan(
        Long scanId,
        String email
);
}
