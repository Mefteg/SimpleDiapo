package com.ladybugriders.simplediapo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SimpleDiapoJSONObject extends JSONObject
{
    private static final String TAG = "SimpleDiapoJSONObject";

    private static final String IMAGES_KEY = "images";
    private static final String FILE_PATH_KEY = "file_path";

    public SimpleDiapoJSONObject(String data) throws JSONException {
        super(data);
    }

    public String[] getImages()
    {
        JSONArray jsonImages = null;
        try {
            jsonImages = getJSONArray(IMAGES_KEY);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
            return new String[0];
        }

        String[] images = new String[jsonImages.length()];
        for (int i = 0; i < images.length; ++i)
        {
            try
            {
                JSONObject jsonImage = jsonImages.getJSONObject(i);
                images[i] = jsonImage.getString(FILE_PATH_KEY);
            }
            catch (Exception e)
            {
                Log.e(TAG, e.toString());
                return new String[0];
            }
        }

        return images;
    }
}
