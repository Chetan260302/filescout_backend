package com.filescout.filescout_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message="Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}
