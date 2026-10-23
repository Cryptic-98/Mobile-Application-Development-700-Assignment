package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.logic.ValidationHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends BaseActivity
{
    private static final List<String> DIET_KEYS = Arrays.asList(
            "none", "vegetarian", "vegan", "pescatarian", "gluten_free", "dairy_free", "keto", "custom");
    private SettingsRepository settingsRepository;
    private final List<String> allergies = new ArrayList<>();
    private RadioGroup themeGroup;
    private RadioGroup allergyModeGroup;
    private EditText allergyAddInput;
    private EditText customDietInput;
    private Spinner dietChoice;
    private LinearLayout allergyList;
    private TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsRepository = new SettingsRepository(databaseHelper);
        themeGroup = findViewById(R.id.theme_choice_group);
        allergyModeGroup = findViewById(R.id.allergy_mode_group);
        allergyAddInput = findViewById(R.id.allergy_add_input);
        customDietInput = findViewById(R.id.custom_diet_input);
        dietChoice = findViewById(R.id.diet_choice);
        allergyList = findViewById(R.id.allergy_list);
        errorView = findViewById(R.id.settings_error);
        initializePreferences();
        findViewById(R.id.allergy_add_button).setOnClickListener(view -> addAllergy());
        themeGroup.setOnCheckedChangeListener((group, checkedId) ->
        {
            if (checkedId == R.id.light_theme_choice)
            {
                saveTheme("light");
            }
            else if (checkedId == R.id.dark_theme_choice)
            {
                saveTheme("dark");
            }
            else if (checkedId == R.id.system_theme_choice)
            {
                saveTheme("system");
            }
        });
        dietChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                customDietInput.setVisibility("custom".equals(DIET_KEYS.get(position))
                        ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                customDietInput.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.settings_back_button).setOnClickListener(view -> savePreferences());
        setupBottomNavigation(R.id.navigation_settings);
    }

    private void initializePreferences()
    {
        String theme = get("theme", "system");
        int themeChoiceId = "dark".equals(theme) ? R.id.dark_theme_choice
                : "light".equals(theme) ? R.id.light_theme_choice : R.id.system_theme_choice;
        themeGroup.check(themeChoiceId);

        allergies.clear();
        allergies.addAll(AllergyListCodec.decode(get("allergies", "")));
        renderAllergies();
        allergyModeGroup.check("show".equals(get("allergy_mode", "hide"))
                ? R.id.allergy_flag_choice : R.id.allergy_hide_choice);

        String savedDiet = get("diet", "none");
        int dietPosition = DIET_KEYS.indexOf(savedDiet);
        dietChoice.setSelection(dietPosition < 0 ? 0 : dietPosition);
        customDietInput.setText(get("custom_diet_terms", ""));
        customDietInput.setVisibility("custom".equals(savedDiet) ? View.VISIBLE : View.GONE);
    }

    private void addAllergy()
    {
        String value = allergyAddInput.getText().toString().trim();
        if (value.isEmpty())
        {
            return;
        }
        if (!AllergyListCodec.addIfUnique(allergies, value))
        {
            errorView.setText(R.string.duplicate_allergy);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        allergyAddInput.setText("");
        errorView.setVisibility(View.GONE);
        renderAllergies();
    }

    private void renderAllergies()
    {
        allergyList.removeAllViews();
        allergyList.setVisibility(allergies.isEmpty() ? View.GONE : View.VISIBLE);
        for (String allergy : allergies)
        {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView name = new TextView(this);
            name.setText(allergy);
            name.setTextAppearance(this, R.style.TextAppearance_SmartPantryManager_Body);
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            com.google.android.material.button.MaterialButton remove =
                    new com.google.android.material.button.MaterialButton(this);
            remove.setText(R.string.remove);
            remove.setContentDescription(getString(R.string.remove_allergy, allergy));
            remove.setOnClickListener(view ->
            {
                allergies.remove(allergy);
                renderAllergies();
            });
            row.addView(name);
            row.addView(remove);
            allergyList.addView(row);
        }
    }

    public String get(String key, String defaultValue)
    {
        return settingsRepository.get(key, defaultValue);
    }

    public void set(String key, String value)
    {
        if ("theme".equals(key))
        {
            value = ValidationHelper.requireTheme(value);
        }
        settingsRepository.set(key, value);
    }

    public void saveTheme(String theme)
    {
        errorView.setVisibility(View.GONE);
        try
        {
            String normalized = ValidationHelper.requireTheme(theme);
            set("theme", normalized);
            applyTheme(normalized);
        }
        catch (IllegalArgumentException exception)
        {
            errorView.setText(exception.getMessage());
            errorView.setVisibility(View.VISIBLE);
        }
    }

    private void savePreferences()
    {
        errorView.setVisibility(View.GONE);
        int dietPosition = dietChoice.getSelectedItemPosition();
        if (dietPosition < 0 || dietPosition >= DIET_KEYS.size())
        {
            errorView.setText(R.string.settings_error_message);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        String diet = DIET_KEYS.get(dietPosition);
        String customTerms = customDietInput.getText().toString().trim();
        if ("custom".equals(diet) && customTerms.isEmpty())
        {
            errorView.setText(R.string.custom_diet_required);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        try
        {
            set("allergies", AllergyListCodec.encode(allergies));
            set("allergy_mode", allergyModeGroup.getCheckedRadioButtonId() == R.id.allergy_flag_choice
                    ? "show" : "hide");
            set("diet", diet);
            set("custom_diet_terms", customTerms);
            applyTheme(get("theme", "system"));
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
        catch (IllegalArgumentException exception)
        {
            errorView.setText(exception.getMessage());
            errorView.setVisibility(View.VISIBLE);
        }
    }

    private static void applyTheme(String theme)
    {
        int nightMode = "dark".equals(theme) ? AppCompatDelegate.MODE_NIGHT_YES
                : "light".equals(theme) ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
