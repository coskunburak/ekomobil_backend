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

    /**
     * Rolleri sadece isim listesi olarak döndür.
     * İstersek RoleDto ile id+name de döndürebiliriz.
     */
    @GetMapping("/roles")
    public List<String> listRoles() {
        return roleRepo.findAll().stream()
                .map(r -> r.getName())
                .sorted(Comparator.nullsLast(String::compareToIgnoreCase))
                .toList();
    }

    /**
     * Bir kullanıcının rollerini TAM SET olarak günceller.
     * Body: ["ADMIN","USER"] gibi.
     * 204 (No Content) döner.
     */
    @PostMapping("/users/{userId}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void updateUserRoles(
            @PathVariable Long userId,
            @RequestBody @Valid @NotEmpty(message = "roles boş olamaz") List<String> roles
    ) {
        final User user = userRepo.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        final Set<Role> entities = roles.stream()
                .map(name -> roleRepo.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Geçersiz rol: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(entities);
        userRepo.save(user);
    }
}
