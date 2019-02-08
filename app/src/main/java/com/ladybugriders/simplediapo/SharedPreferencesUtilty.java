package com.ladybugriders.simplediapo;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class SharedPreferencesUtilty
{
    public static final String MAIN_SHARED_PREFERENCES_KEY = "main_shared_preferences";
    public static final String REMOTE_FOLDER_URL_KEY = "remote_folder_url";
    public static final String IMAGES_PROVIDER_KEY = "images_provider";
    public static final String TIME_INTERVAL_BETWEEN_TWO_IMAGES_KEY = "time_interval_between_two_images";

    public static final int DEFAULT_INTERVAL_BETWEEN_TWO_IMAGES = 15; // Interval in seconds.

    public static String GetRemoteFolderURL(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getString(REMOTE_FOLDER_URL_KEY, "");
    }

    public static void SetRemoteFolderURL(Context context, String remoteFolderURL)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REMOTE_FOLDER_URL_KEY, remoteFolderURL);
        editor.apply();
    }

    public static String GetImagesProvider(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getString(IMAGES_PROVIDER_KEY, "");
    }

    public static void SetImagesProvider(Context context, String imagesProvider)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(IMAGES_PROVIDER_KEY, imagesProvider);
        editor.apply();
    }

    public static int GetTimeIntervalBetweenTwoImages(Context context)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPref.getInt(TIME_INTERVAL_BETWEEN_TWO_IMAGES_KEY, DEFAULT_INTERVAL_BETWEEN_TWO_IMAGES);
    }

    public static void SetTimeIntervalBetweenTwoImages(Context context, int timeIntervalBetweenTwoImages)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                MAIN_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(TIME_INTERVAL_BETWEEN_TWO_IMAGES_KEY, timeIntervalBetweenTwoImages);
        editor.apply();
    }
}
