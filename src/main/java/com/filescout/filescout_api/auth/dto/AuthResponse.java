package com.filescout.filescout_api.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuthResponse {

    private Long id;

    private  String fullName;

    private String email;

    private String role;

    private String accessToken;
    private String refreshToken;
}
