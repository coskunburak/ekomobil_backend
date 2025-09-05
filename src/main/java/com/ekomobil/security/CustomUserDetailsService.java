package com.ekomobil.security;

import com.ekomobil.domain.entity.Role;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = repo.findByEmail(email)
                .or(() -> repo.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        Collection<? extends GrantedAuthority> authorities = toAuthorities(u.getRoles());

        return new UserPrincipal(
                u.getId(),
                u.getEmail(),
                u.getPassword(),
                authorities
        );
    }

    private Collection<? extends GrantedAuthority> toAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles.stream()
                .map(Role::getName)
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
