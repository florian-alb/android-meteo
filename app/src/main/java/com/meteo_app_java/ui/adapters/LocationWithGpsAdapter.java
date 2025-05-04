package com.meteo_app_java.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meteo_app_java.R;
import com.meteo_app_java.databinding.ItemGpsLocationBinding;
import com.meteo_app_java.databinding.ItemLocationBinding;
import com.meteo_app_java.models.SavedLocation;

import java.util.ArrayList;
import java.util.List;

public class LocationWithGpsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GPS = 0;
    private static final int VIEW_TYPE_LOCATION = 1;

    private List<SavedLocation> locations = new ArrayList<>();
    private SavedLocation gpsLocation;
    private double gpsLatitude, gpsLongitude;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onGpsLocationClick();

        void onLocationClick(SavedLocation location);

        void onFavoriteClick(SavedLocation location);

        void onRemoveClick(SavedLocation location);
    }

    public LocationWithGpsAdapter(double gpsLatitude, double gpsLongitude) {
        this.gpsLatitude = gpsLatitude;
        this.gpsLongitude = gpsLongitude;

        // Create the GPS location item
        gpsLocation = new SavedLocation();
        gpsLocation.setCityName("Current Location (GPS)");
        gpsLocation.setLatitude(gpsLatitude);
        gpsLocation.setLongitude(gpsLongitude);
        // Set ID to -1 to distinguish from regular saved locations
        gpsLocation.setId(-1);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLocations(List<SavedLocation> locations) {
        this.locations = new ArrayList<>(locations);
        // Update GPS location coordinates
        gpsLocation.setLatitude(gpsLatitude);
        gpsLocation.setLongitude(gpsLongitude);
        notifyDataSetChanged();
    }

    public void updateGpsCoordinates(double latitude, double longitude) {
        this.gpsLatitude = latitude;
        this.gpsLongitude = longitude;
        gpsLocation.setLatitude(latitude);
        gpsLocation.setLongitude(longitude);
        notifyItemChanged(0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_GPS;
        } else {
            return VIEW_TYPE_LOCATION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_GPS) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_gps_location, parent, false);
            return new GpsLocationViewHolder(view);
        } else {
            ItemLocationBinding binding = ItemLocationBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new LocationViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_GPS) {
            ((GpsLocationViewHolder) holder).bind(gpsLocation);
        } else {
            // Adjust position for the locations list (subtract 1 for GPS item)
            ((LocationViewHolder) holder).bind(locations.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        // +1 for GPS location item
        return locations.size() + 1;
    }

    class GpsLocationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLocationCoordinates;

        public GpsLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvLocationCoordinates = itemView.findViewById(R.id.tv_location_coordinates);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGpsLocationClick();
                }
            });
        }

        public void bind(SavedLocation location) {
            // Set coordinates
            String coordinates = String.format("%.2f, %.2f",
                    location.getLatitude(), location.getLongitude());
            tvLocationCoordinates.setText(coordinates);
        }
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
                    // Adjust position for the locations list (subtract 1 for GPS item)
                    listener.onLocationClick(locations.get(position - 1));
                }
            });

            binding.ivFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Adjust position for the locations list (subtract 1 for GPS item)
                    listener.onFavoriteClick(locations.get(position - 1));
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
