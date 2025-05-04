package com.meteo_app_java.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meteo_app_java.R;
import com.meteo_app_java.databinding.ItemLocationBinding;
import com.meteo_app_java.models.SavedLocation;

public class LocationAdapter extends ListAdapter<SavedLocation, LocationAdapter.LocationViewHolder> {

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SavedLocation location);

        void onFavoriteClick(SavedLocation location);

        void onRemoveClick(SavedLocation location);
    }

    public LocationAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SavedLocation> DIFF_CALLBACK = new DiffUtil.ItemCallback<SavedLocation>() {
        @Override
        public boolean areItemsTheSame(@NonNull SavedLocation oldItem, @NonNull SavedLocation newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SavedLocation oldItem, @NonNull SavedLocation newItem) {
            return oldItem.getCityName().equals(newItem.getCityName())
                    && oldItem.getCountryCode().equals(newItem.getCountryCode())
                    && oldItem.isFavorite() == newItem.isFavorite();
        }
    };

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLocationBinding binding = ItemLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        SavedLocation location = getItem(position);
        holder.bind(location);
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private final ItemLocationBinding binding;

        public LocationViewHolder(@NonNull ItemLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });

            binding.ivFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFavoriteClick(getItem(position));
                }
            });
        }

        public void bind(SavedLocation location) {
            // Set location name and country
            String locationText = location.getCityName();
            if (location.getCountryCode() != null && !location.getCountryCode().isEmpty()) {
                locationText += ", " + location.getCountryCode();
            }
            binding.tvLocationName.setText(locationText);

            // Set coordinates
            String coordinates = String.format("%.2f, %.2f",
                    location.getLatitude(), location.getLongitude());
            binding.tvLocationCoordinates.setText(coordinates);

            // Set favorite icon
            binding.ivFavorite
                    .setImageResource(location.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }
}