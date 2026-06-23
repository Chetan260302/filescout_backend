package com.filescout.filescout_api.auth.service.impl;

// import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.filescout.filescout_api.auth.dto.AuthResponse;
import com.filescout.filescout_api.auth.dto.LoginRequest;
import com.filescout.filescout_api.auth.dto.LoginResponse;
import com.filescout.filescout_api.auth.dto.RefreshTokenRequest;
import com.filescout.filescout_api.auth.dto.RefreshTokenResponse;
import com.filescout.filescout_api.auth.dto.RegisterRequest;
import com.filescout.filescout_api.auth.service.AuthService;
import com.filescout.filescout_api.common.dto.ApiResponse;
import com.filescout.filescout_api.common.exception.ResourceAlreadyExistsException;
import com.filescout.filescout_api.common.exception.ResourceNotFoundException;
import com.filescout.filescout_api.common.exception.UnAuthorizedException;
import com.filescout.filescout_api.security.service.JwtService;
import com.filescout.filescout_api.user.entity.Role;
import com.filescout.filescout_api.user.entity.User;
import com.filescout.filescout_api.user.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public ApiResponse<AuthResponse> register(
        RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new ResourceAlreadyExistsException("Email already Registered");
        }

        User user =User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken =jwtService.generateRefreshToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("User registered successfully")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!matches){
            throw new UnAuthorizedException("Invalid credentials");
        }
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken =jwtService.generateRefreshToken(user.getEmail());
        
        LoginResponse loginResponse = LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();

        return ApiResponse.<LoginResponse>builder()
            .success(true)
            .message("User logged in successfully")
            .data(loginResponse)
            .build();
    }

    @Override
    public ApiResponse<RefreshTokenResponse> refreshToken(RefreshTokenRequest request){
        
        if(!jwtService.isRefreshTokenValid(request.getRefreshToken())){
            throw new UnAuthorizedException("Invalid refresh token");
        }

        String email=jwtService.extractUsername(request.getRefreshToken());
        String newAccessToken = jwtService.generateAccessToken(email);
        
        RefreshTokenResponse refreshTokenResponse = RefreshTokenResponse.builder()
            .accessToken(newAccessToken)
            .build();

        return ApiResponse.<RefreshTokenResponse>builder()
            .success(true)
            .message("Access token generated successfully")
            .data(refreshTokenResponse)
            .build();
    }
}
