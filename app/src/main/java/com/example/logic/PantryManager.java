package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.database.SettingsRepository;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PantryManager
{
    private static final int DEFAULT_EXPIRY_THRESHOLD_DAYS = 3;
    private final PantryRepository pantryRepository;
    private final SettingsRepository settingsRepository;

    public PantryManager(PantryRepository pantryRepository)
    {
        this(pantryRepository, null);
    }

    public PantryManager(PantryRepository pantryRepository, SettingsRepository settingsRepository)
    {
        if (pantryRepository == null)
        {
            throw new IllegalArgumentException("pantryRepository must not be null.");
        }
        this.pantryRepository = pantryRepository;
        this.settingsRepository = settingsRepository;
    }

    public PantryItem addStock(String name, double quantity, String unitCode, String expiryDate)
    {
        ValidationHelper.requireText(name, "name");
        ValidationHelper.requirePositiveQuantity(quantity, "quantity");
        String unit = ValidationHelper.requireUnitCode(unitCode);
        ValidationHelper.parseOptionalDate(expiryDate);
        if (pantryRepository.getUnitByCode(unit) == null)
        {
            throw new IllegalArgumentException("Unknown unit code: " + unit);
        }
        return pantryRepository.addOrMergePantryItem(name, quantity, unit, expiryDate);
    }

    public PantryItem updateStock(int itemId, String name, double quantity, String unitCode, String expiryDate)
    {
        ValidationHelper.requireText(name, "name");
        ValidationHelper.requirePositiveQuantity(quantity, "quantity");
        String unit = ValidationHelper.requireUnitCode(unitCode);
        ValidationHelper.parseOptionalDate(expiryDate);
        if (pantryRepository.getUnitByCode(unit) == null)
        {
            throw new IllegalArgumentException("Unknown unit code: " + unit);
        }
        return pantryRepository.updatePantryItem(itemId, name, quantity, unit, expiryDate);
    }

    public boolean removeStock(int itemId)
    {
        return pantryRepository.deletePantryItem(itemId);
    }

    public PantryItem getItem(int id)
    {
        return pantryRepository.getPantryItemById(id);
    }

    public List<PantryItem> getItems()
    {
        return pantryRepository.getAllPantryItems();
    }

    public List<PantryItem> getItemsExpiringSoon()
    {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(getExpiryThresholdDays());
        List<PantryItem> items = pantryRepository.getItemsExpiringOnOrBefore(lastDay);
        List<PantryItem> soon = new ArrayList<>();
        for (PantryItem item : items)
        {
            if (item.getExpiryDate() != null && !item.getExpiryDate().isBefore(today))
            {
                soon.add(item);
            }
        }
        return soon;
    }

    public List<PantryItem> getExpiredItems()
    {
        List<PantryItem> items = pantryRepository.getItemsExpiringOnOrBefore(LocalDate.now().minusDays(1));
        return items;
    }

    private int getExpiryThresholdDays()
    {
        if (settingsRepository == null)
        {
            return DEFAULT_EXPIRY_THRESHOLD_DAYS;
        }
        String setting = settingsRepository.get("expiring_soon_days", null);
        if (setting == null)
        {
            return DEFAULT_EXPIRY_THRESHOLD_DAYS;
        }
        try
        {
            int days = Integer.parseInt(setting);
            return days >= 0 ? days : DEFAULT_EXPIRY_THRESHOLD_DAYS;
        }
        catch (NumberFormatException exception)
        {
            return DEFAULT_EXPIRY_THRESHOLD_DAYS;
        }
    }
}
