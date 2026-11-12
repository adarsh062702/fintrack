package com.adarsh.fintrack.repository;

import com.adarsh.fintrack.entity.GoalContribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface GoalContributionRepository
        extends JpaRepository<GoalContribution, Long> {

    @Query("""
            SELECT COALESCE(SUM(gc.amount), 0)
            FROM GoalContribution gc
            WHERE gc.goal.id = :goalId
            """)
    BigDecimal getTotalContributed(
            @Param("goalId") Long goalId
    );

    List<GoalContribution> findByGoalIdOrderByContributionDateDesc(
            Long goalId
    );
}