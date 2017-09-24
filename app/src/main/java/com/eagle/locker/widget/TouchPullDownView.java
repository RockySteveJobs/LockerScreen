package com.eagle.locker.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eagle.locker.R;
import com.eagle.locker.util.DimenUtils;

/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.widget
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class TouchPullDownView extends FrameLayout {

    private ImageView mLockLine;

    private boolean mIsMoving;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;
    private float mDownMotionX;
    private float mDownMotionY;

    private int mTouchSlop = 10;

    private float moveDistance = 0;
    private float totalDistance = 0;

    public TouchPullDownView(@NonNull Context context) {
        super(context);
        init();
    }

    public TouchPullDownView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPullDownView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.touch_pull_down_view, this);
        mLockLine = (ImageView) view.findViewById(R.id.imgv_LockLine);
        totalDistance = DimenUtils.dp2px(getContext(), 240);
    }


    private boolean isInTouchArea(float motionX, float motionY) {
        if (motionX >= mLockLine.getX()
                && motionX <= mLockLine.getX() + mLockLine.getWidth()
                && motionY >= mLockLine.getY()
                && motionY <= mLockLine.getY() + mLockLine.getHeight()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isAniming) {
            return true;
        }

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();


        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mIsMoving = false;
                mDownMotionX = x;
                mDownMotionY = y;
                mTouchState = TOUCH_STATE_REST;
                if (isInTouchArea(mDownMotionX, mDownMotionY)) {
                    if (listener != null) {
                        listener.onTouchGiftBoxArea();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsMoving || isInTouchArea(mDownMotionX, mDownMotionY)) {
                    final int deltaX = (int) (x - mDownMotionX);
                    final int deltaY = (int) (y - mDownMotionY);
                    boolean isMoved = Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop;

                    if (mTouchState == TOUCH_STATE_REST && isMoved) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                        mIsMoving = true;

                    }

                    if (mTouchState == TOUCH_STATE_SCROLLING) {
                        moveDistance = deltaY;

                        if (moveDistance < totalDistance) {
                            mLockLine.setTranslationY(moveDistance);
                        }

                        if (listener != null) {
                            listener.onPullPercent(moveDistance / totalDistance);
                        }
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {

                    startKickBackAnim(moveDistance < totalDistance ? moveDistance : totalDistance);
                    mTouchState = TOUCH_STATE_REST;
                    mDownMotionX = 0;
                    mDownMotionY = 0;
                    moveDistance = 0;
                    return true;
                } else if (mTouchState == TOUCH_STATE_REST) {
                    startPullDownAnim();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean isAniming;

    private void startPullDownAnim() {
        isAniming = true;
        ObjectAnimator pullDownAnim = ObjectAnimator.ofFloat(mLockLine, View.TRANSLATION_Y, 0, DimenUtils.dp2px(getContext(), 80), -80, 0, 40, 0, -20, 0);
        pullDownAnim.setRepeatCount(0);
        pullDownAnim.setDuration(500);
        pullDownAnim.setInterpolator(new DecelerateInterpolator());
        pullDownAnim.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAniming = false;
                if (listener != null) {
                    listener.onGiftBoxClick();
                }
            }
        });
        pullDownAnim.start();
    }

    private void startKickBackAnim(final float distance) {
        isAniming = true;
        ObjectAnimator kickBackAnim = ObjectAnimator.ofFloat(mLockLine, View.TRANSLATION_Y, distance, -distance / 2, 0, distance / 4, 0, -distance / 8, 0);
        kickBackAnim.setRepeatCount(0);
        kickBackAnim.setDuration(500);
        kickBackAnim.setInterpolator(new DecelerateInterpolator());
        kickBackAnim.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAniming = false;
                if (listener != null) {
                    if (distance > DimenUtils.dp2px(getContext(), 20)) {
                        listener.onGiftBoxPulled();
                    } else {
                        listener.onPullCanceled();
                    }
                }
            }
        });
        kickBackAnim.start();
    }


    private OnTouchPullDownListener listener;

    public void setOnTouchPullDownListener(OnTouchPullDownListener listener) {
        this.listener = listener;
    }

    public interface OnTouchPullDownListener {
        void onTouchGiftBoxArea();

        void onPullPercent(float percent);

        void onGiftBoxPulled();

        void onPullCanceled();

        void onGiftBoxClick();
    }
}
