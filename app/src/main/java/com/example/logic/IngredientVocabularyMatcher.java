package com.example.smartpantrymanager.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;






















public final class IngredientVocabularyMatcher
{
    private IngredientVocabularyMatcher()
    {
    }








    public static List<String> searchTerms(Map<String, String> vocabulary,
                                            Collection<String> pantryNames,
                                            int maxTermsPerIngredient)
    {
        List<String> terms = new ArrayList<>();
        if (pantryNames == null || maxTermsPerIngredient <= 0)
        {
            return Collections.unmodifiableList(terms);
        }
        Map<String, String> known = vocabulary == null ? Collections.emptyMap() : vocabulary;
        for (String pantryName : pantryNames)
        {
            if (pantryName == null || pantryName.trim().isEmpty())
            {
                continue;
            }
            String canonical = IngredientSynonyms.canonicalTerm(pantryName);
            int added = 0;
            String exact = known.get(canonical);
            if (exact != null)
            {
                addTerm(terms, exact);
                added++;
            }
            for (Map.Entry<String, String> candidate : endingWith(canonical, known).entrySet())
            {
                if (added >= maxTermsPerIngredient)
                {
                    break;
                }
                if (addTerm(terms, candidate.getValue()))
                {
                    added++;
                }
            }
            for (String candidate : IngredientSynonyms.expand(pantryName))
            {
                if (added >= maxTermsPerIngredient)
                {
                    break;
                }
                String exactCandidate = known.get(candidate);
                if (exactCandidate != null && addTerm(terms, exactCandidate))
                {
                    added++;
                }
            }
        }
        return Collections.unmodifiableList(terms);
    }






    public static List<String> searchTermsFor(Map<String, String> vocabulary, String pantryName)
    {
        return searchTerms(vocabulary, Collections.singletonList(pantryName), 3);
    }

    private static Map<String, String> endingWith(String canonical, Map<String, String> vocabulary)
    {
        Map<String, String> matches = new LinkedHashMap<>();
        if (canonical == null || canonical.isEmpty() || vocabulary == null)
        {
            return matches;
        }
        String suffix = " " + canonical;
        for (Map.Entry<String, String> entry : vocabulary.entrySet())
        {
            String name = entry.getKey();
            if (name.equals(canonical) || !name.endsWith(suffix))
            {
                continue;
            }
            matches.put(name, entry.getValue());
        }
        List<Map.Entry<String, String>> ordered = new ArrayList<>(matches.entrySet());
        ordered.sort(Comparator.comparingInt((Map.Entry<String, String> entry) -> entry.getKey().length())
                .thenComparing(Map.Entry::getKey));
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : ordered)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static boolean addTerm(List<String> terms, String term)
    {
        if (term == null || term.trim().isEmpty() || terms.contains(term))
        {
            return false;
        }
        terms.add(term);
        return true;
    }
}
