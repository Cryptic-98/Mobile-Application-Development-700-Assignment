package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class AllergyOnboardingFragment extends Fragment
{
    private OnboardingActivity activity;
    private LinearLayout allergyList;
    private EditText allergyInput;
    private TextView errorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_onboarding_allergies, container, false);
        activity = (OnboardingActivity) requireActivity();
        allergyList = view.findViewById(R.id.onboarding_allergy_list);
        allergyInput = view.findViewById(R.id.onboarding_allergy_input);
        errorView = view.findViewById(R.id.onboarding_allergy_error);
        RadioGroup mode = view.findViewById(R.id.onboarding_allergy_mode);
        mode.check("show".equals(activity.getAllergyMode())
                ? R.id.onboarding_flag_allergy : R.id.onboarding_hide_allergy);
        mode.setOnCheckedChangeListener((group, checkedId) ->
                activity.setAllergyMode(checkedId == R.id.onboarding_flag_allergy ? "show" : "hide"));
        view.findViewById(R.id.onboarding_add_allergy).setOnClickListener(button -> addAllergy());
        renderAllergies();
        return view;
    }

    private void addAllergy()
    {
        String value = allergyInput.getText().toString().trim();
        if (value.isEmpty())
        {
            return;
        }
        if (!activity.addAllergy(value))
        {
            errorView.setText(R.string.allergy_duplicate_onboarding);
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        allergyInput.setText("");
        errorView.setVisibility(View.GONE);
        renderAllergies();
    }

    private void renderAllergies()
    {
        allergyList.removeAllViews();
        for (String allergy : activity.getAllergies())
        {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView name = new TextView(requireContext());
            name.setText(allergy);
            name.setTextAppearance(requireContext(), R.style.TextAppearance_SmartPantryManager_Body);
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            MaterialButton remove = new MaterialButton(requireContext());
            remove.setText(R.string.remove);
            remove.setContentDescription(getString(R.string.remove_allergy, allergy));
            remove.setOnClickListener(button ->
            {
                activity.removeAllergy(allergy);
                renderAllergies();
            });
            row.addView(name);
            row.addView(remove);
            allergyList.addView(row);
        }
        allergyList.setVisibility(activity.getAllergies().isEmpty() ? View.GONE : View.VISIBLE);
    }
}
