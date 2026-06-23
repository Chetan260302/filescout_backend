package com.filescout.filescout_api.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.filescout.filescout_api.auth.dto.AuthResponse;
import com.filescout.filescout_api.auth.dto.LoginRequest;
import com.filescout.filescout_api.auth.dto.LoginResponse;
import com.filescout.filescout_api.auth.dto.RefreshTokenRequest;
import com.filescout.filescout_api.auth.dto.RefreshTokenResponse;
import com.filescout.filescout_api.auth.dto.RegisterRequest;
import com.filescout.filescout_api.auth.service.AuthService;
import com.filescout.filescout_api.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // public AuthController(AuthService authService) {
    //     this.authService = authService;
    // }


    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(

        @Valid
        @RequestBody
        RegisterRequest request
    ){
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
        
        @Valid
        @RequestBody
        LoginRequest request
    ){
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(

        @Valid
        @RequestBody
        RefreshTokenRequest request
    ){
        return authService.refreshToken(request);
    }
    
}
