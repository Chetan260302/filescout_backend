package com.filescout.filescout_api.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.filescout.filescout_api.common.exception.ResourceAlreadyExistsException;
import com.filescout.filescout_api.common.exception.ResourceNotFoundException;
import com.filescout.filescout_api.common.exception.UnAuthorizedException;
import com.filescout.filescout_api.security.service.JwtService;
import com.filescout.filescout_api.user.entity.Role;
import com.filescout.filescout_api.user.entity.User;
import com.filescout.filescout_api.user.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
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

    @Override
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        // Send reset email
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        sendResetEmail(user.getEmail(), user.getFullName(), resetLink);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Password reset link has been sent to your email")
                .build();
    }

    @Override
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new UnAuthorizedException("Invalid or expired reset link"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // Clear expired token
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new UnAuthorizedException("Reset link has expired. Please request a new one.");
        }

        // Update password and clear token
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Password has been reset successfully")
                .build();
    }

    private void sendResetEmail(String toEmail, String fullName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("FileScout - Reset Your Password");

            String htmlContent = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0;">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display: inline-block; background: linear-gradient(135deg, #3b82f6, #6366f1); border-radius: 12px; padding: 12px; margin-bottom: 16px;">
                            <span style="color: white; font-size: 20px; font-weight: bold;">FileScout</span>
                        </div>
                        <h2 style="color: #1e293b; font-size: 22px; margin: 0;">Reset Your Password</h2>
                    </div>
                    <p style="color: #475569; font-size: 14px; line-height: 1.6;">Hi %s,</p>
                    <p style="color: #475569; font-size: 14px; line-height: 1.6;">We received a request to reset your password. Click the button below to set a new password:</p>
                    <div style="text-align: center; margin: 28px 0;">
                        <a href="%s" style="display: inline-block; background: #3b82f6; color: white; text-decoration: none; padding: 12px 32px; border-radius: 10px; font-size: 14px; font-weight: 600;">Reset Password</a>
                    </div>
                    <p style="color: #94a3b8; font-size: 12px; line-height: 1.6;">This link will expire in 30 minutes. If you didn't request this, you can safely ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;" />
                    <p style="color: #94a3b8; font-size: 11px; text-align: center;">© 2026 FileScout. All rights reserved.</p>
                </div>
                """.formatted(fullName, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't fail the request - token is still saved
            System.err.println("Failed to send reset email: " + e.getMessage());
        }
    }
}
