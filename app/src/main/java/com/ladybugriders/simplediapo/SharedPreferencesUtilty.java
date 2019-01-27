package com.ladybugriders.simplediapo;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class SharedPreferencesUtilty
{
    public static final String MAIN_SHARED_PREFERENCES_KEY = "main_shared_preferences";
    public static final String REMOTE_FOLDER_URL_KEY = "remote_folder_url";

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
}
