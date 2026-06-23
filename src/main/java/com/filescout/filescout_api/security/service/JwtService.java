package com.filescout.filescout_api.security.service;

public interface JwtService {

    String generateAccessToken(String email);
    String generateRefreshToken(String email);
    String extractUsername(String token);
    boolean isAccessTokenValid(String token);
    boolean isRefreshTokenValid(String token);

    
    
}
