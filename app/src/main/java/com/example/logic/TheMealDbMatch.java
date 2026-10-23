package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.api.TheMealDbMeal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;








public final class TheMealDbMatch
{
    private final TheMealDbMeal meal;
    private final int matchedRequiredIngredientCount;
    private final int totalRequiredIngredientCount;
    private final List<String> missingRequiredIngredients;
    private final Set<Integer> matchedIngredientIndexes;
    private final Map<Integer, IngredientAvailability> availabilityByIndex;

    public TheMealDbMatch(TheMealDbMeal meal, int matchedRequiredIngredientCount,
                          int totalRequiredIngredientCount, List<String> missingRequiredIngredients,
                          Set<Integer> matchedIngredientIndexes)
    {
        this(meal, matchedRequiredIngredientCount, totalRequiredIngredientCount,
                missingRequiredIngredients, matchedIngredientIndexes, Collections.emptyMap());
    }

    public TheMealDbMatch(TheMealDbMeal meal, int matchedRequiredIngredientCount,
                          int totalRequiredIngredientCount, List<String> missingRequiredIngredients,
                          Set<Integer> matchedIngredientIndexes,
                          Map<Integer, IngredientAvailability> availabilityByIndex)
    {
        if (meal == null || missingRequiredIngredients == null || matchedIngredientIndexes == null
                || availabilityByIndex == null
                || matchedRequiredIngredientCount < 0
                || totalRequiredIngredientCount < matchedRequiredIngredientCount)
        {
            throw new IllegalArgumentException("Invalid TheMealDB match result.");
        }
        this.meal = meal;
        this.matchedRequiredIngredientCount = matchedRequiredIngredientCount;
        this.totalRequiredIngredientCount = totalRequiredIngredientCount;
        this.missingRequiredIngredients = Collections.unmodifiableList(
                new ArrayList<>(missingRequiredIngredients));
        this.matchedIngredientIndexes = Collections.unmodifiableSet(
                new HashSet<>(matchedIngredientIndexes));
        this.availabilityByIndex = Collections.unmodifiableMap(
                new HashMap<>(availabilityByIndex));
    }

    public TheMealDbMeal getMeal()
    {
        return meal;
    }

    public int getMatchedRequiredIngredientCount()
    {
        return matchedRequiredIngredientCount;
    }

    public int getTotalRequiredIngredientCount()
    {
        return totalRequiredIngredientCount;
    }

    public int getMissingRequiredIngredientCount()
    {
        return totalRequiredIngredientCount - matchedRequiredIngredientCount;
    }

    public List<String> getMissingRequiredIngredients()
    {
        return missingRequiredIngredients;
    }





    public boolean isIngredientMatched(int index)
    {
        return availabilityOf(index).isSatisfied();
    }





    public IngredientAvailability getIngredientAvailability(int index)
    {
        return availabilityOf(index);
    }




    public boolean isRecommended()
    {
        return matchedRequiredIngredientCount >= TheMealDbMatcher.MINIMUM_MATCHED_INGREDIENTS;
    }

    public double getMatchProportion()
    {
        return totalRequiredIngredientCount == 0 ? 0.0
                : (double) matchedRequiredIngredientCount / totalRequiredIngredientCount;
    }

    private IngredientAvailability availabilityOf(int index)
    {
        IngredientAvailability availability = availabilityByIndex.get(index);
        return availability == null ? IngredientAvailability.MISSING : availability;
    }
}
