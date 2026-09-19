package com.hackathon.inventory.service;

import com.hackathon.inventory.dto.ConversionDto;
import com.hackathon.inventory.dto.ProductRequest;
import com.hackathon.inventory.dto.ProductResponse;
import com.hackathon.inventory.entity.Product;
import com.hackathon.inventory.entity.StockTransaction;
import com.hackathon.inventory.entity.TransactionType;
import com.hackathon.inventory.entity.UnitConversion;
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
public class ProductService {

    private final ProductRepository products;
    private final StockTransactionRepository txns;
    private final UnitService units;

    @Transactional
    public ProductResponse create(ProductRequest r) {
        String base = units.normalize(r.baseUnit());
        if (!UnitService.BASE_UNITS.contains(base))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Base unit must be kg, litre or piece");
        String name = r.name().trim();
        if (products.existsByNameIgnoreCase(name))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product '" + name + "' already exists");

        Product p = new Product();
        p.setName(name);
        p.setCategory(r.category());
        p.setBaseUnit(base);
        p.setReorderLevelBase(r.reorderLevel() == null ? 0 : r.reorderLevel());
        applyConversions(p, r.conversions());
        p = products.save(p);

        if (r.openingStock() != null && r.openingStock() > 0) {
            StockTransaction t = new StockTransaction();
            t.setProduct(p);
            t.setType(TransactionType.IN);
            t.setQuantity(r.openingStock());
            t.setUnit(base);
            t.setQtyBase(r.openingStock());
            t.setNote("Opening stock");
            txns.save(t);
        }
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String q) {
        List<Product> list = (q == null || q.isBlank())
                ? products.findByActiveTrueOrderByNameAsc()
                : products.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(q.trim());
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock() {
        return list("").stream().filter(p -> p.lowStock() || p.outOfStock()).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest r) {
        Product p = find(id);
        String name = r.name().trim();
        if (!name.equalsIgnoreCase(p.getName()) && products.existsByNameIgnoreCase(name))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product '" + name + "' already exists");
        p.setName(name);
        p.setCategory(r.category());
        if (r.reorderLevel() != null) p.setReorderLevelBase(r.reorderLevel());
        applyConversions(p, r.conversions());
        return toResponse(p);
    }

    /** Soft delete: history is kept. */
    @Transactional
    public void deactivate(Long id) {
        find(id).setActive(false);
    }

    @Transactional
    public ProductResponse addConversion(Long id, ConversionDto c) {
        Product p = find(id);
        upsertConversion(p, c);
        return toResponse(p);
    }

    // ---------- helpers ----------

    private Product find(Long id) {
        return products.findById(id).filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void applyConversions(Product p, List<ConversionDto> list) {
        if (list == null) return;
        p.getConversions().clear();
        for (ConversionDto c : list) upsertConversion(p, c);
    }

    private void upsertConversion(Product p, ConversionDto c) {
        String unit = units.normalize(c.unitName());
        if (unit.equals(p.getBaseUnit()) || units.isStandard(unit))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'" + unit + "' is a standard unit. Use a trade unit like bag, carton or box.");
        UnitConversion existing = p.getConversions().stream()
                .filter(x -> x.getUnitName().equals(unit)).findFirst().orElse(null);
        if (existing == null) {
            existing = new UnitConversion();
            existing.setProduct(p);
            existing.setUnitName(unit);
            p.getConversions().add(existing);
        }
        existing.setToBaseQty(c.toBaseQty());
    }

    public ProductResponse toResponse(Product p) {
        double stock = txns.currentStock(p.getId());
        boolean out = stock <= 0;
        boolean low = p.getReorderLevelBase() > 0 && stock <= p.getReorderLevelBase();
        List<ConversionDto> convs = p.getConversions().stream()
                .map(c -> new ConversionDto(c.getUnitName(), c.getToBaseQty())).toList();
        return new ProductResponse(p.getId(), p.getName(), p.getCategory(), p.getBaseUnit(),
                p.getReorderLevelBase(), stock, units.format(p, stock), low, out,
                convs, units.availableUnits(p));
    }
}
