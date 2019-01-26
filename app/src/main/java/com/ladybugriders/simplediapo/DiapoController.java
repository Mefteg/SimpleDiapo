package com.ladybugriders.simplediapo;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class DiapoController
{
    private static final int DISPLAY_DURATION = 2000;

    private File[] m_imageFiles;
    private int m_displayedImageIndex;

    private Timer m_timer;
    private TimerTask m_timerTask = new TimerTask() {
        @Override
        public void run() {
            showImage(m_imageView, m_displayedImageIndex);
            ++m_displayedImageIndex;
            if (m_displayedImageIndex >= m_imageFiles.length)
            {
                m_displayedImageIndex = 0;
            }
        }
    };

    private ImageView m_imageView;

    public DiapoController()
    {
        m_displayedImageIndex = 0;

        m_timer = new Timer();
    }

    public void startDiapo(final ImageView imageView)
    {
        if (imageView == null)
        {
            return;
        }

        this.m_imageView = imageView;

        // Gather all image paths.
        gatherImagePaths();
        if (m_imageFiles.length == 0)
        {
            return;
        }

        // Stop the timer in case a task is already going.
        //m_timer.cancel();
        // Then schedule the new task.
        m_timer.scheduleAtFixedRate(m_timerTask, 0, DISPLAY_DURATION);
    }

    private void gatherImagePaths()
    {
        File albumDirectory = FileUtility.GetPublicAlbumStorageDir();
        m_imageFiles = FileUtility.GetAllImagesInDirectory(albumDirectory);
    }

    private void showImage(final ImageView imageView, final int imageIndex)
    {
        if (imageIndex >= m_imageFiles.length)
        {
            return;
        }

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Picasso.get().load(m_imageFiles[imageIndex]).into(imageView);
            }
        };
        mainHandler.post(myRunnable);
    }
}
