package com.example.smartpantrymanager.models;

public class IngredientAlias
{
    private Integer id;
    private int ingredientId;
    private String normalizedAlias;
    private String createdAt;

    public IngredientAlias(int ingredientId, String normalizedAlias)
    {
        setIngredientId(ingredientId);
        setNormalizedAlias(normalizedAlias);
    }

    public IngredientAlias(Integer id, int ingredientId, String normalizedAlias, String createdAt)
    {
        this(ingredientId, normalizedAlias);
        setId(id);
        setCreatedAt(createdAt);
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        if (id != null && id <= 0)
        {
            throw new IllegalArgumentException("id must be positive.");
        }
        this.id = id;
    }

    public int getIngredientId()
    {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId)
    {
        if (ingredientId <= 0)
        {
            throw new IllegalArgumentException("ingredientId must be positive.");
        }
        this.ingredientId = ingredientId;
    }

    public String getNormalizedAlias()
    {
        return normalizedAlias;
    }

    public void setNormalizedAlias(String normalizedAlias)
    {
        if (normalizedAlias == null || normalizedAlias.trim().isEmpty())
        {
            throw new IllegalArgumentException("normalizedAlias must not be blank.");
        }
        this.normalizedAlias = normalizedAlias.trim();
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }
}
