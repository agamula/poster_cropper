package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity implements  View
        .OnClickListener {

    public static final String IMAGE_NAME = "image.png";
    Bitmap bitmap;
    TouchImageView touchImageView;
    ModelBitmap modelBitmap = new ModelBitmap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test5, options);
        touchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        touchImageView.setPhoto(bitmap);
        findViewById(R.id.pressButton).setOnClickListener(this);
        Log.d("calculate ","1mm to px=" + Tools.mmToPx(1,this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            touchImageView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View view) {
        modelBitmap.setBitmap(touchImageView.creatNewPhoto());

        File imagePath = new File(getFilesDir(), IMAGE_NAME);
        if (!imagePath.exists()) {
            try {
                imagePath.createNewFile();
                saveImage(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveImage(imagePath);
        }
    }

    private void saveImage(final File imagePath) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Boolean res;
                try {
                    res = modelBitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, new
                            FileOutputStream(imagePath));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    res = false;
                }
                return res;
            }

            @Override
            protected void onPostExecute(Boolean aVoid) {
                super.onPostExecute(aVoid);
                if(aVoid) {
                    Intent intent = new Intent(getApplicationContext(), ShowBitmap.class);
                    startActivity(intent);
                }
            }
        }.execute();
    }
}
