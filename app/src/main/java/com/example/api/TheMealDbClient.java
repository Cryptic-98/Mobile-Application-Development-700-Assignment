package com.example.smartpantrymanager.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.smartpantrymanager.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



















public final class TheMealDbClient
{
    private static final String TAG = "TheMealDbClient";
    private static final String DEFAULT_BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static final long CACHE_FRESH_MILLIS = 24L * 60L * 60L * 1000L;
    private static final long CACHE_STALE_MILLIS = 30L * CACHE_FRESH_MILLIS;
    private static final long MEMORY_CACHE_MILLIS = 6L * 60L * 60L * 1000L;
    private static final int MAX_RESPONSE_BYTES = 2_097_152;
    private static final int CONNECT_TIMEOUT_MILLIS = 7000;
    private static final int READ_TIMEOUT_MILLIS = 7000;

    private static final ExecutorService CALLBACK_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
    private static final Map<String, MemoryEntry<Map<String, String>>> VOCABULARY_MEMORY_CACHE =
            new HashMap<>();
    private static final Map<String, MemoryEntry<TheMealDbMeal>> MEAL_MEMORY_CACHE = new HashMap<>();

    private final String baseUrl;
    private final File cacheDirectory;




    public TheMealDbClient(Context context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context must not be null.");
        }
        this.baseUrl = configuredBaseUrl();
        this.cacheDirectory = new File(context.getApplicationContext().getCacheDir(), "themealdb");
    }





    TheMealDbClient(String baseUrl, File cacheDirectory)
    {
        if (baseUrl == null || baseUrl.trim().isEmpty() || cacheDirectory == null)
        {
            throw new IllegalArgumentException("A base URL and cache directory are required.");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.cacheDirectory = cacheDirectory;
    }






    public Map<String, String> getIngredientVocabulary() throws IOException
    {
        MemoryEntry<Map<String, String>> cached = VOCABULARY_MEMORY_CACHE.get(baseUrl);
        if (cached != null && !cached.isExpired())
        {
            return cached.value;
        }
        Map<String, String> vocabulary;
        try
        {
            vocabulary = TheMealDbParser
                    .parseIngredientVocabulary(readJson(url("list.php?i=list")));
        }
        catch (org.json.JSONException exception)
        {
            throw new IOException("TheMealDB returned an unreadable ingredient vocabulary.", exception);
        }
        VOCABULARY_MEMORY_CACHE.put(baseUrl, new MemoryEntry<>(vocabulary));
        return vocabulary;
    }





    public List<String> getMealIdsForIngredient(String ingredientName) throws IOException
    {
        if (ingredientName == null || ingredientName.trim().isEmpty())
        {
            throw new IllegalArgumentException("ingredientName must not be blank.");
        }
        String url = url("filter.php?i=" + encode(ingredientName));
        try
        {
            return TheMealDbParser.parseMealIds(readJson(url));
        }
        catch (org.json.JSONException exception)
        {
            throw new IOException("TheMealDB returned an unreadable ingredient filter.", exception);
        }
    }





    public TheMealDbMeal getMealById(String mealId) throws IOException
    {
        if (mealId == null || mealId.trim().isEmpty())
        {
            throw new IllegalArgumentException("mealId must not be blank.");
        }
        String cacheKey = baseUrl + "lookup.php?i=" + mealId.trim();
        MemoryEntry<TheMealDbMeal> cached = MEAL_MEMORY_CACHE.get(cacheKey);
        if (cached != null && !cached.isExpired())
        {
            return cached.value;
        }
        TheMealDbMeal meal;
        try
        {
            meal = TheMealDbParser
                    .parseMealResponse(readJson(url("lookup.php?i=" + encode(mealId.trim()))));
        }
        catch (org.json.JSONException exception)
        {
            throw new IOException("TheMealDB returned an unreadable meal.", exception);
        }
        if (meal != null)
        {
            MEAL_MEMORY_CACHE.put(cacheKey, new MemoryEntry<>(meal));
        }
        return meal;
    }







    public void loadMealByIdAsync(String mealId, MealCallback callback)
    {
        if (mealId == null || mealId.trim().isEmpty() || callback == null)
        {
            throw new IllegalArgumentException("A meal id and callback are required.");
        }
        CALLBACK_EXECUTOR.execute(() ->
        {
            try
            {
                TheMealDbMeal meal = getMealById(mealId);
                MAIN_THREAD.post(() -> callback.onLoaded(meal));
            }
            catch (IOException exception)
            {
                Log.e(TAG, "Unable to load TheMealDB meal " + mealId, exception);
                MAIN_THREAD.post(() -> callback.onFailure(exception));
            }
        });
    }




    public String getBaseUrl()
    {
        return baseUrl;
    }

    private static String configuredBaseUrl()
    {
        String configured = BuildConfig.THEMEALDB_BASE_URL;
        if (configured == null || configured.trim().isEmpty())
        {
            return DEFAULT_BASE_URL;
        }
        String trimmed = configured.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private String url(String path)
    {
        return baseUrl + path;
    }




    private String readJson(String address) throws IOException
    {
        File cacheFile = cacheFile(address);
        long now = System.currentTimeMillis();
        String staleResponse = null;
        if (cacheFile.isFile())
        {
            long age = now - cacheFile.lastModified();
            if (age >= 0 && age <= CACHE_FRESH_MILLIS)
            {
                return readFile(cacheFile);
            }
            if (age >= 0 && age <= CACHE_STALE_MILLIS)
            {
                staleResponse = readFile(cacheFile);
            }
        }

        IOException requestFailure = null;
        try
        {
            String response = request(address);
            writeCache(cacheFile, response);
            return response;
        }
        catch (IOException exception)
        {
            requestFailure = exception;
        }
        if (staleResponse != null)
        {
            Log.w(TAG, "Using a cached TheMealDB response while offline.", requestFailure);
            return staleResponse;
        }
        throw requestFailure;
    }

    private static String request(String address) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "CannedHope/1.0");
        try
        {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
            {
                throw new IOException("TheMealDB returned HTTP " + responseCode + ".");
            }
            try (InputStream stream = connection.getInputStream())
            {
                return readResponse(stream);
            }
        }
        finally
        {
            connection.disconnect();
        }
    }

    private static String readResponse(InputStream input) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1)
        {
            total += read;
            if (total > MAX_RESPONSE_BYTES)
            {
                throw new IOException("TheMealDB response exceeded the allowed size.");
            }
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static String readFile(File file) throws IOException
    {
        try (InputStream input = new FileInputStream(file))
        {
            return readResponse(input);
        }
    }

    private void writeCache(File file, String response)
    {
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        try
        {
            if (!cacheDirectory.isDirectory() && !cacheDirectory.mkdirs())
            {
                throw new IOException("Unable to create the TheMealDB cache directory.");
            }
            try (FileOutputStream output = new FileOutputStream(temporary))
            {
                output.write(response.getBytes(StandardCharsets.UTF_8));
                output.getFD().sync();
            }
            if (!temporary.renameTo(file))
            {
                if (file.exists() && file.delete() && temporary.renameTo(file))
                {
                    return;
                }
                throw new IOException("Unable to store a cached TheMealDB response.");
            }
        }
        catch (IOException exception)
        {
            Log.w(TAG, "Could not cache a TheMealDB response.", exception);
            if (temporary.exists() && !temporary.delete())
            {
                Log.w(TAG, "Could not remove the incomplete TheMealDB cache file.");
            }
        }
    }

    private File cacheFile(String address) throws IOException
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(address.getBytes(StandardCharsets.UTF_8));
            StringBuilder filename = new StringBuilder();
            for (byte value : digest)
            {
                filename.append(String.format("%02x", value));
            }
            return new File(cacheDirectory, filename.toString() + ".json");
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IOException("SHA-256 is unavailable for the TheMealDB cache.", exception);
        }
    }

    private static String encode(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException exception)
        {
            throw new IllegalStateException("UTF-8 is unavailable.", exception);
        }
    }

    public interface MealCallback
    {
        void onLoaded(TheMealDbMeal meal);

        void onFailure(Exception exception);
    }





    private static final class MemoryEntry<T>
    {
        private final T value;
        private final long storedAt;

        private MemoryEntry(T value)
        {
            this.value = value;
            this.storedAt = System.currentTimeMillis();
        }

        private boolean isExpired()
        {
            long age = System.currentTimeMillis() - storedAt;
            return age < 0 || age > MEMORY_CACHE_MILLIS;
        }
    }
}
