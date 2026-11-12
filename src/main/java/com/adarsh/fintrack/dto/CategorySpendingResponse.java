package com.adarsh.fintrack.dto;

import java.math.BigDecimal;

public record CategorySpendingResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}