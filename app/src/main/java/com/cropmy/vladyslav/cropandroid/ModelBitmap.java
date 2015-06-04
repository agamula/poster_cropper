package com.cropmy.vladyslav.cropandroid;

import android.graphics.Bitmap;

import java.io.Serializable;


public class ModelBitmap implements Serializable {

    public ModelBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ModelBitmap() {

    }

    private Bitmap bitmap = null;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
