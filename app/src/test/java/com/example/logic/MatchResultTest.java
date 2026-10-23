package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.models.Recipe;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

public class MatchResultTest
{
    @Test
    public void reportsPartialAndFullMatches()
    {
        Recipe recipe = new Recipe("Soup");
        MatchResult partial = new MatchResult(recipe, 1, 2, Collections.singletonList("salt"), 0.5, null);
        MatchResult full = new MatchResult(recipe, 2, 2, Collections.emptyList(), 1.0, null);

        assertFalse(partial.isFullyMatched());
        assertTrue(full.isFullyMatched());
        assertEquals(0.5, partial.getScore(), 0);
    }

    @Test
    public void zeroRequiredIngredientsScoreZero()
    {
        MatchResult result = new MatchResult(new Recipe("Tea"), 0, 0,
                Collections.emptyList(), 0.0, null);

        assertEquals(0.0, result.getScore(), 0);
        assertTrue(result.isFullyMatched());
    }

    @Test
    public void recommendsMealsWithFourMatchesAndKeepsIngredientAvailability()
    {
        Recipe recipe = new Recipe("Pantry meal");
        MatchResult partial = new MatchResult(recipe, 4, 6, Arrays.asList("salt", "pepper"),
                4.0 / 6.0, null, new HashSet<>(Arrays.asList(1, 2, 3, 4)));
        MatchResult threeOfSix = new MatchResult(recipe, 3, 6, Collections.singletonList("salt"),
                0.5, null, new HashSet<>(Arrays.asList(1, 2, 3)));
        MatchResult complete = new MatchResult(recipe, 3, 3, Collections.emptyList(), 1.0, null);

        assertTrue(partial.isRecommended());
        assertTrue(partial.isIngredientMatched(4));
        assertFalse(partial.isIngredientMatched(5));
        assertFalse(threeOfSix.isRecommended());
        assertTrue(complete.isRecommended());
    }
}
