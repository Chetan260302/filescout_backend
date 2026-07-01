package com.filescout.filescout_api.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.filescout.filescout_api.auth.dto.AuthResponse;
import com.filescout.filescout_api.auth.dto.ForgotPasswordRequest;
import com.filescout.filescout_api.auth.dto.LoginRequest;
import com.filescout.filescout_api.auth.dto.LoginResponse;
import com.filescout.filescout_api.auth.dto.RefreshTokenRequest;
import com.filescout.filescout_api.auth.dto.RefreshTokenResponse;
import com.filescout.filescout_api.auth.dto.RegisterRequest;
import com.filescout.filescout_api.auth.dto.ResetPasswordRequest;
import com.filescout.filescout_api.auth.service.AuthService;
import com.filescout.filescout_api.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(

        @Valid
        @RequestBody
        ForgotPasswordRequest request
    ){
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(

        @Valid
        @RequestBody
        ResetPasswordRequest request
    ){
        return authService.resetPassword(request);
    }
    
}
