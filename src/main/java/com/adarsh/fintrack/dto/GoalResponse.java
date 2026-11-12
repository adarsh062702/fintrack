package com.adarsh.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        BigDecimal remainingAmount,
        double progressPercentage,
        LocalDate deadline,
        String status
) {}