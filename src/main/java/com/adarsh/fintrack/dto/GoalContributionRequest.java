package com.adarsh.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalContributionRequest(

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        LocalDate contributionDate,

        @Size(max = 255)
        String note
) {}