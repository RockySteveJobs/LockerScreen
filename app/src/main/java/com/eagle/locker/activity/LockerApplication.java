package com.eagle.locker.activity;


import android.app.Application;

import com.eagle.locker.service.LockerService;
import com.eagle.locker.service.TraceService;
import com.eagle.locker.task.ExecuteTaskManager;
import com.xdandroid.hellodaemon.DaemonEnv;


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
public class LockerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //start service
        LockerService.startService(this);
        ExecuteTaskManager.getInstance().init();
        DaemonEnv.initialize(this, TraceService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        TraceService.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceService.class);
    }
}
