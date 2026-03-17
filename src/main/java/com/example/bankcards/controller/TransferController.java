package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.TransferService;
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

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Money transfer endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create transfer between own cards")
    public ResponseEntity<TransferDto> createTransfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.createTransfer(userId, request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my transfer history")
    public ResponseEntity<PageResponse<TransferDto>> getMyTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        Page<TransferDto> transfers = transferService.getMyTransfers(userId, page, size);
        return ResponseEntity.ok(PageResponse.fromPage(transfers));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        return 1L; // Placeholder - implement based on your UserDetails
    }
}

