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
// @RequiredArgsConstructor — Lombok генерирует конструктор
// для всех final полей. Это и есть Dependency Injection!
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security вызывает этот метод когда нужно
     * загрузить пользователя по username.
     *
     * В нашем случае username = userId (UUID как строка).
     * Мы используем UUID а не email потому что:
     * 1. Гости не имеют email
     * 2. UUID всегда уникален
     */
    // Добавь в CustomUserDetailsService.java новый метод:

    /**
     * Загрузка по email — используется AuthenticationManager при логине.
     * Переопределяем поведение: если строка содержит @ — ищем по email,
     * иначе — по UUID (для JWT фильтра).
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Определяем: это email или UUID?
        if (username.contains("@")) {
            // Логин по email
            return userRepository
                    .findByEmailAndIsDeletedFalse(username)
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Пользователь не найден: " + username
                    ));
        } else {
            // JWT фильтр передаёт UUID
            UUID id;
            try {
                id = UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                throw new UsernameNotFoundException(
                        "Неверный формат: " + username
                );
            }
            return userRepository
                    .findByIdAndIsDeletedFalse(id)
                    .map(CustomUserDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Пользователь не найден: " + username
                    ));
        }
    }