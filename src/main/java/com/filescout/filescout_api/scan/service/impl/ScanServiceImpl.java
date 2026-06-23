package com.filescout.filescout_api.scan.service.impl;

// import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.filescout.filescout_api.common.dto.ApiResponse;
import com.filescout.filescout_api.common.exception.ResourceNotFoundException;
import com.filescout.filescout_api.scan.dto.request.ScanRequest;
import com.filescout.filescout_api.scan.dto.response.DetectedFileResponse;
import com.filescout.filescout_api.scan.dto.response.ScanResponse;
import com.filescout.filescout_api.scan.dto.response.ScanStatsResponse;
import com.filescout.filescout_api.scan.entity.DetectedFile;
import com.filescout.filescout_api.scan.entity.Scan;
import com.filescout.filescout_api.scan.repository.DetectedFileRepository;
import com.filescout.filescout_api.scan.helper.DocumentDetector;
import com.filescout.filescout_api.scan.repository.ScanRepository;
import com.filescout.filescout_api.scan.service.ScanService;
import com.filescout.filescout_api.user.entity.User;
import com.filescout.filescout_api.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScanServiceImpl implements ScanService{

    private final ScanRepository scanRepository;
    private final UserRepository userRepository;
    private final DetectedFileRepository detectedFileRepository;
    private final DocumentDetector documentDetector;

    @Override
    public ApiResponse<ScanResponse> scanUrl(ScanRequest request, String email) {
        long startTime = System.currentTimeMillis();

        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes);

        if (!urlValidator.isValid(request.getUrl())) {
            throw new IllegalArgumentException("Invalid URL format or unsupported protocol");
        }

        Document document;
        try {
            document = Jsoup.connect(request.getUrl())
                // Add a realistic User-Agent to bypass bot protection
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                // .header("Accept-Encoding", "gzip, deflate, br")
                // // .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .referrer("https://www.google.com")
                .timeout(10000) // 10 seconds timeout safety net
                .followRedirects(true)
                .get();
                
        } catch (org.jsoup.HttpStatusException e) {
            // Catches specific HTTP status errors like 403 Forbidden or 404 Not Found
            throw new RuntimeException("Failed to access website. Server responded with status: " + e.getStatusCode());
        } catch (java.io.IOException e) {
            // Catches network connection drops, timeouts, or DNS issues
            throw new RuntimeException("Network error: Unable to connect to the target website.");
        } catch (Exception e) {
            // Fallback for anything else
            throw new RuntimeException("An unexpected error occurred during scanning: " + e.getMessage());
        }

        String title=document.title();
        Elements links = document.select("a[href]");
        
        System.out.println("Page Title: " + title);

        User user = userRepository
        .findByEmail(email)
        .orElseThrow(() ->
                new ResourceNotFoundException("User not found"));
        
        Scan scan = Scan.builder()
        .url(request.getUrl())
        .scannedAt(LocalDateTime.now())
        .durationMs(0L)
        .status("SUCCESS")
        .user(user)
        .title(title)
        .build();

        long endTime = System.currentTimeMillis();
        scan.setDurationMs(endTime - startTime);

        scan = scanRepository.save(scan);

        List<DetectedFile> files = new ArrayList<>();
        for (Element link : links) {
            String href = link.attr("abs:href");
            
            String fileType = documentDetector.detectFileType(href, link.text());
            if (fileType != null) {
                String fileName = documentDetector.extractFileName(href, fileType);
                
                DetectedFile file = DetectedFile.builder()
                        .fileUrl(href)
                        .fileName(fileName)
                        .fileType(fileType)
                        .accessible(true)
                        .scan(scan)
                        .build();

                files.add(file);
                System.out.println("FOUND FILE: " + href + " (type: " + fileType + ")");
            }
        }

        if (!files.isEmpty()) {
            detectedFileRepository.saveAll(files);
        }

        List<DetectedFileResponse> fileResponses = files.stream()
                .map(file -> DetectedFileResponse.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileName(file.getFileName())
                        .fileType(file.getFileType())
                        .accessible(file.getAccessible())
                        .build())
                .toList();

        ScanResponse response = ScanResponse.builder()
        .id(scan.getId())
        .url(request.getUrl())
        .filesFound(files.size())
        .scannedAt(scan.getScannedAt())
        .title(title)
        .files(fileResponses)
        .status(scan.getStatus())
        .build();

        return ApiResponse.<ScanResponse>builder()
            .success(true)
            .message("Scan completed successfully")
            .data(response)
            .build();
        
    }

    @Override
    public ApiResponse<Page<ScanResponse>> getUserScans(
            String email,
            int page,
            int size
    ) {
        
        Pageable pageable = PageRequest.of(page, size,Sort.by("scannedAt").descending());

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        Page<Scan> scans =
                scanRepository.findByUserId(user.getId(),pageable);

        Page<ScanResponse> response =
            scans.map(scan ->
                    ScanResponse.builder()
                            .id(scan.getId())
                            .url(scan.getUrl())
                            .scannedAt(scan.getScannedAt())
                            .filesFound(
                                    scan.getDetectedFiles()
                                            .size()
                            )
                            .title(scan.getTitle())
                            .status(scan.getStatus())
                            .build()
            );

            
        return ApiResponse.<Page<ScanResponse>>builder()
            .success(true)
            .message("Scans retrieved successfully")
            .data(response)
            .build();
    }

    @Override
    public ApiResponse<ScanResponse> getScanById(Long scanId, String email, String query, int page, int size) {
        
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));

        Scan scan = scanRepository
                .findById(scanId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Scan not found"));

        if (!scan.getUser().getId().equals(user.getId())){
            throw new RuntimeException("You do not have access to this scan");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<DetectedFile> filesPage;
        if (query != null && !query.trim().isEmpty()) {
            filesPage = detectedFileRepository.findByScanIdAndFileNameContainingIgnoreCase(scanId, query.trim(), pageable);
        } else {
            filesPage = detectedFileRepository.findByScanId(scanId, pageable);
        }

        List<DetectedFileResponse> files = filesPage.getContent()
                                            .stream()
                                            .map(file -> DetectedFileResponse.builder()
                                                .id(file.getId())
                                                .fileUrl(file.getFileUrl())
                                                .fileName(file.getFileName())
                                                .fileType(file.getFileType())
                                                .accessible(file.getAccessible())
                                                .build())
                                            .toList();

        ScanResponse response = ScanResponse.builder()
        .id(scan.getId())
        .url(scan.getUrl())
        .filesFound(scan.getDetectedFiles().size())
        .scannedAt(scan.getScannedAt())
        .title(scan.getTitle())
        .files(files)
        .status(scan.getStatus())
        .totalPages(filesPage.getTotalPages())
        .totalElements(filesPage.getTotalElements())
        .build();

        return ApiResponse.<ScanResponse>builder()
            .success(true)
            .message("Scan retrieved successfully")
            .data(response)
            .build();
    }


    @Override
    public ApiResponse<Page<ScanResponse>> searchScans(
            String email,
            String keyword,
            int page,
            int size
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("scannedAt").descending()
        );

        Page<Scan> scans =
                scanRepository.findByUserIdAndUrlContainingIgnoreCase(
                        user.getId(),
                        keyword,
                        pageable
                );

        Page<ScanResponse> response =
                scans.map(scan ->
                        ScanResponse.builder()
                                .id(scan.getId())
                                .url(scan.getUrl())
                                .scannedAt(scan.getScannedAt())
                                .filesFound(
                                        scan.getDetectedFiles().size()
                                )
                                .title(scan.getTitle())
                                .status(scan.getStatus())
                                .build()
                );

        return ApiResponse.<Page<ScanResponse>>builder()
                .success(true)
                .message("Search completed successfully")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<ScanStatsResponse> getStats(
            String email
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Long totalScans =
                scanRepository.countByUserId(
                        user.getId()
                );

        Long totalFiles =
                detectedFileRepository.countByScanUserId(
                        user.getId()
                );

        Long pdfCount =
                detectedFileRepository
                        .countByScanUserIdAndFileType(
                                user.getId(),
                                "pdf"
                        );

        Long pptCount =
                detectedFileRepository
                        .countByScanUserIdAndFileType(
                                user.getId(),
                                "ppt"
                        )
                +
                detectedFileRepository
                        .countByScanUserIdAndFileType(
                                user.getId(),
                                "pptx"
                        );

        Long docCount =
                detectedFileRepository
                        .countByScanUserIdAndFileType(
                                user.getId(),
                                "doc"
                        )
                +
                detectedFileRepository
                        .countByScanUserIdAndFileType(
                                user.getId(),
                                "docx"
                        );

        ScanStatsResponse response =
                ScanStatsResponse.builder()
                        .totalScans(totalScans)
                        .totalFiles(totalFiles)
                        .pdfCount(pdfCount)
                        .pptCount(pptCount)
                        .docCount(docCount)
                        .build();

        return ApiResponse.<ScanStatsResponse>builder()
                .success(true)
                .message("Statistics fetched successfully")
                .data(response)
                .build();
        }

        @Override
        public ApiResponse<Void> deleteScan(
                Long scanId,
                String email
            ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Scan scan = scanRepository
                .findById(scanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Scan not found"
                        ));

        if (!scan.getUser()
                .getId()
                .equals(user.getId())) {

                throw new RuntimeException(
                        "You are not authorized to delete this scan"
                );
        }

        scanRepository.delete(scan);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Scan deleted successfully")
                .build();
        }
}
