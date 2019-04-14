package com.ladybugriders.simplediapo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.squareup.picasso.Callback;

import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    private static final int MIN_TIME_INTERVAL_BETWEEN_TWO_IMAGES = 1; // in seconds.
    private static final int MAX_TIME_INTERVAL_BETWEEN_TWO_IMAGES = 30 * 60; // in seconds.

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            m_diapoImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private DiapoController m_diapoController;
    private HttpDiapoLoader m_diapoLoader;
    private final Callback m_startDiapoLoaderCallback = new Callback() {
        @Override
        public void onSuccess() {
            Timber.d("Diapo loading is successful !");

            if (m_diapoController == null || m_diapoImageView == null)
            {
                return;
            }

            m_diapoController.startDiapo(m_diapoImageView);
        }

        @Override
        public void onError(Exception e) {
            Timber.e("Not able to load diapo successfully.");

            if (m_diapoController == null || m_diapoImageView == null)
            {
                return;
            }

            m_diapoController.startDiapo(m_diapoImageView);
        }
    };

    private final Callback m_resetDiapoLoaderCallback = new Callback() {
        @Override
        public void onSuccess() {
            Timber.d("Diapo loading is successful !");
            if (m_diapoController == null || m_diapoImageView == null)
            {
                return;
            }

            m_diapoController.resetDiapo();
        }

        @Override
        public void onError(Exception e) {
            Timber.e("Not able to load diapo successfully.");
        }
    };

    private ImageView m_diapoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }

        m_diapoController = new DiapoController(this);

        m_diapoLoader = new HttpDiapoLoader(this);
        m_diapoLoader.load(m_startDiapoLoaderCallback);

        setContentView(R.layout.activity_main);

        // Keep screen on !
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVisible = true;

        m_diapoImageView = findViewById(R.id.diapo_image_view);
        // Set up the user interaction to manually show or hide the system UI.
        m_diapoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.set_remote_target_folder_url:
                setRemoteTargetFolderURL();
                return true;
            case R.id.set_images_provider:
                setImagesProvider();
                return true;
            case R.id.set_time_interval_between_two_images:
                setTimeIntervalBetweenTwoImages();
                return true;
            case R.id.refresh:
                refreshDiapo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        m_diapoImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void showCustomAlertDialog(String title, final View view, DialogInterface.OnClickListener positiveAction)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, positiveAction);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setRemoteTargetFolderURL()
    {
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        // Set value regarding what is stored in shared preferences.
        input.setText(SharedPreferencesUtilty.GetRemoteFolderURL(getBaseContext()));

        showCustomAlertDialog(
                getResources().getString(R.string.set_remote_target_folder_url),
                input,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get value from text field.
                        String remoteFolderURL = input.getText().toString();
                        // Store value in shared preferences.
                        SharedPreferencesUtilty.SetRemoteFolderURL(getBaseContext(), remoteFolderURL);

                        m_diapoLoader.load(m_resetDiapoLoaderCallback);
                    }
                });
    }

    private void setImagesProvider()
    {
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        // Set value regarding what is stored in shared preferences.
        input.setText(SharedPreferencesUtilty.GetImagesProvider(getBaseContext()));

        showCustomAlertDialog(
                getResources().getString(R.string.set_images_provider),
                input,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get value from text field.
                        String imagesProvider = input.getText().toString();
                        // Store value in shared preferences.
                        SharedPreferencesUtilty.SetImagesProvider(getBaseContext(), imagesProvider);

                        m_diapoLoader.load(m_resetDiapoLoaderCallback);
                    }
                });
    }

    private void setTimeIntervalBetweenTwoImages()
    {
        int timeInterval = SharedPreferencesUtilty.GetTimeIntervalBetweenTwoImages(getBaseContext());
        // Set up the input
        final NumberPicker input = new NumberPicker(this);
        input.setMinValue(MIN_TIME_INTERVAL_BETWEEN_TWO_IMAGES);
        input.setMaxValue(MAX_TIME_INTERVAL_BETWEEN_TWO_IMAGES);
        input.setValue(timeInterval);

        showCustomAlertDialog(
                getResources().getString(R.string.set_time_interval_between_two_images),
                input,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get value from number picker.
                        int timeInterval = input.getValue();
                        // Store value in shared preferences.
                        SharedPreferencesUtilty.SetTimeIntervalBetweenTwoImages(getBaseContext(), timeInterval);

                        m_diapoController.resetDiapo();
                    }
                });
    }

    private void refreshDiapo()
    {
        if (m_diapoLoader == null)
        {
            Timber.e("Refresh is impossible: no HttpDiapoLoader available.");
            return;
        }

        m_diapoLoader.load(m_startDiapoLoaderCallback);
    }
}
