package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.specification.CardSpecifications;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final CardNumberGenerator cardNumberGenerator;

    @Transactional
    public CardDto createCard(Long userId, CardDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BankCardException("User not found: " + userId));

        String cardNumber = dto.getCardNumber() != null ?
                dto.getCardNumber() : cardNumberGenerator.generateCardNumber();

        if (!cardNumberGenerator.isValidCardNumber(cardNumber)) {
            throw new BankCardException("Invalid card number");
        }

        String cardHash = encryptionService.hash(cardNumber);

        if (cardRepository.findByCardNumberHash(cardHash).isPresent()) {
            throw new BankCardException("Card number already exists");
        }

        Card card = Card.builder()
                .cardNumberEncrypted(encryptionService.encrypt(cardNumber))
                .cardNumberHash(cardHash)
                .cardHolderName(dto.getCardHolderName())
                .expirationDate(dto.getExpirationDate())
                .status(Status.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "RUB")
                .user(user)
                .build();

        Card saved = cardRepository.save(card);
        log.info("Created card for user {}: {}", userId, saved.getId());

        return mapToDto(saved, cardNumber);
    }

    @Transactional(readOnly = true)
    public CardDto getCard(Long id) {
        Card card = cardRepository.findWithUserById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        String decryptedNumber = encryptionService.decrypt(card.getCardNumberEncrypted());
        return mapToDto(card, decryptedNumber);
    }

    @Transactional(readOnly = true)
    public Page<CardDto> searchCards(CardSearchRequest request, Long userId, boolean isAdmin) {
        Specification<Card> spec = Specification.where(CardSpecifications.withUserFetched());

        if (!isAdmin && userId != null) {
            spec = spec.and(CardSpecifications.hasUserId(userId));
        }

        spec = spec.and(CardSpecifications.hasStatus(request.getStatus()))
                .and(CardSpecifications.cardHolderNameContains(request.getCardHolderName()))
                .and(CardSpecifications.isExpired(request.getExpiredOnly()))
                .and(CardSpecifications.searchTermContains(request.getSearchTerm()));

        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC") ?
                        Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );

        return cardRepository.findAll(spec, PageRequest.of(request.getPage(), request.getSize(), sort))
                .map(card -> mapToDto(card, null));
    }

    @Transactional(readOnly = true)
    public List<CardDto> getMyCards(Long userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(card -> mapToDto(card, null))
                .toList();
    }

    @Transactional
    public CardDto blockCard(Long cardId, Long userId, boolean isAdmin) {
        Card card = cardRepository.findWithUserById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!isAdmin && !card.getUser().getId().equals(userId)) {
            throw new BankCardException("Access denied to this card");
        }

        card.setStatus(Status.BLOCKED);
        Card saved = cardRepository.save(card);
        log.info("Blocked card: {}", cardId);

        return mapToDto(saved, null);
    }

    @Transactional
    public CardDto activateCard(Long cardId) {
        Card card = cardRepository.findWithUserById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (card.isExpired()) {
            throw new BankCardException("Cannot activate expired card");
        }

        card.setStatus(Status.ACTIVE);
        Card saved = cardRepository.save(card);
        log.info("Activated card: {}", cardId);

        return mapToDto(saved, null);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException(cardId);
        }
        cardRepository.deleteById(cardId);
        log.info("Deleted card: {}", cardId);
    }

    @Transactional
    public void updateExpiredCards() {
        List<Card> expiredCards = cardRepository.findAll().stream()
                .filter(card -> card.isExpired() && card.getStatus() != Status.EXPIRED)
                .toList();

        expiredCards.forEach(card -> {
            card.setStatus(Status.EXPIRED);
            log.info("Auto-expired card: {}", card.getId());
        });

        cardRepository.saveAll(expiredCards);
    }

    private CardDto mapToDto(Card card, String decryptedNumber) {
        return CardDto.builder()
                .id(card.getId())
                .cardNumber(decryptedNumber)
                .maskedCardNumber(decryptedNumber != null ?
                        encryptionService.maskCardNumber(decryptedNumber) : null)
                .cardHolderName(card.getCardHolderName())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .currency(card.getCurrency())
                .userId(card.getUser().getId())
                .expired(card.isExpired())
                .build();
    }
}


