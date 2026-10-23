package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.example.smartpantrymanager.models.MeasurementUnit;
import java.time.LocalDate;
import org.junit.Test;

public class ValidationHelperTest
{
    @Test
    public void normalizeTrimsAndCollapsesWhitespaceAndCase()
    {
        assertEquals("spring onion", IngredientNormalizer.normalize("  SPRING   Onion  "));
    }

    @Test
    public void rejectsNullAndBlankNames()
    {
        assertThrows(IllegalArgumentException.class, () -> IngredientNormalizer.normalize(null));
        assertThrows(IllegalArgumentException.class, () -> IngredientNormalizer.normalize("  "));
    }

    @Test
    public void acceptsPastExpiryDateAndNull()
    {
        assertEquals(LocalDate.of(2000, 1, 1), ValidationHelper.parseOptionalDate("2000-01-01"));
        assertNull(ValidationHelper.parseOptionalDate(null));
        assertNull(ValidationHelper.parseOptionalDate(" "));
    }

    @Test
    public void rejectsInvalidExpiryDates()
    {
        assertThrows(IllegalArgumentException.class, () -> ValidationHelper.parseOptionalDate("2024-02-30"));
        assertThrows(IllegalArgumentException.class, () -> ValidationHelper.parseOptionalDate("24-02-01"));
    }

    @Test
    public void validatesThemeAndPositiveQuantity()
    {
        assertEquals("dark", ValidationHelper.requireTheme("DARK"));
        assertEquals("system", ValidationHelper.requireTheme("SYSTEM"));
        assertThrows(IllegalArgumentException.class, () -> ValidationHelper.requirePositiveQuantity(0, "quantity"));
        assertThrows(IllegalArgumentException.class, () -> ValidationHelper.requirePositiveQuantity(-1, "quantity"));
    }

    @Test
    public void convertsMassAndVolumeUnits()
    {
        MeasurementUnit gram = unit("g", "mass", 1);
        MeasurementUnit kilogram = unit("kg", "mass", 1000);
        MeasurementUnit millilitre = unit("ml", "volume", 1);
        MeasurementUnit litre = unit("l", "volume", 1000);

        assertEquals(1000, ValidationHelper.convertQuantity(1, kilogram, gram), 0);
        assertEquals(1, ValidationHelper.convertQuantity(1000, gram, kilogram), 0);
        assertEquals(2, ValidationHelper.convertQuantity(2000, millilitre, litre), 0);
    }

    @Test
    public void rejectsIncompatibleDimensionsAndFactors()
    {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationHelper.convertQuantity(1, unit("g", "mass", 1), unit("cup", "volume", 240)));
        assertThrows(IllegalArgumentException.class, () -> unit("bad", "mass", 0));
    }

    private static MeasurementUnit unit(String code, String dimension, double factor)
    {
        return new MeasurementUnit(code, code, dimension, factor);
    }
}
