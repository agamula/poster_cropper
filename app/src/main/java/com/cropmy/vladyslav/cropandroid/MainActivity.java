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


public class MainActivity extends Activity implements View.OnTouchListener, View
        .OnClickListener, View.OnLongClickListener {

    public static final String IMAGE_NAME = "image.png";
    CropImageView cropImageView;
    View frontView;
    int w, h, width, height;
    Bitmap bitmap;
    ImageView mTemplateImg, mImg;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    private static final String TAG = "Touch";
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    CropView cropView;
    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    ImageView imageView;
    CustomImageVIew customImageVIew;
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
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test3, options);
        touchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        touchImageView.setPhoto(bitmap);

        touchImageView.setOnClickListener(this);
    }


    void testCruupLibriary() {

//        cropImageView=(CropImageView)findViewById(R.id.CropImageView);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        final int maxSize = height;//(width > height) ? width : height;
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }


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

    public boolean onTouch(View v, MotionEvent event) {

        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
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

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
