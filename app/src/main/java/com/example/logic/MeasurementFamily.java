package com.example.smartpantrymanager.logic;

import java.util.Locale;













public enum MeasurementFamily
{

    MASS("mass", "g"),


    VOLUME("volume", "ml"),


    COUNT("count", "piece");

    private final String dimension;
    private final String baseUnitCode;

    MeasurementFamily(String dimension, String baseUnitCode)
    {
        this.dimension = dimension;
        this.baseUnitCode = baseUnitCode;
    }




    public String getDimension()
    {
        return dimension;
    }




    public String getBaseUnitCode()
    {
        return baseUnitCode;
    }





    public static MeasurementFamily forDimension(String dimension)
    {
        if (dimension == null)
        {
            return null;
        }
        switch (dimension.trim().toLowerCase(Locale.ROOT))
        {
            case "mass":
            case "weight":
                return MASS;
            case "volume":
                return VOLUME;
            case "piece":
            case "slice":
            case "clove":
            case "can":
            case "count":
                return COUNT;
            default:
                return null;
        }
    }
}
