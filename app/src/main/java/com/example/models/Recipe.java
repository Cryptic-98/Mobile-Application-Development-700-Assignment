package com.example.smartpantrymanager.models;

import java.util.Objects;

public class Recipe {
    private Integer id;
    private String name;
    private String description;
    private String imageReference;
    private String imageSource;
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer servings;
    private boolean active = true;
    private String createdAt;
    private String updatedAt;

    public Recipe(String name) {
        setName(name);
    }

    public Recipe(String name, String description, Integer prepTimeMinutes,
                  Integer cookTimeMinutes, Integer servings) {
        setName(name);
        setDescription(description);
        setPrepTimeMinutes(prepTimeMinutes);
        setCookTimeMinutes(cookTimeMinutes);
        setServings(servings);
    }

    public Recipe(Integer id, String name, String description, String imageReference,
                  String imageSource, Integer prepTimeMinutes, Integer cookTimeMinutes,
                  Integer servings, boolean active, String createdAt, String updatedAt) {
        this(name, description, prepTimeMinutes, cookTimeMinutes, servings);
        setId(id);
        setImageReference(imageReference);
        setImageSource(imageSource);
        setActive(active);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private static String blankToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive.");
        this.id = id;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be null or blank.");
        }
        this.name = name.trim();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = blankToNull(description); }

    public String getImageReference() { return imageReference; }
    public void setImageReference(String imageReference) { this.imageReference = blankToNull(imageReference); }

    public String getImageSource() { return imageSource; }
    public void setImageSource(String imageSource) { this.imageSource = blankToNull(imageSource); }

    public Integer getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(Integer prepTimeMinutes) {
        if (prepTimeMinutes != null && prepTimeMinutes < 0) {
            throw new IllegalArgumentException("prepTimeMinutes must be 0 or greater.");
        }
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public Integer getCookTimeMinutes() { return cookTimeMinutes; }
    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        if (cookTimeMinutes != null && cookTimeMinutes < 0) {
            throw new IllegalArgumentException("cookTimeMinutes must be 0 or greater.");
        }
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public Integer getServings() { return servings; }
    public void setServings(Integer servings) {
        if (servings != null && servings <= 0) {
            throw new IllegalArgumentException("servings must be greater than 0.");
        }
        this.servings = servings;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public int getTotalTimeMinutes() {
        return (prepTimeMinutes == null ? 0 : prepTimeMinutes)
                + (cookTimeMinutes == null ? 0 : cookTimeMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe)) return false;
        return name.equals(((Recipe) o).name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() {
        return "Recipe{id=" + id + ", name='" + name + "', servings=" + servings + ", active=" + active + "}";
    }
}