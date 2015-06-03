package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    float x_down = 0;
    float y_down = 0;
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float oldRotation = 0;
    Matrix matrix = new Matrix();
    Matrix matrix1 = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix scaledMatrix = new Matrix();
    private int mViewWidth = -1;
    private int mViewHeight = -1;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int AFTER_ZOOM = 3;
    int mode = NONE;
    DrawLine drawLine = new DrawLine();
    boolean matrixCheck = false;

    int widthScreen;
    int heightScreen;
    private int misScreenSize;

    Bitmap gintama;

    private float distanceToCenterFromLeftEdge, distanceToCenterFromRightEdge,
            distanceToCenterFromTopEdge, distanceToCenterFromBottomEdge;
    private float deltaX, deltaY;
    private float maxAddDeltaX, maxAddDeltaY;
    private float mOldEventX, mOldEventY;
    private float maxScale, minScale;

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            DisplayMetrics dm = new DisplayMetrics();
            if (context instanceof Activity)
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            widthScreen = dm.widthPixels;
            heightScreen = dm.heightPixels;
            misScreenSize = Math.min(widthScreen, heightScreen);
            matrix = new Matrix();
        }
    }


    public void setPhoto(int photo) {
        gintama = BitmapFactory.decodeResource(getResources(), photo);
    }

    public void setPhoto(Bitmap photo) {
        oldBitmap = Bitmap.createBitmap(photo);

        maxScale = Tools.getMaxZoom(photo, getContext());
        minScale = 1f;

//        RectF drawableRect = new RectF(0, 0, gintama.getWidth(), gintama.getHeight());
        ///RectF viewRect = new RectF(0, 0, widthScreen, heightScreen);

        //matrix.setRectToRect(viewRect, viewRect, Matrix.ScaleToFit.CENTER);

        //think grid in center of picture
        // (dist from left grid edge to left image edge = dist from right grid edge to right
        // image edge )
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawLine.setSize(w, h);
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        widthScreen = size.x;
        widthScreen = w;
        heightScreen = h;

        misScreenSize = Math.min(widthScreen, heightScreen);
        if (gintama != null) {
            gintama.recycle();
        }

        if (oldBitmap.getWidth() < oldBitmap.getHeight()) {
            gintama = CropFromBitmap.getResizedBitmap(oldBitmap, misScreenSize);
        } else {
            gintama = CropFromBitmap.getResizedBitmapH(oldBitmap, misScreenSize);
        }

        reload();
    }

    public void reload() {

        if (widthScreen < heightScreen) {
            maxAddDeltaX = maxAddDeltaY = widthScreen / 3f;
            distanceToCenterFromLeftEdge = distanceToCenterFromRightEdge = gintama.getWidth() / 2f;
            distanceToCenterFromBottomEdge = distanceToCenterFromTopEdge = gintama.getHeight() / 2f;
            matrix1.reset();

            if (gintama.getWidth() < gintama.getHeight()) {
                matrix1.postTranslate(0, -(gintama.getHeight() - heightScreen) / 2f);
            } else {
                matrix1.postTranslate(-(gintama.getWidth() - widthScreen) / 2f, (heightScreen -
                        widthScreen) / 2f);
            }
            matrix.set(matrix1);
        } else {
            maxAddDeltaX = maxAddDeltaY = heightScreen / 3f;
            distanceToCenterFromLeftEdge = distanceToCenterFromRightEdge = gintama.getWidth() / 2f;
            distanceToCenterFromBottomEdge = distanceToCenterFromTopEdge = gintama.getHeight() / 2f;
            matrix1.reset();

            if (gintama.getWidth() < gintama.getHeight()) {
                matrix1.postTranslate((widthScreen - heightScreen) / 2, -(gintama.getHeight() -
                        heightScreen) / 2f);
            } else {
                matrix1.postTranslate(-(gintama.getWidth() - widthScreen) / 2f, 0);
            }
            matrix.set(matrix1);
        }

        deltaX = deltaY = 0;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(gintama, matrix, null);
        drawLine.drawThirds(canvas);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                x_down = event.getX();
                y_down = event.getY();
                mOldEventX = event.getX();
                mOldEventY = event.getY();
                savedMatrix.set(matrix);
                scaledMatrix.reset();
                deltaX = deltaY = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                oldDist = spacing(event);
                oldRotation = rotation(event);
                float vals[] = new float[9];
                matrix.getValues(vals);
                float scaledVals[] = new float[9];
                scaledMatrix.getValues(scaledVals);
                float prevVals[] = new float[9];
                savedMatrix.getValues(prevVals);
                float prevScale = prevVals[Matrix.MSCALE_X];
                scaledVals[Matrix.MSCALE_X] = scaledVals[Matrix.MSCALE_Y] = vals[Matrix.MSCALE_X] /
                        prevScale;
                scaledMatrix.setValues(scaledVals);
                midPoint(mid, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {

                    float rotation = rotation(event) - oldRotation;
                    float newDist = spacing1(event);
                    float scale = newDist / oldDist;

                    vals = new float[9];
                    matrix1.set(scaledMatrix);
                    matrix1.getValues(vals);

                    prevVals = new float[9];
                    savedMatrix.getValues(prevVals);
                    prevScale = prevVals[Matrix.MSCALE_X];

                    float newScale = vals[Matrix.MSCALE_X] * scale * prevScale;

                    if (newScale < minScale) {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = minScale;
                    } else if (newScale > maxScale) {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = maxScale;
                    } else {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = scale * prevScale;
                    }
                    matrix1.setValues(vals);
                    vals = new float[9];
                    matrix1.getValues(vals);
                    newScale = vals[Matrix.MSCALE_X];
                    newScale /= prevScale;
                    matrix1.set(savedMatrix);
                    if (deltaX != 0 || deltaY != 0) {
                        matrix1.postTranslate(deltaX, deltaY);
                    }
                    matrix1.postScale(newScale, newScale, mid.x, mid.y);

                    //matrix1.postRotate(rotation, mid.x, mid.y);
                    //matrixCheck = matrixCheck();

                    if (matrixCheck == false) {
                        matrix.set(matrix1);
                        invalidate();
                    }
                } else if (mode == DRAG) {
                    if (mOldEventX == event.getX() && mOldEventY == event.getY()) {
                        break;
                    }
                    mOldEventX = event.getX();
                    mOldEventY = event.getY();

                    float deltaX = event.getX() - x_down;
                    float deltaY = event.getY() - y_down;

                    vals = new float[9];
                    savedMatrix.getValues(vals);

                    float screenWidth_2 = misScreenSize / 2f;

                    if (-screenWidth_2 + distanceToCenterFromLeftEdge - deltaX +
                            maxAddDeltaX < 0) {
                        deltaX = -screenWidth_2 + maxAddDeltaX +
                                distanceToCenterFromLeftEdge;
                    } else if (-screenWidth_2 + distanceToCenterFromRightEdge +
                            deltaX + maxAddDeltaX < 0) {
                        deltaX = screenWidth_2 - maxAddDeltaX -
                                distanceToCenterFromRightEdge;
                    }

                    if (-screenWidth_2 + maxAddDeltaY + distanceToCenterFromTopEdge -
                            deltaY < 0) {
                        deltaY = -screenWidth_2 + maxAddDeltaY +
                                distanceToCenterFromTopEdge;
                    } else if (-screenWidth_2 + maxAddDeltaY +
                            distanceToCenterFromBottomEdge +
                            deltaY < 0) {
                        deltaY = screenWidth_2 - maxAddDeltaY -
                                distanceToCenterFromBottomEdge;
                    }

                    matrix1.set(savedMatrix);
                    matrix1.postTranslate(deltaX, deltaY);

                    this.deltaX = deltaX;
                    this.deltaY = deltaY;

                    if (matrixCheck == false) {
                        matrix.set(matrix1);
                        invalidate();
                    }

                    //matrixCheck = matrixCheck();
                    //matrixCheck = matrixCheck();

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mode == DRAG || mode == AFTER_ZOOM) {
                    moveToEdge(mode);
                }
                deltaX = deltaY = 0;
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = AFTER_ZOOM;
                break;
        }
        return true;
    }

    private void moveToEdge(int mode) {
        float screenWidth_2 = misScreenSize / 2f;
        float vals[] = new float[9];
        matrix1.getValues(vals);
        float scale = vals[Matrix.MSCALE_X];
        savedMatrix.getValues(vals);
        float oldScale = vals[Matrix.MSCALE_X];
        scale /= oldScale;

        if (distanceToCenterFromLeftEdge * scale - screenWidth_2 < deltaX * scale) {
            deltaX = distanceToCenterFromLeftEdge - screenWidth_2 / scale;
        } else if (distanceToCenterFromRightEdge * scale - screenWidth_2 < -deltaX * scale) {
            deltaX = -distanceToCenterFromRightEdge + screenWidth_2 / scale;
        }

        if (distanceToCenterFromTopEdge * scale - screenWidth_2 < deltaY * scale) {
            deltaY = distanceToCenterFromTopEdge - screenWidth_2 / scale;
        } else if (distanceToCenterFromBottomEdge * scale - screenWidth_2 < -deltaY * scale) {
            deltaY = -distanceToCenterFromBottomEdge + screenWidth_2 / scale;
        }

        matrix1.set(savedMatrix);

        if (mode == AFTER_ZOOM) {
            matrix1.postScale(scale, scale, mid.x, mid.y);
            distanceToCenterFromLeftEdge *= scale;
            distanceToCenterFromRightEdge *= scale;
            distanceToCenterFromTopEdge *= scale;
            distanceToCenterFromBottomEdge *= scale;
        }

        distanceToCenterFromLeftEdge -= deltaX;
        distanceToCenterFromRightEdge += deltaX;
        distanceToCenterFromTopEdge -= deltaY;
        distanceToCenterFromBottomEdge += deltaY;

        matrix1.postTranslate(deltaX, deltaY);
        if (matrixCheck == false) {
            matrix.set(matrix1);
            invalidate();
        }
    }

    private boolean matrixCheck() {
        float[] f = new float[9];
        matrix1.getValues(f);
        // 4
        float x1 = f[0] * 0 + f[1] * 0 + f[2];
        float y1 = f[3] * 0 + f[4] * 0 + f[5];
        float x2 = f[0] * gintama.getWidth() + f[1] * 0 + f[2];
        float y2 = f[3] * gintama.getWidth() + f[4] * 0 + f[5];
        float x3 = f[0] * 0 + f[1] * gintama.getHeight() + f[2];
        float y3 = f[3] * 0 + f[4] * gintama.getHeight() + f[5];
        float x4 = f[0] * gintama.getWidth() + f[1] * gintama.getHeight() + f[2];
        float y4 = f[3] * gintama.getWidth() + f[4] * gintama.getHeight() + f[5];
        //
        double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        //
        if (width < widthScreen / 3 || width > widthScreen * 3) {
            return true;
        }
        //
        if ((x1 < widthScreen / 3 && x2 < widthScreen / 3
                && x3 < widthScreen / 3 && x4 < widthScreen / 3)
                || (x1 > widthScreen * 2 / 3 && x2 > widthScreen * 2 / 3
                && x3 > widthScreen * 2 / 3 && x4 > widthScreen * 2 / 3)
                || (y1 < heightScreen / 3 && y2 < heightScreen / 3
                && y3 < heightScreen / 3 && y4 < heightScreen / 3)
                || (y1 > heightScreen * 2 / 3 && y2 > heightScreen * 2 / 3
                && y3 > heightScreen * 2 / 3 && y4 > heightScreen * 2 / 3)) {
            return true;
        }
        return false;
    }

    //
    private float spacing(MotionEvent event) {
        float x0 = event.getX(0);
        float x1 = event.getX(1);
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private float spacing1(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    //
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
        point.set(widthScreen / 2, heightScreen / 2);
    }

    //x1, y1 - (left, right, top, bottom) ; x2, y2 - midpoint

    //scale * (left + (x2-x1)) - do x2, scale * (right - (x2 - x1)),
    // - scale * (x2 - x1)                        + scale * (x2 - x1)
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private Bitmap oldBitmap;

    public Bitmap creatNewPhoto() {
        Bitmap bitmap = Bitmap.createBitmap(widthScreen, heightScreen, Config.ARGB_8888); //
        float scale = (float) oldBitmap.getWidth() / gintama.getWidth();
        float vals[] = new float[9];
        Matrix mSave = new Matrix();
        mSave.set(matrix);
        mSave.getValues(vals);
        vals[Matrix.MSCALE_X] /= scale;
        vals[Matrix.MSCALE_Y] /= scale;
        mSave.setValues(vals);
        Canvas canvas = new Canvas(bitmap); //
        canvas.drawBitmap(oldBitmap, mSave, null); //
        canvas.save(Canvas.ALL_SAVE_FLAG); //
        canvas.restore();

        Bitmap res =  CropFromBitmap.scaleCenterCrop(bitmap, drawLine.getmViewWidth(), drawLine
                .getmViewWidth());
        bitmap.recycle();
        return res;
//        return bitmap;
    }

}
