package com.example.smartpantrymanager.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TheMealDbMeal
{
    private final String id;
    private final String name;
    private final String category;
    private final String area;
    private final String imageUrl;
    private final List<TheMealDbIngredient> ingredients;
    private final List<String> instructions;

    public TheMealDbMeal(String id, String name, String category, String area, String imageUrl,
                         List<TheMealDbIngredient> ingredients, List<String> instructions)
    {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty()
                || ingredients == null || instructions == null)
        {
            throw new IllegalArgumentException("TheMealDB meal data is incomplete.");
        }
        this.id = id;
        this.name = name;
        this.category = blankToNull(category);
        this.area = blankToNull(area);
        this.imageUrl = blankToNull(imageUrl);
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
        this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public String getArea()
    {
        return area;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public List<TheMealDbIngredient> getIngredients()
    {
        return ingredients;
    }

    public List<String> getInstructions()
    {
        return instructions;
    }

    private static String blankToNull(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
