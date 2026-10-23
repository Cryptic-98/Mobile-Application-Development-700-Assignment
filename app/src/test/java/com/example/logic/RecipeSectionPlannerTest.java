package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.api.TheMealDbIngredient;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;




public class RecipeSectionPlannerTest
{
    @Test
    public void aStarterRecipeIsRemovedFromThePantryMatches()
    {
        List<TheMealDbMatch> matches = Arrays.asList(match("Spicy Arrabiata Penne"),
                match("Chicken Congee"));

        List<TheMealDbMatch> remaining = RecipeSectionPlanner.excludeStarterRecipes(matches,
                Arrays.asList("Spicy Arrabiata Penne", "Classic Potato Salad"));

        assertEquals(1, remaining.size());
        assertEquals("Chicken Congee", remaining.get(0).getMeal().getName());
    }

    @Test
    public void namesAreComparedWithoutPunctuationAndCase()
    {
        List<TheMealDbMatch> matches = Collections.singletonList(match("SPICY ARRABIATA PENNE!"));

        assertTrue(RecipeSectionPlanner.excludeStarterRecipes(matches,
                Collections.singletonList("Spicy Arrabiata Penne")).isEmpty());
    }

    @Test
    public void emptyInputIsHandled()
    {
        assertTrue(RecipeSectionPlanner.excludeStarterRecipes(null, null).isEmpty());
        assertTrue(RecipeSectionPlanner.excludeStarterRecipes(Collections.emptyList(), null)
                .isEmpty());
        assertEquals(1, RecipeSectionPlanner.excludeStarterRecipes(
                Collections.singletonList(match("Chicken Congee")), null).size());
    }

    @Test
    public void namesAreReducedToComparableLettersAndDigits()
    {
        assertEquals("spicyarrabiatapenne", RecipeSectionPlanner.normalizeName("Spicy Arrabiata Penne"));
        assertEquals("", RecipeSectionPlanner.normalizeName(null));
    }

    private static TheMealDbMatch match(String name)
    {
        TheMealDbMeal meal = new TheMealDbMeal("1", name, null, null, null,
                Collections.singletonList(new TheMealDbIngredient("Salt", "1 tsp", 1.0, "tsp")),
                Collections.emptyList());
        return new TheMealDbMatch(meal, 4, 5, Collections.emptyList(), Collections.emptySet());
    }
}
