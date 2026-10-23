package com.example.smartpantrymanager;

import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.models.RecipeIngredient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RecipePreferenceEvaluator
{
    private final SettingsRepository settingsRepository;

    public RecipePreferenceEvaluator(SettingsRepository settingsRepository)
    {
        if (settingsRepository == null)
        {
            throw new IllegalArgumentException("settingsRepository must not be null.");
        }
        this.settingsRepository = settingsRepository;
    }

    public boolean shouldHideRecipe(List<RecipeIngredient> ingredients)
    {
        return "hide".equals(settingsRepository.get("allergy_mode", "hide"))
                && hasAllergyWarning(ingredients);
    }

    public boolean hasAllergyWarning(List<RecipeIngredient> ingredients)
    {
        return hasMatch(namesFrom(ingredients),
                AllergyListCodec.decode(settingsRepository.get("allergies", "")));
    }

    public boolean shouldHideRecipeByIngredientNames(List<String> ingredientNames)
    {
        return "hide".equals(settingsRepository.get("allergy_mode", "hide"))
                && hasAllergyWarningByIngredientNames(ingredientNames);
    }

    public boolean hasAllergyWarningByIngredientNames(List<String> ingredientNames)
    {
        return hasMatch(ingredientNames,
                AllergyListCodec.decode(settingsRepository.get("allergies", "")));
    }

    public boolean hasDietWarning(List<RecipeIngredient> ingredients)
    {
        return hasDietWarningByIngredientNames(namesFrom(ingredients));
    }

    public boolean hasDietWarningByIngredientNames(List<String> ingredientNames)
    {
        String diet = settingsRepository.get("diet", "none");
        List<String> terms;
        switch (diet)
        {
            case "vegetarian":
                terms = Arrays.asList("beef", "pork", "chicken", "turkey", "duck", "bacon",
                        "ham", "sausage", "fish", "salmon", "tuna", "anchovy", "gelatin");
                break;
            case "vegan":
                terms = Arrays.asList("beef", "pork", "chicken", "turkey", "duck", "bacon",
                        "ham", "sausage", "fish", "salmon", "tuna", "anchovy", "gelatin",
                        "egg", "milk", "butter", "cheese", "cream", "yogurt", "honey");
                break;
            case "pescatarian":
                terms = Arrays.asList("beef", "pork", "chicken", "turkey", "duck", "bacon",
                        "ham", "sausage");
                break;
            case "gluten_free":
                terms = Arrays.asList("wheat", "flour", "pasta", "bread", "barley", "rye", "couscous");
                break;
            case "dairy_free":
                terms = Arrays.asList("milk", "butter", "cheese", "cream", "yogurt", "parmesan");
                break;
            case "keto":
                terms = Arrays.asList("sugar", "flour", "rice", "pasta", "potato", "bread", "honey");
                break;
            case "custom":
                terms = parseTerms(settingsRepository.get("custom_diet_terms", ""));
                break;
            default:
                terms = Collections.emptyList();
                break;
        }
        return hasMatch(ingredientNames, terms);
    }

    private static boolean hasMatch(List<String> ingredientNames, List<String> terms)
    {
        if (ingredientNames == null || terms.isEmpty())
        {
            return false;
        }
        for (String ingredientName : ingredientNames)
        {
            if (ingredientName == null)
            {
                continue;
            }
            for (String term : terms)
            {
                if (ingredientMatchesTerm(ingredientName, term))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> namesFrom(List<RecipeIngredient> ingredients)
    {
        if (ingredients == null)
        {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (RecipeIngredient ingredient : ingredients)
        {
            if (ingredient != null)
            {
                names.add(ingredient.getIngredientName());
            }
        }
        return names;
    }

    static boolean ingredientMatchesTerm(String ingredientName, String term)
    {
        if (ingredientName == null || term == null || term.trim().isEmpty())
        {
            return false;
        }
        String normalizedTerm = term.trim().toLowerCase(Locale.ROOT);
        String ingredient = ingredientName.toLowerCase(Locale.ROOT);
        if (matchesWord(ingredient, normalizedTerm))
        {
            return true;
        }
        if (normalizedTerm.endsWith("oes") && normalizedTerm.length() > 3)
        {
            return matchesWord(ingredient, normalizedTerm.substring(0, normalizedTerm.length() - 2));
        }
        if (normalizedTerm.endsWith("s") && !normalizedTerm.endsWith("ss")
                && normalizedTerm.length() > 3)
        {
            return matchesWord(ingredient, normalizedTerm.substring(0, normalizedTerm.length() - 1));
        }
        return false;
    }

    private static boolean matchesWord(String ingredient, String term)
    {
        String regex = "(?<![a-z])" + Pattern.quote(term) + "(?:s|es)?(?![a-z])";
        return Pattern.compile(regex).matcher(ingredient).find();
    }

    private static List<String> parseTerms(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        List<String> terms = new ArrayList<>();
        for (String term : value.split("[,;\\n]"))
        {
            String normalized = term.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty() && !terms.contains(normalized))
            {
                terms.add(normalized);
            }
        }
        return terms;
    }
}
