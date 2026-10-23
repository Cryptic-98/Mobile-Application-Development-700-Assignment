package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
















public final class IngredientAvailabilityEvaluator
{
    private IngredientAvailabilityEvaluator()
    {
    }












    public static IngredientAvailability evaluate(PantryItem stock, MeasurementUnit stockUnit,
                                                  MeasurementUnit requiredUnit, double requiredQuantity,
                                                  String ingredientName, LocalDate today)
    {
        if (stock == null)
        {
            return IngredientAvailability.MISSING;
        }
        if (today != null && stock.getExpiryDate() != null
                && stock.getExpiryDate().isBefore(today))
        {
            return IngredientAvailability.EXPIRED;
        }
        if (requiredUnit == null || !Double.isFinite(requiredQuantity) || requiredQuantity <= 0)
        {
            return IngredientAvailability.UNVERIFIABLE;
        }
        if (stockUnit == null)
        {
            return IngredientAvailability.UNVERIFIABLE;
        }
        MeasurementFamily family = MeasurementFamily.forDimension(requiredUnit.getDimension());
        if (family == null)
        {
            return IngredientAvailability.UNVERIFIABLE;
        }
        double requiredBaseQuantity = requiredQuantity * requiredUnit.getToBaseFactor();
        return evaluateAgainstFamily(stock, stockUnit, family, requiredBaseQuantity, ingredientName,
                today);
    }


















    public static IngredientAvailability evaluateAgainstFamily(PantryItem stock,
            MeasurementUnit stockUnit, MeasurementFamily requiredFamily,
            double requiredBaseQuantity, String ingredientName, LocalDate today)
    {
        if (stock == null)
        {
            return IngredientAvailability.MISSING;
        }
        if (today != null && stock.getExpiryDate() != null
                && stock.getExpiryDate().isBefore(today))
        {
            return IngredientAvailability.EXPIRED;
        }
        if (requiredFamily == null || !Double.isFinite(requiredBaseQuantity)
                || requiredBaseQuantity <= 0)
        {
            return IngredientAvailability.UNVERIFIABLE;
        }
        if (stockUnit == null)
        {
            return IngredientAvailability.UNVERIFIABLE;
        }
        Double available = convertToFamily(stock.getQuantity(), stockUnit, ingredientName,
                requiredFamily);
        if (available == null)
        {
            return IngredientAvailability.INCOMPATIBLE_UNIT;
        }
        return available >= requiredBaseQuantity
                ? IngredientAvailability.AVAILABLE
                : IngredientAvailability.INSUFFICIENT_QUANTITY;
    }














    public static Double convertToFamily(double quantity, MeasurementUnit unit,
                                         String ingredientName, MeasurementFamily target)
    {
        if (unit == null || target == null || !Double.isFinite(quantity))
        {
            return null;
        }
        MeasurementFamily family = MeasurementFamily.forDimension(unit.getDimension());
        if (family == null)
        {
            return null;
        }
        if (family == target)
        {
            return quantity * unit.getToBaseFactor();
        }
        if (family == MeasurementFamily.COUNT || target == MeasurementFamily.COUNT)
        {
            return null;
        }
        Double density = IngredientDensity.gramsPerMillilitre(ingredientName);
        if (density == null || density <= 0)
        {
            return null;
        }
        if (family == MeasurementFamily.MASS)
        {
            double millilitres = quantity * unit.getToBaseFactor() / density;
            return target == MeasurementFamily.VOLUME ? millilitres : null;
        }
        double grams = quantity * unit.getToBaseFactor() * density;
        return target == MeasurementFamily.MASS ? grams : null;
    }
}
