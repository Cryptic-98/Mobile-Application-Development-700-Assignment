package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.api.TheMealDbIngredient;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.models.Ingredient;
import com.example.smartpantrymanager.models.IngredientAlias;
import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;













public final class TheMealDbMatcher
{

    public static final int MINIMUM_MATCHED_INGREDIENTS = 4;

    private final PantryDataSource pantryDataSource;

    public TheMealDbMatcher(PantryRepository pantryRepository)
    {
        if (pantryRepository == null)
        {
            throw new IllegalArgumentException("pantryRepository must not be null.");
        }
        this.pantryDataSource = new PantryDataSource()
        {
            @Override
            public List<PantryItem> getAllPantryItems()
            {
                return pantryRepository.getAllPantryItems();
            }

            @Override
            public List<MeasurementUnit> getAllUnits()
            {
                return pantryRepository.getAllUnits();
            }

            @Override
            public List<Ingredient> getAllIngredients()
            {
                return pantryRepository.getAllIngredients();
            }

            @Override
            public List<IngredientAlias> getAllAliases()
            {
                return pantryRepository.getAllAliases();
            }
        };
    }

    TheMealDbMatcher(PantryDataSource pantryDataSource)
    {
        if (pantryDataSource == null)
        {
            throw new IllegalArgumentException("pantryDataSource must not be null.");
        }
        this.pantryDataSource = pantryDataSource;
    }






    public List<TheMealDbMatch> getRecommendations(List<TheMealDbMeal> meals)
    {
        if (meals == null)
        {
            throw new IllegalArgumentException("meals must not be null.");
        }
        EvaluationContext context = buildContext();
        List<TheMealDbMatch> results = new ArrayList<>();
        for (TheMealDbMeal meal : meals)
        {
            TheMealDbMatch result = evaluateMeal(meal, context);
            if (result.isRecommended())
            {
                results.add(result);
            }
        }
        results.sort(Comparator
                .comparingInt(TheMealDbMatch::getMatchedRequiredIngredientCount).reversed()
                .thenComparing(TheMealDbMatch::getMatchProportion, Comparator.reverseOrder())
                .thenComparing(result -> result.getMeal().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(result -> result.getMeal().getId()));
        List<TheMealDbMatch> uniqueResults = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (TheMealDbMatch result : results)
        {
            String normalizedName = result.getMeal().getName().toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]", "");
            if (names.add(normalizedName))
            {
                uniqueResults.add(result);
            }
        }
        return uniqueResults;
    }







    public TheMealDbMatch evaluateMeal(TheMealDbMeal meal)
    {
        if (meal == null)
        {
            throw new IllegalArgumentException("meal must not be null.");
        }
        return evaluateMeal(meal, buildContext());
    }

    private EvaluationContext buildContext()
    {
        List<PantryItem> pantryItems = pantryDataSource.getAllPantryItems();
        Map<Integer, PantryItem> pantryByIngredientId = new HashMap<>();
        Set<Integer> pantryIngredientIds = new HashSet<>();
        for (PantryItem item : pantryItems)
        {
            pantryByIngredientId.put(item.getIngredientId(), item);
            pantryIngredientIds.add(item.getIngredientId());
        }
        Map<String, MeasurementUnit> units = new HashMap<>();
        for (MeasurementUnit unit : pantryDataSource.getAllUnits())
        {
            units.put(unit.getUnitCode(), unit);
        }
        IngredientNameIndex nameIndex = IngredientNameIndex.build(pantryDataSource.getAllIngredients(),
                pantryDataSource.getAllAliases());
        return new EvaluationContext(pantryByIngredientId, pantryIngredientIds, units, nameIndex,
                LocalDate.now());
    }

    private TheMealDbMatch evaluateMeal(TheMealDbMeal meal, EvaluationContext context)
    {
        Map<String, IngredientGroup> groups = new LinkedHashMap<>();
        List<TheMealDbIngredient> ingredients = meal.getIngredients();
        for (int index = 0; index < ingredients.size(); index++)
        {
            TheMealDbIngredient ingredient = ingredients.get(index);
            Integer pantryIngredientId = context.nameIndex.resolveToPantryIngredient(
                    ingredient.getName(), context.pantryIngredientIds);
            String key = pantryIngredientId != null
                    ? "id:" + pantryIngredientId
                    : "name:" + IngredientNameIndex.canonicalTerm(ingredient.getName());
            IngredientGroup group = groups.get(key);
            if (group == null)
            {
                group = new IngredientGroup(ingredient.getName(), pantryIngredientId);
                groups.put(key, group);
            }
            group.indexes.add(index);
            MeasurementUnit requiredUnit = ingredient.getUnitCode() == null
                    ? null : context.units.get(ingredient.getUnitCode());
            if (ingredient.getQuantity() == null || requiredUnit == null)
            {
                group.quantityIsVerifiable = false;
                continue;
            }
            MeasurementFamily family = MeasurementFamily.forDimension(requiredUnit.getDimension());
            if (family == null)
            {
                group.quantityIsVerifiable = false;
                continue;
            }
            Double converted = null;
            if (group.requiredFamily == null)
            {
                converted = ingredient.getQuantity() * requiredUnit.getToBaseFactor();
            }
            else
            {
                converted = IngredientAvailabilityEvaluator.convertToFamily(ingredient.getQuantity(),
                        requiredUnit, group.name, group.requiredFamily);
            }
            if (converted == null)
            {
                group.quantityIsVerifiable = false;
                continue;
            }
            if (group.requiredFamily == null)
            {
                group.requiredFamily = family;
            }
            group.requiredBaseQuantity += converted;
        }

        Set<Integer> matchedIndexes = new HashSet<>();
        Map<Integer, IngredientAvailability> availabilityByIndex = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        int matchedCount = 0;
        for (IngredientGroup group : groups.values())
        {
            PantryItem stock = group.pantryIngredientId == null
                    ? null : context.pantryByIngredientId.get(group.pantryIngredientId);
            MeasurementUnit stockUnit = stock == null ? null : context.units.get(stock.getUnitCode());
            IngredientAvailability availability = IngredientAvailabilityEvaluator.evaluateAgainstFamily(
                    stock, stockUnit, group.requiredFamily,
                    group.quantityIsVerifiable ? group.requiredBaseQuantity : 0.0,
                    group.name, context.today);
            for (Integer index : group.indexes)
            {
                availabilityByIndex.put(index, availability);
            }
            if (availability.isSatisfied())
            {
                matchedCount++;
                matchedIndexes.addAll(group.indexes);
            }
            else
            {
                missing.add(group.name);
            }
        }
        return new TheMealDbMatch(meal, matchedCount, groups.size(), missing, matchedIndexes,
                availabilityByIndex);
    }

    private static final class EvaluationContext
    {
        private final Map<Integer, PantryItem> pantryByIngredientId;
        private final Set<Integer> pantryIngredientIds;
        private final Map<String, MeasurementUnit> units;
        private final IngredientNameIndex nameIndex;
        private final LocalDate today;

        private EvaluationContext(Map<Integer, PantryItem> pantryByIngredientId,
                                  Set<Integer> pantryIngredientIds,
                                  Map<String, MeasurementUnit> units,
                                  IngredientNameIndex nameIndex, LocalDate today)
        {
            this.pantryByIngredientId = pantryByIngredientId;
            this.pantryIngredientIds = pantryIngredientIds;
            this.units = units;
            this.nameIndex = nameIndex;
            this.today = today;
        }
    }

    private static final class IngredientGroup
    {
        private final String name;
        private final Integer pantryIngredientId;
        private final List<Integer> indexes = new ArrayList<>();
        private MeasurementFamily requiredFamily;
        private double requiredBaseQuantity;
        private boolean quantityIsVerifiable = true;

        private IngredientGroup(String name, Integer pantryIngredientId)
        {
            this.name = name;
            this.pantryIngredientId = pantryIngredientId;
        }
    }

    interface PantryDataSource
    {
        List<PantryItem> getAllPantryItems();

        List<MeasurementUnit> getAllUnits();

        List<Ingredient> getAllIngredients();

        List<IngredientAlias> getAllAliases();
    }




    public List<String> getKnownIngredientNames()
    {
        List<String> names = new ArrayList<>();
        for (PantryItem item : pantryDataSource.getAllPantryItems())
        {
            if (item.getIngredientName() != null && !names.contains(item.getIngredientName()))
            {
                names.add(item.getIngredientName());
            }
        }
        return Collections.unmodifiableList(names);
    }
}
