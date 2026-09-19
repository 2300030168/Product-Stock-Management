package com.hackathon.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record ProductRequest(
        @NotBlank String name,
        String category,
        @NotBlank String baseUnit,
        @PositiveOrZero Double reorderLevel,
        @PositiveOrZero Double openingStock,
        @Valid List<ConversionDto> conversions) {}
