package com.example.smartpantrymanager.models;

import java.util.Locale;

public class Ingredient
{
    private Integer id;
    private String canonicalName;
    private String normalizedName;
    private String createdAt;

    public Ingredient(String canonicalName)
    {
        setCanonicalName(canonicalName);
        setNormalizedName(canonicalName.toLowerCase(Locale.ROOT).replaceAll("\\s+", " "));
    }

    public Ingredient(Integer id, String canonicalName, String normalizedName, String createdAt)
    {
        this(canonicalName);
        setId(id);
        setNormalizedName(normalizedName);
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

    public String getCanonicalName()
    {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName)
    {
        if (canonicalName == null || canonicalName.trim().isEmpty())
        {
            throw new IllegalArgumentException("canonicalName must not be blank.");
        }
        this.canonicalName = canonicalName.trim();
    }

    public String getNormalizedName()
    {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName)
    {
        if (normalizedName == null || normalizedName.trim().isEmpty())
        {
            throw new IllegalArgumentException("normalizedName must not be blank.");
        }
        this.normalizedName = normalizedName.trim();
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
