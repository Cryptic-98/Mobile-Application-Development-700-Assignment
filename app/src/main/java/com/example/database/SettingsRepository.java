package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.smartpantrymanager.models.AppSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsRepository
{
    private final DatabaseHelper dbHelper;

    public SettingsRepository(DatabaseHelper dbHelper)
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

    public String get(String key, String defaultValue)
    {
        String settingKey = validateKey(key);
        try (Cursor cursor = dbHelper.getReadableDatabase().query("app_settings",
                new String[]{"setting_value"}, "setting_key = ?",
                new String[]{settingKey}, null, null, null))
        {
            return cursor.moveToFirst() ? cursor.getString(0) : defaultValue;
        }
    }

    public void set(String key, String value)
    {
        String settingKey = validateKey(key);
        if (value == null)
        {
            throw new IllegalArgumentException("value must not be null.");
        }
        String storedValue = value;
        if ("theme".equals(settingKey))
        {
            storedValue = value.trim().toLowerCase(Locale.ROOT);
            if (!"light".equals(storedValue) && !"dark".equals(storedValue)
                    && !"system".equals(storedValue))
            {
                throw new IllegalArgumentException("Theme must be light, dark, or system.");
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            boolean exists;
            try (Cursor cursor = db.query("app_settings", new String[]{"setting_key"},
                    "setting_key = ?", new String[]{settingKey}, null, null, null))
            {
                exists = cursor.moveToFirst();
            }
            if (exists)
            {
                db.execSQL("UPDATE app_settings SET setting_value = ?, "
                                + "updated_at = CURRENT_TIMESTAMP WHERE setting_key = ?",
                        new Object[]{storedValue, settingKey});
            }
            else
            {
                ContentValues values = new ContentValues();
                values.put("setting_key", settingKey);
                values.put("setting_value", storedValue);
                db.insertOrThrow("app_settings", null, values);
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public List<AppSettings> getAll()
    {
        try (Cursor cursor = dbHelper.getReadableDatabase().query("app_settings",
                new String[]{"setting_key", "setting_value", "updated_at"},
                null, null, null, null, "setting_key"))
        {
            List<AppSettings> settings = new ArrayList<>();
            while (cursor.moveToNext())
            {
                settings.add(new AppSettings(cursor.getString(0), cursor.getString(1), cursor.getString(2)));
            }
            return settings;
        }
    }

    private static String validateKey(String key)
    {
        if (key == null || key.trim().isEmpty())
        {
            throw new IllegalArgumentException("key must not be blank.");
        }
        return key.trim();
    }
}
