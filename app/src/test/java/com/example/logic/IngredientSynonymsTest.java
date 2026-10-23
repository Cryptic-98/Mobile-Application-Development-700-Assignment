package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;




public class IngredientSynonymsTest
{
    @Test
    public void namesOfTheSameItemExpandToOneAnother()
    {
        List<String> expanded = IngredientSynonyms.expand("Chicken Breast");

        assertTrue(expanded.contains("chicken"));
        assertTrue(expanded.contains("chicken breast"));
        assertTrue(expanded.contains("chicken breasts"));
    }

    @Test
    public void preparationWordsAreRemoved()
    {
        List<String> expanded = IngredientSynonyms.expand("Chopped Tomatoes");

        assertTrue(expanded.contains("tomatoes"));
        assertTrue(expanded.contains("tomato"));
    }

    @Test
    public void everyNameOfAGroupSharesOneCanonicalTerm()
    {
        assertEquals("tomato", IngredientSynonyms.canonicalTerm("Chopped Tomatoes"));
        assertEquals("tomato", IngredientSynonyms.canonicalTerm("Roma Tomatoes"));
        assertEquals("chicken", IngredientSynonyms.canonicalTerm("Chicken Wings"));
        assertEquals("rice", IngredientSynonyms.canonicalTerm("White Rice"));
    }

    @Test
    public void namesOfDifferentItemsAreKeptApart()
    {
        assertFalse(IngredientSynonyms.expand("Olive Oil").contains("vegetable oil"));
        assertFalse(IngredientSynonyms.expand("Vegetable Oil").contains("olive oil"));
    }

    @Test
    public void unknownNamesExpandToThemselves()
    {
        List<String> expanded = IngredientSynonyms.expand("Saffron");

        assertTrue(expanded.contains("saffron"));
        assertEquals("saffron", IngredientSynonyms.canonicalTerm("Saffron"));
    }

    @Test
    public void synonymsAreReportedForKnownNamesOnly()
    {
        assertTrue(IngredientSynonyms.hasSynonyms("Butter"));
        assertFalse(IngredientSynonyms.hasSynonyms("Saffron"));
    }
}
