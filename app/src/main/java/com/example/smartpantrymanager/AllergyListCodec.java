package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AllergyListCodec
{
    private static final String FORMAT_PREFIX = "allergy-list-v1:";

    private AllergyListCodec()
    {
    }

    public static List<String> decode(String storedValue)
    {
        List<String> allergies = new ArrayList<>();
        if (storedValue == null || storedValue.trim().isEmpty())
        {
            return allergies;
        }
        boolean currentFormat = storedValue.startsWith(FORMAT_PREFIX);
        String value = currentFormat ? storedValue.substring(FORMAT_PREFIX.length()) : storedValue;
        String separator = currentFormat ? "\\n" : "[,;\\n]";
        for (String allergy : value.split(separator))
        {
            addIfUnique(allergies, allergy);
        }
        return allergies;
    }

    public static String encode(List<String> allergies)
    {
        if (allergies == null)
        {
            throw new IllegalArgumentException("allergies must not be null.");
        }
        List<String> normalizedAllergies = new ArrayList<>();
        for (String allergy : allergies)
        {
            addIfUnique(normalizedAllergies, allergy);
        }
        return normalizedAllergies.isEmpty() ? ""
                : FORMAT_PREFIX + String.join("\n", normalizedAllergies);
    }

    public static boolean addIfUnique(List<String> allergies, String value)
    {
        if (allergies == null)
        {
            throw new IllegalArgumentException("allergies must not be null.");
        }
        if (value == null || value.trim().isEmpty())
        {
            return false;
        }
        String normalized = value.trim();
        for (String existing : allergies)
        {
            if (existing.toLowerCase(Locale.ROOT).equals(normalized.toLowerCase(Locale.ROOT)))
            {
                return false;
            }
        }
        allergies.add(normalized);
        return true;
    }
}
