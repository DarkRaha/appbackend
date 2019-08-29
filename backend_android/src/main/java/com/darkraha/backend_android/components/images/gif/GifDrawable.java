package com.darkraha.backend_android.components.images.gif;

import android.graphics.*;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import com.darkraha.backend_android.components.endecode.GifDecoder;

/**
 * Created by Verma Rahul
 * <p/>
 * Tested on width=match_parent , height = wrap_content
 * </p>
 * Use properly stop and start methoths, as example after assign to ImageView in RecyclingView
 * invoke stop(); start();
 */
public class GifDrawable extends Drawable implements Animatable, Runnable {
    private static final String TAG = "GifDrawable";
    protected Bitmap curBitmap;


    //protected int mCounter = 0;
    protected boolean alwaysAnimate;
    protected boolean mRunning;
    protected long mLastTime;
    protected Paint mPaint = new Paint();
    protected Matrix mMatrix = new Matrix();
    protected GifDecoder gifDecoder;
    protected int defaultDelay = 200;
    protected int mCountFrames;

    public GifDrawable(GifDecoder gifDecoder) {
        this.gifDecoder = gifDecoder;
        mPaint.setAntiAlias(true);
        mCountFrames = gifDecoder.getFrameCount();
        curBitmap = gifDecoder.getNextFrame();
    }

    public GifDecoder getGifDecoder() {
        return gifDecoder;
    }

    protected void setFirstFrame() {
        mLastTime = SystemClock.uptimeMillis();
        scheduleSelf(this, 0);
        invalidateSelf();
    }

    /**
     * Set default delay value when gif delay for frame == 0.
     *
     * @param value in ms
     */
    public void setDefaultDelay(int value) {
        defaultDelay = value;
    }

    public int getSize() {
        return gifDecoder.getSize();
    }

    //==============================================================


    @Override
    public int getIntrinsicWidth() {
        return gifDecoder.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return gifDecoder.getHeight();
    }

//    @Override
//    protected void onBoundsChange(Rect bounds) {
//
//        mMatrix.setRectToRect(new RectF(0, 0, curBitmap.getWidth(), curBitmap.getHeight()),
//                new RectF(bounds),
//                Matrix.ScaleToFit.CENTER);
//    }

    @Override
    public void draw(Canvas canvas) {

        if (curBitmap != null) {
            canvas.drawBitmap(curBitmap, mMatrix, mPaint);
        } else {
            setFirstFrame();
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return 0; //PixelFormat.OPAQUE;
    }


    //==============================================================
    @Override
    public void start() {

        if (isRunning() || mCountFrames == 0)
            return;
        mRunning = true;
        mLastTime = SystemClock.uptimeMillis();
        scheduleSelf(this, 0);
        invalidateSelf();
    }

    @Override
    public void stop() {

        if (!alwaysAnimate) {
            mRunning = false;
            unscheduleSelf(this);
            invalidateSelf();
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }


    public void setAlwaysAnimate(boolean v) {
        alwaysAnimate = v;
    }

    //==============================================================
    @Override
    public void run() {

        mLastTime = SystemClock.uptimeMillis();

        curBitmap = gifDecoder.getNextFrame();
        gifDecoder.advance();

        int delay = gifDecoder.getNextDelay();
        if (delay == 0) {
            delay = defaultDelay;
        }

        // check is running necessary for first frame
        if (isRunning()) {
            scheduleSelf(this, mLastTime + delay);
            invalidateSelf();
        }
    }
}
