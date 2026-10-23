package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.api.TheMealDbIngredient;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import com.example.smartpantrymanager.models.Ingredient;
import com.example.smartpantrymanager.models.IngredientAlias;
import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;





public class TheMealDbMatcherTest
{
    @Test
    public void recommendsAtFourSufficientDistinctUnexpiredIngredients()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");

        TheMealDbMatch match = new TheMealDbMatcher(pantry).getRecommendations(
                Collections.singletonList(meal("Flour", "Milk", "Olive oil", "Salt", "Sugar"))).get(0);

        assertEquals(4, match.getMatchedRequiredIngredientCount());
        assertEquals(5, match.getTotalRequiredIngredientCount());
        assertEquals(1, match.getMissingRequiredIngredientCount());
        assertEquals(Collections.singletonList("Sugar"), match.getMissingRequiredIngredients());
        assertTrue(match.isIngredientMatched(3));
        assertFalse(match.isIngredientMatched(4));
        assertTrue(match.isRecommended());
    }

    @Test
    public void rejectsThreeMatches()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");

        assertTrue(new TheMealDbMatcher(pantry)
                .getRecommendations(Collections.singletonList(meal("Flour", "Milk", "Olive oil", "Salt")))
                .isEmpty());
    }

    @Test
    public void countsAnIngredientOnlyOnceEvenWhenTheRecipeListsItTwice()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");

        TheMealDbMatch match = new TheMealDbMatcher(pantry).evaluateMeal(mealOf(
                ingredient("Flour", "50 g"), ingredient("Flour", "50 g"),
                ingredient("Milk", "200 ml"), ingredient("Olive oil", "1 tbsp"),
                ingredient("Salt", "1 tsp")));

        assertEquals(4, match.getMatchedRequiredIngredientCount());
        assertEquals(4, match.getTotalRequiredIngredientCount());
        assertTrue(match.isRecommended());
    }

    @Test
    public void expiredStockDoesNotCount()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp", LocalDate.now().minusDays(1));

        TheMealDbMatch match = new TheMealDbMatcher(pantry)
                .evaluateMeal(meal("Flour", "Milk", "Olive oil", "Salt"));

        assertEquals(3, match.getMatchedRequiredIngredientCount());
        assertEquals(IngredientAvailability.EXPIRED, match.getIngredientAvailability(3));
        assertFalse(match.isRecommended());
    }

    @Test
    public void insufficientQuantityDoesNotCount()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 50, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");

        TheMealDbMatch match = new TheMealDbMatcher(pantry)
                .evaluateMeal(meal("Flour", "Milk", "Olive oil", "Salt"));

        assertEquals(3, match.getMatchedRequiredIngredientCount());
        assertEquals(IngredientAvailability.INSUFFICIENT_QUANTITY, match.getIngredientAvailability(0));
        assertFalse(match.isRecommended());
    }

    @Test
    public void anAmountThatCannotBeMeasuredDoesNotCount()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");

        TheMealDbMatch match = new TheMealDbMatcher(pantry).evaluateMeal(mealOf(
                ingredient("Flour", "100 g"), ingredient("Milk", "200 ml"),
                ingredient("Olive oil", "1 tbsp"), ingredient("Salt", "to taste")));

        assertEquals(3, match.getMatchedRequiredIngredientCount());
        assertEquals(IngredientAvailability.UNVERIFIABLE, match.getIngredientAvailability(3));
        assertFalse(match.isRecommended());
    }

    @Test
    public void aRecipeThatMeasuresTheSameIngredientTwiceInDifferentUnitsIsStillEvaluated()
    {
        Pantry pantry = new Pantry();
        pantry.add("Chicken", 500, "g");
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");

        TheMealDbMatch match = new TheMealDbMatcher(pantry).evaluateMeal(mealOf(
                ingredient("Chicken Breast", "1 piece"), ingredient("Chicken", "500 g"),
                ingredient("Flour", "100 g"), ingredient("Milk", "200 ml"),
                ingredient("Olive oil", "1 tbsp")));

        assertEquals(4, match.getTotalRequiredIngredientCount());
        assertEquals(3, match.getMatchedRequiredIngredientCount());
        assertEquals(IngredientAvailability.UNVERIFIABLE, match.getIngredientAvailability(0));
    }

    @Test
    public void resolvesAliasesAndSortsByCountThenProportionThenName()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");
        pantry.add("Scallions", 2, "piece");
        pantry.alias("Scallions", "Spring onion");

        List<TheMealDbMatch> matches = new TheMealDbMatcher(pantry).getRecommendations(Arrays.asList(
                meal("Flour", "Milk", "Olive oil", "Salt", "Sugar"),
                meal("Flour", "Milk", "Olive oil", "Salt", "Spring onion"),
                meal("Flour", "Milk", "Olive oil", "Salt", "Spring onion", "Sugar")));

        assertEquals(3, matches.size());
        assertEquals(5, matches.get(0).getMatchedRequiredIngredientCount());
        assertEquals(5, matches.get(0).getTotalRequiredIngredientCount());
        assertEquals("Recipe Flour / Milk / Olive oil / Salt / Spring onion",
                matches.get(0).getMeal().getName());
    }

    @Test
    public void removesRecipesThatShareAName()
    {
        Pantry pantry = new Pantry();
        pantry.add("Flour", 100, "g");
        pantry.add("Milk", 200, "ml");
        pantry.add("Olive oil", 1, "tbsp");
        pantry.add("Salt", 1, "tsp");

        TheMealDbMeal first = meal("Flour", "Milk", "Olive oil", "Salt");
        TheMealDbMeal second = new TheMealDbMeal("2", "RECIPE FLOUR / MILK / OLIVE OIL / SALT", null,
                null, null, first.getIngredients(), Collections.emptyList());

        List<TheMealDbMatch> matches = new TheMealDbMatcher(pantry)
                .getRecommendations(Arrays.asList(first, second));

        assertEquals(1, matches.size());
    }

    private static TheMealDbMeal meal(String... names)
    {
        List<TheMealDbIngredient> ingredients = new ArrayList<>();
        for (String name : names)
        {
            ingredients.add(ingredient(name, defaultMeasure(name)));
        }
        return mealOf(ingredients.toArray(new TheMealDbIngredient[0]));
    }

    private static TheMealDbMeal mealOf(TheMealDbIngredient... ingredients)
    {
        List<TheMealDbIngredient> list = Arrays.asList(ingredients);
        StringBuilder name = new StringBuilder("Recipe");
        for (TheMealDbIngredient ingredient : list)
        {
            name.append(' ').append(ingredient.getName());
            if (ingredient != list.get(list.size() - 1))
            {
                name.append(" /");
            }
        }
        return new TheMealDbMeal("1", name.toString(), null, null, null, list,
                Collections.emptyList());
    }

    private static TheMealDbIngredient ingredient(String name, String measure)
    {
        Double quantity = null;
        String unit = null;
        int space = measure.lastIndexOf(' ');
        if (space > 0)
        {
            try
            {
                quantity = Double.parseDouble(measure.substring(0, space));
                unit = measure.substring(space + 1);
            }
            catch (NumberFormatException ignored)
            {
                quantity = null;
                unit = null;
            }
        }
        return new TheMealDbIngredient(name, measure, quantity, unit);
    }

    private static String defaultMeasure(String name)
    {
        if ("Flour".equals(name))
        {
            return "100 g";
        }
        if ("Milk".equals(name))
        {
            return "200 ml";
        }
        if ("Olive oil".equals(name))
        {
            return "1 tbsp";
        }
        if ("Salt".equals(name))
        {
            return "1 tsp";
        }
        if ("Sugar".equals(name))
        {
            return "2 tsp";
        }
        return "1 piece";
    }

    private static final class Pantry implements TheMealDbMatcher.PantryDataSource
    {
        private final List<PantryItem> items = new ArrayList<>();
        private final Map<String, Ingredient> ingredients = new HashMap<>();
        private final List<IngredientAlias> aliases = new ArrayList<>();
        private final List<MeasurementUnit> units = Arrays.asList(
                new MeasurementUnit("g", "grams", "mass", 1),
                new MeasurementUnit("kg", "kilograms", "mass", 1000),
                new MeasurementUnit("ml", "millilitres", "volume", 1),
                new MeasurementUnit("l", "litres", "volume", 1000),
                new MeasurementUnit("tsp", "teaspoons", "volume", 5),
                new MeasurementUnit("tbsp", "tablespoons", "volume", 15),
                new MeasurementUnit("cup", "cups", "volume", 240),
                new MeasurementUnit("piece", "pieces", "piece", 1),
                new MeasurementUnit("slice", "slices", "slice", 1));
        private int nextId = 1;

        private void add(String name, double quantity, String unit)
        {
            add(name, quantity, unit, null);
        }

        private void add(String name, double quantity, String unit, LocalDate expiry)
        {
            Ingredient ingredient = ingredients.get(normalize(name));
            if (ingredient == null)
            {
                ingredient = new Ingredient(nextId, name, normalize(name), null);
                ingredients.put(normalize(name), ingredient);
                nextId++;
            }
            PantryItem item = new PantryItem(ingredient.getId(), quantity, unit, expiry);
            item.setIngredientName(name);
            items.add(item);
        }

        private void alias(String ingredientName, String alias)
        {
            aliases.add(new IngredientAlias(ingredients.get(normalize(ingredientName)).getId(),
                    normalize(alias)));
        }

        @Override
        public List<PantryItem> getAllPantryItems()
        {
            return items;
        }

        @Override
        public List<MeasurementUnit> getAllUnits()
        {
            return units;
        }

        @Override
        public List<Ingredient> getAllIngredients()
        {
            return new ArrayList<>(ingredients.values());
        }

        @Override
        public List<IngredientAlias> getAllAliases()
        {
            return aliases;
        }

        private static String normalize(String value)
        {
            return value.trim().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
