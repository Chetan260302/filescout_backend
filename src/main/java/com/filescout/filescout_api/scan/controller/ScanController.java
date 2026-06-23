package com.filescout.filescout_api.scan.controller;




import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filescout.filescout_api.common.dto.ApiResponse;
import com.filescout.filescout_api.scan.dto.request.ScanRequest;
import com.filescout.filescout_api.scan.dto.response.ScanResponse;
import com.filescout.filescout_api.scan.dto.response.ScanStatsResponse;
import com.filescout.filescout_api.scan.service.ScanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {
    
    private final ScanService scanService;

    @PostMapping("/")
    public ApiResponse<ScanResponse> scanUrl(

        @Valid
        @RequestBody ScanRequest request,
        Authentication authentication
    ){
        return scanService.scanUrl(request, authentication.getName());
    }

    @GetMapping("/")
    public ApiResponse<Page<ScanResponse>> getUserScans(
        Authentication authentication,
        @RequestParam(defaultValue = "0")
        int page,

        @RequestParam(defaultValue = "10")
        int size
    ) {

        return scanService.getUserScans(
            authentication.getName(),
            page,
            size
        );
    }

    @GetMapping("/{scanId}")
    public ApiResponse<ScanResponse> getScanById(
        Authentication authentication,
        @PathVariable Long scanId,
        @RequestParam(required = false) String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return scanService.getScanById(scanId, authentication.getName(), query, page, size);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ScanResponse>> searchScans(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication
    ) {
        return scanService.searchScans(
            authentication.getName(),
            keyword,
            page,
            size
        );
    }

    @GetMapping("/stats")
    public ApiResponse<ScanStatsResponse> getStats(
        Authentication authentication
    ) {
        return scanService.getStats(authentication.getName());
    }

    @DeleteMapping("/{scanId}")
    public ApiResponse<Void> deleteScan(
        @PathVariable Long scanId,
        Authentication authentication
    ) {

        return scanService.deleteScan(
                scanId,
                authentication.getName()
        );
    }
}
