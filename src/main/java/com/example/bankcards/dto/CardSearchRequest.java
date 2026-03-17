package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardSearchRequest {

    private Card status;
    private String cardHolderName;
    private String searchTerm;
    private Boolean expiredOnly;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}


