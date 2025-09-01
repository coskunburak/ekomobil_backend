package com.ekomobil.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest
{
    @NotBlank @Size(min=2, max=80)
    private String name;

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min=6, max=64)
    private String password;

    @NotBlank @Size(min=3, max=40)
    private String username;
}
