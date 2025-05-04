package com.meteo_app_java.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Utility class for file operations.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Write data to a file in the app's internal storage.
     *
     * @param context  The application context
     * @param fileName The name of the file
     * @param data     The data to write
     * @return true if write is successful, false otherwise
     */
    public static boolean writeToFile(Context context, String fileName, String data) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(data);
            writer.close();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file: " + fileName, e);
            return false;
        }
    }

    /**
     * Read data from a file in the app's internal storage.
     *
     * @param context  The application context
     * @param fileName The name of the file
     * @return The content of the file as a String, or null if an error occurs
     */
    public static String readFromFile(Context context, String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            fis.close();
            return content.toString();
        } catch (java.io.FileNotFoundException e) {
            // File doesn't exist yet, which is expected on first run
            Log.d(TAG, "File not found (expected on first run): " + fileName);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from file: " + fileName, e);
            return null;
        }
    }

    /**
     * Check if a file exists in the app's internal storage.
     *
     * @param context  The application context
     * @param fileName The name of the file
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    /**
     * Delete a file from the app's internal storage.
     *
     * @param context  The application context
     * @param fileName The name of the file
     * @return true if the file was deleted, false otherwise
     */
    public static boolean deleteFile(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.delete();
    }

    /**
     * List all files in the app's internal storage.
     *
     * @param context The application context
     * @return An array of file names
     */
    public static String[] listFiles(Context context) {
        return context.fileList();
    }
}