package com.example.smartpantrymanager;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;









final class ThemealdbImageResolver
{
    private static final String TAG = "ThemealdbImageResolver";
    private static final int MAX_RESPONSE_BYTES = 1_048_576;
    private static final Map<String, String> RESOLVED = new HashMap<>();

    private ThemealdbImageResolver()
    {
    }





    static String resolve(String request)
    {
        if (request == null || request.trim().isEmpty())
        {
            return null;
        }
        String cached = RESOLVED.get(request);
        if (cached != null)
        {
            return cached;
        }
        try
        {
            String imageUrl = readFirstThumbnail(request);
            if (imageUrl != null)
            {
                RESOLVED.put(request, imageUrl);
            }
            return imageUrl;
        }
        catch (IOException | org.json.JSONException exception)
        {
            Log.w(TAG, "Could not resolve the recipe picture for " + request, exception);
            return null;
        }
    }

    private static String readFirstThumbnail(String request) throws IOException, org.json.JSONException
    {
        URL url = new URL(request);
        if (!"https".equalsIgnoreCase(url.getProtocol())
                || !"www.themealdb.com".equalsIgnoreCase(url.getHost())
                || !url.getPath().startsWith("/api/json/"))
        {
            throw new IOException("Recipe image references must use TheMealDB's search API.");
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(7000);
        connection.setReadTimeout(7000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "CannedHope/1.0");
        try
        {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                throw new IOException("TheMealDB returned HTTP " + connection.getResponseCode());
            }
            try (InputStream stream = connection.getInputStream())
            {
                String body = readBody(stream);
                JSONArray meals = new JSONObject(body).optJSONArray("meals");
                if (meals == null)
                {
                    return null;
                }
                for (int index = 0; index < meals.length(); index++)
                {
                    JSONObject meal = meals.optJSONObject(index);
                    if (meal == null)
                    {
                        continue;
                    }
                    String thumb = meal.optString("strMealThumb", "").trim();
                    if (!thumb.isEmpty() && thumb.startsWith("https://www.themealdb.com/"))
                    {
                        return thumb;
                    }
                }
                return null;
            }
        }
        finally
        {
            connection.disconnect();
        }
    }

    private static String readBody(InputStream stream) throws IOException
    {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)))
        {
            String line;
            int length = 0;
            while ((line = reader.readLine()) != null)
            {
                length += line.length();
                if (length > MAX_RESPONSE_BYTES)
                {
                    throw new IOException("TheMealDB response exceeded the allowed size.");
                }
                body.append(line);
            }
        }
        return body.toString();
    }
}
