package com.example.jungh.jeju_ar.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import static com.example.jungh.jeju_ar.common.Constants.IMAGE_STRING_FORMAT;
import static com.example.jungh.jeju_ar.common.Constants.SAVE_IMAGE_TEMP_PATH;

/**
 * Created by jungh on 2017-06-09.
 */

public class Utils {

    public static String saveBitmapToFile(Bitmap mBit) {
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(SAVE_IMAGE_TEMP_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(dir, Calendar.getInstance().getTimeInMillis() + IMAGE_STRING_FORMAT);
            if (file.exists()) {
                file.delete();
            }

            fos = new FileOutputStream(file);

            mBit.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();

        } catch (final Exception e) {

        } finally {
            try {
                mBit.recycle();
                if (fos != null) {
                    fos.close();
                }
            } catch (final Exception e) {
            }
        }
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return null;
        }

    }

    public static String saveByteToFile(byte[] image) {
        File file = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(SAVE_IMAGE_TEMP_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(dir, Calendar.getInstance().getTimeInMillis() + IMAGE_STRING_FORMAT);
            if (file.exists()) {
                file.delete();
            }

            fos = new FileOutputStream(file);

            fos.write(image);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }

        return file.getAbsolutePath();
    }

    public static int dpToPx(Context c, int dp) {
        return (int) (dp * c.getResources().getSystem().getDisplayMetrics().density);
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int sampleSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = sampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

}

