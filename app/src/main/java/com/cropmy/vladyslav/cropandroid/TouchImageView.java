package com.cropmy.vladyslav.cropandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int AFTER_ZOOM = 3;

    private float xDown = 0;
    private float yDown = 0;
    private PointF mScaleCenter = new PointF();
    private float mStartDistance = 1f;
    private float mOldRotation = 0;
    private Matrix mMatrix = new Matrix();
    private Matrix mMatrix1 = new Matrix();
    private Matrix mSavedMatrix = new Matrix();
    private Matrix mScaledMatrix = new Matrix();
    private int mMode = NONE;
    //DrawLine drawLine = new DrawLine();
    private final GridDrawable mGridDrawable = new GridDrawable(3);
    private boolean matrixCheck = false;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mMinScreenSize;
    private Bitmap mBitmap;
    private Bitmap mRealBitmap;
    private float mDistanceToCenterFromLeftEdge, mDistanceToCenterFromRightEdge,
            mDistanceToCenterFromTopEdge, mDistanceToCenterFromBottomEdge;
    private float mDeltaX, mDeltaY;
    private float mMaxAddDeltaX, mMaxAddDeltaY;
    private float mOldEventX, mOldEventY;
    private float mMaxScale, mMinScale;

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            DisplayMetrics dm = new DisplayMetrics();
            if (context instanceof Activity)
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
            mMinScreenSize = Math.min(mScreenWidth, mScreenHeight);
            mMatrix = new Matrix();
        }
    }

    public void setPhoto(Bitmap photo) {
        mRealBitmap = Bitmap.createBitmap(photo);

        mMaxScale = ConvertUtils.getMaxZoom(photo, getContext());
        mMinScale = 1f;
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mGridDrawable.setSize(w, h);
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenWidth = w;
        mScreenHeight = h;

        mMinScreenSize = Math.min(mScreenWidth, mScreenHeight);
        if (mBitmap != null) {
            mBitmap.recycle();
        }

        if (mRealBitmap.getWidth() < mRealBitmap.getHeight()) {
            mBitmap = BitmapUtils.getResizedBitmap(mRealBitmap, mMinScreenSize);
        } else {
            mBitmap = BitmapUtils.getResizedBitmapH(mRealBitmap, mMinScreenSize);
        }

        reload();
    }

    public void reload() {

        if (mScreenWidth < mScreenHeight) {
            mMaxAddDeltaX = mMaxAddDeltaY = mScreenWidth / 3f;
            mDistanceToCenterFromLeftEdge = mDistanceToCenterFromRightEdge = mBitmap.getWidth() / 2f;
            mDistanceToCenterFromBottomEdge = mDistanceToCenterFromTopEdge = mBitmap.getHeight() / 2f;
            mMatrix1.reset();

            if (mBitmap.getWidth() < mBitmap.getHeight()) {
                mMatrix1.postTranslate(0, -(mBitmap.getHeight() - mScreenHeight) / 2f);
            } else {
                mMatrix1.postTranslate(-(mBitmap.getWidth() - mScreenWidth) / 2f, (mScreenHeight -
                        mScreenWidth) / 2f);
            }
            mMatrix.set(mMatrix1);
        } else {
            mMaxAddDeltaX = mMaxAddDeltaY = mScreenHeight / 3f;
            mDistanceToCenterFromLeftEdge = mDistanceToCenterFromRightEdge = mBitmap.getWidth() / 2f;
            mDistanceToCenterFromBottomEdge = mDistanceToCenterFromTopEdge = mBitmap.getHeight() / 2f;
            mMatrix1.reset();

            if (mBitmap.getWidth() < mBitmap.getHeight()) {
                mMatrix1.postTranslate((mScreenWidth - mScreenHeight) / 2, -(mBitmap.getHeight() -
                        mScreenHeight) / 2f);
            } else {
                mMatrix1.postTranslate(-(mBitmap.getWidth() - mScreenWidth) / 2f, 0);
            }
            mMatrix.set(mMatrix1);
        }

        mDeltaX = mDeltaY = 0;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(mBitmap, mMatrix, null);
        mGridDrawable.draw(canvas);
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mMode = DRAG;
                xDown = event.getX();
                yDown = event.getY();
                mOldEventX = event.getX();
                mOldEventY = event.getY();
                mSavedMatrix.set(mMatrix);
                mScaledMatrix.reset();
                mDeltaX = mDeltaY = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mMode = ZOOM;
                mStartDistance = spacing(event);
                mOldRotation = rotation(event);
                float vals[] = new float[9];
                mMatrix.getValues(vals);
                float scaledVals[] = new float[9];
                mScaledMatrix.getValues(scaledVals);
                float prevVals[] = new float[9];
                mSavedMatrix.getValues(prevVals);
                float prevScale = prevVals[Matrix.MSCALE_X];
                scaledVals[Matrix.MSCALE_X] = scaledVals[Matrix.MSCALE_Y] = vals[Matrix.MSCALE_X] /
                        prevScale;
                mScaledMatrix.setValues(scaledVals);
                midPoint(mScaleCenter, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMode == ZOOM && mMaxScale > 1.0f) {

                    float rotation = rotation(event) - mOldRotation;
                    float newDist = spacing(event);
                    float scale = newDist / mStartDistance;

                    vals = new float[9];
                    mMatrix1.set(mScaledMatrix);
                    mMatrix1.getValues(vals);

                    prevVals = new float[9];
                    mSavedMatrix.getValues(prevVals);
                    prevScale = prevVals[Matrix.MSCALE_X];

                    float newScale = vals[Matrix.MSCALE_X] * scale * prevScale;

                    if (newScale < mMinScale) {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = mMinScale;
                    } else if (newScale > mMaxScale) {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = mMaxScale;
                    } else {
                        vals[Matrix.MSCALE_X] = vals[Matrix.MSCALE_Y] = scale * prevScale;
                    }
                    mMatrix1.setValues(vals);
                    vals = new float[9];
                    mMatrix1.getValues(vals);
                    newScale = vals[Matrix.MSCALE_X];
                    newScale /= prevScale;
                    mMatrix1.set(mSavedMatrix);
                    if (mDeltaX != 0 || mDeltaY != 0) {
                        mMatrix1.postTranslate(mDeltaX, mDeltaY);
                    }
                    mMatrix1.postScale(newScale, newScale, mScaleCenter.x, mScaleCenter.y);

                    //mMatrix1.postRotate(rotation, mScaleCenter.x, mScaleCenter.y);
                    //matrixCheck = matrixCheck();

                    if (matrixCheck == false) {
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }
                } else if (mMode == DRAG) {
                    if (mOldEventX == event.getX() && mOldEventY == event.getY()) {
                        break;
                    }
                    mOldEventX = event.getX();
                    mOldEventY = event.getY();

                    float deltaX = event.getX() - xDown;
                    float deltaY = event.getY() - yDown;

                    vals = new float[9];
                    mSavedMatrix.getValues(vals);

                    float screenWidth_2 = mMinScreenSize / 2f;

                    if (-screenWidth_2 + mDistanceToCenterFromLeftEdge - deltaX +
                            mMaxAddDeltaX < 0) {
                        deltaX = -screenWidth_2 + mMaxAddDeltaX +
                                mDistanceToCenterFromLeftEdge;
                    } else if (-screenWidth_2 + mDistanceToCenterFromRightEdge +
                            deltaX + mMaxAddDeltaX < 0) {
                        deltaX = screenWidth_2 - mMaxAddDeltaX -
                                mDistanceToCenterFromRightEdge;
                    }

                    if (-screenWidth_2 + mMaxAddDeltaY + mDistanceToCenterFromTopEdge -
                            deltaY < 0) {
                        deltaY = -screenWidth_2 + mMaxAddDeltaY +
                                mDistanceToCenterFromTopEdge;
                    } else if (-screenWidth_2 + mMaxAddDeltaY +
                            mDistanceToCenterFromBottomEdge +
                            deltaY < 0) {
                        deltaY = screenWidth_2 - mMaxAddDeltaY -
                                mDistanceToCenterFromBottomEdge;
                    }

                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postTranslate(deltaX, deltaY);

                    this.mDeltaX = deltaX;
                    this.mDeltaY = deltaY;

                    if (matrixCheck == false) {
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }

                    //matrixCheck = matrixCheck();
                    //matrixCheck = matrixCheck();

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mMode == DRAG || mMode == AFTER_ZOOM && mMaxScale > 1.0f) {
                    moveToEdge(mMode);
                }
                mDeltaX = mDeltaY = 0;
                mMode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mMode = AFTER_ZOOM;
                break;
        }
        return true;
    }

    private void moveToEdge(int mode) {
        float screenWidth_2 = mMinScreenSize / 2f;
        float vals[] = new float[9];
        mMatrix1.getValues(vals);
        float scale = vals[Matrix.MSCALE_X];
        mSavedMatrix.getValues(vals);
        float oldScale = vals[Matrix.MSCALE_X];
        scale /= oldScale;

        if (mDistanceToCenterFromLeftEdge * scale - screenWidth_2 < mDeltaX * scale) {
            mDeltaX = mDistanceToCenterFromLeftEdge - screenWidth_2 / scale;
        } else if (mDistanceToCenterFromRightEdge * scale - screenWidth_2 < -mDeltaX * scale) {
            mDeltaX = -mDistanceToCenterFromRightEdge + screenWidth_2 / scale;
        }

        if (mDistanceToCenterFromTopEdge * scale - screenWidth_2 < mDeltaY * scale) {
            mDeltaY = mDistanceToCenterFromTopEdge - screenWidth_2 / scale;
        } else if (mDistanceToCenterFromBottomEdge * scale - screenWidth_2 < -mDeltaY * scale) {
            mDeltaY = -mDistanceToCenterFromBottomEdge + screenWidth_2 / scale;
        }

        mMatrix1.set(mSavedMatrix);

        if (mode == AFTER_ZOOM) {
            mMatrix1.postScale(scale, scale, mScaleCenter.x, mScaleCenter.y);
            mDistanceToCenterFromLeftEdge *= scale;
            mDistanceToCenterFromRightEdge *= scale;
            mDistanceToCenterFromTopEdge *= scale;
            mDistanceToCenterFromBottomEdge *= scale;
        }

        mDistanceToCenterFromLeftEdge -= mDeltaX;
        mDistanceToCenterFromRightEdge += mDeltaX;
        mDistanceToCenterFromTopEdge -= mDeltaY;
        mDistanceToCenterFromBottomEdge += mDeltaY;

        mMatrix1.postTranslate(mDeltaX, mDeltaY);
        if (matrixCheck == false) {
            mMatrix.set(mMatrix1);
            invalidate();
        }
    }

    private boolean matrixCheck() {
        float[] f = new float[9];
        mMatrix1.getValues(f);
        // 4
        float x1 = f[0] * 0 + f[1] * 0 + f[2];
        float y1 = f[3] * 0 + f[4] * 0 + f[5];
        float x2 = f[0] * mBitmap.getWidth() + f[1] * 0 + f[2];
        float y2 = f[3] * mBitmap.getWidth() + f[4] * 0 + f[5];
        float x3 = f[0] * 0 + f[1] * mBitmap.getHeight() + f[2];
        float y3 = f[3] * 0 + f[4] * mBitmap.getHeight() + f[5];
        float x4 = f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight() + f[2];
        float y4 = f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight() + f[5];
        //
        double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        //
        if (width < mScreenWidth / 3 || width > mScreenWidth * 3) {
            return true;
        }
        //
        if ((x1 < mScreenWidth / 3 && x2 < mScreenWidth / 3
                && x3 < mScreenWidth / 3 && x4 < mScreenWidth / 3)
                || (x1 > mScreenWidth * 2 / 3 && x2 > mScreenWidth * 2 / 3
                && x3 > mScreenWidth * 2 / 3 && x4 > mScreenWidth * 2 / 3)
                || (y1 < mScreenHeight / 3 && y2 < mScreenHeight / 3
                && y3 < mScreenHeight / 3 && y4 < mScreenHeight / 3)
                || (y1 > mScreenHeight * 2 / 3 && y2 > mScreenHeight * 2 / 3
                && y3 > mScreenHeight * 2 / 3 && y4 > mScreenHeight * 2 / 3)) {
            return true;
        }
        return false;
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
        point.set(mScreenWidth / 2, mScreenHeight / 2);
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

    public Pair<Bitmap, Integer> getImageInsideGrid(int countPixels) {
        float vals[] = new float[9];
        Matrix mSave = new Matrix();
        mSave.set(mMatrix);
        mSave.getValues(vals);

        int leftMargin = 0;
        int topMargin = 0;
        int rightMargin = 0;
        int bottomMargin = 0;

        /*if (mDistanceToCenterFromLeftEdge <= mScreenWidth / 2f + countPixels) {
            leftMargin = (int) (mScreenWidth / 2f + countPixels - (int) mDistanceToCenterFromLeftEdge);
        }
        if (mDistanceToCenterFromTopEdge <= mScreenWidth / 2f + countPixels) {
            topMargin = (int) (mScreenWidth / 2f + countPixels - (int) mDistanceToCenterFromTopEdge);
        }
        if (mDistanceToCenterFromRightEdge <= mScreenWidth / 2f + countPixels) {
            rightMargin = (int) (mScreenWidth / 2f + countPixels - (int) mDistanceToCenterFromRightEdge);
        }
        if (mDistanceToCenterFromBottomEdge <= mScreenWidth / 2f + countPixels) {
            bottomMargin = (int) (mScreenWidth / 2f + countPixels - (int)
                    mDistanceToCenterFromBottomEdge);
        } */

        int saveWidth = countPixels - leftMargin + countPixels - rightMargin + mScreenWidth;
        int saveHeight = countPixels - bottomMargin + countPixels - topMargin + mScreenWidth;

        Bitmap bitmap = Bitmap.createBitmap(saveWidth, saveHeight, Config.ARGB_8888); //


        int minSize = Math.min(mRealBitmap.getWidth(), mRealBitmap.getHeight());
        boolean isOriginalLess = minSize < mScreenWidth;
        final Bitmap drawingBitmap;

        if (isOriginalLess) {
            drawingBitmap = mBitmap;
            minSize = mScreenWidth;
        } else {
            drawingBitmap = mRealBitmap;
        }

        int drawingBMWidth = minSize;
        float scale = mScreenWidth / (float)drawingBMWidth;

        vals[Matrix.MSCALE_X] *= scale;
        vals[Matrix.MSCALE_Y] *= scale;

        vals[Matrix.MTRANS_X] -= (mScreenWidth - mMinScreenSize) / 2f;
        vals[Matrix.MTRANS_Y] -= (mScreenHeight - mMinScreenSize) / 2f;

        float diffDistHor = (leftMargin - rightMargin) / 2f;
        float diffDistVert = (topMargin - bottomMargin) / 2f;

        if (vals[Matrix.MTRANS_X] <= -countPixels) {
            vals[Matrix.MTRANS_X] += countPixels;
        } else /*if (vals[Matrix.MTRANS_X] <= -diffDistHor) {
            vals[Matrix.MTRANS_X] += diffDistHor;
        } else */{
            vals[Matrix.MTRANS_X] = 0;
        }

        if (vals[Matrix.MTRANS_Y] <= -countPixels) {
            vals[Matrix.MTRANS_Y] += countPixels;
        } else /*if (vals[Matrix.MTRANS_Y] <= -diffDistVert) {
            vals[Matrix.MTRANS_Y] += diffDistVert;
        } else */{
            vals[Matrix.MTRANS_Y] = 0;
        }

        mSave.setValues(vals);

        Canvas canvas = new Canvas(bitmap); //
        canvas.drawBitmap(drawingBitmap, mSave, null); //
        canvas.save(Canvas.ALL_SAVE_FLAG); //
        canvas.restore();
        return new Pair<>(bitmap, EncodeUtils.encodeMargins(countPixels, new int[]{leftMargin,
                topMargin, rightMargin, bottomMargin}));
    }
}
