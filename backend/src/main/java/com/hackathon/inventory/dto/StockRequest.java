package com.hackathon.inventory.dto;

import com.hackathon.inventory.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequest(
        @NotNull Long productId,
        @NotNull TransactionType type,
        @NotNull @Positive Double quantity,
        @NotBlank String unit,
        @PositiveOrZero Double price,
        String note,
        Boolean force) {}   // force=true allows stock to go below zero
