package com.ladybugriders.simplediapo;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class FileUtility
{
    private static final String TAG = "FileUtility";

    private static final String SIMPLE_DIAPO_ALBUM = "SimpleDiapo";

    public static File GetPublicAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SIMPLE_DIAPO_ALBUM);
        if (file.exists() == false)
        {
            if (file.mkdir() == false)
            {
                Log.e(TAG, "Directory not created.");
                return null;
            }
        }

        if (file.isDirectory() == false)
        {
            Log.e(TAG, "File " + file.getAbsolutePath() + " is not a directory.");
            return null;
        }

        return file;
    }

    public static void WriteDataToFile(byte[] data, File file) throws IOException
    {
        // Open a stream to the file to write image data.
        FileOutputStream imageFileStream = new FileOutputStream(file.getAbsolutePath());
        imageFileStream.write(data);
        imageFileStream.close();
    }

    public static File[] GetAllImagesInDirectory(File directory)
    {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith("jpg");
            }
        };
        return directory.listFiles(filter);
    }
}
