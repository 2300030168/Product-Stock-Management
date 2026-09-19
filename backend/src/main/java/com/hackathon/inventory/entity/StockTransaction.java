package com.hackathon.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One stock movement. Current stock = sum of qtyBase of non-cancelled rows. */
@Entity
@Getter @Setter @NoArgsConstructor
public class StockTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    /** Quantity and unit exactly as the user entered them. */
    private double quantity;
    private String unit;

    /** Signed quantity in base unit (+ for IN, - for OUT/DAMAGED). */
    private double qtyBase;

    private Double price;

    @Column(length = 500)
    private String note;

    private boolean cancelled = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
