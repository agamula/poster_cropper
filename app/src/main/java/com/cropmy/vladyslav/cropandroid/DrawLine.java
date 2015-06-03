package com.cropmy.vladyslav.cropandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class DrawLine {

    private int mViewWidth = -1;
    private int mViewHeight = -1;

    public int getmViewWidth() {
        return mViewWidth;
    }

    public DrawLine() {

    }

    public void setSize(int w, int h) {
        mViewHeight = h;
        mViewWidth = w;
    }

    public void drawThirds(Canvas canvas) {
        int wigthLine = 3;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(wigthLine);
        int blockWidth = mViewWidth / 2;
        int blockHeight = mViewWidth / 3;
        int linekHeight = (mViewHeight - mViewWidth) / 2;
        int linekHeight2 = blockWidth / 3;
        canvas.drawLine(wigthLine, linekHeight + (linekHeight2 * 2), mViewWidth - wigthLine,
                linekHeight + (linekHeight2 * 2), p);
        canvas.drawLine(wigthLine, linekHeight + (linekHeight2 * 4), mViewWidth - wigthLine,
                linekHeight + (linekHeight2 * 4), p);
        canvas.drawLine(blockHeight, linekHeight - wigthLine, blockHeight, (mViewHeight -
                linekHeight) - wigthLine, p);
        canvas.drawLine(blockHeight * 2, linekHeight - wigthLine, blockHeight * 2, (mViewHeight
                - linekHeight) - wigthLine, p);
        canvas.drawRect(wigthLine, linekHeight - wigthLine, mViewWidth - wigthLine, (mViewWidth
                + linekHeight) - wigthLine, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.BLACK);
        p.setAlpha(200);
        canvas.drawRect(0, 0, mViewWidth, linekHeight - wigthLine, p);
        canvas.drawRect(0, (mViewWidth + linekHeight) - wigthLine, mViewWidth, mViewHeight, p);
    }

}
