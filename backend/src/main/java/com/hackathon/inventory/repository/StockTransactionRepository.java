package com.hackathon.inventory.repository;

import com.hackathon.inventory.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    @Query("select coalesce(sum(t.qtyBase), 0.0) from StockTransaction t " +
           "where t.product.id = :pid and t.cancelled = false")
    Double currentStock(@Param("pid") Long productId);

    List<StockTransaction> findTop100ByOrderByCreatedAtDesc();
    List<StockTransaction> findTop100ByProductIdOrderByCreatedAtDesc(Long productId);
}
