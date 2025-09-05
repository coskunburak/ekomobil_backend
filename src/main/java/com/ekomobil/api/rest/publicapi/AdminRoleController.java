package com.ekomobil.api.rest.publicapi;

import com.ekomobil.repo.RoleRepository;
import com.ekomobil.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminRoleController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    @GetMapping("/roles")
    public List<String> listRoles() {
        return roleRepo.findAll().stream()
                .map(r -> r.getName())
                .sorted(Comparator.nullsLast(String::compareToIgnoreCase))
                .toList();
    }
}
