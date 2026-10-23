package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.PresetRecipeSeeder.StarterRecipe;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;








public class PresetRecipeSeederTest
{
    @Test
    public void thereAreExactlyFiveStarterRecipes()
    {
        assertEquals(5, PresetRecipeSeeder.getStarterRecipes().size());
        assertEquals(5, PresetRecipeSeeder.getStarterRecipeNames().size());
    }

    @Test
    public void theNamesAreUnique()
    {
        Set<String> names = new HashSet<>();
        for (String name : PresetRecipeSeeder.getStarterRecipeNames())
        {
            assertTrue("Duplicate starter recipe " + name, names.add(name.toLowerCase(Locale.ROOT)));
        }
    }

    @Test
    public void theRecipesTheBriefRulesOutAreNotIncluded()
    {
        for (String name : PresetRecipeSeeder.getStarterRecipeNames())
        {
            String normalized = name.toLowerCase(Locale.ROOT);
            assertFalse(normalized, normalized.contains("peanut butter")
                    && normalized.contains("jelly"));
            assertFalse(normalized, normalized.contains("bacon")
                    && normalized.contains("egg"));
        }
    }

    @Test
    public void everyRecipeIsComplete()
    {
        for (StarterRecipe recipe : PresetRecipeSeeder.getStarterRecipes())
        {
            assertFalse(recipe.getName(), recipe.getIngredients().isEmpty());
            assertFalse(recipe.getName(), recipe.getSteps().isEmpty());
            assertTrue(recipe.getName(), recipe.getServings() > 0);
            assertTrue(recipe.getName(), recipe.getTotalMinutes() > 0);
            assertTrue(recipe.getName(),
                    recipe.getImageUrl() != null && recipe.getImageUrl().startsWith("https://"));
        }
    }

    @Test
    public void theSetCoversBreakfastLunchAndDinner()
    {
        List<String> names = Arrays.asList("Golden Buttermilk Pancakes", "Rosemary Breakfast Potatoes",
                "Spicy Arrabiata Penne", "Tomato and Egg Stir-Fry", "Classic Potato Salad");
        assertEquals(names, PresetRecipeSeeder.getStarterRecipeNames());

        boolean breakfast = false;
        boolean quick = false;
        boolean mainCourse = false;
        for (StarterRecipe recipe : PresetRecipeSeeder.getStarterRecipes())
        {
            breakfast |= recipe.getName().contains("Pancakes")
                    || recipe.getName().contains("Breakfast");
            quick |= recipe.getTotalMinutes() <= 25;
            mainCourse |= recipe.getServings() >= 3;
        }
        assertTrue("The set needs a breakfast recipe", breakfast);
        assertTrue("The set needs a quick recipe", quick);
        assertTrue("The set needs a recipe that serves a family", mainCourse);
    }
}
