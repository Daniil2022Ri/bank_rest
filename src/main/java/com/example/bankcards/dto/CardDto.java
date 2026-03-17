package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDto {

    private Long id;

    @NotBlank(message = "Card number is required", groups = Create.class)
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits", groups = Create.class)
    private String cardNumber;

    private String maskedCardNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotNull(message = "Expiration date is required")
    private LocalDate expirationDate;

    private Card status;
    private BigDecimal balance;
    private String currency;
    private Long userId;
    private boolean expired;

    public interface Create {}
    public interface Update {}
}

