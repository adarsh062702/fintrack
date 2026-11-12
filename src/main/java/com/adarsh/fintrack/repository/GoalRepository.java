package com.adarsh.fintrack.repository;

import com.adarsh.fintrack.entity.Goal;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT g FROM Goal g
            WHERE g.id = :goalId
            AND g.user.id = :userId
            """)
    Optional<Goal> findByIdAndUserIdForUpdate(
            @Param("goalId") Long goalId,
            @Param("userId") Long userId
    );

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}