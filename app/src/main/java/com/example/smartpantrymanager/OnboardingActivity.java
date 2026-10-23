package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends BaseActivity
{
    private static final String STATE_PAGE = "page";
    private static final String STATE_THEME = "theme";
    private static final String STATE_ALLERGIES = "allergies";
    private static final String STATE_ALLERGY_MODE = "allergy_mode";
    private static final String STATE_DIET = "diet";
    private static final String STATE_CUSTOM_DIET = "custom_diet";
    private static final List<String> DIET_KEYS = Arrays.asList(
            "none", "vegetarian", "vegan", "pescatarian", "gluten_free", "dairy_free", "keto", "custom");

    private final List<String> allergies = new ArrayList<>();
    private SettingsRepository settingsRepository;
    private int page;
    private String theme;
    private String allergyMode;
    private String diet;
    private String customDietTerms;
    private TextView stepStatus;
    private MaterialButton backButton;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        settingsRepository = new SettingsRepository(databaseHelper);
        setContentView(R.layout.activity_onboarding);
        stepStatus = findViewById(R.id.onboarding_step_status);
        backButton = findViewById(R.id.onboarding_back_button);
        nextButton = findViewById(R.id.onboarding_next_button);
        if (savedInstanceState == null)
        {
            theme = settingsRepository.get("theme", "system");
            allergies.addAll(AllergyListCodec.decode(settingsRepository.get("allergies", "")));
            allergyMode = settingsRepository.get("allergy_mode", "hide");
            diet = settingsRepository.get("diet", "none");
            customDietTerms = settingsRepository.get("custom_diet_terms", "");
            showPage(0);
        }
        else
        {
            page = savedInstanceState.getInt(STATE_PAGE, 0);
            theme = savedInstanceState.getString(STATE_THEME, "system");
            allergies.addAll(AllergyListCodec.decode(savedInstanceState.getString(STATE_ALLERGIES, "")));
            allergyMode = savedInstanceState.getString(STATE_ALLERGY_MODE, "hide");
            diet = savedInstanceState.getString(STATE_DIET, "none");
            customDietTerms = savedInstanceState.getString(STATE_CUSTOM_DIET, "");
            showPage(page);
        }
        backButton.setOnClickListener(view -> showPage(page - 1));
        nextButton.setOnClickListener(view ->
        {
            if (page < 2)
            {
                showPage(page + 1);
            }
            else
            {
                finishOnboarding();
            }
        });
        updateNavigation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(STATE_PAGE, page);
        outState.putString(STATE_THEME, theme);
        outState.putString(STATE_ALLERGIES, AllergyListCodec.encode(allergies));
        outState.putString(STATE_ALLERGY_MODE, allergyMode);
        outState.putString(STATE_DIET, diet);
        outState.putString(STATE_CUSTOM_DIET, customDietTerms);
        super.onSaveInstanceState(outState);
    }

    public String getThemeChoice()
    {
        return theme;
    }

    public void setThemeChoice(String value)
    {
        theme = value;
    }

    public List<String> getAllergies()
    {
        return new ArrayList<>(allergies);
    }

    public boolean addAllergy(String value)
    {
        return AllergyListCodec.addIfUnique(allergies, value);
    }

    public void removeAllergy(String value)
    {
        allergies.remove(value);
    }

    public String getAllergyMode()
    {
        return allergyMode;
    }

    public void setAllergyMode(String value)
    {
        allergyMode = value;
    }

    public String getDietChoice()
    {
        return diet;
    }

    public void setDietChoice(String value)
    {
        diet = value;
    }

    public String getCustomDietTerms()
    {
        return customDietTerms;
    }

    public void setCustomDietTerms(String value)
    {
        customDietTerms = value;
    }

    private void showPage(int newPage)
    {
        if (newPage < 0 || newPage > 2)
        {
            return;
        }
        page = newPage;
        Fragment fragment;
        if (page == 0)
        {
            fragment = new WelcomeOnboardingFragment();
        }
        else if (page == 1)
        {
            fragment = new AllergyOnboardingFragment();
        }
        else
        {
            fragment = new DietOnboardingFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.onboarding_fragment_container, fragment)
                .commit();
        updateNavigation();
    }

    private void updateNavigation()
    {
        backButton.setVisibility(page == 0 ? View.GONE : View.VISIBLE);
        nextButton.setText(page == 2 ? R.string.get_started : R.string.next);
        stepStatus.setText(getString(R.string.onboarding_step, page + 1));
    }

    private void finishOnboarding()
    {
        if ("custom".equals(diet) && (customDietTerms == null || customDietTerms.trim().isEmpty()))
        {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentById(R.id.onboarding_fragment_container);
            if (fragment instanceof DietOnboardingFragment)
            {
                ((DietOnboardingFragment) fragment).showValidationError();
            }
            return;
        }
        try
        {
            settingsRepository.set("theme", theme);
            settingsRepository.set("allergies", AllergyListCodec.encode(allergies));
            settingsRepository.set("allergy_mode", allergyMode);
            settingsRepository.set("diet", diet);
            settingsRepository.set("custom_diet_terms", customDietTerms == null ? "" : customDietTerms.trim());
            settingsRepository.set("onboarding_complete", "true");
            AppCompatDelegate.setDefaultNightMode("dark".equals(theme)
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : "light".equals(theme) ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        catch (IllegalArgumentException exception)
        {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
        catch (android.database.sqlite.SQLiteException exception)
        {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_LONG).show();
        }
    }
}
