package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.models.MeasurementUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class ValidationHelper
{
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    private ValidationHelper()
    {
    }

    public static String requireText(String value, String field)
    {
        if (value == null || value.trim().isEmpty())
        {
            throw new IllegalArgumentException(field + " must not be null or blank.");
        }
        return value.trim();
    }

    public static double requirePositiveQuantity(double quantity, String field)
    {
        if (!Double.isFinite(quantity) || quantity <= 0)
        {
            throw new IllegalArgumentException(field + " must be a positive finite number.");
        }
        return quantity;
    }

    public static String requireUnitCode(String unitCode)
    {
        return requireText(unitCode, "unitCode").toLowerCase(Locale.ROOT);
    }

    public static MeasurementUnit requireUnit(MeasurementUnit unit, String field)
    {
        if (unit == null || !Double.isFinite(unit.getToBaseFactor()) || unit.getToBaseFactor() <= 0)
        {
            throw new IllegalArgumentException(field + " is not a valid measurement unit.");
        }
        return unit;
    }

    public static LocalDate parseOptionalDate(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        String text = value.trim();
        if (!text.matches("\\d{4}-\\d{2}-\\d{2}"))
        {
            throw new IllegalArgumentException("Date must use YYYY-MM-DD format.");
        }
        try
        {
            return LocalDate.parse(text, DATE_FORMAT);
        }
        catch (RuntimeException exception)
        {
            throw new IllegalArgumentException("Date must be a valid calendar date.", exception);
        }
    }

    public static String validateDate(String value)
    {
        LocalDate date = parseOptionalDate(value);
        return date == null ? null : date.toString();
    }

    public static int requireNonNegativeTime(Integer minutes, String field)
    {
        if (minutes == null || minutes < 0)
        {
            throw new IllegalArgumentException(field + " must be zero or greater.");
        }
        return minutes;
    }

    public static int requirePositiveServings(Integer servings)
    {
        if (servings == null || servings <= 0)
        {
            throw new IllegalArgumentException("servings must be greater than zero.");
        }
        return servings;
    }

    public static String requireTheme(String theme)
    {
        String normalized = requireText(theme, "theme").toLowerCase(Locale.ROOT);
        if (!"light".equals(normalized) && !"dark".equals(normalized) && !"system".equals(normalized))
        {
            throw new IllegalArgumentException("Theme must be light, dark, or system.");
        }
        return normalized;
    }

    public static double convertQuantity(double quantity, MeasurementUnit from, MeasurementUnit to)
    {
        requirePositiveQuantity(quantity, "quantity");
        requireUnit(from, "from");
        requireUnit(to, "to");
        if (!from.getDimension().equals(to.getDimension()))
        {
            throw new IllegalArgumentException("Measurement units have incompatible dimensions.");
        }
        return quantity * from.getToBaseFactor() / to.getToBaseFactor();
    }
}
