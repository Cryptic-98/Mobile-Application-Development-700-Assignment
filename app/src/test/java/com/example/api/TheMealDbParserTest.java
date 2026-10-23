package com.example.smartpantrymanager.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class TheMealDbParserTest
{
    @Test
    public void parsesRecipeFieldsAndKeepsRawIngredientMeasures() throws Exception
    {
        Map<String, String> meal = new HashMap<>();
        meal.put("idMeal", "52772");
        meal.put("strMeal", "Pancakes");
        meal.put("strCategory", "Dessert");
        meal.put("strArea", "British");
        meal.put("strMealThumb", "https://www.themealdb.com/images/media/meals/example.jpg");
        meal.put("strInstructions", "1. Mix the ingredients.\n\nCook until golden.");
        meal.put("strIngredient1", "Flour");
        meal.put("strMeasure1", "1 1/2 cups");
        meal.put("strIngredient2", "Eggs");
        meal.put("strMeasure2", "2 large");
        meal.put("strIngredient3", "Sugar");
        meal.put("strMeasure3", "to taste");
        meal.put("strIngredient4", "");
        meal.put("strMeasure4", "1 tsp");

        TheMealDbMeal result = TheMealDbParser.parseMealFields(meal);

        assertEquals("52772", result.getId());
        assertEquals("Pancakes", result.getName());
        assertEquals(3, result.getIngredients().size());
        assertEquals(1.5, result.getIngredients().get(0).getQuantity(), 0.0001);
        assertEquals("cup", result.getIngredients().get(0).getUnitCode());
        assertEquals("2 large", result.getIngredients().get(1).getMeasure());
        assertEquals(2.0, result.getIngredients().get(1).getQuantity(), 0.0001);
        assertEquals("piece", result.getIngredients().get(1).getUnitCode());
        assertNull(result.getIngredients().get(2).getQuantity());
        assertEquals(2, result.getInstructions().size());
        assertEquals("Mix the ingredients.", result.getInstructions().get(0));
    }

    @Test
    public void normalizesKnownUnitsAndCountsItemsWithoutAUnit()
    {
        assertEquals("g", TheMealDbParser.parseMeasure("200 grams").unitCode);
        assertEquals("tbsp", TheMealDbParser.parseMeasure("1 tbls").unitCode);
        assertEquals(0.5, TheMealDbParser.parseMeasure("½ tsp").quantity, 0.0001);
        assertEquals(1.5, TheMealDbParser.parseMeasure("1½ cups").quantity, 0.0001);
        assertNull(TheMealDbParser.parseMeasure("to serve").quantity);
        assertEquals("piece", TheMealDbParser.parseMeasure("2 large").unitCode);
        assertEquals(2.0, TheMealDbParser.parseMeasure("2 large").quantity, 0.0001);
        assertEquals("piece", TheMealDbParser.parseMeasure("1").unitCode);
    }

    @Test
    public void rejectsIncompleteMealAndUntrustedImageUrls() throws Exception
    {
        Map<String, String> missingName = new HashMap<>();
        missingName.put("idMeal", "123");
        boolean rejected = false;
        try
        {
            TheMealDbParser.parseMealFields(missingName);
        }
        catch (org.json.JSONException expected)
        {
            rejected = true;
        }
        assertTrue(rejected);

        Map<String, String> meal = new HashMap<>();
        meal.put("idMeal", "123");
        meal.put("strMeal", "Meal");
        meal.put("strMealThumb", "http://example.com/image.jpg");

        assertNull(TheMealDbParser.parseMealFields(meal).getImageUrl());
        assertTrue(TheMealDbParser.parseMealFields(meal).getIngredients().isEmpty());
    }
}
