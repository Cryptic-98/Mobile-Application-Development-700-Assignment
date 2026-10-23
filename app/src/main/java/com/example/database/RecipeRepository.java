package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import com.example.smartpantrymanager.models.RecipeStep;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeRepository
{
    private final DatabaseHelper dbHelper;

    public RecipeRepository(DatabaseHelper dbHelper)
    {
        if (dbHelper == null)
        {
            throw new IllegalArgumentException("dbHelper must not be null.");
        }
        this.dbHelper = dbHelper;
    }

    public DatabaseHelper getDbHelper()
    {
        return dbHelper;
    }

    public long insertRecipe(Recipe recipe)
    {
        validateRecipe(recipe);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insertOrThrow("recipes", null, recipeValues(recipe, false));
        recipe.setId((int) id);
        return id;
    }

    public void updateRecipe(Recipe recipe)
    {
        validateRecipe(recipe);
        if (recipe.getId() == null)
        {
            throw new IllegalArgumentException("recipe id is required for an update.");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = recipeValues(recipe, true);
        db.beginTransaction();
        try
        {
            int changed = db.update("recipes", values, "id = ?", new String[]{String.valueOf(recipe.getId())});
            if (changed != 1)
            {
                throw new IllegalArgumentException("Recipe does not exist.");
            }
            db.execSQL("UPDATE recipes SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    new Object[]{recipe.getId()});
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public boolean deleteRecipe(int id)
    {
        requireId(id, "recipe id");
        return dbHelper.getWritableDatabase().delete("recipes", "id = ?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public Recipe getRecipeById(int id)
    {
        requireId(id, "recipe id");
        try (Cursor cursor = dbHelper.getReadableDatabase().query("recipes", recipeColumns(),
                "id = ?", new String[]{String.valueOf(id)}, null, null, null))
        {
            return cursor.moveToFirst() ? mapRecipe(cursor) : null;
        }
    }

    public Recipe getRecipeByName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("name must not be null or blank.");
        }
        try (Cursor cursor = dbHelper.getReadableDatabase().query("recipes", recipeColumns(),
                "name = ? COLLATE NOCASE", new String[]{name.trim()}, null, null, null))
        {
            return cursor.moveToFirst() ? mapRecipe(cursor) : null;
        }
    }

    public List<Recipe> getAllActiveRecipes()
    {
        try (Cursor cursor = dbHelper.getReadableDatabase().query("recipes", recipeColumns(),
                "is_active = 1", null, null, null, "lower(name), id"))
        {
            List<Recipe> recipes = new ArrayList<>();
            while (cursor.moveToNext())
            {
                recipes.add(mapRecipe(cursor));
            }
            return recipes;
        }
    }

    public void setRecipeActive(int recipeId, boolean active)
    {
        requireId(recipeId, "recipe id");
        ContentValues values = new ContentValues();
        values.put("is_active", active ? 1 : 0);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            int changed = db.update("recipes", values,
                    "id = ?", new String[]{String.valueOf(recipeId)});
            if (changed != 1)
            {
                throw new IllegalArgumentException("Recipe does not exist.");
            }
            db.execSQL("UPDATE recipes SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    new Object[]{recipeId});
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId)
    {
        requireId(recipeId, "recipe id");
        String sql = "SELECT ri.id, ri.recipe_id, ri.ingredient_id, ri.required_quantity, "
                + "ri.unit_code, ri.is_optional, ri.ingredient_note, i.canonical_name "
                + "FROM recipe_ingredients ri JOIN ingredients i ON i.id = ri.ingredient_id "
                + "WHERE ri.recipe_id = ? ORDER BY lower(i.canonical_name), ri.ingredient_id";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(recipeId)}))
        {
            List<RecipeIngredient> ingredients = new ArrayList<>();
            while (cursor.moveToNext())
            {
                RecipeIngredient ingredient = mapRecipeIngredient(cursor);
                ingredient.setIngredientName(cursor.getString(7));
                ingredients.add(ingredient);
            }
            return ingredients;
        }
    }

    public List<RecipeStep> getRecipeSteps(int recipeId)
    {
        requireId(recipeId, "recipe id");
        try (Cursor cursor = dbHelper.getReadableDatabase().query("recipe_steps",
                new String[]{"id", "recipe_id", "step_number", "instruction"},
                "recipe_id = ?", new String[]{String.valueOf(recipeId)}, null, null, "step_number"))
        {
            List<RecipeStep> steps = new ArrayList<>();
            while (cursor.moveToNext())
            {
                steps.add(new RecipeStep(cursor.getInt(0), cursor.getInt(1),
                        cursor.getInt(2), cursor.getString(3)));
            }
            return steps;
        }
    }

    public long saveCompleteRecipe(Recipe recipe, List<RecipeIngredient> ingredients, List<RecipeStep> steps)
    {
        validateRecipe(recipe);
        if (ingredients == null || steps == null)
        {
            throw new IllegalArgumentException("Recipe ingredients and steps must not be null.");
        }
        validateChildren(ingredients, steps);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean isNewRecipe = recipe.getId() == null;
        long recipeId;
        db.beginTransaction();
        try
        {
            if (isNewRecipe)
            {
                recipeId = db.insertOrThrow("recipes", null, recipeValues(recipe, false));
            }
            else
            {
                ContentValues values = recipeValues(recipe, true);
                int changed = db.update("recipes", values, "id = ?",
                        new String[]{String.valueOf(recipe.getId())});
                if (changed != 1)
                {
                    throw new IllegalArgumentException("Recipe does not exist.");
                }
                recipeId = recipe.getId();
                db.execSQL("UPDATE recipes SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        new Object[]{recipeId});
                db.delete("recipe_ingredients", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
                db.delete("recipe_steps", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
            }
            for (RecipeIngredient ingredient : ingredients)
            {
                insertRecipeIngredient(db, recipeId, ingredient);
            }
            for (RecipeStep step : steps)
            {
                insertRecipeStep(db, recipeId, step);
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        if (isNewRecipe)
        {
            recipe.setId((int) recipeId);
        }
        return recipeId;
    }

    private static void insertRecipeIngredient(SQLiteDatabase db, long recipeId, RecipeIngredient ingredient)
    {
        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("ingredient_id", ingredient.getIngredientId());
        values.put("required_quantity", ingredient.getRequiredQuantity());
        values.put("unit_code", ingredient.getUnitCode());
        values.put("is_optional", ingredient.isOptional() ? 1 : 0);
        values.put("ingredient_note", ingredient.getIngredientNote());
        db.insertOrThrow("recipe_ingredients", null, values);
    }

    private static void insertRecipeStep(SQLiteDatabase db, long recipeId, RecipeStep step)
    {
        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("step_number", step.getStepNumber());
        values.put("instruction", step.getInstruction());
        db.insertOrThrow("recipe_steps", null, values);
    }

    private static void validateChildren(List<RecipeIngredient> ingredients, List<RecipeStep> steps)
    {
        Set<Integer> ingredientIds = new HashSet<>();
        for (RecipeIngredient ingredient : ingredients)
        {
            if (ingredient == null)
            {
                throw new IllegalArgumentException("Recipe ingredients must not contain null.");
            }
            if (!ingredientIds.add(ingredient.getIngredientId()))
            {
                throw new IllegalArgumentException("A recipe cannot contain a duplicate ingredient.");
            }
        }
        Set<Integer> stepNumbers = new HashSet<>();
        for (RecipeStep step : steps)
        {
            if (step == null)
            {
                throw new IllegalArgumentException("Recipe steps must not contain null.");
            }
            if (!stepNumbers.add(step.getStepNumber()))
            {
                throw new IllegalArgumentException("Recipe step numbers must be unique.");
            }
        }
    }

    private static ContentValues recipeValues(Recipe recipe, boolean update)
    {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        putNullable(values, "description", recipe.getDescription());
        putNullable(values, "image_reference", recipe.getImageReference());
        putNullable(values, "image_source", recipe.getImageSource());
        putNullable(values, "prep_time_minutes", recipe.getPrepTimeMinutes());
        putNullable(values, "cook_time_minutes", recipe.getCookTimeMinutes());
        putNullable(values, "servings", recipe.getServings());
        values.put("is_active", recipe.isActive() ? 1 : 0);
        return values;
    }

    private static void putNullable(ContentValues values, String column, String value)
    {
        if (value == null)
        {
            values.putNull(column);
        }
        else
        {
            values.put(column, value);
        }
    }

    private static void putNullable(ContentValues values, String column, Integer value)
    {
        if (value == null)
        {
            values.putNull(column);
        }
        else
        {
            values.put(column, value);
        }
    }

    private static void validateRecipe(Recipe recipe)
    {
        if (recipe == null)
        {
            throw new IllegalArgumentException("recipe must not be null.");
        }
        if (recipe.getName() == null || recipe.getName().trim().isEmpty())
        {
            throw new IllegalArgumentException("Recipe name must not be blank.");
        }
    }

    private static void requireId(int id, String field)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException(field + " must be positive.");
        }
    }

    private static String[] recipeColumns()
    {
        return new String[]{"id", "name", "description", "image_reference", "image_source",
                "prep_time_minutes", "cook_time_minutes", "servings", "is_active", "created_at", "updated_at"};
    }

    private static Recipe mapRecipe(Cursor cursor)
    {
        return new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.isNull(5) ? null : cursor.getInt(5),
                cursor.isNull(6) ? null : cursor.getInt(6), cursor.isNull(7) ? null : cursor.getInt(7),
                cursor.getInt(8) == 1, cursor.getString(9), cursor.getString(10));
    }

    private static RecipeIngredient mapRecipeIngredient(Cursor cursor)
    {
        return new RecipeIngredient(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                cursor.getDouble(3), cursor.getString(4), cursor.getInt(5) == 1, cursor.getString(6));
    }
}
