package com.ladybugriders.simplediapo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class DiapoController
{
    private class DisplayImageTimerTask extends TimerTask
    {
        @Override
        public void run() {
            showImage(m_imageView, m_displayedImageIndex);
            ++m_displayedImageIndex;
            if (m_displayedImageIndex >= m_imageFiles.length)
            {
                m_displayedImageIndex = 0;
            }
        }
    }

    private Context m_context;

    private File[] m_imageFiles;
    private int m_displayedImageIndex;

    private Timer m_timer;
    private boolean m_timerStarted;

    private ImageView m_imageView;

    public DiapoController(Context context)
    {
        m_context = context;

        m_displayedImageIndex = 0;

        m_timer = new Timer();
        m_timerStarted = false;
    }

    public void startDiapo(final ImageView imageView)
    {
        if (imageView == null)
        {
            return;
        }

        this.m_imageView = imageView;

        resetDiapo();
    }

    public void resetDiapo()
    {
        // Gather all image paths.
        gatherImagePaths();
        if (m_imageFiles.length == 0)
        {
            return;
        }

        cancelTimerIfNecessary();

        int timeIntervalInMs = SharedPreferencesUtilty.GetTimeIntervalBetweenTwoImages(m_context) * 1000;
        // Then schedule the new task.
        m_timer.scheduleAtFixedRate(new DisplayImageTimerTask(), 0, timeIntervalInMs);
        m_timerStarted = true;
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

    private void cancelTimerIfNecessary()
    {
        if (m_timerStarted == true)
        {
            m_timer.cancel();
            m_timer.purge();
            m_timer = new Timer();
            m_timerStarted = false;
        }
    }
}
