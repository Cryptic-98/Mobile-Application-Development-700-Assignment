package com.example.smartpantrymanager;

import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.database.RecipeRepository;
import com.example.smartpantrymanager.models.Ingredient;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import com.example.smartpantrymanager.models.RecipeStep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;












public class PresetRecipeSeeder
{
    private static final int PENDING_RECIPE_ID = 1;






    public static final String IMAGE_SOURCE_PRESET = "preset";





    private static final String IMAGE_SOURCE_LEGACY_THEMEALDB = "themealdb";





    private static final List<String> LEGACY_STARTER_RECIPE_NAMES = Arrays.asList(
            "Pancakes",
            "Peanut Butter Cookies",
            "Breakfast Potatoes",
            "French Omelette");

    private final PantryRepository pantryRepository;
    private final RecipeRepository recipeRepository;

    public PresetRecipeSeeder(PantryRepository pantryRepository, RecipeRepository recipeRepository)
    {
        if (pantryRepository == null || recipeRepository == null)
        {
            throw new IllegalArgumentException("Repositories must not be null.");
        }
        this.pantryRepository = pantryRepository;
        this.recipeRepository = recipeRepository;
    }




    public static List<String> getStarterRecipeNames()
    {
        List<String> names = new ArrayList<>();
        for (StarterRecipe recipe : getStarterRecipes())
        {
            names.add(recipe.getName());
        }
        return Collections.unmodifiableList(names);
    }




    public static List<StarterRecipe> getStarterRecipes()
    {
        return Arrays.asList(
                new StarterRecipe(
                        "Golden Buttermilk Pancakes",
                        "Fluffy pancakes from everyday breakfast staples.",
                        10, 15, 4,
                        "https://www.themealdb.com/images/media/meals/rwuyqx1511383174.jpg",
                        Arrays.asList(
                                new StarterIngredient("Flour", 100, "g", false),
                                new StarterIngredient("Eggs", 2, "piece", false),
                                new StarterIngredient("Milk", 300, "ml", false),
                                new StarterIngredient("Baking powder", 1, "tsp", false),
                                new StarterIngredient("Sugar", 2, "tsp", false),
                                new StarterIngredient("Butter", 1, "tbsp", false)),
                        Arrays.asList(
                                "Whisk the flour, the baking powder and the sugar together in a bowl.",
                                "Beat the eggs into the milk, then pour them into the dry ingredients "
                                        + "and whisk until the batter is just combined.",
                                "Melt the butter in a non-stick frying pan over medium heat.",
                                "Pour small rounds of batter into the pan and cook until bubbles "
                                        + "appear on the surface.",
                                "Flip the pancakes and cook them for one minute more, then serve.")),
                new StarterRecipe(
                        "Rosemary Breakfast Potatoes",
                        "Crisp oven potatoes that turn a handful of staples into a proper brunch.",
                        10, 25, 3,
                        "https://www.themealdb.com/images/media/meals/1550441882.jpg",
                        Arrays.asList(
                                new StarterIngredient("Potatoes", 4, "piece", false),
                                new StarterIngredient("Olive oil", 2, "tbsp", false),
                                new StarterIngredient("Garlic", 1, "clove", false),
                                new StarterIngredient("Rosemary", 1, "tsp", false),
                                new StarterIngredient("Salt", 1, "tsp", true)),
                        Arrays.asList(
                                "Heat the oven to 200 C and cut the potatoes into bite-sized pieces.",
                                "Toss the potatoes with the olive oil, the minced garlic, the rosemary "
                                        + "and the salt.",
                                "Spread the potatoes on a baking tray so that they sit in a single layer.",
                                "Roast them for 25 minutes, turning once, until they are golden and crisp.")),
                new StarterRecipe(
                        "Spicy Arrabiata Penne",
                        "A quick pasta dinner built around pantry staples.",
                        10, 15, 4,
                        "https://www.themealdb.com/images/media/meals/ustsqw1468250014.jpg",
                        Arrays.asList(
                                new StarterIngredient("Penne pasta", 200, "g", false),
                                new StarterIngredient("Olive oil", 1, "tbsp", false),
                                new StarterIngredient("Garlic", 2, "clove", false),
                                new StarterIngredient("Chopped tomatoes", 1, "can", false),
                                new StarterIngredient("Chilli flakes", 0.5, "tsp", true),
                                new StarterIngredient("Basil", 1, "tsp", true)),
                        Arrays.asList(
                                "Cook the pasta in plenty of boiling water according to the instructions "
                                        + "on the packet.",
                                "Warm the olive oil in a pan and cook the sliced garlic until fragrant "
                                        + "but not coloured.",
                                "Add the chopped tomatoes and the chilli flakes, then simmer for 10 minutes.",
                                "Drain the pasta, toss it through the sauce and finish with the basil.")),
                new StarterRecipe(
                        "Tomato and Egg Stir-Fry",
                        "A fifteen minute lunch or dinner that uses almost nothing but staples.",
                        5, 10, 2,
                        "https://www.themealdb.com/images/media/meals/rwvw8q1765660071.jpg",
                        Arrays.asList(
                                new StarterIngredient("Eggs", 3, "piece", false),
                                new StarterIngredient("Tomatoes", 2, "piece", false),
                                new StarterIngredient("Vegetable oil", 1, "tbsp", false),
                                new StarterIngredient("Garlic", 1, "clove", false),
                                new StarterIngredient("Soy sauce", 1, "tbsp", false),
                                new StarterIngredient("Sugar", 1, "tsp", true)),
                        Arrays.asList(
                                "Beat the eggs in a bowl with a pinch of salt.",
                                "Heat the oil in a wok, add the eggs and stir them until they are "
                                        + "barely set, then take them out of the pan.",
                                "Add the garlic and the chopped tomatoes and cook them until the "
                                        + "tomatoes start to soften.",
                                "Return the eggs to the pan, add the soy sauce and toss everything "
                                        + "together for one minute.")),
                new StarterRecipe(
                        "Classic Potato Salad",
                        "A cool, no-cook lunch that is easy to prepare ahead.",
                        15, 20, 4,
                        "https://www.themealdb.com/images/media/meals/5q88l31784664151.jpg",
                        Arrays.asList(
                                new StarterIngredient("Potatoes", 4, "piece", false),
                                new StarterIngredient("Eggs", 2, "piece", false),
                                new StarterIngredient("Onion", 1, "piece", false),
                                new StarterIngredient("Mayonnaise", 4, "tbsp", false),
                                new StarterIngredient("Mustard", 1, "tsp", false),
                                new StarterIngredient("Salt", 1, "tsp", true)),
                        Arrays.asList(
                                "Boil the potatoes until they are tender, then drain, cool and cut "
                                        + "them into chunks.",
                                "Boil the eggs for 10 minutes, cool them under cold water and slice them.",
                                "Finely chop the onion and mix it with the mayonnaise, the mustard "
                                        + "and the salt.",
                                "Fold the dressing through the potatoes and the eggs, then chill the "
                                        + "salad before serving.")));
    }




    public void seed()
    {
        retireSupersededRecipes();
        for (StarterRecipe preset : getStarterRecipes())
        {
            Recipe recipe = new Recipe(preset.getName(), preset.getDescription(),
                    preset.getPrepMinutes(), preset.getCookMinutes(), preset.getServings());
            Recipe existingRecipe = recipeRepository.getRecipeByName(preset.getName());
            if (existingRecipe != null)
            {
                recipe.setId(existingRecipe.getId());
            }
            recipe.setImageReference(preset.getImageUrl());
            recipe.setImageSource(IMAGE_SOURCE_PRESET);
            List<RecipeIngredient> ingredients = new ArrayList<>();
            for (StarterIngredient requirement : preset.getIngredients())
            {
                Ingredient ingredient = pantryRepository.findOrCreateIngredient(requirement.getName());
                ingredients.add(new RecipeIngredient(PENDING_RECIPE_ID, ingredient.getId(),
                        requirement.getQuantity(), requirement.getUnit(), requirement.isOptional(), null));
            }
            List<RecipeStep> steps = new ArrayList<>();
            for (int index = 0; index < preset.getSteps().size(); index++)
            {
                steps.add(new RecipeStep(PENDING_RECIPE_ID, index + 1, preset.getSteps().get(index)));
            }
            recipeRepository.saveCompleteRecipe(recipe, ingredients, steps);
        }
    }





    private void retireSupersededRecipes()
    {
        Set<String> currentNames = new LinkedHashSet<>();
        for (String name : getStarterRecipeNames())
        {
            currentNames.add(normalize(name));
        }
        Set<String> legacyNames = new LinkedHashSet<>();
        for (String name : LEGACY_STARTER_RECIPE_NAMES)
        {
            legacyNames.add(normalize(name));
        }
        for (Recipe recipe : recipeRepository.getAllActiveRecipes())
        {
            if (recipe.getId() == null || currentNames.contains(normalize(recipe.getName())))
            {
                continue;
            }
            boolean wasPreset = legacyNames.contains(normalize(recipe.getName()))
                    || IMAGE_SOURCE_PRESET.equalsIgnoreCase(recipe.getImageSource())
                    || IMAGE_SOURCE_LEGACY_THEMEALDB.equalsIgnoreCase(recipe.getImageSource());
            if (wasPreset)
            {
                recipeRepository.setRecipeActive(recipe.getId(), false);
            }
        }
    }

    private static String normalize(String name)
    {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }




    public static final class StarterRecipe
    {
        private final String name;
        private final String description;
        private final int prepMinutes;
        private final int cookMinutes;
        private final int servings;
        private final String imageUrl;
        private final List<StarterIngredient> ingredients;
        private final List<String> steps;

        private StarterRecipe(String name, String description, int prepMinutes, int cookMinutes,
                              int servings, String imageUrl, List<StarterIngredient> ingredients,
                              List<String> steps)
        {
            this.name = name;
            this.description = description;
            this.prepMinutes = prepMinutes;
            this.cookMinutes = cookMinutes;
            this.servings = servings;
            this.imageUrl = imageUrl;
            this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public int getPrepMinutes()
        {
            return prepMinutes;
        }

        public int getCookMinutes()
        {
            return cookMinutes;
        }

        public int getServings()
        {
            return servings;
        }

        public int getTotalMinutes()
        {
            return prepMinutes + cookMinutes;
        }

        public String getImageUrl()
        {
            return imageUrl;
        }

        public List<StarterIngredient> getIngredients()
        {
            return ingredients;
        }

        public List<String> getSteps()
        {
            return steps;
        }
    }




    public static final class StarterIngredient
    {
        private final String name;
        private final double quantity;
        private final String unit;
        private final boolean optional;

        private StarterIngredient(String name, double quantity, String unit, boolean optional)
        {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.optional = optional;
        }

        public String getName()
        {
            return name;
        }

        public double getQuantity()
        {
            return quantity;
        }

        public String getUnit()
        {
            return unit;
        }

        public boolean isOptional()
        {
            return optional;
        }
    }
}
