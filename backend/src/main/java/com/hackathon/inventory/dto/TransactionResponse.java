package com.hackathon.inventory.dto;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long productId,
        String productName,
        String type,
        double quantity,
        String unit,
        double qtyBase,
        Double price,
        String note,
        boolean cancelled,
        LocalDateTime createdAt,
        String stockNow) {}
