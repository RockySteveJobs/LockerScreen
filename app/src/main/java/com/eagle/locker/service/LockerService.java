package com.eagle.locker.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eagle.locker.activity.LockerActivity;


/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.service
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class LockerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerLockerReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterLockerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static void startService(Context context) {
        try {
            Intent intent = new Intent(context, LockerService.class);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private LockerReceiver lockerReceiver;

    private void registerLockerReceiver() {
        if (lockerReceiver != null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        lockerReceiver = new LockerReceiver();
        registerReceiver(lockerReceiver, filter);
    }

    private void unregisterLockerReceiver() {
        if (lockerReceiver == null) {
            return;
        }
        unregisterReceiver(lockerReceiver);
        lockerReceiver = null;
    }

    private class LockerReceiver extends BroadcastReceiver {

        public LockerReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    //todo
                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    LockerActivity.startActivity(context);
                }
            }
        }
    }
}
