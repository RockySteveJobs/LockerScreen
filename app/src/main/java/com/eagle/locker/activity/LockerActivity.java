package com.eagle.locker.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eagle.locker.R;
import com.eagle.locker.burst.BurstParticleSystem;
import com.eagle.locker.burst.MyTextureAtlasFactory;
import com.eagle.locker.spark.SparkView;
import com.eagle.locker.task.ExecuteTask;
import com.eagle.locker.task.ExecuteTaskManager;
import com.eagle.locker.util.DateUtils;
import com.eagle.locker.util.DimenUtils;
import com.eagle.locker.util.PowerUtil;
import com.eagle.locker.util.ViewUtils;
import com.eagle.locker.widget.TouchPullDownView;
import com.eagle.locker.widget.TouchToUnLockView;
import com.github.shchurov.particleview.ParticleView;
import com.xdandroid.hellodaemon.IntentWrapper;
import com.zyyoona7.lib.EasyPopup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.activity
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class LockerActivity extends AppCompatActivity {


    private TouchPullDownView mPullDownView;
    private TouchToUnLockView mUnlockView;

    private View mChargeContainer, mSetting;

    private TextView mLockTime, mLockDate, mChargePercent;
    private ImageView mBatteryIcon;

    private View mContainerView;

    private Calendar calendar = GregorianCalendar.getInstance();
    private SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    private ParticleView pv_ParticleView;
    private BurstParticleSystem particleSystem;
    private Random random = new Random();

    private SparkView sp_Spark;
    private SparkTask sparkTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLockerWindow(getWindow());
        registerLockerReceiver();
        setContentView(R.layout.activity_locker);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLockerReceiver();
    }

    private EasyPopup mCirclePop;

    private void initView() {
        mCirclePop = new EasyPopup(this)
                .setContentView(R.layout.locker_setting_item)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setDimValue(0.4f)
                .setDimColor(getResources().getColor(R.color.common_half_alpha))
                .setDimView(mUnlockView)
                .createPopup();
        mCirclePop.getView(R.id.txtv_LockerSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mSetting = ViewUtils.get(this, R.id.settings);
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCirclePop.showAsDropDown(mSetting);
            }
        });

        mChargeContainer = ViewUtils.get(this, R.id.linel_ChargeContainer);
        mContainerView = ViewUtils.get(this, R.id.relel_ContentContainer);

        mLockTime = ViewUtils.get(this, R.id.txtv_LockTime);
        mLockDate = ViewUtils.get(this, R.id.txtv_LockDate);
        mBatteryIcon = ViewUtils.get(this, R.id.imgv_BatteryIcon);
        mChargePercent = ViewUtils.get(this, R.id.txtv_ChargePercent);


        mPullDownView = ViewUtils.get(this, R.id.tpdv_PullDownView);
        mPullDownView.setOnTouchPullDownListener(new TouchPullDownView.OnTouchPullDownListener() {
            @Override
            public void onTouchGiftBoxArea() {

            }

            @Override
            public void onPullPercent(float percent) {

            }

            @Override
            public void onPullCanceled() {
                Toast.makeText(getApplication(), R.string.pull_canceled, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onGiftBoxPulled() {
                particleSystem.addBurst(random.nextInt(DimenUtils.getScreenWidth(getBaseContext())), random.nextInt(DimenUtils.getScreenHeight(getBaseContext())));
            }

            @Override
            public void onGiftBoxClick() {
                particleSystem.addBurst(random.nextInt(DimenUtils.getScreenWidth(getBaseContext())), random.nextInt(DimenUtils.getScreenHeight(getBaseContext())));
            }
        });


        mUnlockView = ViewUtils.get(this, R.id.tulv_UnlockView);
        mUnlockView.setOnTouchToUnlockListener(new TouchToUnLockView.OnTouchToUnlockListener() {
            @Override
            public void onTouchLockArea() {
                if (mContainerView != null) {
                    mContainerView.setBackgroundColor(Color.parseColor("#66000000"));
                }
            }

            @Override
            public void onSlidePercent(float percent) {
                if (mContainerView != null) {
                    mContainerView.setAlpha(1 - percent < 0.05f ? 0.05f : 1 - percent);
                    mContainerView.setScaleX(1 + (percent > 1f ? 1f : percent) * 0.08f);
                    mContainerView.setScaleY(1 + (percent > 1f ? 1f : percent) * 0.08f);
                }
            }

            @Override
            public void onSlideToUnlock() {
                finish();
            }

            @Override
            public void onSlideAbort() {
                if (mContainerView != null) {
                    mContainerView.setAlpha(1.0f);
                    mContainerView.setBackgroundColor(0);
                    mContainerView.setScaleX(1f);
                    mContainerView.setScaleY(1f);
                }
            }
        });

        pv_ParticleView = ViewUtils.get(this, R.id.pv_ParticleView);
        particleSystem = new BurstParticleSystem();
        pv_ParticleView.setTextureAtlasFactory(new MyTextureAtlasFactory(getResources()));
        pv_ParticleView.setParticleSystem(particleSystem);

        if (PowerUtil.isCharging(this)) {
            mChargeContainer.setVisibility(View.VISIBLE);
        } else {
            mChargeContainer.setVisibility(View.GONE);
        }
        updateTimeUI();
        updateBatteryUI();

        sp_Spark = ViewUtils.get(this, R.id.sp_Spark);
        sparkTask = new SparkTask();
    }

    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }


    protected UIChangingReceiver mUIChangingReceiver;

    public void registerLockerReceiver() {
        if (mUIChangingReceiver != null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        mUIChangingReceiver = new UIChangingReceiver();
        registerReceiver(mUIChangingReceiver, filter);
    }

    public void unregisterLockerReceiver() {
        if (mUIChangingReceiver == null) {
            return;
        }
        unregisterReceiver(mUIChangingReceiver);
        mUIChangingReceiver = null;
    }


    private class UIChangingReceiver extends BroadcastReceiver {

        public UIChangingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                onActionReceived(action);
            }
        }
    }

    private void updateTimeUI() {
        mLockTime.setText(DateUtils.getHourString(this, System.currentTimeMillis()));
        mLockDate.setText(weekFormat.format(calendar.getTime()) + "    " + monthFormat.format(calendar.getTime()));
    }


    private void updateBatteryUI() {
        int level = PowerUtil.getLevel(this);
        mChargePercent.setText(level + "%");

        if (level <= 30) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_30);
        } else if (level <= 60) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_60);
        } else if (level < 100) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_90);
        } else if (level == 100) {
            mBatteryIcon.setImageResource(R.drawable.ic_lock_charge_four);
        }

        if (level < 100 && mBatteryIcon.getDrawable() instanceof Animatable) {
            Animatable animatable = (Animatable) mBatteryIcon.getDrawable();
            if (PowerUtil.isCharging(this)) {
                animatable.start();
            } else {
                animatable.stop();
            }
        }
    }


    protected void onActionReceived(String action) {
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                updateBatteryUI();
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                updateTimeUI();
            } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                mChargeContainer.setVisibility(View.VISIBLE);
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                mChargeContainer.setVisibility(View.GONE);
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mUnlockView.startAnim();
        pv_ParticleView.startRendering();
        ExecuteTaskManager.getInstance().newExecuteTask(sparkTask);
    }

    private class SparkTask extends ExecuteTask {
        @Override
        public ExecuteTask doTask() {

            for (int i = 0; i < SparkView.WIDTH; i++) {
                sp_Spark.setActive(true);
                sp_Spark.startSpark(i, random.nextInt(SparkView.HEIGHT));
                try {
                    Thread.sleep(2 + random.nextInt(8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sp_Spark.setActive(false);
            }

            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mUnlockView.stopAnim();
        pv_ParticleView.stopRendering();
        ExecuteTaskManager.getInstance().removeExecuteTask(sparkTask);
    }

    public static void startActivity(Context context) {
        Intent screenIntent = getIntent(context);
        context.startActivity(screenIntent);
    }


    @NonNull
    private static Intent getIntent(Context context) {
        Intent screenIntent = new Intent();
        screenIntent.setClass(context, LockerActivity.class);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        return screenIntent;
    }


    private void setLockerWindow(Window window) {
        WindowManager.LayoutParams lp = window.getAttributes();
        if (Build.VERSION.SDK_INT > 18) {
            lp.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        window.setAttributes(lp);
        window.getDecorView().setSystemUiVisibility(0x0);

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
}
