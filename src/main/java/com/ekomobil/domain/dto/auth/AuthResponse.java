package com.ekomobil.domain.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponse
{
    private String token;
    private Long id;
    private String name;
    private String email;

    public AuthResponse(String token, Long id, String name, String email) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
