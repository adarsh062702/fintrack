
package com.adarsh.fintrack.dto;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        BigDecimal spent,
        BigDecimal remaining,
        Integer month,
        Integer year
) {
}
