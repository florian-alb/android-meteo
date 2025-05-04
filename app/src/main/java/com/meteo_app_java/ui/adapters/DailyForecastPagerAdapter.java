package com.meteo_app_java.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meteo_app_java.R;
import com.meteo_app_java.databinding.ItemDailyForecastDetailBinding;
import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.utils.WeatherUtils;

import java.util.List;

public class DailyForecastPagerAdapter extends RecyclerView.Adapter<DailyForecastPagerAdapter.DailyForecastViewHolder> {

    private final Context context;
    private List<DailyForecast> forecasts;
    private final boolean isMetric;

    public DailyForecastPagerAdapter(Context context, List<DailyForecast> forecasts, boolean isMetric) {
        this.context = context;
        this.forecasts = forecasts;
        this.isMetric = isMetric;
    }

    public void setForecasts(List<DailyForecast> forecasts) {
        this.forecasts = forecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DailyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_daily_forecast_detail, parent, false);

        // Ensure the view uses match_parent for width and height
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        return new DailyForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyForecastViewHolder holder, int position) {
        DailyForecast forecast = forecasts.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    class DailyForecastViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDate;
        private final TextView tvDay;
        private final TextView tvDescription;
        private final TextView tvTempMax;
        private final TextView tvTempMin;
        private final TextView tvHumidity;
        private final TextView tvWind;
        private final TextView tvPrecipitation;
        private final ImageView ivWeatherIcon;

        public DailyForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTempMax = itemView.findViewById(R.id.tv_temp_max);
            tvTempMin = itemView.findViewById(R.id.tv_temp_min);
            tvHumidity = itemView.findViewById(R.id.tv_humidity);
            tvWind = itemView.findViewById(R.id.tv_wind);
            tvPrecipitation = itemView.findViewById(R.id.tv_precipitation);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
        }

        public void bind(DailyForecast forecast) {
            // Set date
            tvDate.setText(WeatherUtils.formatShortDate(forecast.getTimestamp()));

            // Set day
            tvDay.setText(WeatherUtils.formatDayOfWeek(forecast.getTimestamp()));

            // Set weather description
            tvDescription.setText(WeatherUtils.formatWeatherDescription(forecast.getWeatherDescription()));

            // Set min/max temp
            tvTempMax.setText(WeatherUtils.formatTemperature(forecast.getTempMax(), isMetric));
            tvTempMin.setText(WeatherUtils.formatTemperature(forecast.getTempMin(), isMetric));

            // Set humidity
            String humidity = context.getString(R.string.humidity,
                    WeatherUtils.formatHumidity(forecast.getHumidity()));
            tvHumidity.setText(humidity);

            // Set wind
            String wind = context.getString(R.string.wind,
                    WeatherUtils.formatWindSpeed(forecast.getWindSpeed(), isMetric));
            tvWind.setText(wind);

            // Set precipitation
            String precipitation = context.getString(R.string.precipitation,
                    WeatherUtils.formatPop(forecast.getPop()));
            tvPrecipitation.setText(precipitation);

            // Set weather icon
            WeatherUtils.loadWeatherIcon(context, forecast.getWeatherIcon(), ivWeatherIcon);
        }
    }
}