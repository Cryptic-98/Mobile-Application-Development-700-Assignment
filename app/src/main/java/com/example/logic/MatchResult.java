package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.models.Recipe;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class MatchResult
{
    private final Recipe recipe;
    private final int matchedRequiredIngredientCount;
    private final int totalRequiredIngredientCount;
    private final List<String> missingRequiredIngredients;
    private final double score;
    private final LocalDate earliestQualifyingExpiryDate;
    private final Set<Integer> matchedIngredientIds;
    private final Map<Integer, IngredientAvailability> availabilityByIngredientId;

    public MatchResult(Recipe recipe, int matchedRequiredIngredientCount, int totalRequiredIngredientCount,
                       List<String> missingRequiredIngredients, double score, LocalDate earliestQualifyingExpiryDate)
    {
        this(recipe, matchedRequiredIngredientCount, totalRequiredIngredientCount,
                missingRequiredIngredients, score, earliestQualifyingExpiryDate, Collections.emptySet(),
                Collections.emptyMap());
    }

    public MatchResult(Recipe recipe, int matchedRequiredIngredientCount, int totalRequiredIngredientCount,
                       List<String> missingRequiredIngredients, double score,
                       LocalDate earliestQualifyingExpiryDate, Set<Integer> matchedIngredientIds)
    {
        this(recipe, matchedRequiredIngredientCount, totalRequiredIngredientCount,
                missingRequiredIngredients, score, earliestQualifyingExpiryDate, matchedIngredientIds,
                Collections.emptyMap());
    }

    public MatchResult(Recipe recipe, int matchedRequiredIngredientCount, int totalRequiredIngredientCount,
                       List<String> missingRequiredIngredients, double score,
                       LocalDate earliestQualifyingExpiryDate, Set<Integer> matchedIngredientIds,
                       Map<Integer, IngredientAvailability> availabilityByIngredientId)
    {
        if (recipe == null || missingRequiredIngredients == null
                || matchedIngredientIds == null || availabilityByIngredientId == null
                || matchedRequiredIngredientCount < 0
                || totalRequiredIngredientCount < matchedRequiredIngredientCount
                || !Double.isFinite(score))
        {
            throw new IllegalArgumentException("Invalid recipe match result.");
        }
        this.recipe = recipe;
        this.matchedRequiredIngredientCount = matchedRequiredIngredientCount;
        this.totalRequiredIngredientCount = totalRequiredIngredientCount;
        this.missingRequiredIngredients = Collections.unmodifiableList(new ArrayList<>(missingRequiredIngredients));
        this.score = score;
        this.earliestQualifyingExpiryDate = earliestQualifyingExpiryDate;
        this.matchedIngredientIds = Collections.unmodifiableSet(new HashSet<>(matchedIngredientIds));
        this.availabilityByIngredientId = Collections.unmodifiableMap(
                new HashMap<>(availabilityByIngredientId));
    }

    public Recipe getRecipe()
    {
        return recipe;
    }

    public int getMatchedRequiredIngredientCount()
    {
        return matchedRequiredIngredientCount;
    }

    public int getTotalRequiredIngredientCount()
    {
        return totalRequiredIngredientCount;
    }

    public List<String> getMissingRequiredIngredients()
    {
        return missingRequiredIngredients;
    }

    public double getScore()
    {
        return score;
    }

    public LocalDate getEarliestQualifyingExpiryDate()
    {
        return earliestQualifyingExpiryDate;
    }

    public boolean isFullyMatched()
    {
        return matchedRequiredIngredientCount == totalRequiredIngredientCount;
    }





    public boolean isRecommended()
    {
        return isFullyMatched()
                || matchedRequiredIngredientCount >= TheMealDbMatcher.MINIMUM_MATCHED_INGREDIENTS;
    }

    public boolean isIngredientMatched(int ingredientId)
    {
        return matchedIngredientIds.contains(ingredientId);
    }





    public IngredientAvailability getIngredientAvailability(int ingredientId)
    {
        IngredientAvailability availability = availabilityByIngredientId.get(ingredientId);
        return availability == null ? IngredientAvailability.MISSING : availability;
    }
}
