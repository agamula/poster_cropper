package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class MainActivity extends Activity implements  View
        .OnClickListener {

    public static final String IMAGE_NAME = "image.png";
    public static final String MARGINS_PATH = "margins.txt";

    private Bitmap mBitmap;
    private Bitmap mCroppedBitmap;
    private TouchImageView mTouchImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2, options);
        mTouchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        mTouchImageView.setPhoto(mBitmap);
        findViewById(R.id.pressButton).setOnClickListener(this);
        Log.d("calculate ","1mm to px=" + ConvertUtils.mmToPx(1, this));
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
            mTouchImageView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View view) {
        int countInPixels = 50;
        Pair<Bitmap, Integer> photo = mTouchImageView.getImageInsideGrid(countInPixels);
        mCroppedBitmap = photo.first;

        File imagePath = new File(getFilesDir(), IMAGE_NAME);
        File marginPath = new File(getFilesDir(), MARGINS_PATH);
        if (!imagePath.exists()) {
            try {
                imagePath.createNewFile();
                marginPath.createNewFile();
                saveMargins(marginPath, countInPixels, photo.second);
                saveImage(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveMargins(marginPath, countInPixels, photo.second);
            saveImage(imagePath);
        }
    }

    private void saveMargins(File marginPath, int countInPixels, int encodedMargin) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream
                    (marginPath));
            writer.write(countInPixels + ";" + encodedMargin);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(final File imagePath) {
        boolean res = false;
        try {
            res = mCroppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, new
                    FileOutputStream(imagePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            res = false;
        } finally {
            if(res) {
                Intent intent = new Intent(getApplicationContext(), ShowCroppedBitmapActivity.class);
                startActivity(intent);
            }
        }   /*

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
        }.execute();  */
    }
}
