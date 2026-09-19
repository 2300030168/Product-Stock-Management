package com.hackathon.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Per-product trade unit, e.g. rice: 1 bag = 25 (kg). */
@Entity
@Getter @Setter @NoArgsConstructor
public class UnitConversion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    @Column(nullable = false)
    private String unitName;

    /** How many base units in one of this unit. */
    private double toBaseQty;
}
