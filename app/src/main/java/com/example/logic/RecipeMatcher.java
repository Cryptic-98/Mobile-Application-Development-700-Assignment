package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.database.RecipeRepository;
import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;








public class RecipeMatcher
{
    private final RecipeRepository recipeRepository;
    private final PantryRepository pantryRepository;

    public RecipeMatcher(RecipeRepository recipeRepository, PantryRepository pantryRepository)
    {
        if (recipeRepository == null || pantryRepository == null)
        {
            throw new IllegalArgumentException("Repositories must not be null.");
        }
        this.recipeRepository = recipeRepository;
        this.pantryRepository = pantryRepository;
    }

    public RecipeRepository getRecipeRepository()
    {
        return recipeRepository;
    }

    public PantryRepository getPantryRepository()
    {
        return pantryRepository;
    }

    public List<MatchResult> evaluateMatches()
    {
        LocalDate today = LocalDate.now();
        Map<Integer, PantryItem> pantryByIngredientId = new HashMap<>();
        Set<Integer> pantryIngredientIds = new HashSet<>();
        for (PantryItem item : pantryRepository.getAllPantryItems())
        {
            pantryByIngredientId.put(item.getIngredientId(), item);
            pantryIngredientIds.add(item.getIngredientId());
        }
        Map<String, MeasurementUnit> units = new HashMap<>();
        for (MeasurementUnit unit : pantryRepository.getAllUnits())
        {
            units.put(unit.getUnitCode(), unit);
        }
        IngredientNameIndex nameIndex = IngredientNameIndex.build(pantryRepository.getAllIngredients(),
                pantryRepository.getAllAliases());

        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipeRepository.getAllActiveRecipes())
        {
            int matched = 0;
            int total = 0;
            LocalDate earliestExpiry = null;
            List<String> missing = new ArrayList<>();
            Set<Integer> matchedIngredientIds = new HashSet<>();
            Map<Integer, IngredientAvailability> availabilityByIngredientId = new HashMap<>();
            for (RecipeIngredient requirement : recipeRepository.getRecipeIngredients(recipe.getId()))
            {
                PantryItem stock = resolveStock(requirement, pantryByIngredientId, pantryIngredientIds,
                        nameIndex);
                MeasurementUnit pantryUnit = stock == null ? null : units.get(stock.getUnitCode());
                MeasurementUnit requiredUnit = units.get(requirement.getUnitCode());
                IngredientAvailability availability = IngredientAvailabilityEvaluator.evaluate(stock,
                        pantryUnit, requiredUnit, requirement.getRequiredQuantity(),
                        requirement.getIngredientName(), today);
                availabilityByIngredientId.put(requirement.getIngredientId(), availability);
                if (availability.isSatisfied())
                {
                    matchedIngredientIds.add(requirement.getIngredientId());
                    if (!requirement.isOptional())
                    {
                        matched++;
                    }
                    if (stock != null && stock.getExpiryDate() != null
                            && (earliestExpiry == null || stock.getExpiryDate().isBefore(earliestExpiry)))
                    {
                        earliestExpiry = stock.getExpiryDate();
                    }
                }
                else if (!requirement.isOptional())
                {
                    String name = requirement.getIngredientName();
                    missing.add(name == null ? "Ingredient " + requirement.getIngredientId() : name);
                }
                if (!requirement.isOptional())
                {
                    total++;
                }
            }
            double score = total == 0 ? 0.0 : (double) matched / total;
            results.add(new MatchResult(recipe, matched, total, missing, score, earliestExpiry,
                    matchedIngredientIds, availabilityByIngredientId));
        }
        results.sort(matchComparator());
        return results;
    }

    public List<MatchResult> getSuggestedRecipes()
    {
        List<MatchResult> suggestions = new ArrayList<>();
        for (MatchResult result : evaluateMatches())
        {
            if (result.isRecommended())
            {
                suggestions.add(result);
            }
        }
        return suggestions;
    }

    private static PantryItem resolveStock(RecipeIngredient requirement,
                                            Map<Integer, PantryItem> pantryByIngredientId,
                                            Set<Integer> pantryIngredientIds,
                                            IngredientNameIndex nameIndex)
    {
        Integer pantryIngredientId = null;
        if (requirement.getIngredientName() != null)
        {
            pantryIngredientId = nameIndex.resolveToPantryIngredient(requirement.getIngredientName(),
                    pantryIngredientIds);
        }
        if (pantryIngredientId == null)
        {
            pantryIngredientId = requirement.getIngredientId();
        }
        return pantryByIngredientId.get(pantryIngredientId);
    }

    private static Comparator<MatchResult> matchComparator()
    {
        return Comparator.comparing(MatchResult::isFullyMatched).reversed()
                .thenComparing(MatchResult::getScore, Comparator.reverseOrder())
                .thenComparing(MatchResult::getEarliestQualifyingExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(result -> result.getRecipe().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(result -> result.getRecipe().getId(),
                        Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
