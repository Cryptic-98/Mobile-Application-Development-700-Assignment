package com.example.smartpantrymanager.models;

import java.util.Objects;

public class AppSettings {
    private String settingKey;
    private String settingValue;
    private String updatedAt;

    public AppSettings(String settingKey, String settingValue) {
        setSettingKey(settingKey);
        setSettingValue(settingValue);
    }

    public AppSettings(String settingKey, String settingValue, String updatedAt) {
        this(settingKey, settingValue);
        this.updatedAt = updatedAt;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) {
        if (settingKey == null || settingKey.trim().isEmpty()) {
            throw new IllegalArgumentException("settingKey must not be null or blank.");
        }
        this.settingKey = settingKey.trim();
    }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) {
        if (settingValue == null) {
            throw new IllegalArgumentException("settingValue must not be null.");
        }
        this.settingValue = settingValue;
    }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppSettings)) return false;
        return settingKey.equals(((AppSettings) o).settingKey);
    }

    @Override
    public int hashCode() { return Objects.hash(settingKey); }

    @Override
    public String toString() {
        return "AppSettings{settingKey='" + settingKey + "', settingValue='" + settingValue + "'}";
    }
}