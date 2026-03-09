// repository/UserRepository.java
package com.plantidentifier.repository;

import com.plantidentifier.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data автоматически создаёт реализацию
 * этого интерфейса — не нужно писать SQL для простых запросов.
 *
 * Имя метода → SQL запрос:
 * findByEmail → SELECT * FROM users WHERE email = ?
 * findByIdAndIsDeletedFalse → WHERE id = ? AND is_deleted = false
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    Optional<User> findByDeviceIdAndIsDeletedFalse(String deviceId);

    boolean existsByEmailAndIsDeletedFalse(String email);
}