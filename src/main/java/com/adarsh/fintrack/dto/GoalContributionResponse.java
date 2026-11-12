package com.adarsh.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalContributionResponse(
        Long id,
        BigDecimal amount,
        LocalDate contributionDate,
        String note,
        LocalDateTime createdAt
) {
}
