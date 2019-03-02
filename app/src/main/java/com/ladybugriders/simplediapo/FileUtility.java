package com.ladybugriders.simplediapo;

import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public abstract class FileUtility
{
    private static final String SIMPLE_DIAPO_ALBUM = "SimpleDiapo";
    private static final String IMAGE_EXT = "jpg";

    public static File GetPublicAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SIMPLE_DIAPO_ALBUM);
        if (file.exists() == false)
        {
            if (file.mkdir() == false)
            {
                Timber.e("Directory not created.");
                return null;
            }
        }

        if (file.isDirectory() == false)
        {
            Timber.e("File " + file.getAbsolutePath() + " is not a directory.");
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
                return file.isFile() && file.getName().toLowerCase().endsWith(IMAGE_EXT);
            }
        };
        return directory.listFiles(filter);
    }
}
