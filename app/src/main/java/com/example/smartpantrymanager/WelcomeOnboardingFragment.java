package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WelcomeOnboardingFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);
        OnboardingActivity activity = (OnboardingActivity) requireActivity();
        RadioGroup themeGroup = view.findViewById(R.id.onboarding_theme_group);
        String theme = activity.getThemeChoice();
        themeGroup.check("light".equals(theme) ? R.id.onboarding_light_theme
                : "dark".equals(theme) ? R.id.onboarding_dark_theme : R.id.onboarding_system_theme);
        themeGroup.setOnCheckedChangeListener((group, checkedId) ->
        {
            if (checkedId == R.id.onboarding_light_theme)
            {
                activity.setThemeChoice("light");
            }
            else if (checkedId == R.id.onboarding_dark_theme)
            {
                activity.setThemeChoice("dark");
            }
            else if (checkedId == R.id.onboarding_system_theme)
            {
                activity.setThemeChoice("system");
            }
        });
        return view;
    }
}
