package com.example.smartpantrymanager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.smartpantrymanager.api.TheMealDbIngredient;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import com.example.smartpantrymanager.database.RecipeRepository;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.logic.IngredientAvailability;
import com.example.smartpantrymanager.logic.MatchResult;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.logic.TheMealDbMatch;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import com.example.smartpantrymanager.models.RecipeStep;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends BaseActivity
{
    private static final String TAG = "RecipeDetailActivity";
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_THEMEALDB_MEAL_ID = "themealdb_meal_id";
    private RecipeRepository recipeRepository;
    private RecipePreferenceEvaluator preferenceEvaluator;
    private RecipeMatcher recipeMatcher;
    private TheMealDbRecipeRepository onlineRecipeRepository;
    private int recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        recipeRepository = new RecipeRepository(databaseHelper);
        preferenceEvaluator = new RecipePreferenceEvaluator(new SettingsRepository(databaseHelper));
        PantryRepository pantryRepository = new PantryRepository(databaseHelper);
        recipeMatcher = new RecipeMatcher(recipeRepository, pantryRepository);
        onlineRecipeRepository = new TheMealDbRecipeRepository(this, pantryRepository);
        recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, 0);
        findViewById(R.id.recipe_detail_back_button).setOnClickListener(view -> finish());
        String onlineMealId = getIntent().getStringExtra(EXTRA_THEMEALDB_MEAL_ID);
        if (onlineMealId != null && !onlineMealId.trim().isEmpty())
        {
            loadOnlineMeal(onlineMealId);
            return;
        }
        if (recipeId <= 0)
        {
            showMissingRecipe();
            return;
        }
        Recipe recipe = loadRecipe();
        if (recipe == null)
        {
            showMissingRecipe();
            return;
        }
        bindRecipe(recipe);
    }

    private void loadOnlineMeal(String mealId)
    {
        TextView status = findViewById(R.id.recipe_detail_status);
        status.setText(R.string.recipe_detail_loading);
        status.setVisibility(View.VISIBLE);
        onlineRecipeRepository.loadMealByIdAsync(mealId,
                new com.example.smartpantrymanager.api.TheMealDbClient.MealCallback()
                {
                    @Override
                    public void onLoaded(TheMealDbMeal meal)
                    {
                        if (isFinishing() || isDestroyed())
                        {
                            return;
                        }
                        if (meal == null)
                        {
                            showOnlineMealError();
                            return;
                        }
                        onlineRecipeRepository.evaluateMealAsync(meal,
                                new TheMealDbRecipeRepository.MatchCallback()
                                {
                                    @Override
                                    public void onEvaluated(TheMealDbMatch match)
                                    {
                                        if (!isFinishing() && !isDestroyed())
                                        {
                                            bindOnlineMeal(meal, match);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception exception)
                                    {
                                        Log.e(TAG, "Could not compare the meal with current pantry stock.", exception);
                                        showOnlineMealError();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception exception)
                    {
                        Log.e(TAG, "Could not load online meal details.", exception);
                        showOnlineMealError();
                    }
                });
    }

    private void bindOnlineMeal(TheMealDbMeal meal, TheMealDbMatch match)
    {
        List<String> ingredientNames = ingredientNames(meal);
        if (preferenceEvaluator.shouldHideRecipeByIngredientNames(ingredientNames))
        {
            Toast.makeText(this, R.string.allergy_meal_hidden, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Recipe recipe = toDisplayRecipe(meal);
        bindHeader(recipe, ingredientNames);
        ((TextView) findViewById(R.id.recipe_detail_metadata)).setText(formatOnlineMetadata(meal));
        TextView status = findViewById(R.id.recipe_detail_status);
        status.setVisibility(View.GONE);
        TextView availability = findViewById(R.id.recipe_detail_availability);
        availability.setText(getString(R.string.recipe_availability_summary,
                match.getMatchedRequiredIngredientCount(),
                match.getTotalRequiredIngredientCount(),
                match.getMissingRequiredIngredientCount()));
        renderOnlineIngredients(meal.getIngredients(), match);
        ((TextView) findViewById(R.id.recipe_steps_text)).setText(
                formatOnlineSteps(meal.getInstructions()));
    }

    public Recipe loadRecipe()
    {
        return recipeRepository.getRecipeById(recipeId);
    }

    public List<RecipeIngredient> loadIngredients()
    {
        return recipeRepository.getRecipeIngredients(recipeId);
    }

    public List<RecipeStep> loadSteps()
    {
        return recipeRepository.getRecipeSteps(recipeId);
    }

    private void bindRecipe(Recipe recipe)
    {
        List<RecipeIngredient> ingredients = loadIngredients();
        if (preferenceEvaluator.shouldHideRecipe(ingredients))
        {
            Toast.makeText(this, R.string.allergy_meal_hidden, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        bindHeader(recipe, ingredientNames(ingredients));
        MatchResult match = findMatch(recipe.getId());
        renderIngredients(ingredients, match);
        int available = match == null ? 0 : match.getMatchedRequiredIngredientCount();
        int total = match == null ? countRequiredIngredients(ingredients)
                : match.getTotalRequiredIngredientCount();
        ((TextView) findViewById(R.id.recipe_detail_availability)).setText(
                getString(R.string.recipe_availability_summary, available, total, total - available));
        ((TextView) findViewById(R.id.recipe_steps_text)).setText(formatSteps(loadSteps()));
    }

    private void bindHeader(Recipe recipe, List<String> ingredientNames)
    {
        ((TextView) findViewById(R.id.recipe_detail_name)).setText(recipe.getName());
        findViewById(R.id.recipe_detail_allergy_warning).setVisibility(
                preferenceEvaluator.hasAllergyWarningByIngredientNames(ingredientNames)
                        ? View.VISIBLE : View.GONE);
        findViewById(R.id.recipe_detail_diet_warning).setVisibility(
                preferenceEvaluator.hasDietWarningByIngredientNames(ingredientNames)
                        ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.recipe_detail_metadata)).setText(formatMetadata(recipe));
        TextView description = findViewById(R.id.recipe_detail_description);
        if (recipe.getDescription() == null)
        {
            description.setVisibility(View.GONE);
        }
        else
        {
            description.setText(recipe.getDescription());
        }
        ImageView image = findViewById(R.id.recipe_detail_image);
        image.setContentDescription(getString(R.string.recipe_detail) + ": " + recipe.getName());
        FoodImageLoader.load(image, recipe);
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

    private MatchResult findMatch(int id)
    {
        for (MatchResult match : recipeMatcher.evaluateMatches())
        {
            if (match.getRecipe().getId() != null && match.getRecipe().getId() == id)
            {
                return match;
            }
        }
        return null;
    }

    private void renderIngredients(List<RecipeIngredient> ingredients, MatchResult match)
    {
        LinearLayout container = findViewById(R.id.recipe_ingredients_text);
        container.removeAllViews();
        if (ingredients.isEmpty())
        {
            TextView empty = new TextView(this);
            empty.setText(R.string.no_ingredients);
            empty.setTextAppearance(this, R.style.TextAppearance_SmartPantryManager_Body);
            container.addView(empty);
            return;
        }
        for (RecipeIngredient ingredient : ingredients)
        {
            IngredientAvailability availability = match == null
                    ? IngredientAvailability.MISSING
                    : match.getIngredientAvailability(ingredient.getIngredientId());
            StringBuilder text = new StringBuilder();
            text.append(formatQuantity(ingredient.getRequiredQuantity()))
                    .append(' ').append(ingredient.getUnitCode()).append(' ')
                    .append(ingredient.getIngredientName() == null
                            ? "Ingredient " + ingredient.getIngredientId() : ingredient.getIngredientName());
            if (ingredient.isOptional())
            {
                text.append(" (").append(getString(R.string.optional)).append(')');
            }
            if (ingredient.getIngredientNote() != null)
            {
                text.append(" - ").append(ingredient.getIngredientNote());
            }
            addIngredientRow(container, text.toString(), availability);
        }
    }

    private void renderOnlineIngredients(List<TheMealDbIngredient> ingredients, TheMealDbMatch match)
    {
        LinearLayout container = findViewById(R.id.recipe_ingredients_text);
        container.removeAllViews();
        if (ingredients.isEmpty())
        {
            TextView empty = new TextView(this);
            empty.setText(R.string.no_ingredients);
            empty.setTextAppearance(this, R.style.TextAppearance_SmartPantryManager_Body);
            container.addView(empty);
            return;
        }
        for (int index = 0; index < ingredients.size(); index++)
        {
            TheMealDbIngredient ingredient = ingredients.get(index);
            String measure = ingredient.getMeasure();
            String label = measure == null || measure.trim().isEmpty()
                    ? ingredient.getName() : measure.trim() + " " + ingredient.getName();
            addIngredientRow(container, label, match.getIngredientAvailability(index));
        }
    }











    private void addIngredientRow(LinearLayout container, String label,
                                  IngredientAvailability availability)
    {
        String status = statusLabel(availability);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.space_sm));

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.status_icon_size),
                getResources().getDimensionPixelSize(R.dimen.status_icon_size));
        iconParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_sm));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(statusIcon(availability));
        icon.setContentDescription(getString(R.string.ingredient_status_icon_description)
                + ": " + status);
        row.addView(icon);

        TextView text = new TextView(this);
        text.setText(getString(R.string.recipe_ingredient_status, label, status));
        text.setTextAppearance(this, R.style.TextAppearance_SmartPantryManager_Body);
        text.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        text.setContentDescription(getString(R.string.recipe_ingredient_status, label, status));
        row.addView(text);

        container.addView(row);
    }





    private String statusLabel(IngredientAvailability availability)
    {
        switch (availability)
        {
            case AVAILABLE:
                return getString(R.string.ingredient_available);
            case INSUFFICIENT_QUANTITY:
                return getString(R.string.ingredient_insufficient);
            case EXPIRED:
                return getString(R.string.ingredient_expired);
            case INCOMPATIBLE_UNIT:
                return getString(R.string.ingredient_incompatible_unit);
            case UNVERIFIABLE:
                return getString(R.string.ingredient_unverifiable);
            case MISSING:
            default:
                return getString(R.string.ingredient_missing);
        }
    }





    private int statusIcon(IngredientAvailability availability)
    {
        switch (availability)
        {
            case AVAILABLE:
                return R.drawable.ic_status_available;
            case MISSING:
                return R.drawable.ic_status_missing;
            case INSUFFICIENT_QUANTITY:
            case EXPIRED:
            case INCOMPATIBLE_UNIT:
            case UNVERIFIABLE:
            default:
                return R.drawable.ic_status_unclear;
        }
    }

    private String formatSteps(List<RecipeStep> steps)
    {
        if (steps.isEmpty())
        {
            return getString(R.string.no_steps);
        }
        StringBuilder text = new StringBuilder();
        for (RecipeStep step : steps)
        {
            if (text.length() > 0)
            {
                text.append("\n\n");
            }
            text.append(step.getStepNumber()).append(". ").append(step.getInstruction());
        }
        return text.toString();
    }

    private String formatOnlineSteps(List<String> steps)
    {
        if (steps.isEmpty())
        {
            return getString(R.string.no_steps);
        }
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < steps.size(); index++)
        {
            if (text.length() > 0)
            {
                text.append("\n\n");
            }
            text.append(index + 1).append(". ").append(steps.get(index));
        }
        return text.toString();
    }

    private static List<String> ingredientNames(TheMealDbMeal meal)
    {
        List<String> result = new java.util.ArrayList<>();
        for (TheMealDbIngredient ingredient : meal.getIngredients())
        {
            result.add(ingredient.getName());
        }
        return result;
    }

    private static List<String> ingredientNames(List<RecipeIngredient> ingredients)
    {
        List<String> result = new java.util.ArrayList<>();
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

    private static int countRequiredIngredients(List<RecipeIngredient> ingredients)
    {
        int count = 0;
        for (RecipeIngredient ingredient : ingredients)
        {
            if (!ingredient.isOptional())
            {
                count++;
            }
        }
        return count;
    }

    private void showOnlineMealError()
    {
        if (isFinishing() || isDestroyed())
        {
            return;
        }
        TextView status = findViewById(R.id.recipe_detail_status);
        status.setText(R.string.recipe_detail_unavailable);
        status.setVisibility(View.VISIBLE);
    }

    private String formatMetadata(Recipe recipe)
    {
        StringBuilder text = new StringBuilder();
        if (recipe.getPrepTimeMinutes() != null)
        {
            text.append(getString(R.string.prep_time,
                    getString(R.string.time_minutes, recipe.getPrepTimeMinutes())));
        }
        if (recipe.getCookTimeMinutes() != null)
        {
            if (text.length() > 0)
            {
                text.append(" | ");
            }
            text.append(getString(R.string.cook_time,
                    getString(R.string.time_minutes, recipe.getCookTimeMinutes())));
        }
        if (recipe.getServings() != null)
        {
            if (text.length() > 0)
            {
                text.append(" | ");
            }
            text.append(getResources().getQuantityString(R.plurals.servings,
                    recipe.getServings(), recipe.getServings()));
        }
        return text.toString();
    }

    private void showMissingRecipe()
    {
        Toast.makeText(this, R.string.empty_recipes, Toast.LENGTH_LONG).show();
        finish();
    }

    private static String formatQuantity(double quantity)
    {
        return quantity == Math.rint(quantity)
                ? String.format(Locale.ROOT, "%.0f", quantity)
                : String.format(Locale.ROOT, "%.2f", quantity);
    }
}
