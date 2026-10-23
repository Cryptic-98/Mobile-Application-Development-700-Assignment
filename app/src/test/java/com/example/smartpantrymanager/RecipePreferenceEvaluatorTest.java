package com.example.smartpantrymanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RecipePreferenceEvaluatorTest
{
    @Test
    public void allergyTermsMatchWholeIngredientWordsAndPluralForms()
    {
        assertTrue(RecipePreferenceEvaluator.ingredientMatchesTerm("Chopped tomatoes", "tomato"));
        assertTrue(RecipePreferenceEvaluator.ingredientMatchesTerm("Eggs", "egg"));
        assertTrue(RecipePreferenceEvaluator.ingredientMatchesTerm("Peanut butter", "peanuts"));
        assertFalse(RecipePreferenceEvaluator.ingredientMatchesTerm("Coconut", "nut"));
        assertFalse(RecipePreferenceEvaluator.ingredientMatchesTerm("Milk", "peanut"));
    }
}
