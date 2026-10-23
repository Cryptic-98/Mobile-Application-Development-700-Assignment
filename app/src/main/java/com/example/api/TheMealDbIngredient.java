package com.example.smartpantrymanager.api;

public final class TheMealDbIngredient
{
    private final String name;
    private final String measure;
    private final Double quantity;
    private final String unitCode;

    public TheMealDbIngredient(String name, String measure, Double quantity, String unitCode)
    {
        this.name = name;
        this.measure = measure;
        this.quantity = quantity;
        this.unitCode = unitCode;
    }

    public String getName()
    {
        return name;
    }

    public String getMeasure()
    {
        return measure;
    }

    public Double getQuantity()
    {
        return quantity;
    }

    public String getUnitCode()
    {
        return unitCode;
    }
}
