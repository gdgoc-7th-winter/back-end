package com.project.user.domain.repository;
import com.project.user.domain.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.id = :id")
    Optional<User> findById(Long id);

    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.socialAccounts sa WHERE sa.email = :email AND sa.provider = :provider")
    boolean existsBySocialAuth(@Param("email") String email, @Param("provider") String provider);

    @Query("SELECT u FROM User u WHERE EXISTS (" +
            "SELECT 1 FROM u.socialAccounts s WHERE s.providerId = :providerId)")
    Optional<User> findByProviderId(@Param("providerId") String providerId);
}
