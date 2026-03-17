package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Card management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new card (Admin only)")
    public ResponseEntity<CardDto> createCard(
            @RequestParam Long userId,
            @Valid @RequestBody CardDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(userId, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @cardService.isCardOwner(#id, authentication.principal.username)")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<CardDto> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search all cards with filters (Admin only)")
    public ResponseEntity<PageResponse<CardDto>> searchCards(CardSearchRequest request) {
        Page<CardDto> page = cardService.searchCards(request, null, true);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get current user's cards")
    public ResponseEntity<List<CardDto>> getMyCards(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(cardService.getMyCards(userId));
    }

    @GetMapping("/my/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search current user's cards")
    public ResponseEntity<PageResponse<CardDto>> searchMyCards(
            CardSearchRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Page<CardDto> page = cardService.searchCards(request, userId, false);
        return ResponseEntity.ok(PageResponse.fromPage(page));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Request card block (User)")
    public ResponseEntity<CardDto> blockCard(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(cardService.blockCard(id, userId, false));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change card status (Admin only)")
    public ResponseEntity<CardDto> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(cardService.activateCard(id));
        } else if ("BLOCKED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(cardService.blockCard(id, null, true));
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete card (Admin only)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // Implementation depends on your UserDetails implementation
        return 1L; // Placeholder
    }
}

