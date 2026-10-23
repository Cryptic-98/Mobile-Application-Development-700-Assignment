package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class AllergyListCodecTest
{
    @Test
    public void encodesAndDecodesDistinctAllergies()
    {
        List<String> allergies = Arrays.asList("Peanuts", "Milk");

        assertEquals(allergies, AllergyListCodec.decode(AllergyListCodec.encode(allergies)));
    }

    @Test
    public void decodesExistingCommaSeparatedPreferences()
    {
        assertEquals(Arrays.asList("Peanuts", "Milk"),
                AllergyListCodec.decode("Peanuts, Milk"));
    }

    @Test
    public void preservesCommasInsideAllergyNames()
    {
        List<String> allergies = Arrays.asList("Mustard, yellow", "Peanuts");

        assertEquals(allergies, AllergyListCodec.decode(AllergyListCodec.encode(allergies)));
    }

    @Test
    public void addingAllergyTrimsAndRejectsDuplicatesRegardlessOfCase()
    {
        List<String> allergies = new ArrayList<>();

        assertTrue(AllergyListCodec.addIfUnique(allergies, " Peanuts "));
        assertFalse(AllergyListCodec.addIfUnique(allergies, "peanuts"));
        assertEquals(Arrays.asList("Peanuts"), allergies);
    }
}
