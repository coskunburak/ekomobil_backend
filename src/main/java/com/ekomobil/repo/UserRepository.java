package com.ekomobil.repo;

import com.ekomobil.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable
    );
    boolean existsByEmail(String email);
    boolean existsByUsername(String userName);

}
