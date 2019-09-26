package com.darkraha.backend_android.components.images.gif

import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.SystemClock
import com.darkraha.backend.Backend
import com.darkraha.backend_android.components.endecode.GifDecoder

/**
 * Created by Verma Rahul
 *
 *
 * Tested on width=match_parent , height = wrap_content
 *
 * Use properly stop and start methoths, as example after assign to ImageView in RecyclingView
 * invoke stop(); start();
 */
class GifDrawable(gifDecoder: GifDecoder) : Drawable(), Animatable, Runnable {
    protected var curBitmap: Bitmap? = null


    //protected int mCounter = 0;
    var alwaysAnimate: Boolean = false
    protected var mRunning: Boolean = false
    protected var mLastTime: Long = 0
    val mPaint = Paint()
    protected var mMatrix = Matrix()
    var gifDecoder: GifDecoder
        protected set
    var defaultDelay = 200
    var mCountFrames: Int = 0
        protected set

    /**
     * How long show frame image in animated mode.
     */
    val frameDelay: Int
        get() = if (gifDecoder.nextDelay == 0) defaultDelay else gifDecoder.nextDelay


    val size: Int
        get() = gifDecoder.size

    init {
        this.gifDecoder = gifDecoder
        mPaint.isAntiAlias = true
        mCountFrames = gifDecoder.frameCount
        curBitmap = gifDecoder.nextFrame
    }

    protected fun setFirstFrame() {
        mLastTime = SystemClock.uptimeMillis()
        scheduleSelf(this, 0)
        invalidateSelf()
    }


    //==============================================================


    override fun getIntrinsicWidth(): Int {
        return gifDecoder.width
    }

    override fun getIntrinsicHeight(): Int {
        return gifDecoder.height
    }

    override fun draw(canvas: Canvas) {

        if (curBitmap != null) {
            canvas.drawBitmap(curBitmap!!, mMatrix, mPaint)
        } else {
            setFirstFrame()
        }
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return 0 //PixelFormat.OPAQUE;
    }


    //==============================================================
    override fun start() {

        if (isRunning || mCountFrames == 0)
            return
        mRunning = true
        mLastTime = SystemClock.uptimeMillis()
        scheduleSelf(this, 0)
        invalidateSelf()
    }

    override fun stop() {

        if (!alwaysAnimate) {
            mRunning = false
            unscheduleSelf(this)
            invalidateSelf()
        }
    }

    override fun isRunning(): Boolean {
        return mRunning
    }


    //==============================================================
    override fun run() {
//        if (!isRunning) {
//            return
//        }
        mLastTime = SystemClock.uptimeMillis()

        Backend.sharedInstance.exeAsync({
            curBitmap = gifDecoder.nextFrame
            gifDecoder.advance()
        }, {
            sheduleNextFrame()
        })
    }

    fun sheduleNextFrame() {
        if (isRunning) {
            scheduleSelf(this, mLastTime + frameDelay)
            println("GifDrawable on run")
            invalidateSelf()
        }
    }

    companion object {
        private val TAG = "GifDrawable"
    }
}
