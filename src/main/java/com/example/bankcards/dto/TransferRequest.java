package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Source card ID is required")
    private Long fromCardId;

    @NotNull(message = "Destination card ID is required")
    private Long toCardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}

