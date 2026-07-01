package com.filescout.filescout_api.auth.service;

import com.filescout.filescout_api.auth.dto.AuthResponse;
import com.filescout.filescout_api.auth.dto.ForgotPasswordRequest;
import com.filescout.filescout_api.auth.dto.LoginRequest;
import com.filescout.filescout_api.auth.dto.LoginResponse;
import com.filescout.filescout_api.auth.dto.RefreshTokenRequest;
import com.filescout.filescout_api.auth.dto.RefreshTokenResponse;
import com.filescout.filescout_api.auth.dto.RegisterRequest;
import com.filescout.filescout_api.auth.dto.ResetPasswordRequest;
import com.filescout.filescout_api.common.dto.ApiResponse;

public interface AuthService {

    ApiResponse<AuthResponse> register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);
    ApiResponse<RefreshTokenResponse> refreshToken(RefreshTokenRequest request);
    ApiResponse<Void> forgotPassword(ForgotPasswordRequest request);
    ApiResponse<Void> resetPassword(ResetPasswordRequest request);
}
