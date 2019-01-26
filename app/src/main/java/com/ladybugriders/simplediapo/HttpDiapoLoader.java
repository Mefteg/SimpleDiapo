package com.ladybugriders.simplediapo;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpDiapoLoader
{
    private static final String TAG = "HttpDiapoLoader";
    private static final String IMAGE_URL = "http://37.187.65.218/tom_www/public/images/gringo_avatar_tom.jpg";
    private static final String IMAGE_NAME = "gringo_avatar_tom";
    private static final String IMAGE_EXT = "jpg";
    private static final String SIMPLE_DIAPO_JSON_FILENAME = "simplediapo.json";

    private Context m_context;
    private String[] m_images;

    public HttpDiapoLoader()
    {

    }

    public HttpDiapoLoader(Context context)
    {
        m_context = context;
    }

    public void load(final Callback callback)
    {
        Runnable loadingTask = new Runnable() {
            @Override
            public void run()
            {
                if (isDeviceConnected() == false)
                {
                    if (callback != null)
                    {
                        NetworkErrorException e = new NetworkErrorException();
                        callback.onError(e);
                    }

                    return;
                }

                // Load simple diapo JSON from remote folder.
                if (loadSimpleDiapoJSON() == false)
                {
                    if (callback != null)
                    {
                        Exception e = new Exception();
                        callback.onError(e);
                    }

                    return;
                }

                if (m_images.length == 0)
                {
                    if (callback != null)
                    {
                        callback.onSuccess();
                    }

                    return;
                }

                for (int i = 0; i < m_images.length; ++i)
                {
                    String imageName = m_images[0];
                    String imageURL = SharedPreferencesUtilty.GetRemoteFolderURL(m_context) + imageName;
                    Bitmap image;
                    try
                    {
                        image = Picasso.get().load(imageURL).get();
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG, "Not able to load " + imageURL + " :\n" + e.toString());
                        callback.onError(e);

                        return;
                    }

                    boolean imageSuccessfullySaved = saveAsJpeg(image, imageName);
                    if (imageSuccessfullySaved == false)
                    {
                        if (callback != null)
                        {
                            callback.onError(new IOException());
                        }

                        return;
                    }
                }

                if (callback != null)
                {
                    callback.onSuccess();
                }
            }
        };
        new Thread(loadingTask).start();
    }

    private boolean isDeviceConnected()
    {
        if (m_context == null)
        {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)m_context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private boolean loadSimpleDiapoJSON()
    {
        String remoteFolderURL = SharedPreferencesUtilty.GetRemoteFolderURL(m_context);
        String jsonURL = remoteFolderURL + SIMPLE_DIAPO_JSON_FILENAME;
        try
        {
            URL url = new URL(jsonURL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);
            InputStream input = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(input, "utf-8"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            input.close();
            String data = sBuilder.toString();

            SimpleDiapoJSONObject simpleDiapJSONObject = new SimpleDiapoJSONObject(data);
            m_images = simpleDiapJSONObject.getImages();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    private boolean saveAsJpeg(Bitmap image, String imageName)
    {
        if (isExternalStorageWritable() == false)
        {
            return false;
        }

        File albumDirectory = FileUtility.GetPublicAlbumStorageDir();
        if (albumDirectory == null)
        {
            return false;
        }

        // Get image data compressed in JPEG format.
        byte[] imageData = getImageData(image, Bitmap.CompressFormat.JPEG);

        // Get the file in the album directory.
        File imageFile = new File(albumDirectory, imageName);
        try
        {
            // Create the file.
            if (imageFile.exists() == false)
            {
                imageFile.createNewFile();
            }

            if (imageFile.canRead() == false)
            {
                imageFile.setReadable(true);
            }

            if (imageFile.canWrite() == false)
            {
                imageFile.setWritable(true);
            }

            FileUtility.WriteDataToFile(imageData, imageFile);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
            return false;
        }

        return true;
    }

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }

        return false;
    }

    private byte[] getImageData(Bitmap image, Bitmap.CompressFormat format)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(format, 100, stream);
        return stream.toByteArray();
    }
}
