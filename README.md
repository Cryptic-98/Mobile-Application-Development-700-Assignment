# Mobile-Application-Development-700-Assignment
The assignment is a designing and developing a Smart Pantry Manager application that helps user manage ingredients that are available to them to reduce the waste of food by suggesting what they can make using available ingredients.

# Features
- Allow users to add, view, edit, and delete ingredients.
- Store a collection of recipes and compare required ingredients to user's current pantry.
- Strict recipe-matching system - recipe suggested only when user has every ingredient.

# Built with:
- Programming Language: Java
- Development Environment: Android Studio
- Database: SQLite (SQLiteOpenHelper)
- Application Type: Mobile Application (Android)

# Activities
1. MainActivity.java — Pantry List screen.
2. AddEditIngredientActivity.java — Add and Edit Ingredient screen.
3. SuggestedRecipesActivity.java — Suggested Recipes screen.
4. RecipeDetailActivity.java — Recipe Detail screen.
5. SettingsActivity.java — Settings screen.
(Each activity has it's own corresponding XML layout file)

# Business Logic
1. PantryManager.java	Handles pantry-related operations, including adding, editing, and deleting ingredients.
2. RecipeMatcher.java	Determines which recipes can be prepared using the current pantry.
3. IngredientNormalizer.java	Standardizes ingredient names and supported units to handle simple variations.
4. ValidationHelper.java	Checks user input before it is saved.

# Data
1. DatabaseHelper.java	Creates and manages the SQLite database and its tables.
2. PantryRepository.java	Provides methods for creating, reading, updating, and deleting pantry records.
3. RecipeRepository.java	Retrieves recipes, ingredients, and preparation instructions from the database.
4. SettingsRepository.java	Stores and retrieves application preferences.

# Project structure

```text
SmartPantryManager/
│
└── app/
    └── src/
        └── main/
            ├── java/
            │   └── com/
            │       └── example/
            │           └── smartpantrymanager/
            │               ├── MainActivity.java
            │               ├── AddEditIngredientActivity.java
            │               ├── SuggestedRecipesActivity.java
            │               ├── RecipeDetailActivity.java
            │               ├── SettingsActivity.java
            │               │
            │               ├── models/
            │               │   ├── PantryItem.java
            │               │   ├── Recipe.java
            │               │   ├── RecipeIngredient.java
            │               │   └── AppSettings.java
            │               │
            │               ├── database/
            │               │   ├── DatabaseHelper.java
            │               │   ├── PantryRepository.java
            │               │   ├── RecipeRepository.java
            │               │   └── SettingsRepository.java
            │               │
            │               ├── logic/
            │               │   ├── PantryManager.java
            │               │   ├── RecipeMatcher.java
            │               │   ├── IngredientNormalizer.java
            │               │   └── ValidationHelper.java
            │               │
            │               └── adapters/
            │                   ├── PantryAdapter.java
            │                   └── RecipeAdapter.java
            │
            ├── res/
            │   ├── layout/
            │   │   ├── activity_main.xml
            │   │   ├── activity_add_edit_ingredient.xml
            │   │   ├── activity_suggested_recipes.xml
            │   │   ├── activity_recipe_detail.xml
            │   │   ├── activity_settings.xml
            │   │   ├── item_pantry.xml
            │   │   └── item_recipe.xml
            │   │
            │   ├── drawable/
            │   ├── menu/
            │   └── values/
            │       ├── strings.xml
            │       ├── colors.xml
            │       └── themes.xml
            │
            └── AndroidManifest.xml
