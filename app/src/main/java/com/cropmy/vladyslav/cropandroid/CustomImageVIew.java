package com.cropmy.vladyslav.cropandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by vladyslav on 5/21/15.
 */
public class CustomImageVIew extends ImageView implements View.OnTouchListener {


    private Point mBitmapMiddlePoint = new Point();

    private int mViewWidth = -1;
    private int mViewHeight = -1;
    private int mBitmapWidth = -1;
    private int mBitmapHeight = -1;
    private boolean mDraggable = false;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    private static final String TAG = "Touch";
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    float scale = 0;
    boolean drawLine = true;
    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    public void setImage(Bitmap image) {
        this.image = image;
    }

    private Bitmap image;

    public Bitmap getCropimage() {
        return cropimage;
    }

    private Bitmap cropimage;

    public CustomImageVIew(Context context) {
        this(context, null, 0);
        init(context);
    }

    public CustomImageVIew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CustomImageVIew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnTouchListener(this);
        init(context);
    }

    void init(Context context) {

    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        int w=0,h=0;
////        heightofBitMap = width * heightofBitMap / widthofBitMap;
//        if(image.getWidth() < mViewWidth){
//            w=mViewWidth;
//            h=mViewWidth*mViewHeight/image.getWidth();
//            image=getResizedBitmap(image,w,h);
//        }


        Bitmap imageBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), savedMatrix, false);


        int xCenter = (mViewWidth - imageBitmap.getWidth()) / 2;
        int yCenter = (mViewHeight - imageBitmap.getHeight()) / 2;


        canvas.drawBitmap(imageBitmap, xCenter, yCenter, null);

        if (drawLine) {
            drawThirds(canvas);
        }
    }

    void drawThirds(Canvas canvas) {
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

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            setImageBitmap(bitmap);

            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
            mBitmapMiddlePoint.x = (mViewWidth / 2) - (mBitmapWidth / 2);
            mBitmapMiddlePoint.y = (mViewHeight / 2) - (mBitmapHeight / 2);

            matrix.postTranslate(mBitmapMiddlePoint.x, mBitmapMiddlePoint.y);
            this.setImageMatrix(matrix);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        // make the image scalable as a matrix
        view.setScaleType(ImageView.ScaleType.MATRIX);


        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: //first finger down only
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                break;
            case MotionEvent.ACTION_UP: //first finger lifted
            case MotionEvent.ACTION_POINTER_UP: //second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //second finger down
                oldDist = spacing(event); // calculates the distance between two points where
                // user touched.
                Log.d(TAG, "oldDist=" + oldDist);
                // minimal distance between both the fingers
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event); // sets the mid-point of the straight line between two
                    // points where user touched.
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) { //movement of first finger
                    matrix.set(savedMatrix);
                    if (view.getLeft() >= 0) {

                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                        invalidate();
                    }
                } else if (mode == ZOOM) { //pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; //thinking I need to play around with this
                        // value to limit it**
                        if (scale < 4) {
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                        Log.d("","newDist " + newDist + " oldDist " + oldDist);
                    }

                }
                break;
        }

        Log.d("scale", "scale " + scale);
        // Perform the transformation
//        view.setImageMatrix(matrix);

        return true; // indicate event was handled
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

    public Bitmap getCroppedImage() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        float x = 0;
        float y = 0;
//        Bitmap cropped = Bitmap.createBitmap(drawable.getBitmap(),(int)x,(int)y,50,50);

        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        return bitmap;
    }

}