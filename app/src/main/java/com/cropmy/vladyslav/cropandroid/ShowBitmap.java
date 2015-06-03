package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ShowBitmap extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView resultImage = new ImageView(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        File imagePath = new File(getFilesDir(), MainActivity.IMAGE_NAME);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), options);
        resultImage.setImageBitmap(bitmap);
        setContentView(resultImage);
    }
}
