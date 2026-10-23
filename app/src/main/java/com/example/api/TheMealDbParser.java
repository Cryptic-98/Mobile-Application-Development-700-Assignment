package com.example.smartpantrymanager.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TheMealDbParser
{
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "^\\s*(\\d+/\\d+|\\d+(?:\\.\\d+)?(?:\\s+\\d+/\\d+)?|½|¼|¾|⅓|⅔)\\s*(.*)$");

    private TheMealDbParser()
    {
    }

    public static TheMealDbMeal parseMeal(JSONObject object) throws JSONException
    {
        if (object == null)
        {
            throw new JSONException("TheMealDB meal was missing.");
        }
        Map<String, String> fields = new HashMap<>();
        String[] keys = {"idMeal", "strMeal", "strCategory", "strArea",
                "strMealThumb", "strInstructions"};
        for (String key : keys)
        {
            fields.put(key, optionalString(object, key));
        }
        for (int index = 1; index <= 20; index++)
        {
            fields.put("strIngredient" + index, optionalString(object, "strIngredient" + index));
            fields.put("strMeasure" + index, optionalString(object, "strMeasure" + index));
        }
        return parseMealFields(fields);
    }








    public static TheMealDbMeal parseMealResponse(String response) throws JSONException
    {
        if (response == null || response.trim().isEmpty())
        {
            return null;
        }
        JSONArray meals = new JSONObject(response).optJSONArray("meals");
        if (meals == null || meals.length() == 0)
        {
            return null;
        }
        JSONObject meal = meals.optJSONObject(0);
        return meal == null ? null : parseMeal(meal);
    }













    public static Map<String, String> parseIngredientVocabulary(String response) throws JSONException
    {
        Map<String, String> vocabulary = new LinkedHashMap<>();
        if (response == null || response.trim().isEmpty())
        {
            return vocabulary;
        }
        JSONArray meals = new JSONObject(response).optJSONArray("meals");
        if (meals == null)
        {
            return vocabulary;
        }
        for (int index = 0; index < meals.length(); index++)
        {
            JSONObject entry = meals.optJSONObject(index);
            if (entry == null)
            {
                continue;
            }
            String name = optionalString(entry, "strIngredient");
            if (name == null)
            {
                continue;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (!vocabulary.containsKey(key))
            {
                vocabulary.put(key, name);
            }
        }
        return vocabulary;
    }








    public static List<String> parseMealIds(String response) throws JSONException
    {
        List<String> identifiers = new ArrayList<>();
        if (response == null || response.trim().isEmpty())
        {
            return identifiers;
        }
        JSONArray meals = new JSONObject(response).optJSONArray("meals");
        if (meals == null)
        {
            return identifiers;
        }
        for (int index = 0; index < meals.length(); index++)
        {
            JSONObject entry = meals.optJSONObject(index);
            if (entry == null)
            {
                continue;
            }
            String id = optionalString(entry, "idMeal");
            if (id != null && !identifiers.contains(id))
            {
                identifiers.add(id);
            }
        }
        return identifiers;
    }

    static TheMealDbMeal parseMealFields(Map<String, String> fields) throws JSONException
    {
        if (fields == null)
        {
            throw new JSONException("TheMealDB meal fields were missing.");
        }
        String id = requiredString(fields, "idMeal");
        String name = requiredString(fields, "strMeal");
        List<TheMealDbIngredient> ingredients = new ArrayList<>();
        for (int index = 1; index <= 20; index++)
        {
            String nameValue = optionalString(fields, "strIngredient" + index);
            if (nameValue == null)
            {
                continue;
            }
            String measure = optionalString(fields, "strMeasure" + index);
            ParsedMeasure parsedMeasure = parseMeasure(measure);
            ingredients.add(new TheMealDbIngredient(nameValue, measure,
                    parsedMeasure.quantity, parsedMeasure.unitCode));
        }
        String imageUrl = optionalString(fields, "strMealThumb");
        if (imageUrl != null && !imageUrl.startsWith("https://www.themealdb.com/"))
        {
            imageUrl = null;
        }
        return new TheMealDbMeal(id, name, optionalString(fields, "strCategory"),
                optionalString(fields, "strArea"), imageUrl, ingredients,
                parseInstructions(optionalString(fields, "strInstructions")));
    }

    static ParsedMeasure parseMeasure(String measure)
    {
        if (measure == null || measure.trim().isEmpty())
        {
            return new ParsedMeasure(null, null);
        }
        String normalized = measure.trim()
                .replace("½", " 1/2")
                .replace("¼", " 1/4")
                .replace("¾", " 3/4")
                .replace("⅓", " 1/3")
                .replace("⅔", " 2/3");
        Matcher matcher = QUANTITY_PATTERN.matcher(normalized);
        if (!matcher.matches())
        {
            return new ParsedMeasure(null, null);
        }
        Double quantity = parseQuantity(matcher.group(1));
        if (quantity == null || quantity <= 0)
        {
            return new ParsedMeasure(null, null);
        }
        String unit = normalizeUnit(matcher.group(2));
        if (unit == null)
        {


            return new ParsedMeasure(quantity, "piece");
        }
        return new ParsedMeasure(quantity, unit);
    }

    private static Double parseQuantity(String value)
    {
        try
        {
            String normalized = value.trim();
            int space = normalized.indexOf(' ');
            if (space > 0)
            {
                double whole = Double.parseDouble(normalized.substring(0, space));
                return whole + parseFraction(normalized.substring(space + 1));
            }
            if (normalized.contains("/"))
            {
                return parseFraction(normalized);
            }
            return Double.parseDouble(normalized);
        }
        catch (NumberFormatException | ArithmeticException exception)
        {
            return null;
        }
    }

    private static double parseFraction(String value)
    {
        String[] fraction = value.split("/");
        if (fraction.length != 2)
        {
            throw new NumberFormatException("Invalid fraction.");
        }
        double numerator = Double.parseDouble(fraction[0].trim());
        double denominator = Double.parseDouble(fraction[1].trim());
        if (denominator == 0)
        {
            throw new ArithmeticException("Fraction denominator must not be zero.");
        }
        return numerator / denominator;
    }

    private static String normalizeUnit(String value)
    {
        String unit = value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[.,]", "")
                .replaceAll("\\s+", " ");
        switch (unit)
        {
            case "g":
            case "gram":
            case "grams":
                return "g";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "mg":
            case "milligram":
            case "milligrams":
                return "mg";
            case "ml":
            case "milliliter":
            case "milliliters":
            case "millilitre":
            case "millilitres":
                return "ml";
            case "l":
            case "liter":
            case "liters":
            case "litre":
            case "litres":
                return "l";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "tsp";
            case "tbsp":
            case "tbs":
            case "tbl":
            case "tbls":
            case "tablespoon":
            case "tablespoons":
                return "tbsp";
            case "cup":
            case "cups":
                return "cup";
            case "piece":
            case "pieces":
            case "pc":
            case "pcs":
                return "piece";
            case "clove":
            case "cloves":
                return "clove";
            case "slice":
            case "slices":
                return "slice";
            case "can":
            case "cans":
                return "can";
            default:
                return null;
        }
    }

    private static List<String> parseInstructions(String instructions)
    {
        if (instructions == null)
        {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String step : instructions.split("(?:\\r?\\n)+"))
        {
            String cleaned = step.trim().replaceFirst("^\\d+[.)]\\s*", "");
            if (!cleaned.isEmpty())
            {
                result.add(cleaned);
            }
        }
        if (result.isEmpty() && !instructions.trim().isEmpty())
        {
            result.add(instructions.trim());
        }
        return result;
    }

    private static String requiredString(Map<String, String> fields, String key) throws JSONException
    {
        String value = optionalString(fields, key);
        if (value == null)
        {
            throw new JSONException("TheMealDB response omitted " + key + ".");
        }
        return value;
    }

    private static String optionalString(Map<String, String> fields, String key)
    {
        String value = fields.get(key);
        return value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())
                ? null : value.trim();
    }

    private static String optionalString(JSONObject object, String key)
    {
        String value = object.optString(key, "").trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    static final class ParsedMeasure
    {
        final Double quantity;
        final String unitCode;

        ParsedMeasure(Double quantity, String unitCode)
        {
            this.quantity = quantity;
            this.unitCode = unitCode;
        }
    }
}
