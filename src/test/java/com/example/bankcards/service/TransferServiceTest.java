package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private TransferService transferService;

    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).build();

        fromCard = Card.builder()
                .id(1L)
                .balance(new BigDecimal("1000.00"))
                .currency("RUB")
                .status(Status.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .user(user)
                .build();

        toCard = Card.builder()
                .id(2L)
                .balance(new BigDecimal("500.00"))
                .currency("RUB")
                .status(Status.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .user(user)
                .build();
    }

    @Test
    void createTransfer_InsufficientFunds_ThrowsException() {
        // Given
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("2000.00"), "Test");

        when(cardRepository.existsByUserIdAndId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByUserIdAndId(1L, 2L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(1L, request))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void createTransfer_SameCard_ThrowsException() {
        // Given
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("100.00"), "Test");

        when(cardRepository.existsByUserIdAndId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("same card");
    }
}

