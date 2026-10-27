
package com.adarsh.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String type,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
