package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Arrays;
import java.util.List;

public class DietOnboardingFragment extends Fragment
{
    private static final List<String> DIET_KEYS = Arrays.asList(
            "none", "vegetarian", "vegan", "pescatarian", "gluten_free", "dairy_free", "keto", "custom");
    private OnboardingActivity activity;
    private EditText customDiet;
    private TextView errorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_onboarding_diet, container, false);
        activity = (OnboardingActivity) requireActivity();
        customDiet = view.findViewById(R.id.onboarding_custom_diet);
        errorView = view.findViewById(R.id.onboarding_diet_error);
        customDiet.setText(activity.getCustomDietTerms());
        Spinner dietChoice = view.findViewById(R.id.onboarding_diet_choice);
        int selectedPosition = DIET_KEYS.indexOf(activity.getDietChoice());
        dietChoice.setSelection(selectedPosition < 0 ? 0 : selectedPosition);
        customDiet.setVisibility("custom".equals(activity.getDietChoice()) ? View.VISIBLE : View.GONE);
        dietChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View item, int position, long id)
            {
                String key = DIET_KEYS.get(position);
                activity.setDietChoice(key);
                customDiet.setVisibility("custom".equals(key) ? View.VISIBLE : View.GONE);
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                customDiet.setVisibility(View.GONE);
            }
        });
        customDiet.addTextChangedListener(new android.text.TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count)
            {
                activity.setCustomDietTerms(text.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable text)
            {
            }
        });
        return view;
    }

    public void showValidationError()
    {
        errorView.setText(R.string.custom_diet_required);
        errorView.setVisibility(View.VISIBLE);
    }
}
