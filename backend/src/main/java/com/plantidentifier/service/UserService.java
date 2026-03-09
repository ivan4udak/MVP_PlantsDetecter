package com.plantidentifier.service;

import com.plantidentifier.dto.request.LanguageUpdateRequest;
import com.plantidentifier.entity.User;
import com.plantidentifier.exception.PlantNotFoundException;
import com.plantidentifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updateLanguage(UUID userId, LanguageUpdateRequest request) {
        log.info("Updating language for userId={} to {}",
                userId, request.preferredLanguage());

        User user = userRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() ->
                        new PlantNotFoundException("Пользователь не найден")
                );

        user.setPreferredLanguage(request.preferredLanguage());
        user.setUpdatedBy(userId.toString());

        userRepository.save(user);

        // Заметка: язык в уже выданных JWT не обновляется.
        // Новый язык появится в токене после следующего логина.
        // Это нормально — JWT это кэш, БД источник истины.
        log.info("Language updated for userId={}", userId);
    }
}