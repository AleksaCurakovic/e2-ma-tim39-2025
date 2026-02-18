package com.example.myapp.presentation.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface CategoryClickListener {
        void onChangeColor(Category category);
        void onDelete(Category category);
    }

    private List<Category> categories;
    private final CategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, CategoryClickListener listener) {
        this.categories = categories;
        this.listener   = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position), listener);
    }

    @Override public int getItemCount() { return categories.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvName;
        ImageButton btnChangeColor, btnDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor       = itemView.findViewById(R.id.viewCategoryColor);
            tvName          = itemView.findViewById(R.id.tvCategoryName);
            btnChangeColor  = itemView.findViewById(R.id.btnChangeColor);
            btnDelete       = itemView.findViewById(R.id.btnDeleteCategory);
        }

        void bind(Category category, CategoryClickListener listener) {
            tvName.setText(category.getName());

            // Postavi boju kruga
            try {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setColor(Color.parseColor(category.getColor()));
                viewColor.setBackground(drawable);
            } catch (Exception e) {
                viewColor.setBackgroundColor(Color.GRAY);
            }

            btnChangeColor.setOnClickListener(v -> listener.onChangeColor(category));
            btnDelete.setOnClickListener(v -> listener.onDelete(category));
        }
    }
}