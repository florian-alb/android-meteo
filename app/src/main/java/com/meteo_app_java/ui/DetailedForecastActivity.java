package com.meteo_app_java.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.meteo_app_java.R;
import com.meteo_app_java.data.WeatherRepository;
import com.meteo_app_java.databinding.ActivityDetailedForecastBinding;
import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.models.HourlyForecast;
import com.meteo_app_java.ui.adapters.DailyForecastPagerAdapter;
import com.meteo_app_java.ui.adapters.HourlyForecastAdapter;
import com.meteo_app_java.utils.PreferencesManager;
import com.meteo_app_java.utils.WeatherUtils;

import java.util.ArrayList;
import java.util.List;

public class DetailedForecastActivity extends AppCompatActivity {

    private ActivityDetailedForecastBinding binding;
    private WeatherRepository weatherRepository;
    private PreferencesManager preferencesManager;

    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastPagerAdapter dailyPagerAdapter;

    private boolean isMetric;
    private double latitude;
    private double longitude;
    private String cityName;

    private List<DailyForecast> dailyForecasts;
    private List<HourlyForecast> hourlyForecasts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailedForecastBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize repositories and utilities
        weatherRepository = WeatherRepository.getInstance();
        preferencesManager = new PreferencesManager(this);

        // Get unit preference
        isMetric = preferencesManager.isUsingMetricUnits();

        // Get location data from intent
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            latitude = getIntent().getDoubleExtra("latitude", 0);
            longitude = getIntent().getDoubleExtra("longitude", 0);
            cityName = getIntent().getStringExtra("city");

            // If cityName is null, it might be using GPS
            if (cityName == null && preferencesManager.shouldUseDeviceLocation()) {
                cityName = getString(R.string.current_location);
            }

            // Set location name
            binding.tvLocation.setText(cityName);

            // Load data
            loadForecastData();
        } else {
            finish();
        }

        // Setup RecyclerView
        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        // Setup hourly forecast RecyclerView
        hourlyAdapter = new HourlyForecastAdapter(isMetric);
        binding.rvHourlyForecast.setAdapter(hourlyAdapter);
        binding.rvHourlyForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Setup ViewPager for daily forecasts
        dailyPagerAdapter = new DailyForecastPagerAdapter(this, new ArrayList<>(), isMetric);
        binding.viewpagerDays.setAdapter(dailyPagerAdapter);

        // Setup TabLayout with ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewpagerDays, (tab, position) -> {
            // Tab text will be set in the adapter
            if (dailyForecasts != null && position < dailyForecasts.size()) {
                DailyForecast forecast = dailyForecasts.get(position);
                tab.setText(WeatherUtils.formatShortDayOfWeek(forecast.getTimestamp()));
            }
        }).attach();

        // Handle page change
        binding.viewpagerDays.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (dailyForecasts != null && position < dailyForecasts.size()) {
                    updateSelectedDayUI(dailyForecasts.get(position));
                    updateHourlyForecastForDay(dailyForecasts.get(position).getTimestamp());
                    updateTemperatureChart(dailyForecasts.get(position).getTimestamp());
                }
            }
        });
    }

    private void loadForecastData() {
        showLoading(true);

        // Load daily forecast
        weatherRepository.getDailyForecast(latitude, longitude)
                .observe(this, new Observer<List<DailyForecast>>() {
                    @Override
                    public void onChanged(List<DailyForecast> forecasts) {
                        if (forecasts != null && !forecasts.isEmpty()) {
                            dailyForecasts = forecasts;
                            dailyPagerAdapter.setForecasts(forecasts);

                            // Update tabs
                            new TabLayoutMediator(binding.tabLayout, binding.viewpagerDays, (tab, position) -> {
                                if (position < forecasts.size()) {
                                    DailyForecast forecast = forecasts.get(position);
                                    tab.setText(WeatherUtils.formatShortDayOfWeek(forecast.getTimestamp()));
                                }
                            }).attach();

                            // Update UI for the first day
                            binding.viewpagerDays.setCurrentItem(0);
                            updateSelectedDayUI(forecasts.get(0));
                        }
                        showLoading(false);
                    }
                });

        // Load hourly forecast
        weatherRepository.getHourlyForecast(latitude, longitude)
                .observe(this, new Observer<List<HourlyForecast>>() {
                    @Override
                    public void onChanged(List<HourlyForecast> forecasts) {
                        if (forecasts != null && !forecasts.isEmpty()) {
                            hourlyForecasts = forecasts;

                            // If daily forecasts are already loaded, update hourly for the selected day
                            if (dailyForecasts != null && !dailyForecasts.isEmpty()) {
                                int currentPosition = binding.viewpagerDays.getCurrentItem();
                                updateHourlyForecastForDay(dailyForecasts.get(currentPosition).getTimestamp());
                                updateTemperatureChart(dailyForecasts.get(currentPosition).getTimestamp());
                            }
                        }
                    }
                });
    }

    private void updateSelectedDayUI(DailyForecast forecast) {
        // Set day and date
        String dayDate = WeatherUtils.formatDayOfWeek(forecast.getTimestamp()) + ", " +
                WeatherUtils.formatShortDate(forecast.getTimestamp());
        binding.tvSelectedDay.setText(dayDate);

        // Set weather description
        binding.tvSelectedDayDescription.setText(
                WeatherUtils.formatWeatherDescription(forecast.getWeatherDescription()));

        // Set weather icon
        WeatherUtils.loadWeatherIcon(this, forecast.getWeatherIcon(), binding.ivSelectedDayIcon);

        // Set temperatures
        binding.tvDayTemp.setText(WeatherUtils.formatTemperature(
                forecast.getTempDay(), isMetric));
        binding.tvNightTemp.setText(WeatherUtils.formatTemperature(
                forecast.getTempNight(), isMetric));

        // Set humidity
        binding.tvHumidity.setText(WeatherUtils.formatHumidity(forecast.getHumidity()));

        // Set precipitation
        binding.tvPrecipitation.setText(WeatherUtils.formatPop(forecast.getPop()));

        // Set wind
        binding.tvWind.setText(WeatherUtils.formatWindSpeed(forecast.getWindSpeed(), isMetric));

        // Set pressure
        binding.tvPressure.setText(String.valueOf(forecast.getPressure()) + " hPa");

        // Set sunrise and sunset
        binding.tvSunrise.setText(WeatherUtils.formatSunTime(forecast.getSunrise()));
        binding.tvSunset.setText(WeatherUtils.formatSunTime(forecast.getSunset()));
    }

    private void updateHourlyForecastForDay(long dayTimestamp) {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
            return;
        }

        // Calculate start and end of the day
        long startOfDay = dayTimestamp - (dayTimestamp % 86400);
        long endOfDay = startOfDay + 86400;

        // Filter hourly forecasts for the day
        List<HourlyForecast> dayForecasts = new ArrayList<>();
        for (HourlyForecast forecast : hourlyForecasts) {
            if (forecast.getTimestamp() >= startOfDay && forecast.getTimestamp() < endOfDay) {
                dayForecasts.add(forecast);
            }
        }

        hourlyAdapter.submitList(dayForecasts);
    }

    private void updateTemperatureChart(long dayTimestamp) {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
            return;
        }

        // Calculate start and end of the day
        long startOfDay = dayTimestamp - (dayTimestamp % 86400);
        long endOfDay = startOfDay + 86400;

        // Filter hourly forecasts for the day
        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        int i = 0;
        for (HourlyForecast forecast : hourlyForecasts) {
            if (forecast.getTimestamp() >= startOfDay && forecast.getTimestamp() < endOfDay) {
                entries.add(new Entry(i, (float) forecast.getTemperature()));
                xLabels.add(WeatherUtils.formatHour(forecast.getTimestamp()));
                i++;
            }
        }

        if (entries.isEmpty()) {
            binding.temperatureChart.setVisibility(View.GONE);
            return;
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return WeatherUtils.formatTemperature(value, isMetric);
            }
        });

        // Create LineData object
        LineData lineData = new LineData(dataSet);

        // Configure chart
        binding.temperatureChart.setData(lineData);
        binding.temperatureChart.getDescription().setEnabled(false);
        binding.temperatureChart.getLegend().setEnabled(false);

        // X-axis setup
        XAxis xAxis = binding.temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                }
                return "";
            }
        });

        // Y-axis setup
        YAxis leftAxis = binding.temperatureChart.getAxisLeft();
        YAxis rightAxis = binding.temperatureChart.getAxisRight();
        rightAxis.setEnabled(false);

        binding.temperatureChart.setVisibility(View.VISIBLE);
        binding.temperatureChart.invalidate();
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}