package com.lixyz.lifekeeperforkotlin.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class BitmapUtil {
    private static final String TAG = "TTT";

    public static byte[] compressBitmapToBytes(String filePath, int reqWidth, int reqHeight, int quality, Bitmap.CompressFormat format) {
        Bitmap bitmap = getSmallBitmap(filePath, reqWidth, reqHeight);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, quality, baos);
        byte[] bytes = baos.toByteArray();
        bitmap.recycle();
        Log.i(TAG, "Bitmap compressed success, size: " + bytes.length);
        return bytes;
    }

    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        //      options.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int h = options.outHeight;
        int w = options.outWidth;
        int inSampleSize = 0;
        if (h > reqHeight || w > reqWidth) {
            float ratioW = (float) w / reqWidth;
            float ratioH = (float) h / reqHeight;
            inSampleSize = (int) Math.min(ratioH, ratioW);
        }
        inSampleSize = Math.max(1, inSampleSize);
        return inSampleSize;
    }
}
