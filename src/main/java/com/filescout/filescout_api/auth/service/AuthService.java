package com.filescout.filescout_api.auth.service;

import com.filescout.filescout_api.auth.dto.AuthResponse;
import com.filescout.filescout_api.auth.dto.LoginRequest;
import com.filescout.filescout_api.auth.dto.LoginResponse;
import com.filescout.filescout_api.auth.dto.RefreshTokenRequest;
import com.filescout.filescout_api.auth.dto.RefreshTokenResponse;
import com.filescout.filescout_api.auth.dto.RegisterRequest;
import com.filescout.filescout_api.common.dto.ApiResponse;

public interface AuthService {

    ApiResponse<AuthResponse> register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);
    ApiResponse<RefreshTokenResponse> refreshToken(RefreshTokenRequest request);
}
