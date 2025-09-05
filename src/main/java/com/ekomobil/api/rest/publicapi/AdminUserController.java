package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.entity.Role;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.RoleRepository;
import com.ekomobil.repo.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public record AdminUserDto(
            Long id,
            String name,
            String username,
            String email,
            Instant createdAt,
            Instant updatedAt,
            boolean enabled,
            Set<String> roles
    ) {}

    public record ToggleEnabledRequest(boolean enabled) {}

    @GetMapping
    public Page<AdminUserDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q
    ) {
        final int p = Math.max(page, 0);
        final int s = Math.min(Math.max(size, 1), 200);
        final Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

        Page<User> data;
        if (q == null || q.isBlank()) {
            data = userRepo.findAll(pageable);
        } else {
            data = userRepo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        }

        final List<AdminUserDto> dtos = data.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageImpl<>(dtos, pageable, data.getTotalElements());
    }

    @GetMapping("/{id}")
    public AdminUserDto get(@PathVariable Long id) {
        final var u = userRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
        return toDto(u);
    }

    @PatchMapping("/{id}/enabled")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void toggleEnabled(@PathVariable Long id, @RequestBody @Valid ToggleEnabledRequest req) {
        final var u = userRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        if (u.isEnabled() == req.enabled()) return;

        u.setEnabled(req.enabled());
        userRepo.save(u);
    }

    @PostMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void updateUserRoles(
            @PathVariable Long userId,
            @RequestBody @Valid List<String> roles
    ) {
        var u = userRepo.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        final Set<String> safeRoles = (roles == null) ? Set.of() : new HashSet<>(roles);

        final Set<Role> roleEntities = safeRoles.stream()
                .map(name -> roleRepo.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Geçersiz rol: " + name)))
                .collect(Collectors.toSet());

        u.setRoles(roleEntities);
        userRepo.save(u);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı");
        }
        userRepo.deleteById(id);
    }


    private AdminUserDto toDto(User u) {
        final Set<String> roleNames = (u.getRoles() == null)
                ? Set.of()
                : u.getRoles().stream()
                .map(Role::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new AdminUserDto(
                u.getId(),
                u.getName(),
                u.getUsername(),
                u.getEmail(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.isEnabled(),
                roleNames
        );
    }
}
