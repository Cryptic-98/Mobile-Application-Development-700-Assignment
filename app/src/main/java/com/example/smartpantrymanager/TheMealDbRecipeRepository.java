package com.example.smartpantrymanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.smartpantrymanager.api.TheMealDbClient;
import com.example.smartpantrymanager.api.TheMealDbMeal;
import com.example.smartpantrymanager.database.PantryRepository;
import com.example.smartpantrymanager.logic.IngredientVocabularyMatcher;
import com.example.smartpantrymanager.logic.TheMealDbMatch;
import com.example.smartpantrymanager.logic.TheMealDbMatcher;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


















public final class TheMealDbRecipeRepository
{
    private static final String TAG = "TheMealDbRecipes";


    static final int MAX_SEARCH_TERMS_PER_INGREDIENT = 3;


    static final int MAX_DETAIL_LOOKUPS = 24;


    static final int MAX_SEARCH_TERMS = 40;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final ExecutorService REQUEST_EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private final PantryRepository pantryRepository;
    private final TheMealDbClient client;
    private final TheMealDbMatcher matcher;

    public TheMealDbRecipeRepository(Context context, PantryRepository pantryRepository)
    {
        if (context == null || pantryRepository == null)
        {
            throw new IllegalArgumentException("Context and pantry repository are required.");
        }
        this.pantryRepository = pantryRepository;
        client = new TheMealDbClient(context);
        matcher = new TheMealDbMatcher(pantryRepository);
    }

    TheMealDbRecipeRepository(PantryRepository pantryRepository, TheMealDbClient client,
                              TheMealDbMatcher matcher)
    {
        if (pantryRepository == null || client == null || matcher == null)
        {
            throw new IllegalArgumentException("Repositories, client and matcher are required.");
        }
        this.pantryRepository = pantryRepository;
        this.client = client;
        this.matcher = matcher;
    }






    public void discoverAsync(Callback callback)
    {
        if (callback == null)
        {
            throw new IllegalArgumentException("callback must not be null.");
        }
        EXECUTOR.execute(() ->
        {
            try
            {
                DiscoveryResult result = discover();
                MAIN_THREAD.post(() -> callback.onLoaded(result.getMatches(), result.hadErrors()));
            }
            catch (Exception exception)
            {
                Log.e(TAG, "Unable to discover recipes from TheMealDB.", exception);
                MAIN_THREAD.post(() -> callback.onFailure(exception));
            }
        });
    }





    DiscoveryResult discover() throws Exception
    {
        List<String> pantryNames = readUsablePantryNames();
        List<TheMealDbMatch> matches = new ArrayList<>();
        boolean hadErrors = false;
        if (!pantryNames.isEmpty())
        {
            try
            {
                Map<String, String> vocabulary = client.getIngredientVocabulary();
                List<String> searchTerms = IngredientVocabularyMatcher.searchTerms(vocabulary,
                        pantryNames, MAX_SEARCH_TERMS_PER_INGREDIENT);
                List<TheMealDbMeal> meals = fetchMeals(searchTerms);
                matches = matcher.getRecommendations(meals);
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
                throw exception;
            }
            catch (Exception exception)
            {
                hadErrors = true;
                Log.e(TAG, "TheMealDB discovery failed.", exception);
            }
        }
        return new DiscoveryResult(matches, hadErrors);
    }




    List<String> readUsablePantryNames()
    {
        LocalDate today = LocalDate.now();
        List<String> names = new ArrayList<>();
        for (PantryItem item : pantryRepository.getAllPantryItems())
        {
            if (item.getIngredientName() == null || item.getIngredientName().trim().isEmpty())
            {
                continue;
            }
            if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(today))
            {
                continue;
            }
            String name = item.getIngredientName().trim();
            if (!names.contains(name))
            {
                names.add(name);
            }
        }
        return names;
    }




    private List<TheMealDbMeal> fetchMeals(List<String> searchTerms)
            throws InterruptedException, java.io.IOException
    {
        if (searchTerms.isEmpty())
        {
            return Collections.emptyList();
        }
        List<String> terms = searchTerms.size() > MAX_SEARCH_TERMS
                ? searchTerms.subList(0, MAX_SEARCH_TERMS) : searchTerms;

        List<Callable<FilterResult>> filterCalls = new ArrayList<>();
        for (String term : terms)
        {
            filterCalls.add(() -> new FilterResult(term, client.getMealIdsForIngredient(term)));
        }
        Map<String, Integer> candidateHits = new LinkedHashMap<>();
        boolean hadErrors = false;
        for (Future<FilterResult> future : REQUEST_EXECUTOR.invokeAll(filterCalls))
        {
            try
            {
                FilterResult filter = future.get();
                for (String mealId : filter.mealIds)
                {
                    candidateHits.merge(mealId, 1, Integer::sum);
                }
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
                throw exception;
            }
            catch (Exception exception)
            {
                hadErrors = true;
                Log.w(TAG, "A TheMealDB ingredient filter failed.", exception);
            }
        }
        if (hadErrors && candidateHits.isEmpty())
        {
            throw new java.io.IOException("TheMealDB could not return any recipe candidates.");
        }

        List<String> identifiers = new ArrayList<>(candidateHits.keySet());
        identifiers.sort(Comparator.comparingInt((String id) -> candidateHits.get(id)).reversed()
                .thenComparing(Comparator.naturalOrder()));
        if (identifiers.size() > MAX_DETAIL_LOOKUPS)
        {
            identifiers = new ArrayList<>(identifiers.subList(0, MAX_DETAIL_LOOKUPS));
        }

        List<Callable<TheMealDbMeal>> detailCalls = new ArrayList<>();
        for (String identifier : identifiers)
        {
            detailCalls.add(() -> client.getMealById(identifier));
        }
        List<TheMealDbMeal> meals = new ArrayList<>();
        for (Future<TheMealDbMeal> future : REQUEST_EXECUTOR.invokeAll(detailCalls))
        {
            try
            {
                TheMealDbMeal meal = future.get();
                if (meal != null)
                {
                    meals.add(meal);
                }
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
                throw exception;
            }
            catch (Exception exception)
            {
                hadErrors = true;
                Log.w(TAG, "A TheMealDB meal detail request failed.", exception);
            }
        }
        return meals;
    }

    public void loadMealByIdAsync(String mealId, TheMealDbClient.MealCallback callback)
    {
        client.loadMealByIdAsync(mealId, callback);
    }

    public TheMealDbMatch evaluateMeal(TheMealDbMeal meal)
    {
        return matcher.evaluateMeal(meal);
    }

    public void evaluateMealAsync(TheMealDbMeal meal, MatchCallback callback)
    {
        if (meal == null || callback == null)
        {
            throw new IllegalArgumentException("Meal and callback are required.");
        }
        EXECUTOR.execute(() ->
        {
            try
            {
                TheMealDbMatch result = matcher.evaluateMeal(meal);
                MAIN_THREAD.post(() -> callback.onEvaluated(result));
            }
            catch (RuntimeException exception)
            {
                Log.e(TAG, "Unable to compare the recipe with pantry quantities.", exception);
                MAIN_THREAD.post(() -> callback.onFailure(exception));
            }
        });
    }

    public interface Callback
    {
        void onLoaded(List<TheMealDbMatch> matches, boolean hadErrors);

        void onFailure(Exception exception);
    }

    public interface MatchCallback
    {
        void onEvaluated(TheMealDbMatch match);

        void onFailure(Exception exception);
    }




    public static final class DiscoveryResult
    {
        private final List<TheMealDbMatch> matches;
        private final boolean hadErrors;

        DiscoveryResult(List<TheMealDbMatch> matches, boolean hadErrors)
        {
            this.matches = Collections.unmodifiableList(new ArrayList<>(matches));
            this.hadErrors = hadErrors;
        }

        public List<TheMealDbMatch> getMatches()
        {
            return matches;
        }

        public boolean hadErrors()
        {
            return hadErrors;
        }
    }

    private static final class FilterResult
    {
        private final String term;
        private final List<String> mealIds;

        private FilterResult(String term, List<String> mealIds)
        {
            this.term = term;
            this.mealIds = mealIds;
        }

        @Override
        public String toString()
        {
            return term + " -> " + mealIds.size() + " meals";
        }
    }
}
