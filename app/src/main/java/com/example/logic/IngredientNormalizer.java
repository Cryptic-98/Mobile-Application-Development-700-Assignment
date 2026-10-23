package com.example.smartpantrymanager.logic;

import java.util.Locale;

public final class IngredientNormalizer
{
    private IngredientNormalizer()
    {
    }

    public static String normalize(String name)
    {
        return ValidationHelper.requireText(name, "name")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
