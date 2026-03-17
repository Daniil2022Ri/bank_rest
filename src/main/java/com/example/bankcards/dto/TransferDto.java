package com.example.bankcards.dto;

import com.example.bankcards.entity.Transfer;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferDto {

    private Long id;

    @NotNull(message = "Source card ID is required")
    private Long fromCardId;

    @NotNull(message = "Destination card ID is required")
    private Long toCardId;

    private String maskedFromCardNumber;
    private String maskedToCardNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String currency;
    private Transfer status;
    private String description;
    private LocalDateTime createdAt;
}

