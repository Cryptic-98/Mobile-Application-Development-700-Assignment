package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.logic.PantryManager;
import com.example.smartpantrymanager.models.MeasurementUnit;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddEditIngredientActivity extends BaseActivity
{
    public static final String EXTRA_PANTRY_ITEM_ID = "pantry_item_id";
    private PantryManager pantryManager;
    private EditText nameInput;
    private EditText quantityInput;
    private EditText expiryInput;
    private Spinner unitSpinner;
    private TextView heading;
    private TextView errorView;
    private View deleteButton;
    private Integer pantryItemId;
    private List<MeasurementUnit> units;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        pantryManager = new PantryManager(new PantryRepository(databaseHelper));
        nameInput = findViewById(R.id.ingredient_name_input);
        quantityInput = findViewById(R.id.quantity_input);
        expiryInput = findViewById(R.id.expiry_input);
        unitSpinner = findViewById(R.id.unit_spinner);
        heading = findViewById(R.id.form_heading);
        errorView = findViewById(R.id.form_error);
        deleteButton = findViewById(R.id.delete_ingredient_button);
        initializeUnits();
        initializeEditMode(savedInstanceState);
        expiryInput.setShowSoftInputOnFocus(false);
        expiryInput.setOnClickListener(view -> showDatePicker());
        findViewById(R.id.save_ingredient_button).setOnClickListener(view -> saveFromForm());
        findViewById(R.id.cancel_ingredient_button).setOnClickListener(view -> finish());
        deleteButton.setOnClickListener(view -> confirmDelete());
    }

    public PantryItem save(String name, double quantity, String unitCode, String expiryDate)
    {
        PantryItem saved = pantryItemId == null
                ? pantryManager.addStock(name, quantity, unitCode, expiryDate)
                : pantryManager.updateStock(pantryItemId, name, quantity, unitCode, expiryDate);
        setResult(RESULT_OK);
        finish();
        return saved;
    }

    public boolean delete()
    {
        if (pantryItemId == null)
        {
            throw new IllegalStateException("There is no pantry item to delete.");
        }
        boolean deleted = pantryManager.removeStock(pantryItemId);
        if (deleted)
        {
            setResult(RESULT_OK);
            finish();
        }
        return deleted;
    }

    private void initializeUnits()
    {
        units = new PantryRepository(databaseHelper).getAllUnits();
        List<String> labels = new ArrayList<>();
        for (MeasurementUnit unit : units)
        {
            labels.add(unit.getDisplayName() + " (" + unit.getUnitCode() + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
    }

    private void initializeEditMode(Bundle savedInstanceState)
    {
        int itemId = getIntent().getIntExtra(EXTRA_PANTRY_ITEM_ID, 0);
        if (!getIntent().hasExtra(EXTRA_PANTRY_ITEM_ID))
        {
            return;
        }
        if (itemId <= 0)
        {
            showError(getString(R.string.save_error));
            findViewById(R.id.save_ingredient_button).setEnabled(false);
            return;
        }
        pantryItemId = itemId;
        PantryItem item = pantryManager.getItem(itemId);
        if (item == null)
        {
            showError(getString(R.string.save_error));
            findViewById(R.id.save_ingredient_button).setEnabled(false);
            return;
        }
        heading.setText(R.string.edit_ingredient_title);
        deleteButton.setVisibility(View.VISIBLE);
        nameInput.setText(item.getIngredientName());
        quantityInput.setText(String.format(Locale.ROOT, "%s", item.getQuantity()));
        expiryInput.setText(item.getExpiryDate() == null ? "" : item.getExpiryDate().toString());
        int unitIndex = 0;
        for (int index = 0; index < units.size(); index++)
        {
            if (units.get(index).getUnitCode().equals(item.getUnitCode()))
            {
                unitIndex = index;
                break;
            }
        }
        unitSpinner.setSelection(unitIndex);
    }

    private void saveFromForm()
    {
        errorView.setVisibility(View.GONE);
        String name = nameInput.getText().toString();
        String quantity = quantityInput.getText().toString().trim();
        if (quantity.isEmpty())
        {
            showError(getString(R.string.invalid_quantity));
            return;
        }
        try
        {
            save(name, Double.parseDouble(quantity), units.get(unitSpinner.getSelectedItemPosition()).getUnitCode(),
                    expiryInput.getText().toString());
        }
        catch (IllegalArgumentException exception)
        {
            showError(isInvalidExpiry(expiryInput.getText().toString())
                    ? getString(R.string.invalid_expiry) : getString(R.string.save_error));
        }
        catch (SQLiteException exception)
        {
            showError(getString(R.string.save_error));
        }
    }

    private void confirmDelete()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                {
                    try
                    {
                        if (!delete())
                        {
                            showError(getString(R.string.delete_error));
                        }
                    }
                    catch (IllegalArgumentException | IllegalStateException exception)
                    {
                        showError(getString(R.string.delete_error));
                    }
                })
                .show();
    }

    private void showDatePicker()
    {
        LocalDate initial = ValidationDate.tryParse(expiryInput.getText().toString());
        LocalDate date = initial == null ? LocalDate.now() : initial;
        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) ->
        {
            expiryInput.setText(String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day));
        }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.cancel), (picker, which) ->
                expiryInput.setText(""));
        dialog.show();
    }

    private void showError(String message)
    {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private static boolean isInvalidExpiry(String value)
    {
        try
        {
            com.example.smartpantrymanager.logic.ValidationHelper.parseOptionalDate(value);
            return false;
        }
        catch (IllegalArgumentException exception)
        {
            return true;
        }
    }

    private static final class ValidationDate
    {
        private static LocalDate tryParse(String value)
        {
            try
            {
                return com.example.smartpantrymanager.logic.ValidationHelper.parseOptionalDate(value);
            }
            catch (IllegalArgumentException exception)
            {
                return null;
            }
        }
    }
}
