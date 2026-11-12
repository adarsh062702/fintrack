package com.adarsh.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 255)
        String description,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal targetAmount,

        LocalDate deadline
) {}