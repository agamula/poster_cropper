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
import android.os.Bundle;
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


public class MainActivity extends Activity implements View.OnTouchListener, View
        .OnClickListener, View.OnLongClickListener {
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
    ModelBitmap modelBitmap= new ModelBitmap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test3,options);

////        mTemplateImg.setImageBitmap(bitmap);
//        mImg.buildDrawingCache(true);
//        mImg.setDrawingCacheEnabled(true);
//        mTemplateImg.buildDrawingCache(true);
//        mTemplateImg.setDrawingCacheEnabled(true);

//
//        final int maxSize = (bitmap.getWidth() > bitmap.getHeight()) ? width : height;
//        int outWidth;
//        int outHeight;
//        int inWidth = bitmap.getWidth();
//        int inHeight = bitmap.getHeight();
//        if (inWidth > inHeight) {
//            outWidth = maxSize;
//            outHeight = (inHeight * maxSize) / inWidth;
//        } else {
//            outHeight = maxSize;
//            outWidth = (inWidth * maxSize) / inHeight;
//        }

        findViewById(R.id.pressButton).setOnClickListener(this);
//        findViewById(R.id.pressButton).setVisibility(View.GONE);
//        cropImageView = (CropImageView) findViewById(R.id.CropImageView);
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
//        cropImageView.setImageBitmap(resizedBitmap);
////        cropImageView.setImageBitmap(resizedBitmap);
//        cropImageView.setAspectRatio(10, 10);
//        cropImageView.setFixedAspectRatio(true);
//        cropImageView.setGuidelines(2);

//        cropImageView.setCropType(CropImageView.CropType.CENTER_TOP);
//        testCruupLibriary();
//        ImageView customImageVIew= (CustomImageVIew)findViewById(R.id.image);
//        customImageVIew.setImageBitmap(bitmap);
//        customImageVIew.setScaleType(ImageView.ScaleType.FIT_CENTER);  // make the image fit to
// the center.
//        customImageVIew.setOnTouchListener(this);
//        customImageVIew = (CustomImageVIew) findViewById(R.id.image);
//        customImageVIew.setScaleType(ImageView.ScaleType.CENTER_CROP);
////        customImageVIew.setImageBitmap(bitmap);
//
//        customImageVIew.setImage(bitmap);
//        customImageVIew.setDrawingCacheEnabled(true);
//        customImageVIew.buildDrawingCache(true);
        touchImageView =(TouchImageView)findViewById(R.id.touchImageView);
        touchImageView.setPhoto(bitmap);

        touchImageView.setOnClickListener(this);
//        cropView=(CropView)findViewById(R.id.CropViewImage);
//        cropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        cropView.setImageBitmap(bitmap);
//        imageView = (ImageView) findViewById(R.id.resultImage);
//        imageView.setImageBitmap(touchImageView.creatNewPhoto());

//        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, bitmap);
//        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//        bitmapOptions.inJustDecodeBounds = false;
//        Bitmap wallpaperBitmap = BitmapFactory.decodeResource(getResources(), R.drawable
// .test_image, bitmapOptions);
//
//        cropImageView = (CropImageView) this.findViewById(R.id.CropImageView);
//        cropImageView.setImageBitmap(resizedBitmap);
//        cropImageView.setFixedAspectRatio(true);
//        cropImageView.setAspectRatio(10, 10);
//        cropImageView.rotateImage(90);
//
//        ImageView imageView = (ImageView) findViewById(R.id.resultImage);
//        Bitmap croppedImage = cropImageView.getCroppedImage();
//
//        if (croppedImage != null) {
//            imageView.setImageBitmap(cropImageView.getCroppedImage());
//        }

//        cropImage.setOutlineColor(0xFF03A9F4);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        cropImage.setSourceImage(byteArray);
//
//        imageView.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));

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
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        modelBitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Intent intent = new Intent(getApplicationContext(), ShowBitmap.class);
        intent.putExtra("image", byteArray);
        startActivity(intent);


    }

    @Override
    public boolean onLongClick(View view) {

        return false;
    }
}
