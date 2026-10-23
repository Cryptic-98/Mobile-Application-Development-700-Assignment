package com.example.smartpantrymanager.models;

import java.util.Objects;

public class RecipeIngredient {
    private Integer id;
    private int recipeId;
    private int ingredientId;
    private double requiredQuantity;
    private String unitCode;
    private boolean optional;
    private String ingredientNote;
    private String ingredientName;

    public RecipeIngredient(int recipeId, int ingredientId, double requiredQuantity,
                            String unitCode, boolean optional, String ingredientNote) {
        setRecipeId(recipeId);
        setIngredientId(ingredientId);
        setRequiredQuantity(requiredQuantity);
        setUnitCode(unitCode);
        setOptional(optional);
        setIngredientNote(ingredientNote);
    }

    public RecipeIngredient(int recipeId, int ingredientId, double requiredQuantity, String unitCode) {
        this(recipeId, ingredientId, requiredQuantity, unitCode, false, null);
    }

    public RecipeIngredient(Integer id, int recipeId, int ingredientId, double requiredQuantity,
                            String unitCode, boolean optional, String ingredientNote) {
        this(recipeId, ingredientId, requiredQuantity, unitCode, optional, ingredientNote);
        setId(id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive.");
        this.id = id;
    }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) {
        if (recipeId <= 0) throw new IllegalArgumentException("recipeId must be positive.");
        this.recipeId = recipeId;
    }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) {
        if (ingredientId <= 0) throw new IllegalArgumentException("ingredientId must be positive.");
        this.ingredientId = ingredientId;
    }

    public double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(double requiredQuantity) {
        if (Double.isNaN(requiredQuantity) || Double.isInfinite(requiredQuantity) || requiredQuantity <= 0) {
            throw new IllegalArgumentException("requiredQuantity must be a finite number greater than 0.");
        }
        this.requiredQuantity = requiredQuantity;
    }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) {
        if (unitCode == null || unitCode.trim().isEmpty()) {
            throw new IllegalArgumentException("unitCode must not be null or blank.");
        }
        this.unitCode = unitCode.trim();
    }

    public boolean isOptional() { return optional; }
    public void setOptional(boolean optional) { this.optional = optional; }

    public String getIngredientNote() { return ingredientNote; }
    public void setIngredientNote(String ingredientNote) {
        this.ingredientNote = (ingredientNote == null || ingredientNote.trim().isEmpty())
                ? null : ingredientNote.trim();
    }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredient)) return false;
        RecipeIngredient that = (RecipeIngredient) o;
        return recipeId == that.recipeId && ingredientId == that.ingredientId;
    }

    @Override
    public int hashCode() { return Objects.hash(recipeId, ingredientId); }

    @Override
    public String toString() {
        return "RecipeIngredient{id=" + id + ", recipeId=" + recipeId + ", ingredientId=" + ingredientId
                + ", requiredQuantity=" + requiredQuantity + ", unitCode='" + unitCode
                + "', optional=" + optional + "}";
    }
}