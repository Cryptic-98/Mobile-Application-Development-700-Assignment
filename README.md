# Mobile-Application-Development-700-Assignment
The assignment is a designing and developing a Smart Pantry Manager application that helps user manage ingredients that are available to them to reduce the waste of food by suggesting what they can make using available ingredients.

# Features
- Allow users to add, view, edit, and delete ingredients.
- Store a collection of recipes and compare required ingredients to user's current pantry.
- **Pantry Matches** — online recipes from [TheMealDB](https://www.themealdb.com/) that the user
  can already make: a recipe qualifies when the pantry holds at least **four distinct required
  ingredients** in a sufficient, unexpired quantity.
- **Everyday Starter Recipes** — exactly five permanently available recipes that work offline,
  so the screen is never empty.
- Full recipe detail screen: ingredient list with per-ingredient availability, ordered steps and
  an availability summary.
- Allergen aware: recipes that contain an ingredient the user is allergic to are hidden.

# Built with:
- Programming Language: Java
- Development Environment: Android Studio
- Database: SQLite (SQLiteOpenHelper)
- Application Type: Mobile Application (Android)
- Online recipe source: TheMealDB public API (JSON over HTTPS, `HttpURLConnection`)
- Image loading: Glide 4.16.0

# Activities
1. MainActivity.java — Pantry List screen.
2. AddEditIngredientActivity.java — Add and Edit Ingredient screen.
3. SuggestedRecipesActivity.java — Suggested Recipes screen (Pantry Matches + Everyday Starter Recipes).
4. RecipeDetailActivity.java — Recipe Detail screen (online and starter recipes).
5. SettingsActivity.java — Settings screen.
(Each activity has it's own corresponding XML layout file)

# Online recipes (TheMealDB)

## API key configuration

The base URL and the API key are build configuration, not hard-coded strings. They are read from
Gradle properties and exposed through `BuildConfig`:

| Gradle property    | Default                                             |
|--------------------|-----------------------------------------------------|
| `themealdbApiKey`  | `1`                                                 |
| `themealdbBaseUrl` | `https://www.themealdb.com/api/json/v1/{key}/`      |

Set them in `gradle.properties` (project or `~/.gradle/gradle.properties`), or on the command
line with `-PthemealdbApiKey=...`:

```properties
themealdbApiKey=YOUR_KEY
```

The build turns them into `BuildConfig.THEMEALDB_BASE_URL` and `BuildConfig.THEMEALDB_API_KEY`.

> **Important:** the key `1` is TheMealDB's *development* key. Its terms of use allow it for
> development and educational use only — "You cannot publish apps to an appstore unless you are a
> paid subscriber." A build that is published to an app store must supply a paid production key
> (`themealdbApiKey`), and TheMealDB requires the attribution notice to be shown.

## Endpoints used

| Endpoint                | Purpose                                                          |
|-------------------------|------------------------------------------------------------------|
| `list.php?i=list`       | The ingredient vocabulary of the API (992 names), used to translate pantry names into searchable terms. |
| `filter.php?i=<term>`   | Meals that contain a given ingredient.                            |
| `lookup.php?i=<id>`     | The full detail of a meal (ingredients, measures, instructions). |

Discovery flow: pantry names → `IngredientVocabularyMatcher` → vocabulary terms → `filter.php`
→ deduplicated meal ids → `lookup.php` for the best candidates → `TheMealDbMatcher` compares the
meals with the pantry.

## Caching and errors

- Responses are cached in memory for 6 hours and on disk (JSON files in the app cache directory)
  for 24 hours. A stale disk cache is still served for up to 30 hours when the device is offline.
- All network work happens on a background thread; the screen shows a progress indicator, an
  empty state and a partial-error message ("Some online recipes could not be updated.") when
  individual requests fail.
- The `INTERNET` permission is declared in `AndroidManifest.xml`.

# Matching rules

A recipe from TheMealDB is a **pantry match** when the pantry holds at least four *distinct*
required ingredients that are present, unexpired and in a sufficient quantity.

- **Distinct counting** — the same ingredient listed twice in one recipe counts once, and the
  amounts of the two lines are added together.
- **Expiry** — an expired pantry entry never counts towards a match.
- **Amounts** — quantities are compared through the base factor of the measurement unit
  (`kg` → grams, `l` → millilitres, `tbsp` → 15 ml, ...).
- **Countable units** — `piece`, `slice`, `clove` and `can` form one family, so five slices of
  onion cover a recipe that asks for one onion. A recipe amount without a unit ("1 onion") is
  read as a count of items.
- **Mass against volume** — a weighed pantry entry ("500 g of butter") covers a measured recipe
  amount ("1 tablespoon of butter") when the density of that ingredient is known. The reference
  densities live in `IngredientDensity.java`; an ingredient whose density is unknown is reported
  as *unclear* instead of being guessed at, so the app never invents availability.
- **Names** — `IngredientSynonyms.java` maps the way TheMealDB spells ingredients onto the way
  people label their pantry ("Chicken Breast" → "Chicken", "Chopped Tomatoes" → "Tomatoes").
- **Sorting** — pantry matches are ordered by matched ingredient count (descending), then by the
  proportion of the recipe that is covered (descending), then by recipe name.
- **Two sections stay apart** — `RecipeSectionPlanner` removes starter recipes from the online
  section, so no recipe is ever shown twice.

# Business Logic
1. PantryManager.java	Handles pantry-related operations, including adding, editing, and deleting ingredients.
2. RecipeMatcher.java	Determines which recipes can be prepared using the current pantry.
3. IngredientNormalizer.java	Standardizes ingredient names and supported units to handle simple variations.
4. ValidationHelper.java	Checks user input before it is saved.
5. TheMealDbMatcher.java	Compares the online recipes with the pantry and applies the four-ingredient rule.
6. IngredientAvailabilityEvaluator.java	Decides, for one ingredient, whether the pantry covers the requirement.
7. IngredientDensity.java	Reference densities that allow weighed amounts to cover measured amounts.
8. IngredientSynonyms.java	Catalogue of ingredient names that describe the same kitchen item.
9. IngredientVocabularyMatcher.java	Translates pantry names into the search terms of the API.
10. RecipeSectionPlanner.java	Keeps the two recipe sections of the screen apart.

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
            │               ├── PresetRecipeSeeder.java
            │               ├── TheMealDbRecipeRepository.java
            │               ├── ThemealdbImageResolver.java
            │               ├── FoodImageLoader.java
            │               │
            │               ├── api/
            │               │   ├── TheMealDbClient.java
            │               │   ├── TheMealDbParser.java
            │               │   ├── TheMealDbMeal.java
            │               │   └── TheMealDbIngredient.java
            │               │
            │               ├── models/
            │               │   ├── PantryItem.java
            │               │   ├── Recipe.java
            │               │   ├── RecipeIngredient.java
            │               │   ├── RecipeStep.java
            │               │   ├── Ingredient.java
            │               │   ├── IngredientAlias.java
            │               │   ├── MeasurementUnit.java
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
            │               │   ├── ValidationHelper.java
            │               │   ├── TheMealDbMatcher.java
            │               │   ├── IngredientAvailabilityEvaluator.java
            │               │   ├── IngredientDensity.java
            │               │   ├── IngredientSynonyms.java
            │               │   ├── IngredientVocabularyMatcher.java
            │               │   ├── IngredientNameIndex.java
            │               │   ├── MeasurementFamily.java
            │               │   └── RecipeSectionPlanner.java
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

# Starter recipes

The second section of the recipe screen always shows the same five recipes. They are seeded into
the local database by `PresetRecipeSeeder`, so they are available without a network connection and
are never hidden by the matching rules. The set covers a breakfast, a brunch side, a quick lunch
and two dinners:

1. Golden Buttermilk Pancakes
2. Rosemary Breakfast Potatoes
3. Spicy Arrabiata Penne
4. Tomato and Egg Stir-Fry
5. Classic Potato Salad

The seeder also retires the two recipes the brief rules out (a peanut butter and jelly sandwich
and a bacon and egg sandwich) by marking them inactive.

# Tests

The business rules are covered by plain JVM unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

| Test class                          | What it covers                                                        |
|-------------------------------------|-----------------------------------------------------------------------|
| `TheMealDbMatcherTest`              | The four-ingredient rule, distinct counting, expiry, quantities, sorting, name deduplication. |
| `IngredientAvailabilityEvaluatorTest` | Missing, expired, insufficient, incompatible and unverifiable amounts, density conversion, countable units. |
| `IngredientSynonymsTest`            | The synonym catalogue and canonical terms.                             |
| `RecipeSectionPlannerTest`          | Keeping the two sections apart.                                       |
| `PresetRecipeSeederTest`            | The five starter recipes.                                             |
| `TheMealDbParserTest`               | Parsing meals and their measures.                                     |
| `MatchResultTest`                   | The result of comparing a local recipe with the pantry.               |
| `ValidationHelperTest`, `AllergyListCodecTest`, `RecipePreferenceEvaluatorTest` | Input validation and the allergy rules.           |

