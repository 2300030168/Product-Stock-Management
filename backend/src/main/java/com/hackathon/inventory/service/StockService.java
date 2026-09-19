package com.hackathon.inventory.service;

import com.hackathon.inventory.dto.StockRequest;
import com.hackathon.inventory.dto.TransactionResponse;
import com.hackathon.inventory.entity.Product;
import com.hackathon.inventory.entity.StockTransaction;
import com.hackathon.inventory.entity.TransactionType;
import com.hackathon.inventory.repository.ProductRepository;
import com.hackathon.inventory.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository products;
    private final StockTransactionRepository txns;
    private final UnitService units;

    /** Stock IN / OUT / DAMAGED. The voice module will call this after the user confirms. */
    @Transactional
    public TransactionResponse record(StockRequest r) {
        Product p = products.findById(r.productId()).filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        double base = units.toBase(p, r.unit(), r.quantity());
        double signed = r.type() == TransactionType.IN ? base : -base;
        double current = txns.currentStock(p.getId());

        if (signed < 0 && current + signed < -1e-9 && !Boolean.TRUE.equals(r.force())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only " + units.format(p, current) + " of " + p.getName() + " in stock.");
        }

        StockTransaction t = new StockTransaction();
        t.setProduct(p);
        t.setType(r.type());
        t.setQuantity(r.quantity());
        t.setUnit(units.normalize(r.unit()));
        t.setQtyBase(signed);
        t.setPrice(r.price());
        t.setNote(r.note());
        t = txns.save(t);
        return toResponse(t, units.format(p, current + signed));
    }

    /** Undo = mark as cancelled. The row stays in history. */
    @Transactional
    public TransactionResponse undo(Long id) {
        StockTransaction t = txns.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        if (t.isCancelled())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already cancelled");
        double current = txns.currentStock(t.getProduct().getId());
        if (current - t.getQtyBase() < -1e-9)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot undo: stock would go below zero");
        t.setCancelled(true);
        return toResponse(t, units.format(t.getProduct(), current - t.getQtyBase()));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(Long productId) {
        List<StockTransaction> list = productId == null
                ? txns.findTop100ByOrderByCreatedAtDesc()
                : txns.findTop100ByProductIdOrderByCreatedAtDesc(productId);
        return list.stream().map(t -> toResponse(t, null)).toList();
    }

    private TransactionResponse toResponse(StockTransaction t, String stockNow) {
        return new TransactionResponse(t.getId(), t.getProduct().getId(), t.getProduct().getName(),
                t.getType().name(), t.getQuantity(), t.getUnit(), t.getQtyBase(), t.getPrice(),
                t.getNote(), t.isCancelled(), t.getCreatedAt(), stockNow);
    }
}
