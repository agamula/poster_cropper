package com.cropmy.vladyslav.cropandroid;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtils {

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth, int
            leftDiffMargin, int topDiffMargin) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2 - leftDiffMargin;
        float top = (newHeight - scaledHeight) / 2 - topDiffMargin;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static Bitmap getShowing(Bitmap bitmap, int countPixels, int margins[]) {
        Rect targetRect = new Rect(countPixels - margins[0], countPixels - margins[1],
                bitmap.getWidth() - countPixels + margins[2], bitmap.getHeight() - countPixels +
                margins[3]);

        int width = targetRect.width();
        int height = targetRect.height();

        Bitmap dest = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(bitmap, targetRect, new RectF(0, 0, width, height), null);
        bitmap.recycle();
        return dest;
    }


    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float aspect = (float)width / height;

        float scaleWidth = newWidth;

        float scaleHeight = scaleWidth / aspect;        // yeah!

        // create a matrix for the manipulation

        Matrix matrix = new Matrix();

        // resize the bit map

        matrix.postScale(scaleWidth / width, scaleHeight / height);

        // recreate the new Bitmap

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

        return resizedBitmap;
    }

    public static Bitmap getResizedBitmapH(Bitmap bm, int newWidth) {

        int width = bm.getWidth();

        int height = bm.getHeight();

        float aspect = (float) height /  width;

        float scaleHeight = newWidth;

        float scaleWidth = scaleHeight / aspect;        // yeah!

        // create a matrix for the manipulation

        Matrix matrix = new Matrix();

        // resize the bit map

        matrix.postScale(scaleWidth / width, scaleHeight / height);

        // recreate the new Bitmap

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);



        return resizedBitmap;
    }

    public static Bitmap performResize(Bitmap bitmap, int requiredWidth,
                                       int requiredHeight) {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float differenceWidth = requiredWidth - imageWidth;
        float percentage = differenceWidth / imageWidth * 100;
        float estimatedheight = imageHeight + (percentage * imageHeight / 100);
        float estimatedwidth = requiredWidth;

        if (estimatedheight < requiredHeight) {
            float incresePercentage = (float) (requiredHeight - estimatedheight);
            percentage += (incresePercentage / imageHeight * 100);
            estimatedheight = imageHeight + (percentage * imageHeight / 100);
            estimatedwidth = imageWidth + (percentage * imageWidth / 100);
        }

//        bitmap = CropFromBitmap.performResize(bitmap, (int) estimatedheight,
//                (int) estimatedwidth);

        if (bitmap.getHeight() < requiredHeight) // if calculate height is
        // smaller then the required
        // Height
        {
        } else {
            int xCropPosition = (int) ((bitmap.getWidth() - requiredWidth) / 2);
            int yCropPosition = (int) ((bitmap.getHeight() - requiredHeight) / 2);

            bitmap = Bitmap.createBitmap(bitmap, xCropPosition, yCropPosition,
                    (int) requiredWidth, (int) requiredHeight);
        }
        return bitmap;
    }
}
