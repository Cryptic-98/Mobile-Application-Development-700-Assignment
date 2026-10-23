#!/usr/bin/env python3
import os
import zipfile
from datetime import datetime

out_path = os.path.expanduser('~/StudioProjects/Mobile-Application-Development-700-Assignment/Mobile App Development 700 - Practical Assignment.docx')
os.makedirs(os.path.dirname(out_path), exist_ok=True)

# Minimal DOCX structure
content_types = '''<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
  <Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
</Types>'''

rels = '''<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>'''

doc_rels = '''<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>
</Relationships>'''

styles = '''<?xml version="1.0" encoding="UTF-8"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
      </w:rPr>
    </w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:pPr><w:outlineLvl w:val="0"/><w:spacing w:before="240" w:after="120"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="32"/><w:szCs w:val="32"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:pPr><w:outlineLvl w:val="1"/><w:spacing w:before="200" w:after="100"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="28"/><w:szCs w:val="28"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading3">
    <w:name w:val="heading 3"/>
    <w:basedOn w:val="Normal"/>
    <w:next w:val="Normal"/>
    <w:pPr><w:outlineLvl w:val="2"/><w:spacing w:before="160" w:after="80"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>
  </w:style>
</w:styles>'''

numbering = '''<?xml version="1.0" encoding="UTF-8"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
</w:numbering>'''

settings = '''<?xml version="1.0" encoding="UTF-8"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
</w:settings>'''

def p(text, style=None):
    style_xml = f'<w:pPr><w:pStyle w:val="{style}"/></w:pPr>' if style else '<w:pPr/>'
    return f'<w:p>{style_xml}<w:r><w:t xml:space="preserve">{escape(text)}</w:t></w:r></w:p>'

def p_bold(text, style=None):
    style_xml = f'<w:pPr><w:pStyle w:val="{style}"/></w:pPr>' if style else '<w:pPr/>'
    return f'<w:p>{style_xml}<w:r><w:rPr><w:b/></w:rPr><w:t xml:space="preserve">{escape(text)}</w:t></w:r></w:p>'

def p_code(text):
    return f'<w:p><w:pPr><w:spacing w:before="60" w:after="60"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Courier New" w:hAnsi="Courier New"/><w:sz w:val="18"/></w:rPr><w:t xml:space="preserve">{escape(text)}</w:t></w:r></w:p>'

def br():
    return '<w:p><w:pPr/></w:p>'

def escape(s):
    if s is None:
        return ''
    return (str(s)
            .replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;'))

# Build document
body_parts = []

# 1. Cover page
body_parts.append(p('Mobile App Development 700 - Practical Assignment', None))
body_parts.append(br())
body_parts.append(p_bold('App Name:', None))
body_parts.append(p('Canned Hope (Smart Pantry Manager)', None))
body_parts.append(br())
body_parts.append(p_bold('Student Name:', None))
body_parts.append(p('Goabaone Diala', None))  # Update if different
body_parts.append(br())
body_parts.append(p_bold('Student Number:', None))
body_parts.append(p('[Insert Student Number Here]', None))
body_parts.append(br())
body_parts.append(p_bold('Module Name:', None))
body_parts.append(p('Mobile App Development 700', None))
body_parts.append(br())
body_parts.append(p_bold('Date:', None))
body_parts.append(p(datetime.now().strftime('%d %B %Y'), None))
body_parts.append(br())
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 2. Table of contents
body_parts.append(p('2. Table of Contents', 'Heading1'))
body_parts.append(p('1. Cover Page........................................................................................................1'))
body_parts.append(p('2. Table of Contents.............................................................................................2'))
body_parts.append(p('3. Introduction......................................................................................................3'))
body_parts.append(p('4. System Design...................................................................................................3'))
body_parts.append(p('5. Screenshots of Every Output.............................................................................4'))
body_parts.append(p('6. Key Code Snippets............................................................................................5'))
body_parts.append(p('7. Challenges and Solutions..................................................................................7'))
body_parts.append(p('8. Conclusion and Reflection...............................................................................8'))
body_parts.append(p('9. Reference List...................................................................................................8'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 3. Introduction
body_parts.append(p('3. Introduction', 'Heading1'))
body_parts.append(p_bold('The Problem:', 'Heading2'))
body_parts.append(p('Food waste is a significant issue, with many households throwing away edible ingredients simply because they are forgotten in the pantry or go unused before expiring. It is difficult to keep track of what ingredients are available, their quantities, expiry dates, and what meals can be prepared from them.'))
body_parts.append(br())
body_parts.append(p_bold('The Solution:', 'Heading2'))
body_parts.append(p('Canned Hope (Smart Pantry Manager) is an Android mobile application developed to help users manage their pantry more effectively. The app allows users to track ingredients, monitor expiry dates, and discover meal ideas based on what they currently have available.'))
body_parts.append(br())
body_parts.append(p_bold('Target Audience:', 'Heading2'))
body_parts.append(p('The app is designed for individuals and households who want to reduce food waste, stay organised with their groceries, and quickly find recipes they can make with ingredients already in their pantry.'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 4. System design
body_parts.append(p('4. System Design', 'Heading1'))
body_parts.append(p_bold('4.1 Screen Flow / Wireframe Diagram', 'Heading2'))
body_parts.append(p('The application consists of five main screens connected as follows:'))
body_parts.append(p('1. MainActivity (Pantry List) - The main screen displaying all pantry items, items expiring soon, and a button to add ingredients. Bottom navigation allows access to Recipes and Settings.'))
body_parts.append(p('2. AddEditIngredientActivity - Allows users to add a new ingredient or edit/delete an existing pantry item. Includes fields for name, quantity, unit, and optional expiry date with validation.'))
body_parts.append(p('3. SuggestedRecipesActivity (Recipes) - Displays two sections: Everyday Starter Recipes (5 offline recipes) and Pantry Matches (online recipes from TheMealDB that match at least 4 distinct pantry ingredients).'))
body_parts.append(p('4. RecipeDetailActivity - Shows full recipe details including ingredients with availability status (In pantry/Not enough/Missing/Expired/Incompatible/Unverifiable), preparation steps, metadata, and allergy/diet warnings.'))
body_parts.append(p('5. SettingsActivity - Allows users to configure theme (light/dark/system), manage allergies with hide/flag options, and set dietary preferences.'))
body_parts.append(p('6. OnboardingActivity - Guides new users through theme selection, allergy setup, and diet preferences.'))
body_parts.append(br())
body_parts.append(p_bold('4.2 Data Model / ER Diagram', 'Heading2'))
body_parts.append(p('The application uses SQLite with the following tables:'))
body_parts.append(p('• measurement_units (unit_code PK, display_name, dimension, to_base_factor)'))
body_parts.append(p('• ingredients (id PK, canonical_name, normalized_name UNIQUE)'))
body_parts.append(p('• ingredient_aliases (id PK, ingredient_id FK -> ingredients, normalized_alias UNIQUE)'))
body_parts.append(p('• pantry_items (id PK, ingredient_id FK UNIQUE -> ingredients, quantity, unit_code FK, expiry_date)'))
body_parts.append(p('• recipes (id PK, name UNIQUE, description, image_reference, is_active)'))
body_parts.append(p('• recipe_ingredients (id PK, recipe_id FK, ingredient_id FK, required_quantity, unit_code FK, is_optional)'))
body_parts.append(p('• recipe_steps (id PK, recipe_id FK, step_number, instruction)'))
body_parts.append(p('• app_settings (setting_key PK, setting_value)'))
body_parts.append(p('Relationships: ingredients have many aliases and many pantry_items/recipe_ingredients. Recipes have many recipe_ingredients and recipe_steps. All foreign keys enforce referential integrity with CASCADE/RESTRICT rules.'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 5. Screenshots
body_parts.append(p('5. Screenshots of Every Output', 'Heading1'))
body_parts.append(p('Note: Screenshots of the running application are to be inserted here by the student. The following screens and functions are present in the app:'))
body_parts.append(p('5.1 Onboarding Screens - Welcome, Allergy setup, Diet preferences'))
body_parts.append(p('5.2 MainActivity (Pantry List) - Display of pantry items, expiring soon section, empty state'))
body_parts.append(p('5.3 AddEditIngredientActivity - Add ingredient form, Edit ingredient form with delete option, validation errors'))
body_parts.append(p('5.4 SuggestedRecipesActivity - Starter recipes section, Pantry Matches from TheMealDB, loading states, empty/error states'))
body_parts.append(p('5.5 RecipeDetailActivity - Recipe details with ingredient availability indicators, preparation steps, allergy/diet warnings'))
body_parts.append(p('5.6 SettingsActivity - Theme selection, allergy management, diet preferences'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 6. Key code snippets
body_parts.append(p('6. Key Code Snippets', 'Heading1'))

body_parts.append(p_bold('6.1 ValidationHelper - Input Validation', 'Heading2'))
body_parts.append(p('Purpose: Ensures all user inputs are validated before saving to prevent invalid data.'))
body_parts.append(p_code('public static double requirePositiveQuantity(double quantity, String field)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    if (!Double.isFinite(quantity) || quantity <= 0)'))
body_parts.append(p_code('    {'))
body_parts.append(p_code('        throw new IllegalArgumentException(field + " must be a positive finite number.");'))
body_parts.append(p_code('    }'))
body_parts.append(p_code('    return quantity;'))
body_parts.append(p_code('}'))
body_parts.append(p_code(''))
body_parts.append(p_code('public static LocalDate parseOptionalDate(String value)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    if (value == null || value.trim().isEmpty()) return null;'))
body_parts.append(p_code('    String text = value.trim();'))
body_parts.append(p_code('    if (!text.matches("\\\\d{4}-\\\\d{2}-\\\\d{2}"))'))
body_parts.append(p_code('        throw new IllegalArgumentException("Date must use YYYY-MM-DD format.");'))
body_parts.append(p_code('    try { return LocalDate.parse(text, DATE_FORMAT); }'))
body_parts.append(p_code('    catch (RuntimeException ex) { throw new IllegalArgumentException("Date must be a valid calendar date.", ex); }'))
body_parts.append(p_code('}'))
body_parts.append(br())

body_parts.append(p_bold('6.2 PantryManager - Business Logic for Pantry Operations', 'Heading2'))
body_parts.append(p('Purpose: Orchestrates pantry operations with validation and provides expiry-related queries.'))
body_parts.append(p_code('public PantryItem addStock(String name, double quantity, String unitCode, String expiryDate)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    ValidationHelper.requireText(name, "name");'))
body_parts.append(p_code('    ValidationHelper.requirePositiveQuantity(quantity, "quantity");'))
body_parts.append(p_code('    String unit = ValidationHelper.requireUnitCode(unitCode);'))
body_parts.append(p_code('    ValidationHelper.parseOptionalDate(expiryDate);'))
body_parts.append(p_code('    if (pantryRepository.getUnitByCode(unit) == null)'))
body_parts.append(p_code('        throw new IllegalArgumentException("Unknown unit code: " + unit);'))
body_parts.append(p_code('    return pantryRepository.addOrMergePantryItem(name, quantity, unit, expiryDate);'))
body_parts.append(p_code('}'))
body_parts.append(br())

body_parts.append(p_bold('6.3 TheMealDbMatcher - Recipe Matching Logic', 'Heading2'))
body_parts.append(p('Purpose: Implements the core matching algorithm requiring at least 4 distinct ingredients. Handles unit conversions, density calculations, synonyms, and expiry checking.'))
body_parts.append(p_code('public static final int MINIMUM_MATCHED_INGREDIENTS = 4;'))
body_parts.append(p_code(''))
body_parts.append(p_code('public List<TheMealDbMatch> getRecommendations(List<TheMealDbMeal> meals)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    EvaluationContext context = buildContext();'))
body_parts.append(p_code('    List<TheMealDbMatch> results = new ArrayList<>();'))
body_parts.append(p_code('    for (TheMealDbMeal meal : meals)'))
body_parts.append(p_code('    {'))
body_parts.append(p_code('        TheMealDbMatch result = evaluateMeal(meal, context);'))
body_parts.append(p_code('        if (result.isRecommended()) { results.add(result); }'))
body_parts.append(p_code('    }'))
body_parts.append(p_code('    // Sort by matched count desc, proportion desc, name'))
body_parts.append(p_code('    results.sort(Comparator.comparingInt(TheMealDbMatch::getMatchedRequiredIngredientCount).reversed()'))
body_parts.append(p_code('        .thenComparing(TheMealDbMatch::getMatchProportion, Comparator.reverseOrder())'))
body_parts.append(p_code('        .thenComparing(r -> r.getMeal().getName(), String.CASE_INSENSITIVE_ORDER));'))
body_parts.append(p_code('    return uniqueResults;'))
body_parts.append(p_code('}'))
body_parts.append(br())

body_parts.append(p_bold('6.4 DatabaseHelper - Schema Initialization', 'Heading2'))
body_parts.append(p('Purpose: Loads and executes the database schema from DATABASE.sql asset file on first run.'))
body_parts.append(p_code('@Override'))
body_parts.append(p_code('public void onCreate(SQLiteDatabase db)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    for (String statement : readStatements())'))
body_parts.append(p_code('    {'))
body_parts.append(p_code('        String control = statement.replaceAll("(?s)/\\\\*.*?\\\\*/", "")'))
body_parts.append(p_code('            .replaceAll("(?m)--.*$", "").trim();'))
body_parts.append(p_code('        if (control.isEmpty() || control.matches("(?is)PRAGMA\\\\b.*")'))
body_parts.append(p_code('            || control.matches("(?is)BEGIN(?:\\\\s+TRANSACTION)?")'))
body_parts.append(p_code('            || control.matches("(?is)COMMIT")) { continue; }'))
body_parts.append(p_code('        db.execSQL(statement);'))
body_parts.append(p_code('    }'))
body_parts.append(p_code('}'))
body_parts.append(br())

body_parts.append(p_bold('6.5 IngredientAvailabilityEvaluator - Availability Checking', 'Heading2'))
body_parts.append(p('Purpose: Determines if pantry stock satisfies recipe requirements, handling expiry, unit compatibility, density conversions, and countable units.'))
body_parts.append(p_code('public static IngredientAvailability evaluateAgainstFamily(PantryItem stock,'))
body_parts.append(p_code('        MeasurementUnit stockUnit, MeasurementFamily requiredFamily,'))
body_parts.append(p_code('        double requiredBaseQuantity, String ingredientName, LocalDate today)'))
body_parts.append(p_code('{'))
body_parts.append(p_code('    if (stock == null) return IngredientAvailability.MISSING;'))
body_parts.append(p_code('    if (today != null && stock.getExpiryDate() != null'))
body_parts.append(p_code('        if (stock.getExpiryDate().isBefore(today)) return IngredientAvailability.EXPIRED;'))
body_parts.append(p_code('    if (requiredFamily == null || !Double.isFinite(requiredBaseQuantity) || requiredBaseQuantity <= 0)'))
body_parts.append(p_code('        return IngredientAvailability.UNVERIFIABLE;'))
body_parts.append(p_code('    Double available = convertToFamily(stock.getQuantity(), stockUnit, ingredientName, requiredFamily);'))
body_parts.append(p_code('    if (available == null) return IngredientAvailability.INCOMPATIBLE_UNIT;'))
body_parts.append(p_code('    return available >= requiredBaseQuantity ? IngredientAvailability.AVAILABLE'))
body_parts.append(p_code('        : IngredientAvailability.INSUFFICIENT_QUANTITY;'))
body_parts.append(p_code('}'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 7. Challenges and solutions
body_parts.append(p('7. Challenges and Solutions', 'Heading1'))

body_parts.append(p_bold('Challenge 1: Complex Ingredient Matching Across Different Units and Forms', 'Heading2'))
body_parts.append(p_bold('Problem:', None))
body_parts.append(p('TheMealDB recipes use different units and naming conventions than what users enter in their pantry. For example, recipes might say "1 onion" while pantry might have "500g onion", or use different names like "Chicken Breast" vs "Chicken".'))
body_parts.append(p_bold('Solution:', None))
body_parts.append(p('Implemented a comprehensive matching system with IngredientNormalizer, IngredientSynonyms, IngredientNameIndex, and IngredientDensity classes. The system handles countable units (piece/slice/clove/can), converts between mass and volume using ingredient-specific densities, and maps synonyms. The IngredientAvailabilityEvaluator intelligently handles different measurement families.'))

body_parts.append(br())
body_parts.append(p_bold('Challenge 2: Handling Offline Functionality and API Reliability', 'Heading2'))
body_parts.append(p_bold('Problem:', None))
body_parts.append(p('The app needs to work offline for core pantry functionality and always show recipes even when TheMealDB API is unavailable or slow. Network requests could fail partially.'))
body_parts.append(p_bold('Solution:', None))
body_parts.append(p('Created 5 permanent "Everyday Starter Recipes" seeded by PresetRecipeSeeder into the local SQLite database. Implemented caching in TheMealDbRecipeRepository (memory 6 hours, disk 24 hours, stale up to 30 hours offline). Added proper error handling with progress indicators, partial error messages, and graceful degradation - if online recipes fail to load, starter recipes remain visible.'))

body_parts.append(br())
body_parts.append(p_bold('Challenge 3: Database Schema Migration and Data Integrity', 'Heading2'))
body_parts.append(p_bold('Problem:', None))
body_parts.append(p('Needed to ensure referential integrity, prevent duplicate ingredients/pantry items, handle unit conversions correctly, and validate all data.'))
body_parts.append(p_bold('Solution:', None))
body_parts.append(p('Designed a normalized SQLite schema with foreign keys enabled, proper constraints (CHECK constraints), unique indexes, and CASCADE/RESTRICT rules. Implemented PantryRepository with transaction handling for complex operations like addOrMergePantryItem. Used ingredient normalization and alias system to prevent duplicates. All validations centralized in ValidationHelper.'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 8. Conclusion and reflection
body_parts.append(p('8. Conclusion and Reflection', 'Heading1'))
body_parts.append(p_bold('What I Learned:', 'Heading2'))
body_parts.append(p('• Android Development: Gained proficiency in Activities, Fragments, RecyclerView patterns, SQLiteOpenHelper, and Material Design components.'))
body_parts.append(p('• Database Design: Learned to design normalized relational databases with proper constraints and relationships.'))
body_parts.append(p('• API Integration: Implemented HTTP networking with HttpURLConnection, JSON parsing, caching strategies, and asynchronous operations.'))
body_parts.append(p('• Business Logic: Developed complex matching algorithms handling unit conversions, edge cases, and data validation.'))
body_parts.append(p('• Software Engineering: Applied MVC-like architecture separating models, logic, database, and UI layers.'))
body_parts.append(br())
body_parts.append(p_bold('What I Would Improve Given More Time:', 'Heading2'))
body_parts.append(p('• User Experience: Add barcode scanning for ingredients, recipe favorites/bookmarks, shopping list generation from missing ingredients.'))
body_parts.append(p('• Functionality: Implement recipe creation by users, meal planning calendar, nutritional information, and better recipe search filters.'))
body_parts.append(p('• Performance: Optimize database queries with better indexing, implement pagination for large recipe lists, and add image caching improvements.'))
body_parts.append(p('• Testing: Add more comprehensive unit tests and instrumented UI tests.'))
body_parts.append(p('• Polish: Improve animations, add dark mode refinements, support multiple languages, and enhance accessibility features.'))
body_parts.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

# 9. Reference list
body_parts.append(p('9. Reference List', 'Heading1'))
body_parts.append(p('Android Developers. (n.d.). Android documentation. Google. https://developer.android.com/docs'))
body_parts.append(p('Google. (n.d.). Material Design 3. https://m3.material.io/'))
body_parts.append(p('MealDB. (n.d.). TheMealDB API documentation. https://www.themealdb.com/api.php'))
body_parts.append(p('SQLite. (n.d.). SQLite documentation. https://www.sqlite.org/docs.html'))
body_parts.append(p('Oracle. (n.d.). Java documentation. https://docs.oracle.com/en/java/'))
body_parts.append(p('Bumptech. (n.d.). Glide image loading library. https://bumptech.github.io/glide/'))

document_xml = '''<?xml version="1.0" encoding="UTF-8"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    ''' + '\n    '.join(body_parts) + '''
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>'''

with zipfile.ZipFile(out_path, 'w') as z:
    z.writestr('[Content_Types].xml', content_types)
    z.writestr('_rels/.rels', rels)
    z.writestr('word/_rels/document.xml.rels', doc_rels)
    z.writestr('word/document.xml', document_xml)
    z.writestr('word/styles.xml', styles)
    z.writestr('word/numbering.xml', numbering)
    z.writestr('word/settings.xml', settings)

print('Created:', out_path)
