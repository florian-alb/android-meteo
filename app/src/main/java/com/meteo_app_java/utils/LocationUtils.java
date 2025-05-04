package com.meteo_app_java.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Utility class for handling location services.
 */
public class LocationUtils {
    private static final String TAG = "LocationUtils";

    // Request codes
    public static final int REQUEST_LOCATION_PERMISSION = 1001;
    public static final int REQUEST_CHECK_SETTINGS = 1002;

    // Update interval for location updates
    private static final long UPDATE_INTERVAL = 10 * 60 * 1000; // 10 minutes
    private static final long FASTEST_INTERVAL = 5 * 60 * 1000; // 5 minutes

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private OnLocationResultListener locationListener;

    /**
     * Listener interface for location results.
     */
    public interface OnLocationResultListener {
        void onLocationResult(Location location);

        void onLocationFailed(String message);
    }

    /**
     * Constructor for LocationUtils.
     *
     * @param context Context
     */
    public LocationUtils(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();
    }

    /**
     * Create the location request with desired parameters.
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Check if location permissions are granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permissions.
     *
     * @param activity The activity requesting permissions
     */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCATION_PERMISSION);
    }

    /**
     * Get the last known location.
     *
     * @param listener Callback listener for location result
     */
    public void getLastLocation(OnLocationResultListener listener) {
        this.locationListener = listener;

        if (!hasLocationPermission()) {
            listener.onLocationFailed("Location permission not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    listener.onLocationResult(location);
                } else {
                    // Last location might be null, request location updates instead
                    requestLocationUpdates();
                }
            }).addOnFailureListener(e -> {
                listener.onLocationFailed("Failed to get location: " + e.getMessage());
                Log.e(TAG, "Failed to get location", e);
            });
        } catch (SecurityException e) {
            listener.onLocationFailed("Location permission not granted");
            Log.e(TAG, "Location permission not granted", e);
        }
    }

    /**
     * Request location updates.
     */
    private void requestLocationUpdates() {
        if (!hasLocationPermission()) {
            if (locationListener != null) {
                locationListener.onLocationFailed("Location permission not granted");
            }
            return;
        }

        // Create callback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null && locationListener != null) {
                    locationListener.onLocationResult(locationResult.getLastLocation());
                    stopLocationUpdates(); // Stop updates after receiving a location
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            if (locationListener != null) {
                locationListener.onLocationFailed("Location permission not granted");
            }
            Log.e(TAG, "Location permission not granted", e);
        }
    }

    /**
     * Stop location updates.
     */
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Check if location settings are satisfied and request changes if needed.
     *
     * @param activity Activity to show resolution dialog if needed
     */
    public void checkLocationSettings(Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(activity);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // All location settings are satisfied, we can start requesting updates
                        if (locationListener != null) {
                            requestLocationUpdates();
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException) {
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult()
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error
                                Log.e(TAG, "Error showing location settings resolution dialog", sendEx);
                            }
                        } else {
                            if (locationListener != null) {
                                locationListener.onLocationFailed("Location settings are inadequate");
                            }
                        }
                    }
                });
    }

    /**
     * Calculate distance between two locations.
     *
     * @param lat1 Latitude of first location
     * @param lon1 Longitude of first location
     * @param lat2 Latitude of second location
     * @param lon2 Longitude of second location
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Using Haversine formula
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}