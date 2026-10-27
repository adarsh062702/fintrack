
package com.adarsh.fintrack.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        String month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal savings,
        long transactionCount
) {
}
