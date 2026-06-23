package com.filescout.filescout_api.scan.helper;

import org.springframework.stereotype.Component;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class DocumentDetector {

    /**
     * Analyzes a URL and anchor text to determine if it is a document.
     * Uses fast suffix matching first, followed by keyword heuristics and a fallback HEAD request.
     * 
     * @param href The absolute URL link to analyze.
     * @param anchorText The text of the anchor link.
     * @return The file extension (e.g. "pdf", "pptx") if it's a document, or null otherwise.
     */
    public String detectFileType(String href, String anchorText) {
    if (href == null || href.isEmpty()) return null;

    String hrefLower = href.toLowerCase();
    String textLower = anchorText != null ? anchorText.toLowerCase() : "";

    if (hrefLower.contains("docs.google.com/document")) {
        return "gdoc";
    } else if (hrefLower.contains("docs.google.com/presentation")) {
        return "gslides";
    } else if (hrefLower.contains("docs.google.com/spreadsheets")) {
        return "gsheet";
    }
    if (hrefLower.contains("drive.google.com/file")) {
        return "drive-file";
    }
    
    if (hrefLower.contains("dropbox.com/s/") || hrefLower.contains("dropbox.com/sh/") || hrefLower.contains("dl.dropboxusercontent.com")) {
        return "dropbox";
    }
    if (hrefLower.contains("onedrive.live.com") || hrefLower.contains("1drv.ms") || hrefLower.contains("sharepoint.com")) {
        return "onedrive";
    }
    if (hrefLower.contains("box.com/s/") || hrefLower.contains("box.com/shared/")) {
        return "box";
    }
    if (hrefLower.contains("s3.amazonaws.com") || hrefLower.contains(".s3.amazon") || (hrefLower.contains(".s3-") && hrefLower.contains(".amazonaws.com"))) {
        return "s3";
    }
    
    // 1. Fast suffix check (handles clean URLs like /slides.pptx)
    String[] suffixes = {".pdf", ".pptx", ".ppt", ".docx", ".doc"};
    for (String suffix : suffixes) {
        if (hrefLower.endsWith(suffix)) {
            return suffix.substring(1); // remove the dot
        }
    }

    // 2. Check URL PATH before the query string
    //    handles: /download?file=slides.pptx  or  /report.pdf?v=2
    try {
        String path = new java.net.URL(href).getPath().toLowerCase();
        for (String suffix : suffixes) {
            if (path.endsWith(suffix)) {
                return suffix.substring(1);
            }
        }
    } catch (Exception ignored) {}

    // 3. Keyword heuristics → then HEAD request fallback
    boolean hasKeywords = 
        hrefLower.contains("download") || hrefLower.contains("attachment") ||
        hrefLower.contains(".pdf") || hrefLower.contains(".ppt") || 
        hrefLower.contains(".doc") ||
        textLower.contains("download") || textLower.contains("pdf") ||
        textLower.contains("powerpoint") || textLower.contains("presentation") ||
        textLower.contains("word document") || textLower.contains("slides");

    if (hasKeywords) {
        return detectFileTypeByHeadRequest(href);
    }

    return null;
}
    /**
     * Extracts a clean filename from a URL.
     */
    public String extractFileName(String url, String fileType) {
        try {
            // Strip query parameters
            int queryIdx = url.indexOf('?');
            String cleanUrl = queryIdx != -1 ? url.substring(0, queryIdx) : url;

            // Get last segment
            String lastSegment = cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1);
            if (lastSegment.isEmpty() || !lastSegment.contains(".")) {
                return "document_" + System.currentTimeMillis() + "." + fileType;
            }
            return lastSegment;
        } catch (Exception e) {
            return "document_" + System.currentTimeMillis() + "." + fileType;
        }
    }

    private String detectFileTypeByHeadRequest(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000); // 2 seconds connection timeout
            connection.setReadTimeout(2000);    // 2 seconds read timeout

            // Add a common User-Agent to bypass server security blocks
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = connection.getContentType();
                if (contentType != null) {
                    contentType = contentType.toLowerCase();
                    if (contentType.contains("application/pdf")) {
                        return "pdf";
                    } else if (contentType.contains("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                        return "pptx";
                    } else if (contentType.contains("application/vnd.ms-powerpoint")) {
                        return "ppt";
                    } else if (contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                        return "docx";
                    } else if (contentType.contains("application/msword")) {
                        return "doc";
                    }
                }
            }
        } catch (Exception e) {
            // Silently ignore connection/DNS failures for scanned URLs
        }
        return null;
    }
}
