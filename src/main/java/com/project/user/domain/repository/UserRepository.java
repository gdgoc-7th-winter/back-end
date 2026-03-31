package com.project.user.domain.repository;

import com.project.user.domain.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 활성만. socialAccounts fetch join. */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    /** 활성만. fetch 없음. */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveByIdLean(@Param("id") Long id);

    /** 탈퇴 포함 전체 조회. 관리/시스템용. */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE u.id = :id")
    Optional<User> findByIdIncludingDeleted(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.socialAccounts sa WHERE sa.provider = :provider AND sa.providerId = :providerId AND u.deletedAt IS NULL")
    boolean existsBySocialAuth(@Param("provider") String provider, @Param("providerId") String providerId);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.socialAccounts WHERE EXISTS (" +
            "SELECT 1 FROM u.socialAccounts s WHERE s.provider = :provider AND s.providerId = :providerId) AND u.deletedAt IS NULL")
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.totalPoint = u.totalPoint + :delta WHERE u.id = :userId AND u.deletedAt IS NULL")
    int addTotalPoints(@Param("userId") Long userId, @Param("delta") int delta);
}
