package com.hackathon.inventory.repository;

import com.hackathon.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Product> findByActiveTrueOrderByNameAsc();
    List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String q);
}
