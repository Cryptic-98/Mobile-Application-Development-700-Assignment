package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.database.sqlite.SQLiteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.database.RecipeRepository;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.logic.PantryManager;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity
{
    private PantryManager pantryManager;
    private LinearLayout pantryContainer;
    private LinearLayout expiringContainer;
    private TextView pantryEmpty;
    private TextView expiringEmpty;
    private TextView pantryError;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        SettingsRepository settings = new SettingsRepository(databaseHelper);
        if (!"true".equals(settings.get("onboarding_complete", "false"))
                && !hasExistingUserData())
        {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        settings.set("onboarding_complete", "true");
        new PresetRecipeSeeder(new PantryRepository(databaseHelper),
                new RecipeRepository(databaseHelper)).seed();
        setContentView(R.layout.activity_main);
        pantryManager = new PantryManager(new PantryRepository(databaseHelper),
                new SettingsRepository(databaseHelper));
        pantryContainer = findViewById(R.id.pantry_items_container);
        expiringContainer = findViewById(R.id.expiring_items_container);
        pantryEmpty = findViewById(R.id.pantry_empty);
        expiringEmpty = findViewById(R.id.expiring_empty);
        pantryError = findViewById(R.id.pantry_error);
        findViewById(R.id.add_ingredient_button).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        setupBottomNavigation(R.id.navigation_pantry);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        loadPantryItems();
    }

    public List<PantryItem> loadPantryItems()
    {
        try
        {
            List<PantryItem> items = pantryManager.getItems();
            renderItems(items, pantryContainer, pantryEmpty);
            renderItems(loadItemsExpiringSoon(), expiringContainer, expiringEmpty);
            pantryError.setVisibility(View.GONE);
            return items;
        }
        catch (SQLiteException exception)
        {
            pantryContainer.removeAllViews();
            expiringContainer.removeAllViews();
            pantryEmpty.setVisibility(View.GONE);
            expiringEmpty.setVisibility(View.GONE);
            pantryError.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.data_load_error, Toast.LENGTH_LONG).show();
            return java.util.Collections.emptyList();
        }
    }

    private boolean hasExistingUserData()
    {
        long pantryItems = DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(),
                "SELECT COUNT(*) FROM pantry_items", null);
        long recipes = DatabaseUtils.longForQuery(databaseHelper.getReadableDatabase(),
                "SELECT COUNT(*) FROM recipes", null);
        return pantryItems > 0 || recipes > 0;
    }

    public List<PantryItem> loadItemsExpiringSoon()
    {
        return pantryManager.getItemsExpiringSoon();
    }

    public List<PantryItem> loadExpiredItems()
    {
        return pantryManager.getExpiredItems();
    }

    private void renderItems(List<PantryItem> items, LinearLayout container, TextView emptyView)
    {
        container.removeAllViews();
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (PantryItem item : items)
        {
            View row = inflater.inflate(R.layout.item_pantry, container, false);
            TextView name = row.findViewById(R.id.pantry_item_name);
            TextView quantity = row.findViewById(R.id.pantry_item_quantity);
            TextView expiry = row.findViewById(R.id.pantry_item_expiry);
            name.setText(item.getIngredientName());
            quantity.setText(String.format(Locale.getDefault(), "%s %s",
                    formatQuantity(item.getQuantity()), item.getUnitCode()));
            if (item.getExpiryDate() == null)
            {
                expiry.setText(R.string.no_expiry);
            }
            else if (item.getExpiryDate().isBefore(LocalDate.now()))
            {
                expiry.setText(getString(R.string.expiry_expired, item.getExpiryDate()));
                expiry.setTextColor(getColor(R.color.color_error));
            }
            else if (item.getExpiryDate().isEqual(LocalDate.now()))
            {
                expiry.setText(R.string.expiry_today);
                expiry.setTextColor(getColor(R.color.color_error));
            }
            else
            {
                expiry.setText(getString(R.string.expires_on, item.getExpiryDate()));
                expiry.setTextColor(getColor(R.color.color_text_secondary));
            }
            row.setContentDescription(item.getIngredientName() + ", "
                    + quantity.getText() + ", " + expiry.getText());
            row.setOnClickListener(view ->
            {
                Intent intent = new Intent(this, AddEditIngredientActivity.class);
                intent.putExtra(AddEditIngredientActivity.EXTRA_PANTRY_ITEM_ID, item.getId());
                startActivity(intent);
            });
            container.addView(row);
        }
    }

    private static String formatQuantity(double quantity)
    {
        if (quantity == Math.rint(quantity))
        {
            return String.format(Locale.getDefault(), "%.0f", quantity);
        }
        return String.format(Locale.getDefault(), "%.2f", quantity);
    }
}
