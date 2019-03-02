package com.ladybugriders.simplediapo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class SimpleDiapoJSONObject extends JSONObject
{
    private static final String IMAGES_KEY = "images";

    private ImageJSONObject[] m_images;

    public SimpleDiapoJSONObject(String data) throws JSONException {
        super(data);

        JSONArray jsonImages = null;
        try {
            jsonImages = getJSONArray(IMAGES_KEY);
        }
        catch (Exception e)
        {
            Timber.e(e.toString());
            m_images = new ImageJSONObject[0];
        }

        int imageCount = jsonImages.length();
        m_images = new ImageJSONObject[imageCount];
        for (int i = 0; i < imageCount; ++i)
        {
            try
            {
                JSONObject jsonImage = jsonImages.getJSONObject(i);
                ImageJSONObject image = new ImageJSONObject(jsonImage.toString());
                m_images[i] = image;
            }
            catch (Exception e)
            {
                Timber.e(e.toString());
                m_images =  new ImageJSONObject[0];
            }
        }
    }

    public ImageJSONObject[] getImages()
    {
        return m_images;
    }
}
