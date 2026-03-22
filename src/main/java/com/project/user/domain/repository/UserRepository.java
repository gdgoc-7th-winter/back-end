package com.project.user.domain.repository;

import com.project.user.domain.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.id = :id")
    Optional<User> findById(Long id);

    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.socialAccounts sa WHERE sa.provider = :provider AND sa.providerId = :providerId")
    boolean existsBySocialAuth(@Param("provider") String provider, @Param("providerId") String providerId);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE EXISTS (" +
            "SELECT 1 FROM u.socialAccounts s WHERE s.provider = :provider AND s.providerId = :providerId)")
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
}
