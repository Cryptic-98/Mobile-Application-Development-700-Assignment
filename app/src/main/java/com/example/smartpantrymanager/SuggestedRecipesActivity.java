package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.database.RecipeRepository;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.logic.MatchResult;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.logic.RecipeSectionPlanner;
import com.example.smartpantrymanager.logic.TheMealDbMatch;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuggestedRecipesActivity extends BaseActivity
{
    private RecipeMatcher recipeMatcher;
    private RecipeRepository recipeRepository;
    private RecipePreferenceEvaluator preferenceEvaluator;
    private TheMealDbRecipeRepository onlineRecipeRepository;
    private LinearLayout starterRecipesContainer;
    private LinearLayout pantryMatchesContainer;
    private TextView pantryMatchesEmpty;
    private TextView apiStatus;
    private ProgressBar apiLoading;
    private boolean apiRequestInFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        recipeRepository = new RecipeRepository(databaseHelper);
        PantryRepository pantryRepository = new PantryRepository(databaseHelper);
        recipeMatcher = new RecipeMatcher(recipeRepository, pantryRepository);
        preferenceEvaluator = new RecipePreferenceEvaluator(new SettingsRepository(databaseHelper));
        onlineRecipeRepository = new TheMealDbRecipeRepository(this, pantryRepository);
        starterRecipesContainer = findViewById(R.id.starter_recipes_container);
        pantryMatchesContainer = findViewById(R.id.api_recipes_container);
        pantryMatchesEmpty = findViewById(R.id.api_recipes_empty);
        apiStatus = findViewById(R.id.recipes_api_status);
        apiLoading = findViewById(R.id.recipes_api_loading);
        setupBottomNavigation(R.id.navigation_recipes);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        renderStarterRecipes();
        loadPantryMatches();
    }

    public List<MatchResult> getSuggestedRecipes()
    {
        return recipeMatcher.getSuggestedRecipes();
    }

    private void renderStarterRecipes()
    {
        List<String> starterNames = PresetRecipeSeeder.getStarterRecipeNames();
        boolean missingStarter = false;
        for (String name : starterNames)
        {
            if (recipeRepository.getRecipeByName(name) == null)
            {
                missingStarter = true;
                break;
            }
        }
        if (missingStarter)
        {
            new PresetRecipeSeeder(new PantryRepository(databaseHelper), recipeRepository).seed();
        }

        Map<Integer, MatchResult> matches = new HashMap<>();
        for (MatchResult match : recipeMatcher.evaluateMatches())
        {
            if (match.getRecipe().getId() != null)
            {
                matches.put(match.getRecipe().getId(), match);
            }
        }
        starterRecipesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String name : starterNames)
        {
            Recipe recipe = recipeRepository.getRecipeByName(name);
            if (recipe == null)
            {
                continue;
            }
            List<RecipeIngredient> ingredients = recipeRepository.getRecipeIngredients(recipe.getId());
            if (preferenceEvaluator.shouldHideRecipe(ingredients))
            {
                continue;
            }
            MatchResult match = matches.get(recipe.getId());
            int available = match == null ? 0 : match.getMatchedRequiredIngredientCount();
            int total = match == null ? 0 : match.getTotalRequiredIngredientCount();
            View card = createRecipeCard(inflater, starterRecipesContainer, recipe,
                    ingredientNames(ingredients), formatAvailability(available, total),
                    formatMetadata(recipe));
            card.setOnClickListener(view ->
            {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            });
            starterRecipesContainer.addView(card);
        }
    }

    private void loadPantryMatches()
    {
        if (apiRequestInFlight)
        {
            return;
        }
        apiRequestInFlight = true;
        apiLoading.setVisibility(View.VISIBLE);
        apiStatus.setText(R.string.recipes_loading);
        apiStatus.setVisibility(View.VISIBLE);
        pantryMatchesEmpty.setVisibility(View.GONE);
        pantryMatchesContainer.removeAllViews();
        onlineRecipeRepository.discoverAsync(new TheMealDbRecipeRepository.Callback()
        {
            @Override
            public void onLoaded(List<TheMealDbMatch> matches, boolean hadErrors)
            {
                apiRequestInFlight = false;
                apiLoading.setVisibility(View.GONE);
                renderPantryMatches(matches);
                if (hadErrors && matches.isEmpty())
                {

                    apiStatus.setText(R.string.recipes_api_unavailable);
                    apiStatus.setVisibility(View.VISIBLE);
                }
                else if (hadErrors)
                {
                    apiStatus.setText(R.string.recipes_api_partial_error);
                    apiStatus.setVisibility(View.VISIBLE);
                }
                else
                {
                    apiStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception exception)
            {
                apiRequestInFlight = false;
                apiLoading.setVisibility(View.GONE);
                apiStatus.setText(R.string.recipes_api_unavailable);
                apiStatus.setVisibility(View.VISIBLE);
                pantryMatchesContainer.removeAllViews();
                pantryMatchesEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderPantryMatches(List<TheMealDbMatch> matches)
    {
        pantryMatchesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        List<TheMealDbMatch> sectionMatches = RecipeSectionPlanner.excludeStarterRecipes(matches,
                PresetRecipeSeeder.getStarterRecipeNames());
        for (TheMealDbMatch match : sectionMatches)
        {
            TheMealDbMeal meal = match.getMeal();
            List<String> ingredientNames = ingredientNames(meal);
            if (preferenceEvaluator.shouldHideRecipeByIngredientNames(ingredientNames))
            {
                continue;
            }
            Recipe recipe = toDisplayRecipe(meal);
            View card = createRecipeCard(inflater, pantryMatchesContainer, recipe, ingredientNames,
                    formatAvailability(match.getMatchedRequiredIngredientCount(),
                            match.getTotalRequiredIngredientCount()),
                    formatOnlineMetadata(meal));
            card.setOnClickListener(view ->
            {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_THEMEALDB_MEAL_ID, meal.getId());
                startActivity(intent);
            });
            pantryMatchesContainer.addView(card);
        }
        pantryMatchesEmpty.setVisibility(pantryMatchesContainer.getChildCount() == 0
                ? View.VISIBLE : View.GONE);
    }

    private View createRecipeCard(LayoutInflater inflater, ViewGroup parent, Recipe recipe,
                                  List<String> ingredientNames, String availability,
                                  String metadataText)
    {
        View card = inflater.inflate(R.layout.item_recipe, parent, false);
        ImageView image = card.findViewById(R.id.recipe_item_image);
        ImageView allergyWarning = card.findViewById(R.id.recipe_item_allergy_warning);
        ImageView dietWarning = card.findViewById(R.id.recipe_item_diet_warning);
        TextView name = card.findViewById(R.id.recipe_item_name);
        TextView metadata = card.findViewById(R.id.recipe_item_metadata);
        TextView description = card.findViewById(R.id.recipe_item_description);
        TextView matchStatus = card.findViewById(R.id.recipe_item_match_status);
        name.setText(recipe.getName());
        allergyWarning.setVisibility(preferenceEvaluator.hasAllergyWarningByIngredientNames(ingredientNames)
                ? View.VISIBLE : View.GONE);
        dietWarning.setVisibility(preferenceEvaluator.hasDietWarningByIngredientNames(ingredientNames)
                ? View.VISIBLE : View.GONE);
        image.setContentDescription(recipe.getName());
        FoodImageLoader.load(image, recipe);
        metadata.setText(metadataText);
        metadata.setVisibility(metadataText.isEmpty() ? View.GONE : View.VISIBLE);
        matchStatus.setText(availability);
        if (recipe.getDescription() == null)
        {
            description.setVisibility(View.GONE);
        }
        else
        {
            description.setText(recipe.getDescription());
        }
        return card;
    }

    private static List<String> ingredientNames(TheMealDbMeal meal)
    {
        List<String> result = new ArrayList<>();
        for (com.example.smartpantrymanager.api.TheMealDbIngredient ingredient : meal.getIngredients())
        {
            result.add(ingredient.getName());
        }
        return result;
    }

    private static List<String> ingredientNames(List<RecipeIngredient> ingredients)
    {
        List<String> result = new ArrayList<>();
        for (RecipeIngredient ingredient : ingredients)
        {
            result.add(ingredient.getIngredientName());
        }
        return result;
    }

    private static Recipe toDisplayRecipe(TheMealDbMeal meal)
    {
        Recipe recipe = new Recipe(meal.getName(), null, null, null, null);
        recipe.setImageReference(meal.getImageUrl());
        return recipe;
    }

    private String formatAvailability(int available, int total)
    {
        return getString(R.string.recipe_match_indicator, available, total);
    }

    private String formatOnlineMetadata(TheMealDbMeal meal)
    {
        if (meal.getCategory() == null)
        {
            return meal.getArea() == null ? "" : meal.getArea();
        }
        return meal.getArea() == null ? meal.getCategory()
                : meal.getCategory() + " | " + meal.getArea();
    }

    private String formatMetadata(Recipe recipe)
    {
        StringBuilder metadata = new StringBuilder();
        if (recipe.getPrepTimeMinutes() != null)
        {
            metadata.append(getString(R.string.time_minutes, recipe.getPrepTimeMinutes()));
        }
        if (recipe.getCookTimeMinutes() != null)
        {
            if (metadata.length() > 0)
            {
                metadata.append(" | ");
            }
            metadata.append(getString(R.string.time_minutes, recipe.getCookTimeMinutes()));
        }
        if (recipe.getServings() != null)
        {
            if (metadata.length() > 0)
            {
                metadata.append(" | ");
            }
            metadata.append(getResources().getQuantityString(R.plurals.servings,
                    recipe.getServings(), recipe.getServings()));
        }
        return metadata.toString();
    }
}
