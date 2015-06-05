package com.cropmy.vladyslav.cropandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class ConvertUtils {
    private ConvertUtils() {
    }

    boolean isZoom = false;

    public static float pxToMm(final float px, final Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return applyDimension(TypedValue.COMPLEX_UNIT_MM, px, metrics);
    }

    public static float mmToPx(final float mm, final Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm,
                context.getResources().getDisplayMetrics());
    }

    public static float getMaxZoom(Bitmap bitmap, Context context) {

        float maxZoom = getDpi(bitmap.getWidth() > bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight(), context);

        return maxZoom / 250;
    }

    public static float applyDimension(int unit, float value,
                                       DisplayMetrics metrics) {
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f / 72);
            case TypedValue.COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f / 25.4f);
        }
        return 0;
    }

    public static float getDpi(float width, Context context) {
        float sizeWidth = ConvertUtils.pxToMm(width, context);
        float sizeInInch = sizeWidth * 0.1f / 2.54f;
        float dpi = width / sizeInInch;
        return width / dpi;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
