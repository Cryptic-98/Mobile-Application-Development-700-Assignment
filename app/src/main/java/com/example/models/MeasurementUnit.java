package com.example.smartpantrymanager.models;

public class MeasurementUnit
{
    private String unitCode;
    private String displayName;
    private String dimension;
    private double toBaseFactor;

    public MeasurementUnit(String unitCode, String displayName, String dimension, double toBaseFactor)
    {
        setUnitCode(unitCode);
        setDisplayName(displayName);
        setDimension(dimension);
        setToBaseFactor(toBaseFactor);
    }

    public String getUnitCode()
    {
        return unitCode;
    }

    public void setUnitCode(String unitCode)
    {
        if (unitCode == null || unitCode.trim().isEmpty())
        {
            throw new IllegalArgumentException("unitCode must not be blank.");
        }
        this.unitCode = unitCode.trim();
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        if (displayName == null || displayName.trim().isEmpty())
        {
            throw new IllegalArgumentException("displayName must not be blank.");
        }
        this.displayName = displayName.trim();
    }

    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        if (dimension == null || dimension.trim().isEmpty())
        {
            throw new IllegalArgumentException("dimension must not be blank.");
        }
        this.dimension = dimension.trim();
    }

    public double getToBaseFactor()
    {
        return toBaseFactor;
    }

    public void setToBaseFactor(double toBaseFactor)
    {
        if (!Double.isFinite(toBaseFactor) || toBaseFactor <= 0)
        {
            throw new IllegalArgumentException("toBaseFactor must be positive and finite.");
        }
        this.toBaseFactor = toBaseFactor;
    }
}
