package com.eagle.locker.util;

import android.content.Context;


import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.util
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class DateUtils {
    private static SimpleDateFormat sHourFormat24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat sHourFormat12 = new SimpleDateFormat("hh:mm", Locale.getDefault());

    public static String getHourString(Context context, long time) {
        String strTimeFormat = android.provider.Settings.System.getString(context.getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        if (("12").equals(strTimeFormat)) {
            try {
                return sHourFormat12.format(time);
            } catch (Exception e) {
            }
        }
        return sHourFormat24.format(time);
    }

}
