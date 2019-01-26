package com.ladybugriders.simplediapo;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class DiapoController
{
    private File[] m_imageFiles;
    private int m_displayedImageIndex;

    public DiapoController()
    {
        m_displayedImageIndex = 0;
    }

    public void startDiapo(final ImageView imageView)
    {
        if (imageView == null)
        {
            return;
        }

        // Gather all image paths.
        gatherImagePaths();
        if (m_imageFiles.length == 0)
        {
            return;
        }

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Picasso.get().load(m_imageFiles[m_displayedImageIndex]).into(imageView);
            }
        };
        mainHandler.post(myRunnable);

        /*Runnable displayImageTask = new Runnable() {
            @Override
            public void run() {
                // Display the first image.
                Picasso.get().load(m_imageFiles[m_displayedImageIndex]).into(imageView);
            }
        };
        new Thread(displayImageTask).start();*/
    }

    private void gatherImagePaths()
    {
        File albumDirectory = FileUtility.GetPublicAlbumStorageDir();
        m_imageFiles = FileUtility.GetAllImagesInDirectory(albumDirectory);
    }
}
