package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumberEncrypted("encrypted")
                .cardNumberHash("hash")
                .cardHolderName("Test User")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(Status.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency("RUB")
                .user(testUser)
                .build();
    }

    @Test
    void createCard_Success() {
        // Given
        CardDto dto = CardDto.builder()
                .cardHolderName("Test User")
                .expirationDate(LocalDate.now().plusYears(2))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardNumberGenerator.generateCardNumber()).thenReturn("4111111111111111");
        when(encryptionService.hash(any())).thenReturn("hash");
        when(encryptionService.encrypt(any())).thenReturn("encrypted");
        when(cardRepository.save(any())).thenReturn(testCard);
        when(encryptionService.maskCardNumber(any())).thenReturn("**** **** **** 1111");

        // When
        CardDto result = cardService.createCard(1L, dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardHolderName()).isEqualTo("Test User");
        assertThat(result.getMaskedCardNumber()).isEqualTo("**** **** **** 1111");
        verify(cardRepository).save(any());
    }

    @Test
    void getCard_NotFound_ThrowsException() {
        // Given
        when(cardRepository.findWithUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.getCard(999L))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void blockCard_Success() {
        // Given
        when(cardRepository.findWithUserById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any())).thenReturn(testCard);

        // When
        CardDto result = cardService.blockCard(1L, 1L, false);

        // Then
        assertThat(result.getStatus()).isEqualTo(Status.BLOCKED);
    }
}

