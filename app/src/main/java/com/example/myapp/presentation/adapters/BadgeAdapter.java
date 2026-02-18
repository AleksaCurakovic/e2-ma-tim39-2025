package com.example.myapp.presentation.adapters;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<String> badges;

    public BadgeAdapter(List<String> badges) {
        this.badges = badges;
    }

    public void updateBadges(List<String> newBadges) {
        this.badges = newBadges;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        holder.bind(badges.get(position));
    }

    @Override public int getItemCount() { return badges.size(); }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBadge;
        TextView tvBadgeName;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBadge    = itemView.findViewById(R.id.imgBadge);
            tvBadgeName = itemView.findViewById(R.id.tvBadgeName);
        }

        void bind(String badgeId) {
            tvBadgeName.setText(badgeId);
            // Postavi ikonicu na osnovu badgeId
            int resId = itemView.getContext().getResources()
                    .getIdentifier("badge_" + badgeId, "drawable",
                            itemView.getContext().getPackageName());
            if (resId != 0) {
                imgBadge.setImageResource(resId);
            } else {
                imgBadge.setImageResource(R.drawable.horns);
            }
        }
    }
}