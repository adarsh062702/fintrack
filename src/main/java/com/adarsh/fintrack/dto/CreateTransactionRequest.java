
package com.adarsh.fintrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateTransactionRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 20)
    private String type;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @Size(max = 500)
    private String description;

    @NotNull
    private LocalDate transactionDate;

    @NotBlank
    @Size(max = 50)
    private String paymentMethod;
}
