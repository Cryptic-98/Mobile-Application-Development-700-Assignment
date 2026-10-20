PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;


CREATE TABLE IF NOT EXISTS measurement_units (
    unit_code TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    dimension TEXT NOT NULL,
    to_base_factor REAL NOT NULL CHECK (to_base_factor > 0)
);

INSERT OR IGNORE INTO measurement_units
    (unit_code, display_name, dimension, to_base_factor)
VALUES
    ('mg', 'milligram(s)', 'mass', 0.001),
    ('g', 'gram(s)', 'mass', 1.0),
    ('kg', 'kilogram(s)', 'mass', 1000.0),
    ('ml', 'millilitre(s)', 'volume', 1.0),
    ('l', 'litre(s)', 'volume', 1000.0),
    ('tsp', 'teaspoon(s)', 'volume', 5.0),
    ('tbsp', 'tablespoon(s)', 'volume', 15.0),
    ('cup', 'cup(s)', 'volume', 240.0),
    ('piece', 'piece(s)', 'piece', 1.0),
    ('clove', 'clove(s)', 'clove', 1.0),
    ('slice', 'slice(s)', 'slice', 1.0),
    ('can', 'can(s)', 'can', 1.0);

CREATE TABLE IF NOT EXISTS ingredients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    canonical_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (length(trim(canonical_name)) > 0),
    CHECK (length(trim(normalized_name)) > 0)
);

CREATE TABLE IF NOT EXISTS ingredient_aliases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ingredient_id INTEGER NOT NULL,
    normalized_alias TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (length(trim(normalized_alias)) > 0),
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ingredient_aliases_ingredient_id
    ON ingredient_aliases(ingredient_id);

CREATE TABLE IF NOT EXISTS pantry_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ingredient_id INTEGER NOT NULL UNIQUE,
    quantity REAL NOT NULL CHECK (quantity > 0),
    unit_code TEXT NOT NULL,
    expiry_date TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (unit_code) REFERENCES measurement_units(unit_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CHECK (
        expiry_date IS NULL OR
        (length(expiry_date) = 10 AND date(expiry_date) IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_pantry_items_expiry_date
    ON pantry_items(expiry_date);

CREATE TABLE IF NOT EXISTS recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    image_reference TEXT,
    image_source TEXT,
    prep_time_minutes INTEGER CHECK (
        prep_time_minutes IS NULL OR prep_time_minutes >= 0
    ),
    cook_time_minutes INTEGER CHECK (
        cook_time_minutes IS NULL OR cook_time_minutes >= 0
    ),
    servings INTEGER CHECK (servings IS NULL OR servings > 0),

