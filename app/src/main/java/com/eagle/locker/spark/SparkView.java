package com.eagle.locker.spark;

import java.util.Date;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SparkView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;

    private Canvas mCanvas;

    private boolean isRun;

    private SparkManager sparkManager;

    // 当前触摸点X，Y坐标
    private double X, Y;

    // 屏幕宽高
    public static int WIDTH, HEIGHT;

    private Random random = new Random();

    public SparkView(Context context) {
        super(context);
        init();
    }

    public SparkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SparkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void init() {
        // 关闭硬件加速
        /*setLayerType(LAYER_TYPE_SOFTWARE, null);*/

        //背景 透明
        this.setZOrderOnTop(true);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // 设置视图宽高（像素）
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metric);
        WIDTH = metric.widthPixels;
        HEIGHT = metric.heightPixels;

        // 火花管理器
        sparkManager = new SparkManager();

        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }


    @Override
    public void run() {

        // 火花数组
        int[][] sparks = new int[400][10];

        Date date = null;
        while (isRun) {
            date = new Date();
            try {
                mCanvas = mHolder.lockCanvas(null);
                if (mCanvas != null) {
                    synchronized (mHolder) {
                        // 清屏
                        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                        // 循环绘制所有火花
                        for (int[] n : sparks) {
                            n = sparkManager.drawSpark(mCanvas, (int) X, (int) Y, n);
                        }

                        // 控制帧数
                        Thread.sleep(Math.max(0, 10 - (new Date().getTime() - date.getTime())));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCanvas != null) {
                    mHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }
    }


    public void startSpark(float x, float y) {
        X = x;
        Y = y;
    }

    public void setActive(boolean isActive) {
        sparkManager.isActive = isActive;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getPointerCount()) {
            // 单点触摸
            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        sparkManager.isActive = true;
                        X = event.getX();
                        Y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        sparkManager.isActive = false;
                        break;
                    default:
                        break;
                }
                break;
        }

        return true;
    }

    // Surface的大小发生改变时调用
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*drawBackgound(holder);*/
    }

    // Surface创建时激发，一般在这里调用画面的线程
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        new Thread(this).start();
    }

    // 销毁时激发，一般在这里将画面的线程停止、释放。
    @Override
    public void surfaceDestroyed(SurfaceHolder argholder0) {
        isRun = false;
    }

    private void drawBackgound(SurfaceHolder holder) {
        mCanvas = mHolder.lockCanvas();
        mCanvas.drawColor(Color.parseColor("#00000000"));
        mHolder.unlockCanvasAndPost(mCanvas);
    }
}
