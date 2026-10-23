package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import com.example.smartpantrymanager.logic.IngredientNormalizer;
import com.example.smartpantrymanager.models.Ingredient;
import com.example.smartpantrymanager.models.IngredientAlias;
import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PantryRepository
{
    private final DatabaseHelper dbHelper;

    public PantryRepository(DatabaseHelper dbHelper)
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

    public Ingredient getIngredientById(int id)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException("id must be positive.");
        }
        try (Cursor cursor = dbHelper.getReadableDatabase().query("ingredients",
                new String[]{"id", "canonical_name", "normalized_name", "created_at"},
                "id = ?", new String[]{String.valueOf(id)}, null, null, null))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    public Ingredient findIngredientByNormalizedName(String name)
    {
        String normalized = IngredientNormalizer.normalize(name);
        try (Cursor cursor = dbHelper.getReadableDatabase().query("ingredients",
                new String[]{"id", "canonical_name", "normalized_name", "created_at"},
                "normalized_name = ?", new String[]{normalized}, null, null, null))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    public Ingredient findIngredientByAlias(String alias)
    {
        String normalized = IngredientNormalizer.normalize(alias);
        String sql = "SELECT i.id, i.canonical_name, i.normalized_name, i.created_at "
                + "FROM ingredient_aliases a JOIN ingredients i ON i.id = a.ingredient_id "
                + "WHERE a.normalized_alias = ?";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{normalized}))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    public Ingredient findOrCreateIngredient(String name)
    {
        String canonical = requireName(name);
        String normalized = IngredientNormalizer.normalize(canonical);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            Ingredient ingredient = findIngredient(db, normalized);
            if (ingredient == null)
            {
                ingredient = findAlias(db, normalized);
            }
            if (ingredient == null)
            {
                ContentValues values = new ContentValues();
                values.put("canonical_name", canonical);
                values.put("normalized_name", normalized);
                long id;
                try
                {
                    id = db.insertOrThrow("ingredients", null, values);
                }
                catch (SQLiteConstraintException exception)
                {
                    ingredient = findIngredient(db, normalized);
                    if (ingredient == null)
                    {
                        throw exception;
                    }
                    id = ingredient.getId();
                }
                ingredient = getIngredient(db, id);
            }
            db.setTransactionSuccessful();
            return ingredient;
        }
        finally
        {
            db.endTransaction();
        }
    }

    public IngredientAlias addAlias(int ingredientId, String alias)
    {
        if (ingredientId <= 0)
        {
            throw new IllegalArgumentException("ingredientId must be positive.");
        }
        String normalized = IngredientNormalizer.normalize(alias);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            Ingredient canonical = getIngredient(db, ingredientId);
            if (canonical == null)
            {
                throw new IllegalArgumentException("Ingredient does not exist.");
            }
            Ingredient nameConflict = findIngredient(db, normalized);
            Ingredient aliasConflict = findAlias(db, normalized);
            if (nameConflict != null || aliasConflict != null)
            {
                throw new IllegalArgumentException("Alias conflicts with an existing ingredient name or alias.");
            }
            ContentValues values = new ContentValues();
            values.put("ingredient_id", ingredientId);
            values.put("normalized_alias", normalized);
            long id;
            try
            {
                id = db.insertOrThrow("ingredient_aliases", null, values);
            }
            catch (SQLiteConstraintException exception)
            {
                throw new IllegalArgumentException("Alias conflicts with an existing alias.", exception);
            }
            IngredientAlias result = new IngredientAlias((int) id, ingredientId, normalized, null);
            db.setTransactionSuccessful();
            return result;
        }
        finally
        {
            db.endTransaction();
        }
    }

    public long upsertPantryItem(PantryItem item)
    {
        if (item == null)
        {
            throw new IllegalArgumentException("item must not be null.");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            long id = writePantryItem(db, item);
            db.setTransactionSuccessful();
            return id;
        }
        finally
        {
            db.endTransaction();
        }
    }

    public PantryItem addOrMergePantryItem(String name, double quantity, String unitCode, String expiryDate)
    {
        String canonicalName = requireName(name);
        validateQuantity(quantity);
        String normalizedUnit = requireUnitCode(unitCode);
        LocalDate incomingExpiry = ValidationDate.parseOptional(expiryDate);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            String normalizedName = IngredientNormalizer.normalize(canonicalName);
            Ingredient ingredient = findIngredient(db, normalizedName);
            if (ingredient == null)
            {
                ingredient = findAlias(db, normalizedName);
            }
            if (ingredient == null)
            {
                ContentValues values = new ContentValues();
                values.put("canonical_name", canonicalName);
                values.put("normalized_name", normalizedName);
                long id = db.insertOrThrow("ingredients", null, values);
                ingredient = getIngredient(db, id);
            }
            MeasurementUnit incomingUnit = getUnit(db, normalizedUnit);
            if (incomingUnit == null)
            {
                throw new IllegalArgumentException("Unknown unit code: " + normalizedUnit);
            }
            PantryItem existing = getPantryItemByIngredient(db, ingredient.getId());
            if (existing == null)
            {
                PantryItem created = new PantryItem(ingredient.getId(), quantity, normalizedUnit, incomingExpiry);
                created.setIngredientName(ingredient.getCanonicalName());
                writePantryItem(db, created);
            }
            else
            {
                MeasurementUnit existingUnit = getUnit(db, existing.getUnitCode());
                if (existingUnit == null || !existingUnit.getDimension().equals(incomingUnit.getDimension()))
                {
                    throw new IllegalArgumentException("Stock units must have the same measurement dimension.");
                }
                double merged = (existing.getQuantity() * existingUnit.getToBaseFactor()
                        + quantity * incomingUnit.getToBaseFactor()) / existingUnit.getToBaseFactor();
                validateQuantity(merged);
                LocalDate currentExpiry = existing.getExpiryDate();
                LocalDate mergedExpiry = currentExpiry == null ? incomingExpiry
                        : incomingExpiry == null ? currentExpiry
                        : currentExpiry.isBefore(incomingExpiry) ? currentExpiry : incomingExpiry;
                db.execSQL("UPDATE pantry_items SET quantity = ?, expiry_date = ?, "
                                + "updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        new Object[]{merged, mergedExpiry == null ? null : mergedExpiry.toString(), existing.getId()});
            }
            PantryItem result = getPantryItemByIngredient(db, ingredient.getId());
            result.setIngredientName(ingredient.getCanonicalName());
            db.setTransactionSuccessful();
            return result;
        }
        finally
        {
            db.endTransaction();
        }
    }

    public PantryItem updatePantryItem(int itemId, String name, double quantity, String unitCode, String expiryDate)
    {
        if (itemId <= 0)
        {
            throw new IllegalArgumentException("itemId must be positive.");
        }
        String canonicalName = requireName(name);
        validateQuantity(quantity);
        String normalizedUnit = requireUnitCode(unitCode);
        LocalDate parsedExpiry = ValidationDate.parseOptional(expiryDate);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            PantryItem current = getPantryItem(db, itemId);
            if (current == null)
            {
                throw new IllegalArgumentException("Pantry item does not exist.");
            }
            String normalized = IngredientNormalizer.normalize(canonicalName);
            Ingredient ingredient = findIngredient(db, normalized);
            if (ingredient == null)
            {
                ingredient = findAlias(db, normalized);
            }
            if (ingredient == null)
            {
                ContentValues values = new ContentValues();
                values.put("canonical_name", canonicalName);
                values.put("normalized_name", normalized);
                long id = db.insertOrThrow("ingredients", null, values);
                ingredient = getIngredient(db, id);
            }
            if (getUnit(db, normalizedUnit) == null)
            {
                throw new IllegalArgumentException("Unknown unit code: " + normalizedUnit);
            }
            ContentValues values = new ContentValues();
            values.put("ingredient_id", ingredient.getId());
            values.put("quantity", quantity);
            values.put("unit_code", normalizedUnit);
            values.put("expiry_date", parsedExpiry == null ? null : parsedExpiry.toString());
            int updated = db.update("pantry_items", values, "id = ?", new String[]{String.valueOf(itemId)});
            if (updated != 1)
            {
                throw new IllegalStateException("Pantry item update failed.");
            }
            db.execSQL("UPDATE pantry_items SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    new Object[]{itemId});
            PantryItem result = getPantryItem(db, itemId);
            result.setIngredientName(ingredient.getCanonicalName());
            db.setTransactionSuccessful();
            return result;
        }
        finally
        {
            db.endTransaction();
        }
    }

    public boolean deletePantryItem(int itemId)
    {
        if (itemId <= 0)
        {
            throw new IllegalArgumentException("itemId must be positive.");
        }
        return dbHelper.getWritableDatabase().delete("pantry_items", "id = ?",
                new String[]{String.valueOf(itemId)}) > 0;
    }

    public PantryItem getPantryItemById(int id)
    {
        if (id <= 0)
        {
            throw new IllegalArgumentException("id must be positive.");
        }
        return getPantryItem(dbHelper.getReadableDatabase(), id);
    }

    public List<PantryItem> getAllPantryItems()
    {
        String sql = "SELECT p.id, p.ingredient_id, p.quantity, p.unit_code, p.expiry_date, "
                + "p.created_at, p.updated_at, i.canonical_name FROM pantry_items p "
                + "JOIN ingredients i ON i.id = p.ingredient_id ORDER BY lower(i.canonical_name), p.id";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, null))
        {
            List<PantryItem> items = new ArrayList<>();
            while (cursor.moveToNext())
            {
                PantryItem item = mapPantryItem(cursor);
                item.setIngredientName(cursor.getString(7));
                items.add(item);
            }
            return items;
        }
    }

    public List<PantryItem> getItemsExpiringOnOrBefore(LocalDate date)
    {
        if (date == null)
        {
            throw new IllegalArgumentException("date must not be null.");
        }
        String sql = "SELECT p.id, p.ingredient_id, p.quantity, p.unit_code, p.expiry_date, "
                + "p.created_at, p.updated_at, i.canonical_name FROM pantry_items p "
                + "JOIN ingredients i ON i.id = p.ingredient_id "
                + "WHERE p.expiry_date IS NOT NULL AND p.expiry_date <= ? "
                + "ORDER BY p.expiry_date, lower(i.canonical_name)";
        try (Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{date.toString()}))
        {
            List<PantryItem> items = new ArrayList<>();
            while (cursor.moveToNext())
            {
                PantryItem item = mapPantryItem(cursor);
                item.setIngredientName(cursor.getString(7));
                items.add(item);
            }
            return items;
        }
    }

    public List<MeasurementUnit> getAllUnits()
    {
        try (Cursor cursor = dbHelper.getReadableDatabase().query("measurement_units",
                new String[]{"unit_code", "display_name", "dimension", "to_base_factor"},
                null, null, null, null, "unit_code"))
        {
            List<MeasurementUnit> units = new ArrayList<>();
            while (cursor.moveToNext())
            {
                units.add(mapUnit(cursor));
            }
            return units;
        }
    }

    public MeasurementUnit getUnitByCode(String code)
    {
        String unitCode = requireUnitCode(code);
        try (Cursor cursor = dbHelper.getReadableDatabase().query("measurement_units",
                new String[]{"unit_code", "display_name", "dimension", "to_base_factor"},
                "unit_code = ?", new String[]{unitCode}, null, null, null))
        {
            return cursor.moveToFirst() ? mapUnit(cursor) : null;
        }
    }







    public List<Ingredient> getAllIngredients()
    {
        try (Cursor cursor = dbHelper.getReadableDatabase().query("ingredients",
                new String[]{"id", "canonical_name", "normalized_name", "created_at"},
                null, null, null, null, "id"))
        {
            List<Ingredient> ingredients = new ArrayList<>();
            while (cursor.moveToNext())
            {
                ingredients.add(mapIngredient(cursor));
            }
            return ingredients;
        }
    }







    public List<IngredientAlias> getAllAliases()
    {
        try (Cursor cursor = dbHelper.getReadableDatabase().query("ingredient_aliases",
                new String[]{"id", "ingredient_id", "normalized_alias", "created_at"},
                null, null, null, null, "id"))
        {
            List<IngredientAlias> aliases = new ArrayList<>();
            while (cursor.moveToNext())
            {
                aliases.add(new IngredientAlias(cursor.getInt(0), cursor.getInt(1),
                        cursor.getString(2), cursor.getString(3)));
            }
            return aliases;
        }
    }

    public boolean deleteIngredient(int ingredientId)
    {
        if (ingredientId <= 0)
        {
            throw new IllegalArgumentException("ingredientId must be positive.");
        }
        try
        {
            return dbHelper.getWritableDatabase().delete("ingredients", "id = ?",
                    new String[]{String.valueOf(ingredientId)}) > 0;
        }
        catch (SQLiteConstraintException exception)
        {
            throw new IllegalStateException("Ingredient is still referenced by pantry stock or a recipe.", exception);
        }
    }

    private long writePantryItem(SQLiteDatabase db, PantryItem item)
    {
        ContentValues values = new ContentValues();
        values.put("ingredient_id", item.getIngredientId());
        values.put("quantity", item.getQuantity());
        values.put("unit_code", item.getUnitCode());
        values.put("expiry_date", item.getExpiryDate() == null ? null : item.getExpiryDate().toString());
        if (item.getId() != null)
        {
            int changed = db.update("pantry_items", values, "id = ?", new String[]{String.valueOf(item.getId())});
            if (changed == 0)
            {
                throw new IllegalArgumentException("Pantry item does not exist.");
            }
            db.execSQL("UPDATE pantry_items SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    new Object[]{item.getId()});
            return item.getId();
        }
        long existingId = -1;
        try (Cursor cursor = db.query("pantry_items", new String[]{"id"}, "ingredient_id = ?",
                new String[]{String.valueOf(item.getIngredientId())}, null, null, null))
        {
            if (cursor.moveToFirst())
            {
                existingId = cursor.getLong(0);
            }
        }
        if (existingId >= 0)
        {
            db.update("pantry_items", values, "id = ?", new String[]{String.valueOf(existingId)});
            db.execSQL("UPDATE pantry_items SET updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    new Object[]{existingId});
            return existingId;
        }
        return db.insertOrThrow("pantry_items", null, values);
    }

    private PantryItem getPantryItem(SQLiteDatabase db, int id)
    {
        String sql = "SELECT p.id, p.ingredient_id, p.quantity, p.unit_code, p.expiry_date, "
                + "p.created_at, p.updated_at, i.canonical_name FROM pantry_items p "
                + "JOIN ingredients i ON i.id = p.ingredient_id WHERE p.id = ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)}))
        {
            if (!cursor.moveToFirst())
            {
                return null;
            }
            PantryItem item = mapPantryItem(cursor);
            item.setIngredientName(cursor.getString(7));
            return item;
        }
    }

    private PantryItem getPantryItemByIngredient(SQLiteDatabase db, int ingredientId)
    {
        try (Cursor cursor = db.query("pantry_items",
                new String[]{"id", "ingredient_id", "quantity", "unit_code", "expiry_date", "created_at", "updated_at"},
                "ingredient_id = ?", new String[]{String.valueOf(ingredientId)}, null, null, null))
        {
            return cursor.moveToFirst() ? mapPantryItem(cursor) : null;
        }
    }

    private Ingredient findIngredient(SQLiteDatabase db, String normalized)
    {
        try (Cursor cursor = db.query("ingredients",
                new String[]{"id", "canonical_name", "normalized_name", "created_at"},
                "normalized_name = ?", new String[]{normalized}, null, null, null))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    private Ingredient findAlias(SQLiteDatabase db, String normalized)
    {
        try (Cursor cursor = db.rawQuery("SELECT i.id, i.canonical_name, i.normalized_name, i.created_at "
                + "FROM ingredient_aliases a JOIN ingredients i ON i.id = a.ingredient_id "
                + "WHERE a.normalized_alias = ?", new String[]{normalized}))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    private Ingredient getIngredient(SQLiteDatabase db, long id)
    {
        try (Cursor cursor = db.query("ingredients",
                new String[]{"id", "canonical_name", "normalized_name", "created_at"},
                "id = ?", new String[]{String.valueOf(id)}, null, null, null))
        {
            return cursor.moveToFirst() ? mapIngredient(cursor) : null;
        }
    }

    private MeasurementUnit getUnit(SQLiteDatabase db, String code)
    {
        try (Cursor cursor = db.query("measurement_units",
                new String[]{"unit_code", "display_name", "dimension", "to_base_factor"},
                "unit_code = ?", new String[]{code}, null, null, null))
        {
            return cursor.moveToFirst() ? mapUnit(cursor) : null;
        }
    }

    private static Ingredient mapIngredient(Cursor cursor)
    {
        return new Ingredient(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
    }

    private static PantryItem mapPantryItem(Cursor cursor)
    {
        return new PantryItem(cursor.getInt(0), cursor.getInt(1), cursor.getDouble(2),
                cursor.getString(3), cursor.isNull(4) ? null : cursor.getString(4),
                cursor.getString(5), cursor.getString(6));
    }

    private static MeasurementUnit mapUnit(Cursor cursor)
    {
        return new MeasurementUnit(cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getDouble(3));
    }

    private static String requireName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("name must not be blank.");
        }
        return name.trim();
    }

    private static String requireUnitCode(String unitCode)
    {
        if (unitCode == null || unitCode.trim().isEmpty())
        {
            throw new IllegalArgumentException("unitCode must not be blank.");
        }
        return unitCode.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static void validateQuantity(double quantity)
    {
        if (!Double.isFinite(quantity) || quantity <= 0)
        {
            throw new IllegalArgumentException("quantity must be positive and finite.");
        }
    }

    private static final class ValidationDate
    {
        private static LocalDate parseOptional(String value)
        {
            if (value == null || value.trim().isEmpty())
            {
                return null;
            }
            String date = value.trim();
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                throw new IllegalArgumentException("expiryDate must be in YYYY-MM-DD format.");
            }
            try
            {
                return LocalDate.parse(date);
            }
            catch (RuntimeException exception)
            {
                throw new IllegalArgumentException("expiryDate must be a valid calendar date.", exception);
            }
        }
    }
}
