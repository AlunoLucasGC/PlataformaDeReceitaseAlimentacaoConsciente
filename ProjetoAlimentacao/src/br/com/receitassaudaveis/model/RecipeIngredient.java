package br.com.receitassaudaveis.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/** Ingrediente e quantidade cadastrada para uma receita. */
public class RecipeIngredient {

    private final String name;
    private final BigDecimal quantity;
    private final String unit;

    public RecipeIngredient(String name, BigDecimal quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public BigDecimal getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    public String formatForServings(int requestedServings, int baseServings) {
        BigDecimal scaled = quantity
                .multiply(BigDecimal.valueOf(requestedServings))
                .divide(BigDecimal.valueOf(baseServings), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        DecimalFormat format = new DecimalFormat("0.##");
        String amount = scaled.compareTo(BigDecimal.ZERO) == 0 ? "0" : format.format(scaled);
        return amount + (unit == null || unit.isBlank() ? "" : " " + unit) + " de " + name;
    }
}
