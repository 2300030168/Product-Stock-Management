package com.hackathon.inventory.controller;

import com.hackathon.inventory.dto.StockRequest;
import com.hackathon.inventory.dto.TransactionResponse;
import com.hackathon.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody StockRequest r) {
        return service.record(r);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> history(@RequestParam(required = false) Long productId) {
        return service.history(productId);
    }

    @PostMapping("/transactions/{id}/undo")
    public TransactionResponse undo(@PathVariable Long id) {
        return service.undo(id);
    }
}
