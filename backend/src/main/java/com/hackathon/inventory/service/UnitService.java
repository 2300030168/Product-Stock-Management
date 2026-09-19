package com.hackathon.inventory.service;

import com.hackathon.inventory.entity.Product;
import com.hackathon.inventory.entity.UnitConversion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/** Trade-unit engine: converts any unit to the product's base unit and back to "3 bag 10 kg". */
@Service
public class UnitService {

    private record Std(String family, double factor) {}

    private static final Map<String, Std> STANDARD = Map.of(
            "kg", new Std("kg", 1), "g", new Std("kg", 0.001), "quintal", new Std("kg", 100),
            "litre", new Std("litre", 1), "ml", new Std("litre", 0.001),
            "piece", new Std("piece", 1), "dozen", new Std("piece", 12));

    private static final Map<String, String> ALIAS = Map.ofEntries(
            Map.entry("kilo", "kg"), Map.entry("kgs", "kg"), Map.entry("kilogram", "kg"),
            Map.entry("gram", "g"), Map.entry("grams", "g"), Map.entry("quintals", "quintal"),
            Map.entry("liter", "litre"), Map.entry("l", "litre"), Map.entry("litres", "litre"),
            Map.entry("pcs", "piece"), Map.entry("pc", "piece"), Map.entry("pieces", "piece"),
            Map.entry("dozens", "dozen"), Map.entry("bags", "bag"),
            Map.entry("cartons", "carton"), Map.entry("boxes", "box"));

    public static final Set<String> BASE_UNITS = Set.of("kg", "litre", "piece");

    public String normalize(String unit) {
        String u = unit.trim().toLowerCase();
        return ALIAS.getOrDefault(u, u);
    }

    public boolean isStandard(String unit) {
        return STANDARD.containsKey(unit);
    }

    /** Convert quantity in the given unit to the product's base unit. */
    public double toBase(Product p, String unit, double qty) {
        String u = normalize(unit);
        if (u.equals(p.getBaseUnit())) return qty;
        for (UnitConversion c : p.getConversions()) {
            if (c.getUnitName().equals(u)) return qty * c.getToBaseQty();
        }
        Std s = STANDARD.get(u);
        if (s != null && s.family().equals(p.getBaseUnit())) return qty * s.factor();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No conversion for '" + u + "' on " + p.getName()
                        + ". Add it first (how many " + p.getBaseUnit() + " in 1 " + u + "?)");
    }

    /** Units the user can pick for this product (trade units first). */
    public List<String> availableUnits(Product p) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        p.getConversions().forEach(c -> set.add(c.getUnitName()));
        set.add(p.getBaseUnit());
        STANDARD.entrySet().stream()
                .filter(e -> e.getValue().family().equals(p.getBaseUnit()))
                .map(Map.Entry::getKey).sorted().forEach(set::add);
        return new ArrayList<>(set);
    }

    /** 85 kg with 1 bag = 25 kg  ->  "3 bag 10 kg". */
    public String format(Product p, double baseQty) {
        if (baseQty < 0) return "-" + format(p, -baseQty);
        double rem = round(baseQty);
        if (rem == 0) return "0 " + p.getBaseUnit();
        List<UnitConversion> big = p.getConversions().stream()
                .filter(c -> c.getToBaseQty() > 1)
                .sorted(Comparator.comparingDouble(UnitConversion::getToBaseQty).reversed())
                .toList();
        StringBuilder sb = new StringBuilder();
        for (UnitConversion c : big) {
            long n = (long) Math.floor(rem / c.getToBaseQty() + 1e-9);
            if (n > 0) {
                sb.append(n).append(' ').append(c.getUnitName()).append(' ');
                rem = round(rem - n * c.getToBaseQty());
            }
        }
        if (rem > 0 || sb.length() == 0) sb.append(clean(rem)).append(' ').append(p.getBaseUnit());
        return sb.toString().trim();
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }

    private String clean(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
