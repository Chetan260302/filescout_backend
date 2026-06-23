package com.filescout.filescout_api.security.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.sql.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import com.filescout.filescout_api.security.service.JwtService;

@Service
public class JwtServiceImpl implements JwtService{

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public String generateAccessToken(String email) {
        return Jwts.builder()
        .subject(email)
        .claim("type", "ACCESS")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis()+accessTokenExpiration))
        .signWith(getSigningKey())
        .compact();
    }

    @Override
    public String generateRefreshToken(String email) {
        return Jwts.builder()
        .subject(email)
        .claim("type", "REFRESH")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis()+refreshTokenExpiration))
        .signWith(getSigningKey())
        .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            
            return "ACCESS".equals(claims.get("type", String.class));

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);

            return "REFRESH".equals(claims.get("type", String.class));

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        Claims claims=Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

        return claims.getSubject();
    }

    // @Override
    // public boolean isTokenValid(String token) {
    //     try {
    //         Jwts.parser()
    //         .verifyWith(getSigningKey())
    //         .build()
    //         .parseSignedClaims(token);

    //         return true;

    //     } catch (Exception e) {
    //         return false;
    //     }
        
    // }
}
