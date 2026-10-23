package com.example.smartpantrymanager;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.smartpantrymanager.models.Recipe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



















public final class FoodImageLoader
{
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private FoodImageLoader()
    {
    }







    public static void load(ImageView imageView, Recipe recipe)
    {
        if (imageView == null || recipe == null)
        {
            return;
        }
        String reference = recipe.getImageReference();
        if (reference == null || reference.trim().isEmpty())
        {
            Glide.with(imageView).clear(imageView);
            imageView.setImageResource(R.drawable.recipe_placeholder);
            return;
        }
        if (isSearchRequest(recipe))
        {
            loadFromSearchRequest(imageView, recipe, reference.trim());
            return;
        }
        Glide.with(imageView)
                .load(reference.trim())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_placeholder)
                .fallback(R.drawable.recipe_placeholder)
                .into(imageView);
    }





    private static boolean isSearchRequest(Recipe recipe)
    {
        String source = recipe.getImageSource();
        if (source == null)
        {
            return false;
        }
        if (!"themealdb".equalsIgnoreCase(source))
        {
            return false;
        }
        String reference = recipe.getImageReference();
        return reference != null && reference.contains("/search.php");
    }




    private static void loadFromSearchRequest(ImageView imageView, Recipe recipe, String request)
    {
        Glide.with(imageView).clear(imageView);
        imageView.setImageResource(R.drawable.recipe_placeholder);
        imageView.setTag(request);
        EXECUTOR.execute(() ->
        {
            String imageUrl = ThemealdbImageResolver.resolve(request);
            if (imageUrl == null)
            {
                return;
            }
            imageView.post(() ->
            {
                if (!request.equals(imageView.getTag()))
                {
                    return;
                }
                Glide.with(imageView)
                        .load(imageUrl)
                        .placeholder(R.drawable.recipe_placeholder)
                        .error(R.drawable.recipe_placeholder)
                        .into(imageView);
            });
        });
    }
}
