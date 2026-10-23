package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;




public class IngredientAvailabilityEvaluatorTest
{
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @Test
    public void anIngredientThatIsNotInThePantryIsMissing()
    {
        assertEquals(IngredientAvailability.MISSING, IngredientAvailabilityEvaluator.evaluate(
                null, unit("g"), unit("g"), 100, "Flour", TODAY));
    }

    @Test
    public void anExpiredIngredientNeverCounts()
    {
        PantryItem stock = stock(500, "g", TODAY.minusDays(1));

        assertEquals(IngredientAvailability.EXPIRED, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("g"), unit("g"), 100, "Flour", TODAY));
    }

    @Test
    public void anIngredientThatExpiresTodayStillCounts()
    {
        PantryItem stock = stock(500, "g", TODAY);

        assertEquals(IngredientAvailability.AVAILABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("g"), unit("g"), 100, "Flour", TODAY));
    }

    @Test
    public void tooLittleOfAnIngredientDoesNotCount()
    {
        PantryItem stock = stock(50, "g", null);

        assertEquals(IngredientAvailability.INSUFFICIENT_QUANTITY,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("g"), unit("g"), 100, "Flour",
                        TODAY));
    }

    @Test
    public void quantitiesAreComparedThroughTheBaseFactorOfTheUnit()
    {
        PantryItem stock = stock(1, "kg", null);

        assertEquals(IngredientAvailability.AVAILABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("kg"), unit("g"), 500, "Flour", TODAY));
        assertEquals(IngredientAvailability.INSUFFICIENT_QUANTITY,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("kg"), unit("g"), 1500, "Flour",
                        TODAY));
    }

    @Test
    public void countableUnitsCoverOneAnother()
    {
        PantryItem stock = stock(5, "slice", null);

        assertEquals(IngredientAvailability.AVAILABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("slice"), unit("piece"), 1, "Onion", TODAY));
        assertEquals(IngredientAvailability.INSUFFICIENT_QUANTITY,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("slice"), unit("piece"), 6,
                        "Onion", TODAY));
    }

    @Test
    public void aWeighedAmountCoversAMeasuredAmountWhenTheDensityIsKnown()
    {
        PantryItem stock = stock(500, "g", null);

        assertEquals(IngredientAvailability.AVAILABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("g"), unit("tsp"), 1, "Salt", TODAY));
        assertEquals(IngredientAvailability.INSUFFICIENT_QUANTITY,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("g"), unit("tsp"), 200, "Salt",
                        TODAY));
    }

    @Test
    public void aMeasuredAmountCoversAWeighedAmountWhenTheDensityIsKnown()
    {
        PantryItem stock = stock(1, "l", null);

        assertEquals(IngredientAvailability.AVAILABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("l"), unit("g"), 900, "Water", TODAY));
    }

    @Test
    public void anAmountWithoutAKnownDensityCannotBeCompared()
    {
        PantryItem stock = stock(500, "g", null);

        assertEquals(IngredientAvailability.INCOMPATIBLE_UNIT,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("g"), unit("tsp"), 1,
                        "Chicken breast", TODAY));
    }

    @Test
    public void aRequirementWithoutAUnitCannotBeChecked()
    {
        PantryItem stock = stock(500, "g", null);

        assertEquals(IngredientAvailability.UNVERIFIABLE, IngredientAvailabilityEvaluator.evaluate(
                stock, unit("g"), null, 1, "Flour", TODAY));
    }

    @Test
    public void countedItemsAreNeverComparedWithWeighedAmounts()
    {
        PantryItem stock = stock(500, "g", null);

        assertEquals(IngredientAvailability.INCOMPATIBLE_UNIT,
                IngredientAvailabilityEvaluator.evaluate(stock, unit("g"), unit("piece"), 1,
                        "Chicken breast", TODAY));
    }

    @Test
    public void densitiesAreFoundThroughTheSynonymCatalogue()
    {
        assertEquals(IngredientDensity.gramsPerMillilitre("butter"),
                IngredientDensity.gramsPerMillilitre("Unsalted Butter"));
        assertEquals(IngredientDensity.gramsPerMillilitre("olive oil"),
                IngredientDensity.gramsPerMillilitre("Extra Virgin Olive Oil"));
        assertNull(IngredientDensity.gramsPerMillilitre("Chicken breast"));
        assertTrue(IngredientDensity.isKnown("Salt"));
        assertFalse(IngredientDensity.isKnown("Saffron"));
    }

    @Test
    public void everyMeasurementFamilyIsRecognised()
    {
        assertEquals(MeasurementFamily.MASS, MeasurementFamily.forDimension("mass"));
        assertEquals(MeasurementFamily.VOLUME, MeasurementFamily.forDimension("volume"));
        assertEquals(MeasurementFamily.COUNT, MeasurementFamily.forDimension("piece"));
        assertEquals(MeasurementFamily.COUNT, MeasurementFamily.forDimension("slice"));
        assertEquals(MeasurementFamily.COUNT, MeasurementFamily.forDimension("clove"));
        assertEquals(MeasurementFamily.COUNT, MeasurementFamily.forDimension("can"));
        assertNull(MeasurementFamily.forDimension("parsec"));
        assertNull(MeasurementFamily.forDimension(null));
    }

    private static PantryItem stock(double quantity, String unitCode, LocalDate expiry)
    {
        return new PantryItem(1, quantity, unitCode, expiry);
    }

    private static MeasurementUnit unit(String unitCode)
    {
        List<MeasurementUnit> units = Arrays.asList(
                new MeasurementUnit("g", "grams", "mass", 1),
                new MeasurementUnit("kg", "kilograms", "mass", 1000),
                new MeasurementUnit("ml", "millilitres", "volume", 1),
                new MeasurementUnit("l", "litres", "volume", 1000),
                new MeasurementUnit("tsp", "teaspoons", "volume", 5),
                new MeasurementUnit("tbsp", "tablespoons", "volume", 15),
                new MeasurementUnit("cup", "cups", "volume", 240),
                new MeasurementUnit("piece", "pieces", "piece", 1),
                new MeasurementUnit("slice", "slices", "slice", 1));
        for (MeasurementUnit unit : units)
        {
            if (unit.getUnitCode().equals(unitCode))
            {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown unit " + unitCode);
    }
}
