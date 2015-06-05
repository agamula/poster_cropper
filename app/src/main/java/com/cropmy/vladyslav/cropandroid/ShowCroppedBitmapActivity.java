package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class ShowCroppedBitmapActivity extends Activity{

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
        File marginPath = new File(getFilesDir(), MainActivity.MARGINS_PATH);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), options);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream
                    (marginPath)));
            String fileContent = reader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(fileContent, ";");
            int countPixels = Integer.parseInt(tokenizer.nextToken());
            int encoded = Integer.parseInt(tokenizer.nextToken());
            int margins[] = EncodeUtils.decodeMargins(countPixels, encoded);
            Bitmap bm = BitmapUtils.getShowing(bitmap, countPixels, margins);
            resultImage.setImageBitmap(bm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(resultImage);
    }
}
