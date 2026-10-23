package com.example.smartpantrymanager.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;













public final class RecipeSectionPlanner
{
    private RecipeSectionPlanner()
    {
    }





    public static String normalizeName(String name)
    {
        if (name == null)
        {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }








    public static List<TheMealDbMatch> excludeStarterRecipes(List<TheMealDbMatch> matches,
                                                             Collection<String> starterNames)
    {
        if (matches == null || matches.isEmpty())
        {
            return Collections.emptyList();
        }
        Set<String> starterRecipeNames = new HashSet<>();
        if (starterNames != null)
        {
            for (String starterName : starterNames)
            {
                String normalized = normalizeName(starterName);
                if (!normalized.isEmpty())
                {
                    starterRecipeNames.add(normalized);
                }
            }
        }
        List<TheMealDbMatch> result = new ArrayList<>();
        for (TheMealDbMatch match : matches)
        {
            if (match == null || match.getMeal() == null)
            {
                continue;
            }
            if (starterRecipeNames.contains(normalizeName(match.getMeal().getName())))
            {
                continue;
            }
            result.add(match);
        }
        return Collections.unmodifiableList(result);
    }
}
