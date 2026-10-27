
package com.adarsh.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateBudgetRequest(

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Budget amount is required")
        @DecimalMin(value = "0.01", message = "Budget must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Invalid year")
        Integer year
) {
}
