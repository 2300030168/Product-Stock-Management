package com.hackathon.inventory.dto;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String category,
        String baseUnit,
        double reorderLevelBase,
        double currentStockBase,
        String stockDisplay,      // e.g. "3 bag 10 kg"
        boolean lowStock,
        boolean outOfStock,
        List<ConversionDto> conversions,
        List<String> availableUnits) {}
