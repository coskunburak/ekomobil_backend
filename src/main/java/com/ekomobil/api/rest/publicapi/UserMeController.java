package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.*;
import com.ekomobil.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserMeController {
    private final UserService service;
    public UserMeController(UserService service){ this.service = service; }

    @Operation(summary = "Me - profilimi getir")
    @GetMapping
    public UserDto me(@AuthenticationPrincipal(expression = "id") Long userId){
        return service.me(userId);
    }

    @Operation(summary = "Me - profilimi güncelle")
    @PutMapping
    public UserDto updateMe(@AuthenticationPrincipal(expression = "id") Long userId,
                            @RequestBody @Valid UpdateProfileRequest req){
        return service.updateMe(userId, req);
    }

    @Operation(summary = "Me - şifre değiştir")
    @PatchMapping("/password")
    public void changePassword(@AuthenticationPrincipal(expression = "id") Long userId,
                               @RequestBody @Valid ChangePasswordRequest req){
        service.changePassword(userId, req);
    }
}
