package com.plantidentifier.security;

import com.plantidentifier.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Обёртка над нашим User entity для Spring Security.
 *
 * Spring Security не знает про наш User класс.
 * Он работает только с UserDetails интерфейсом.
 * Этот класс — мост между ними.
 */
public class CustomUserDetails implements UserDetails {

    // Храним оригинальный entity — можем достать в любой момент
    @Getter
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Удобный метод — достать userId без user.getId()
     */
    public UUID getUserId() {
        return user.getId();
    }

    /**
     * Язык пользователя — используем в контроллерах
     * чтобы не делать запрос в БД
     */
    public String getLanguage() {
        return user.getPreferredLanguage();
    }

    // ──────────────────────────────────────────────────
    // Методы UserDetails — Spring Security их вызывает
    // ──────────────────────────────────────────────────

    /**
     * САМЫЙ ВАЖНЫЙ метод.
     * Spring Security спрашивает: "какие права у этого пользователя?"
     * Мы отвечаем: "вот его роль"
     *
     * SimpleGrantedAuthority("ROLE_ADMIN") означает:
     *   hasRole("ADMIN") → true
     *   hasRole("USER")  → false
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(user.getRole().name())
                // user.getRole().name() → "ROLE_GUEST" | "ROLE_USER" |
                //                         "ROLE_ADMIN" | "ROLE_SYSTEM"
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // Spring использует это как уникальный идентификатор
        // Используем UUID как строку
        return user.getId().toString();
    }

    /**
     * Аккаунт не заблокирован?
     * Проверяем наш статус BLOCKED
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != User.UserStatus.BLOCKED;
    }

    /**
     * Аккаунт активен?
     * Удалённый пользователь не может войти
     */
    @Override
    public boolean isEnabled() {
        return !user.getIsDeleted()
                && user.getStatus() == User.UserStatus.ACTIVE;
    }

    // Эти два метода оставляем true — не используем
    // истечение credentials в нашей архитектуре
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}