package com.eagle.locker.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eagle.locker.service.LockerService;


/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.receiver
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //start service
        LockerService.startService(context);
    }
}
