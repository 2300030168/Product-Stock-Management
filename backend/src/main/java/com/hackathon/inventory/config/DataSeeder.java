package com.hackathon.inventory.config;

import com.hackathon.inventory.dto.ConversionDto;
import com.hackathon.inventory.dto.ProductRequest;
import com.hackathon.inventory.repository.ProductRepository;
import com.hackathon.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adds sample products on first run so the app is not empty. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository repo;
    private final ProductService service;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        service.create(new ProductRequest("Rice", "Grocery", "kg", 25.0, 80.0,
                List.of(new ConversionDto("bag", 25.0))));
        service.create(new ProductRequest("Sugar", "Grocery", "kg", 20.0, 100.0,
                List.of(new ConversionDto("bag", 50.0))));
        service.create(new ProductRequest("Cooking Oil", "Grocery", "litre", 10.0, 8.0,
                List.of(new ConversionDto("carton", 12.0))));
        service.create(new ProductRequest("Soap", "Household", "piece", 24.0, 100.0,
                List.of(new ConversionDto("carton", 48.0))));
    }
}
