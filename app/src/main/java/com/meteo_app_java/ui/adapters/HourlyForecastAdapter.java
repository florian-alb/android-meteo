package com.meteo_app_java.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meteo_app_java.R;
import com.meteo_app_java.databinding.ItemHourlyForecastBinding;
import com.meteo_app_java.models.HourlyForecast;
import com.meteo_app_java.utils.WeatherUtils;

public class HourlyForecastAdapter extends ListAdapter<HourlyForecast, HourlyForecastAdapter.HourlyForecastViewHolder> {

    private final boolean isMetric;

    public HourlyForecastAdapter(boolean isMetric) {
        super(DIFF_CALLBACK);
        this.isMetric = isMetric;
    }

    private static final DiffUtil.ItemCallback<HourlyForecast> DIFF_CALLBACK = new DiffUtil.ItemCallback<HourlyForecast>() {
        @Override
        public boolean areItemsTheSame(@NonNull HourlyForecast oldItem, @NonNull HourlyForecast newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull HourlyForecast oldItem, @NonNull HourlyForecast newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp()
                    && oldItem.getTemperature() == newItem.getTemperature()
                    && oldItem.getWeatherIcon().equals(newItem.getWeatherIcon());
        }
    };

    @NonNull
    @Override
    public HourlyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHourlyForecastBinding binding = ItemHourlyForecastBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HourlyForecastViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyForecastViewHolder holder, int position) {
        HourlyForecast forecast = getItem(position);
        holder.bind(forecast);
    }

    class HourlyForecastViewHolder extends RecyclerView.ViewHolder {
        private final ItemHourlyForecastBinding binding;

        public HourlyForecastViewHolder(@NonNull ItemHourlyForecastBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HourlyForecast forecast) {
            // Set time
            binding.tvTime.setText(WeatherUtils.formatHour(forecast.getTimestamp()));

            // Set temperature
            binding.tvTemperature.setText(WeatherUtils.formatTemperature(
                    forecast.getTemperature(), isMetric));

            // Set weather icon using the new method
            WeatherUtils.loadWeatherIcon(binding.getRoot().getContext(),
                    forecast.getWeatherIcon(), binding.ivWeatherIcon);

            // Set precipitation probability
            if (forecast.getPop() > 0) {
                binding.tvPop.setVisibility(View.VISIBLE);
                binding.tvPop.setText(WeatherUtils.formatPop(forecast.getPop()));
            } else {
                binding.tvPop.setVisibility(View.GONE);
            }
        }
    }
}