package com.example.smartpantrymanager.models;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class PantryItem {
    private Integer id;
    private int ingredientId;
    private double quantity;
    private String unitCode;
    private LocalDate expiryDate;
    private String ingredientName;
    private String createdAt;
    private String updatedAt;

    public PantryItem(int ingredientId, double quantity, String unitCode, LocalDate expiryDate) {
        setIngredientId(ingredientId);
        setQuantity(quantity);
        setUnitCode(unitCode);
        setExpiryDate(expiryDate);
    }

    public PantryItem(int ingredientId, double quantity, String unitCode) {
        this(ingredientId, quantity, unitCode, null);
    }

    public PantryItem(Integer id, int ingredientId, double quantity, String unitCode,
                      String expiryDate, String createdAt, String updatedAt) {
        this(ingredientId, quantity, unitCode, null);
        setId(id);
        setExpiryDate(expiryDate);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive.");
        this.id = id;
    }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) {
        if (ingredientId <= 0) throw new IllegalArgumentException("ingredientId must be positive.");
        this.ingredientId = ingredientId;
    }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) {
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be a finite number greater than 0.");
        }
        this.quantity = quantity;
    }

    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) {
        if (unitCode == null || unitCode.trim().isEmpty()) {
            throw new IllegalArgumentException("unitCode must not be null or blank.");
        }
        this.unitCode = unitCode.trim();
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public void setExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            this.expiryDate = null;
            return;
        }
        try {
            String value = expiryDate.trim();
            if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new DateTimeParseException("Invalid date format.", value, 0);
            }
            this.expiryDate = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("expiryDate must be in YYYY-MM-DD format.", e);
        }
    }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PantryItem)) return false;
        return ingredientId == ((PantryItem) o).ingredientId;
    }

    @Override
    public int hashCode() { return Objects.hash(ingredientId); }

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", ingredientId=" + ingredientId + ", quantity=" + quantity
                + ", unitCode='" + unitCode + "', expiryDate=" + expiryDate + "}";
    }
}