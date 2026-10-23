package com.example.smartpantrymanager.logic;








public enum IngredientAvailability
{

    AVAILABLE,


    INSUFFICIENT_QUANTITY,


    EXPIRED,


    INCOMPATIBLE_UNIT,


    UNVERIFIABLE,


    MISSING;




    public boolean isSatisfied()
    {
        return this == AVAILABLE;
    }
}
