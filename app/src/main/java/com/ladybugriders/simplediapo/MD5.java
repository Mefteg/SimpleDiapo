/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package com.ladybugriders.simplediapo;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class MD5
{
    public static boolean CheckMD5(String md5, File updateFile)
    {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Timber.e("MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = CalculateMD5(updateFile);
        if (calculatedDigest == null) {
            Timber.e("calculatedDigest null");
            return false;
        }

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String CalculateMD5(File updateFile) {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            Timber.e("Exception while getting digest: %s", e);
            return null;
        }

        InputStream is;
        try
        {
            is = new FileInputStream(updateFile);
        }
        catch (FileNotFoundException e)
        {
            Timber.e("Exception while getting FileInputStream: %s", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try
        {
            while ((read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');

            return output;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                Timber.e("Exception on closing MD5 input stream: %s", e);
            }
        }
    }
}
