package com.eagle.locker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

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
public class TouchToUnLockView extends FrameLayout {

    private RippleBackground mLockRipple;
    private View mUnLockContainer;
    private TextView mUnlockTips;

    private boolean mIsMoving;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;
    private float mDownMotionX;
    private float mDownMotionY;

    private int mTouchSlop = 10;

    private Paint circlePaint = new Paint(); //draw circle paint
    private int circleRadius = 0;

    private int moveDistance = 0;
    private float unlockDistance = 0;

    public TouchToUnLockView(@NonNull Context context) {
        super(context);
        init();
    }

    public TouchToUnLockView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchToUnLockView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.touch_to_unlock_view, this);
        mUnLockContainer = view.findViewById(R.id.fram_UnLockContainer);
        unlockDistance = (float) DimenUtils.getScreenWidth(getContext()) * 2 / 3;
        mLockRipple = (RippleBackground) view.findViewById(R.id.rb_LockRipple);
        mUnlockTips = (TextView) view.findViewById(R.id.txtv_UnlockTips);
        circleRadius = DimenUtils.dp2px(getContext(), 22) + 1;

        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3);
        circlePaint.setColor(Color.WHITE);
    }


    public void startAnim() {
        mLockRipple.startRippleAnimation();
    }

    public void stopAnim() {
        mLockRipple.stopRippleAnimation();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int circleRadius = getCircleRadius();
        canvas.drawCircle(mUnLockContainer.getX() + mUnLockContainer.getWidth() / 2, mUnLockContainer.getY() + mUnLockContainer.getHeight() / 2, circleRadius, circlePaint);
    }

    private boolean isInTouchUnlockArea(float motionX, float motionY) {
        if (motionX >= mUnLockContainer.getX()
                && motionX <= mUnLockContainer.getX() + mUnLockContainer.getWidth()
                && motionY >= mUnLockContainer.getY()
                && motionY <= mUnLockContainer.getY() + mUnLockContainer.getHeight()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mIsMoving = false;
                mDownMotionX = x;
                mDownMotionY = y;
                mTouchState = TOUCH_STATE_REST;
                if (isInTouchUnlockArea(mDownMotionX, mDownMotionY)) {
                    if (listener != null) {
                        listener.onTouchLockArea();
                    }
                    mUnlockTips.setVisibility(VISIBLE);
                    mUnlockTips.setText(R.string.slide_up_to_unlock);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsMoving || isInTouchUnlockArea(mDownMotionX, mDownMotionY)) {
                    final int deltaX = (int) (x - mDownMotionX);
                    final int deltaY = (int) (y - mDownMotionY);
                    boolean isMoved = Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop;
                    if (mTouchState == TOUCH_STATE_REST && isMoved) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                        mIsMoving = true;
                    }
                    if (mTouchState == TOUCH_STATE_SCROLLING) {
                        moveDistance = countDistance(mDownMotionX, mDownMotionY, x, y);
                        invalidate();
                        if (listener != null) {
                            listener.onSlidePercent(moveDistance / unlockDistance > 1 ? 1 : moveDistance / unlockDistance);
                        }
                        if (moveDistance > (unlockDistance * 2 / 3)) {
                            mUnlockTips.setText(R.string.release_to_unlock);
                        } else {
                            mUnlockTips.setText(R.string.slide_up_to_unlock);
                        }
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mUnlockTips.setVisibility(GONE);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    if (listener != null) {
                        if (moveDistance > (unlockDistance * 2 / 3)) {
                            listener.onSlideToUnlock();
                        } else {
                            listener.onSlideAbort();
                        }
                    }
                    mTouchState = TOUCH_STATE_REST;
                    mDownMotionX = 0;
                    mDownMotionY = 0;
                    moveDistance = 0;
                    invalidate();
                    return true;
                } else if (mTouchState == TOUCH_STATE_REST) {
                    if (listener != null) {
                        listener.onSlideAbort();
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    private int getCircleRadius() {
        return circleRadius + moveDistance;
    }


    private int countDistance(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt((x2 -= x1) * x2 + (y2 -= y1) * y2);
    }

    private OnTouchToUnlockListener listener;

    public void setOnTouchToUnlockListener(OnTouchToUnlockListener listener) {
        this.listener = listener;
    }

    public interface OnTouchToUnlockListener {
        void onTouchLockArea();

        void onSlidePercent(float percent);

        void onSlideToUnlock();

        void onSlideAbort();
    }
}
