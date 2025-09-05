// src/main/java/com/ekomobil/api/rest/publicapi/AdminRoleController.java
package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.entity.Role;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.RoleRepository;
import com.ekomobil.repo.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
