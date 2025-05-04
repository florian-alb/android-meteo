package com.meteo_app_java.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meteo_app_java.R;
import com.meteo_app_java.databinding.ItemDailyForecastBinding;
import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.utils.WeatherUtils;

public class DailyForecastAdapter extends ListAdapter<DailyForecast, DailyForecastAdapter.DailyForecastViewHolder> {

    private final boolean isMetric;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DailyForecast forecast);
    }

    public DailyForecastAdapter(boolean isMetric) {
        super(DIFF_CALLBACK);
        this.isMetric = isMetric;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DailyForecast> DIFF_CALLBACK = new DiffUtil.ItemCallback<DailyForecast>() {
        @Override
        public boolean areItemsTheSame(@NonNull DailyForecast oldItem, @NonNull DailyForecast newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DailyForecast oldItem, @NonNull DailyForecast newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp()
                    && oldItem.getTempMax() == newItem.getTempMax()
                    && oldItem.getTempMin() == newItem.getTempMin()
                    && oldItem.getWeatherIcon().equals(newItem.getWeatherIcon());
        }
    };

    @NonNull
    @Override
    public DailyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDailyForecastBinding binding = ItemDailyForecastBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DailyForecastViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyForecastViewHolder holder, int position) {
        DailyForecast forecast = getItem(position);
        holder.bind(forecast);
    }

    class DailyForecastViewHolder extends RecyclerView.ViewHolder {
        private final ItemDailyForecastBinding binding;

        public DailyForecastViewHolder(@NonNull ItemDailyForecastBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(DailyForecast forecast) {
            // Set day of week
            binding.tvDay.setText(WeatherUtils.formatDayOfWeek(forecast.getTimestamp()));

            // Set date
            binding.tvDate.setText(WeatherUtils.formatShortDate(forecast.getTimestamp()));

            // Set temperature min/max
            binding.tvTempMin.setText(WeatherUtils.formatTemperature(
                    forecast.getTempMin(), isMetric));
            binding.tvTempMax.setText(WeatherUtils.formatTemperature(
                    forecast.getTempMax(), isMetric));

            // Set weather icon
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