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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class HttpDiapoLoader
{
    private static final String TAG = "HttpDiapoLoader";
    private static final String IMAGE_URL = "http://37.187.65.218/tom_www/public/images/gringo_avatar_tom.jpg";
    private static final String IMAGE_NAME = "gringo_avatar_tom";
    private static final String IMAGE_EXT = "jpg";

    private Context m_context;

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

                Bitmap image;
                try
                {
                    image = Picasso.get().load(IMAGE_URL).get();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Not able to load " + IMAGE_URL + " :\n" + e.toString());
                    callback.onError(e);

                    return;
                }

                boolean imageSuccessfullySaved = saveAsJpeg(image);
                if (imageSuccessfullySaved == false)
                {
                    if (callback != null)
                    {
                        callback.onError(new IOException());
                    }

                    return;
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

    private boolean saveAsJpeg(Bitmap image)
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
        File imageFile = new File(albumDirectory, IMAGE_NAME + "." + IMAGE_EXT);
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
