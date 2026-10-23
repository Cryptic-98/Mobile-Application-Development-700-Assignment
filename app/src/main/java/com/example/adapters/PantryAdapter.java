package com.example.smartpantrymanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.models.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder>
{
    public interface OnItemClickListener
    {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public PantryAdapter(List<PantryItem> items)
    {
        setItems(items);
    }

    public void setItems(List<PantryItem> newItems)
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

    public PantryItem getItem(int position)
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        PantryItem item = items.get(position);
        holder.name.setText(item.getIngredientName() == null
                ? "Ingredient " + item.getIngredientId() : item.getIngredientName());
        holder.quantity.setText(String.format(Locale.getDefault(), "%s %s",
                formatQuantity(item.getQuantity()), item.getUnitCode()));
        if (item.getExpiryDate() == null)
        {
            holder.expiry.setText(R.string.no_expiry);
        }
        else if (item.getExpiryDate().isBefore(LocalDate.now()))
        {
            holder.expiry.setText(holder.itemView.getContext().getString(
                    R.string.expiry_expired, item.getExpiryDate()));
            holder.expiry.setTextColor(holder.itemView.getContext().getColor(R.color.color_error));
        }
        else if (item.getExpiryDate().isEqual(LocalDate.now()))
        {
            holder.expiry.setText(R.string.expiry_today);
            holder.expiry.setTextColor(holder.itemView.getContext().getColor(R.color.color_error));
        }
        else
        {
            holder.expiry.setText(holder.itemView.getContext().getString(R.string.expires_on, item.getExpiryDate()));
            holder.expiry.setTextColor(holder.itemView.getContext().getColor(R.color.color_text_secondary));
        }
        holder.itemView.setOnClickListener(view ->
        {
            if (listener != null)
            {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    private static String formatQuantity(double quantity)
    {
        return quantity == Math.rint(quantity)
                ? String.format(Locale.getDefault(), "%.0f", quantity)
                : String.format(Locale.getDefault(), "%.2f", quantity);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        final TextView name;
        final TextView quantity;
        final TextView expiry;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.pantry_item_name);
            quantity = itemView.findViewById(R.id.pantry_item_quantity);
            expiry = itemView.findViewById(R.id.pantry_item_expiry);
        }
    }
}
