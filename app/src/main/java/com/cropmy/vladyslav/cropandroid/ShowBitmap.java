package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowBitmap extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView resultImage = new ImageView(this);
        Bundle b = this.getIntent().getExtras();

        byte[] byteArray  =  b.getByteArray("image");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,options);
        resultImage.setImageBitmap(bitmap);
        setContentView(resultImage);
    }
}
