package com.ladybugriders.simplediapo;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import timber.log.Timber;

public class HttpDiapoLoader
{

    private Context m_context;
    private ImageJSONObject[] m_images;

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

                // Remove unnecessary images.
                removeUnnecessaryImages(m_images);

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
                    String imageFilePath = m_images[i].GetFilePath();
                    String imageName;
                    String imageURL;
                    if (m_images[i].GetPathType() == ImageJSONObject.PathType.Absolute)
                    {
                        imageName = Uri.parse(imageFilePath).getLastPathSegment();
                        imageURL = imageFilePath;
                    }
                    else
                    {
                        imageName = imageFilePath;
                        imageURL = SharedPreferencesUtilty.GetRemoteFolderURL(m_context) + imageFilePath;
                    }

                    // Don't overwrite kept images.
                    File imageFile = getImageFileFromAlbum(imageName);
                    if (imageFile.exists())
                    {
                        // Process next image.
                        continue;
                    }

                    //boolean imageSuccessfullySaved = saveAsJpeg(image, imageName);
                    boolean imageSuccessfullySaved = DownloadImage(imageURL, imageName);

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
        String imagesProvider = SharedPreferencesUtilty.GetImagesProvider(m_context);
        String jsonURL = remoteFolderURL + imagesProvider;
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
            Timber.e(e.toString());
            return false;
        }

        return true;
    }

    private void removeUnnecessaryImages(ImageJSONObject[] newImages)
    {
        File albumDirectory = FileUtility.GetPublicAlbumStorageDir();
        File[] storedImageFiles = FileUtility.GetAllImagesInDirectory(albumDirectory);

        for (File imageFile : storedImageFiles)
        {
            int imageIndex = arrayFind(newImages, imageFile.getName());
            // If the stored images is part of the new images to download.
            if (imageIndex > -1)
            {
                // Check change by comparing md5.
                boolean sameFiles = MD5.CheckMD5(newImages[imageIndex].GetMD5(), imageFile);
                // Files are the same.
                if (sameFiles == true)
                {
                    // Image must not be removed.
                    continue;
                }
            }

            // Otherwise, the image is not necessary anymore.
            // Remove it.
            imageFile.delete();
        }
    }

    // element index if found, -1 otherwise.
    private int arrayFind(ImageJSONObject[] array, String value)
    {
        int elementCount = array.length;
        for (int i = 0; i < elementCount; ++i)
        {
            if (array[i].GetFilePath().equals(value))
            {
                return i;
            }
        }

        return -1;
    }

    private  File getImageFileFromAlbum(String imageName)
    {
        if (isExternalStorageWritable() == false)
        {
            return null;
        }

        File albumDirectory = FileUtility.GetPublicAlbumStorageDir();
        if (albumDirectory == null)
        {
            return null;
        }

        // Get the file in the album directory.
        return new File(albumDirectory, imageName);
    }

    private boolean DownloadImage(String imageURL, String imageName)
    {
        File imageFile = getImageFileFromAlbum(imageName);
        if (imageFile == null)
        {
            Timber.e("Cannot get image file from album.");
            return false;
        }

        try
        {
            URL url = new URL(imageURL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);
            InputStream input = urlConnection.getInputStream();

            FileOutputStream output = new FileOutputStream(imageFile);

            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > -1)
            {
                output.write(buffer, 0, read);
            }

            output.close();
            input.close();
        }
        catch (Exception e)
        {
            Timber.e(e.toString());
            return false;
        }

        return true;
    }

    private boolean saveAsJpeg(Bitmap image, String imageName)
    {
        File imageFile = getImageFileFromAlbum(imageName);
        if (imageFile == null)
        {
            return false;
        }

        // Get image data compressed in JPEG format.
        byte[] imageData = getImageData(image, Bitmap.CompressFormat.JPEG);

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
            Timber.e(e.toString());
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
