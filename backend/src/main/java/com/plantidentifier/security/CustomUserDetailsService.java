package com.plantidentifier.security;

import com.plantidentifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security вызывает этот метод в двух случаях:
     *
     * 1. JwtAuthFilter — передаёт UUID пользователя
     *    (достали из JWT токена)
     *
     * 2. AuthenticationManager при логине —
     *    передаёт email (из LoginRequest)
     *
     * Определяем что пришло по наличию символа @
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("loadUserByUsername: {}", username);

        // Если содержит @ — это email (логин)
        // Если не содержит @ — это UUID (JWT фильтр)
        if (username.contains("@")) {
            return loadByEmail(username);
        } else {
            return loadByUuid(username);
        }
    }

    // ─────────────────────────────────────────────
    // Приватные методы
    // ─────────────────────────────────────────────

    private UserDetails loadByEmail(String email) {
        log.debug("Loading user by email: {}", email);

        return userRepository
                .findByEmailAndIsDeletedFalse(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + email
                ));
    }

    private UserDetails loadByUuid(String userId) {
        log.debug("Loading user by UUID: {}", userId);

        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException(
                    "Неверный формат userId: " + userId
            );
        }

        return userRepository
                .findByIdAndIsDeletedFalse(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь не найден: " + userId
                ));
    }
}