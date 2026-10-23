package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity
{
    protected DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        databaseHelper = new DatabaseHelper(this);
        applySavedTheme();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        View root = findViewById(R.id.screen_root);
        if (root != null)
        {
            applySystemBarInsets(root);
        }
    }

    protected void applySavedTheme()
    {
        String theme = new SettingsRepository(databaseHelper).get("theme", "system");
        int nightMode = "dark".equals(theme) ? AppCompatDelegate.MODE_NIGHT_YES
                : "light".equals(theme) ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    protected void setupBottomNavigation(int selectedItemId)
    {
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        if (navigation == null)
        {
            return;
        }
        navigation.setSelectedItemId(selectedItemId);
        navigation.setOnItemSelectedListener(item ->
        {
            if (item.getItemId() == selectedItemId)
            {
                return true;
            }
            Class<?> destination;
            if (item.getItemId() == R.id.navigation_pantry)
            {
                destination = MainActivity.class;
            }
            else if (item.getItemId() == R.id.navigation_recipes)
            {
                destination = SuggestedRecipesActivity.class;
            }
            else if (item.getItemId() == R.id.navigation_settings)
            {
                destination = SettingsActivity.class;
            }
            else
            {
                return false;
            }
            Intent intent = new Intent(this, destination);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void applySystemBarInsets(View root)
    {
        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) ->
        {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(initialLeft + insets.left, initialTop + insets.top,
                    initialRight + insets.right, initialBottom + insets.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
        boolean isDark = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), root);
        controller.setAppearanceLightStatusBars(!isDark);
        controller.setAppearanceLightNavigationBars(!isDark);
    }

    @Override
    protected void onDestroy()
    {
        if (databaseHelper != null)
        {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
