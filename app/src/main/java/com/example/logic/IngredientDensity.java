package com.example.smartpantrymanager.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
















public final class IngredientDensity
{

    public static final double WATER = 1.0;

    private static final Map<String, Double> GRAMS_PER_MILLILITRE = buildTable();

    private IngredientDensity()
    {
    }





    public static Double gramsPerMillilitre(String ingredientName)
    {
        if (ingredientName == null || ingredientName.trim().isEmpty())
        {
            return null;
        }
        String normalized = IngredientSynonyms.normalize(ingredientName);
        if (normalized.isEmpty())
        {
            return null;
        }
        Double direct = GRAMS_PER_MILLILITRE.get(normalized);
        if (direct != null)
        {
            return direct;
        }
        for (String candidate : IngredientSynonyms.expand(normalized))
        {
            Double density = GRAMS_PER_MILLILITRE.get(candidate);
            if (density != null)
            {
                return density;
            }
        }
        return null;
    }





    public static boolean isKnown(String ingredientName)
    {
        return gramsPerMillilitre(ingredientName) != null;
    }




    public static Map<String, Double> knownDensities()
    {
        return Collections.unmodifiableMap(GRAMS_PER_MILLILITRE);
    }

    private static Map<String, Double> buildTable()
    {
        Map<String, Double> table = new HashMap<>();

        table.put("water", WATER);
        table.put("milk", 1.03);
        table.put("cream", 1.01);
        table.put("yogurt", 1.03);
        table.put("olive oil", 0.918);
        table.put("vegetable oil", 0.92);
        table.put("sunflower oil", 0.92);
        table.put("canola oil", 0.92);
        table.put("rapeseed oil", 0.92);
        table.put("peanut oil", 0.92);
        table.put("sesame oil", 0.92);
        table.put("vinegar", 1.01);
        table.put("soy sauce", 1.1);
        table.put("honey", 1.42);
        table.put("maple syrup", 1.32);
        table.put("wine", 0.99);
        table.put("stock", WATER);
        table.put("broth", WATER);

        table.put("butter", 0.911);
        table.put("margarine", 0.92);
        table.put("ghee", 0.91);
        table.put("lard", 0.91);
        table.put("mayonnaise", 0.91);
        table.put("peanut butter", 1.08);

        table.put("salt", 1.2);
        table.put("sugar", 0.85);
        table.put("brown sugar", 0.9);
        table.put("icing sugar", 0.55);
        table.put("powdered sugar", 0.55);
        table.put("flour", 0.53);
        table.put("plain flour", 0.53);
        table.put("self raising flour", 0.53);
        table.put("whole wheat flour", 0.51);
        table.put("cornflour", 0.54);
        table.put("cornstarch", 0.54);
        table.put("cocoa powder", 0.5);
        table.put("garlic powder", 0.6);
        table.put("baking powder", 0.85);
        table.put("baking soda", 1.1);
        table.put("rice", 0.85);
        table.put("oats", 0.4);
        table.put("rolled oats", 0.4);
        table.put("breadcrumbs", 0.35);
        table.put("panko breadcrumbs", 0.25);
        table.put("grated parmesan", 0.4);
        table.put("grated cheese", 0.4);
        table.put("ketchup", 1.1);
        table.put("tomato paste", 1.1);
        table.put("jam", 1.35);
        return Collections.unmodifiableMap(table);
    }

}
