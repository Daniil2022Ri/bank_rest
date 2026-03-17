package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Status;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.BankCardException;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransferDto createTransfer(Long userId, TransferRequest request) {
        // Validate cards belong to user
        if (!cardRepository.existsByUserIdAndId(userId, request.getFromCardId())) {
            throw new BankCardException("Source card does not belong to user");
        }
        if (!cardRepository.existsByUserIdAndId(userId, request.getToCardId())) {
            throw new BankCardException("Destination card does not belong to user");
        }

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getFromCardId()));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getToCardId()));

        // Validate transfer
        validateTransfer(fromCard, toCard, request.getAmount());

        // Create transfer record
        Transfer transfer = Transfer.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.getAmount())
                .currency(fromCard.getCurrency())
                .status(Status.PENDING)
                .description(request.getDescription())
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);

        try {
            // Execute transfer
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            savedTransfer.setStatus(Status.COMPLETED);
            Transfer completed = transferRepository.save(savedTransfer);

            log.info("Transfer completed: {} from card {} to card {}",
                    request.getAmount(), fromCard.getId(), toCard.getId());

            return mapToDto(completed);

        } catch (Exception e) {
            savedTransfer.setStatus(Status.FAILED);
            transferRepository.save(savedTransfer);
            log.error("Transfer failed: {}", e.getMessage());
            throw new BankCardException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<TransferDto> getMyTransfers(Long userId, int page, int size) {
        return transferRepository.findByUserId(userId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToDto);
    }

    private void validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (!fromCard.canTransfer()) {
            throw new CardBlockedException(fromCard.getId());
        }
        if (!toCard.canTransfer()) {
            throw new CardBlockedException(toCard.getId());
        }
        if (!fromCard.getCurrency().equals(toCard.getCurrency())) {
            throw new BankCardException("Currency mismatch: " + fromCard.getCurrency() + " vs " + toCard.getCurrency());
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromCard.getId(), fromCard.getBalance(), amount);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankCardException("Transfer amount must be positive");
        }
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BankCardException("Cannot transfer to the same card");
        }
    }

    private TransferDto mapToDto(Transfer transfer) {
        String fromNumber = encryptionService.decrypt(transfer.getFromCard().getCardNumberEncrypted());
        String toNumber = encryptionService.decrypt(transfer.getToCard().getCardNumberEncrypted());

        return TransferDto.builder()
                .id(transfer.getId())
                .fromCardId(transfer.getFromCard().getId())
                .toCardId(transfer.getToCard().getId())
                .maskedFromCardNumber(encryptionService.maskCardNumber(fromNumber))
                .maskedToCardNumber(encryptionService.maskCardNumber(toNumber))
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}


