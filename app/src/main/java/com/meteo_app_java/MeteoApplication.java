package com.meteo_app_java;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meteo_app_java.data.local.MeteoDatabase;
import com.meteo_app_java.utils.PreferencesManager;

public class MeteoApplication extends Application {
    private static final String TAG = "MeteoApplication";

    private static Context context;
    private static MeteoDatabase database;
    private static PreferencesManager preferencesManager;
    private static FirebaseAnalytics firebaseAnalytics;
    private static FirebaseCrashlytics firebaseCrashlytics;
    private static FirebaseFirestore firebaseFirestore;
    private static boolean firebaseAvailable = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        // Check if Google Play Services are available
        if (checkGooglePlayServices()) {
            try {
                // Initialize Firebase
                FirebaseApp.initializeApp(this);
                firebaseAnalytics = FirebaseAnalytics.getInstance(this);
                firebaseCrashlytics = FirebaseCrashlytics.getInstance();
                firebaseFirestore = FirebaseFirestore.getInstance();

                // Enable Crashlytics collection
                firebaseCrashlytics.setCrashlyticsCollectionEnabled(true);

                firebaseAvailable = true;
                Log.d(TAG, "Firebase initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Firebase", e);
                firebaseAvailable = false;
            }
        } else {
            Log.w(TAG, "Google Play Services not available, Firebase features disabled");
            firebaseAvailable = false;
        }

        // Initialize Room database
        database = Room.databaseBuilder(
                getApplicationContext(),
                MeteoDatabase.class,
                "meteo_database")
                .fallbackToDestructiveMigration()
                .build();

        // Initialize SharedPreferences manager
        preferencesManager = new PreferencesManager(getApplicationContext());
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static Context getAppContext() {
        return context;
    }

    public static MeteoDatabase getDatabase() {
        return database;
    }

    public static PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAvailable ? firebaseAnalytics : null;
    }

    public static FirebaseCrashlytics getFirebaseCrashlytics() {
        return firebaseAvailable ? firebaseCrashlytics : null;
    }

    public static FirebaseFirestore getFirebaseFirestore() {
        return firebaseAvailable ? firebaseFirestore : null;
    }

    public static boolean isFirebaseAvailable() {
        return firebaseAvailable;
    }
}