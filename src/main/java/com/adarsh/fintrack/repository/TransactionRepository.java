
package com.adarsh.fintrack.repository;

import com.adarsh.fintrack.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
    @Query("""
        SELECT
            c.id AS categoryId,
            c.name AS categoryName,
            SUM(t.amount) AS totalAmount
        FROM Transaction t
        JOIN t.category c
        WHERE t.user.id = :userId
          AND c.user.id = :userId
          AND t.type = 'EXPENSE'
          AND c.type = 'EXPENSE'
          AND t.transactionDate BETWEEN :startDate AND :endDate
        GROUP BY c.id, c.name
        ORDER BY SUM(t.amount) DESC
        """)
    List<CategorySpendingProjection> findCategoryWiseSpending(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
