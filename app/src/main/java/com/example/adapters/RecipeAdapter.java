package com.example.smartpantrymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.models.Recipe;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder>
{
    public interface OnItemClickListener
    {
        void onItemClick(Recipe recipe);
    }

    private final List<Recipe> items = new ArrayList<>();
    private OnItemClickListener listener;

    public RecipeAdapter(List<Recipe> items)
    {
        setItems(items);
    }

    public void setItems(List<Recipe> newItems)
    {
        if (newItems == null)
        {
            throw new IllegalArgumentException("items must not be null.");
        }
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    public Recipe getItem(int position)
    {
        if (position < 0 || position >= items.size())
        {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Recipe recipe = items.get(position);
        holder.name.setText(recipe.getName());
        StringBuilder metadata = new StringBuilder();
        if (recipe.getPrepTimeMinutes() != null)
        {
            metadata.append(holder.itemView.getContext().getString(R.string.time_minutes, recipe.getPrepTimeMinutes()));
        }
        if (recipe.getCookTimeMinutes() != null)
        {
            if (metadata.length() > 0)
            {
                metadata.append(" | ");
            }
            metadata.append(holder.itemView.getContext().getString(R.string.time_minutes, recipe.getCookTimeMinutes()));
        }
        if (recipe.getServings() != null)
        {
            if (metadata.length() > 0)
            {
                metadata.append(" | ");
            }
            metadata.append(holder.itemView.getContext().getResources().getQuantityString(
                    R.plurals.servings, recipe.getServings(), recipe.getServings()));
        }
        holder.metadata.setText(metadata.toString());
        if (recipe.getDescription() == null)
        {
            holder.description.setVisibility(View.GONE);
        }
        else
        {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(recipe.getDescription());
        }
        holder.itemView.setOnClickListener(view ->
        {
            if (listener != null)
            {
                listener.onItemClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        final TextView name;
        final TextView metadata;
        final TextView description;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_item_name);
            metadata = itemView.findViewById(R.id.recipe_item_metadata);
            description = itemView.findViewById(R.id.recipe_item_description);
        }
    }
}
