package com.hackathon.inventory.controller;

import com.hackathon.inventory.dto.ConversionDto;
import com.hackathon.inventory.dto.ProductRequest;
import com.hackathon.inventory.dto.ProductResponse;
import com.hackathon.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public List<ProductResponse> list(@RequestParam(defaultValue = "") String q) {
        return service.list(q);
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> lowStock() {
        return service.lowStock();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deactivate(id);
    }

    @PostMapping("/{id}/conversions")
    public ProductResponse addConversion(@PathVariable Long id, @Valid @RequestBody ConversionDto c) {
        return service.addConversion(id, c);
    }
}
