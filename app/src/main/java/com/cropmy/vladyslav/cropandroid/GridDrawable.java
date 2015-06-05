package com.cropmy.vladyslav.cropandroid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class GridDrawable extends Drawable{
    
    private int mDrawableWidth, mDrawableHeight;
    private final int mLineWidth;
    
    public GridDrawable(int mLineWidth) {
        this.mLineWidth = mLineWidth;
    }

    public void setSize(int mDrawableWidth, int mDrawableHeight) {
        this.mDrawableWidth = mDrawableWidth;
        this.mDrawableHeight = mDrawableHeight;
    }
    
    @Override
    public void draw(Canvas canvas) {
        if(mDrawableWidth == 0) {
            return;
        }
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(mLineWidth);
        int blockWidth = mDrawableWidth / 2;
        int blockHeight = mDrawableWidth / 3;
        int linekHeight = (mDrawableHeight - mDrawableWidth) / 2;
        int linekHeight2 = blockWidth / 3;
        canvas.drawLine(mLineWidth, linekHeight + (linekHeight2 * 2), mDrawableWidth - mLineWidth,
                linekHeight + (linekHeight2 * 2), p);
        canvas.drawLine(mLineWidth, linekHeight + (linekHeight2 * 4), mDrawableWidth - mLineWidth,
                linekHeight + (linekHeight2 * 4), p);
        canvas.drawLine(blockHeight, linekHeight - mLineWidth, blockHeight, (mDrawableHeight -
                linekHeight) - mLineWidth, p);
        canvas.drawLine(blockHeight * 2, linekHeight - mLineWidth, blockHeight * 2, (mDrawableHeight
                - linekHeight) - mLineWidth, p);
        canvas.drawRect(mLineWidth, linekHeight - mLineWidth, mDrawableWidth - mLineWidth, (mDrawableWidth
                + linekHeight) - mLineWidth, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.BLACK);
        p.setAlpha(200);
        canvas.drawRect(0, 0, mDrawableWidth, linekHeight - mLineWidth, p);
        canvas.drawRect(0, (mDrawableWidth + linekHeight) - mLineWidth, mDrawableWidth, mDrawableHeight, p);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
