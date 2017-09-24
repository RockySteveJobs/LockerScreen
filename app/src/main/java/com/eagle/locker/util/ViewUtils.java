package com.eagle.locker.util;

import android.app.Activity;
import android.view.View;

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
public class ViewUtils {

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View parent, int id) {
        if (parent == null) {
            return null;
        }
        return (T) parent.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(Activity activity, int id) {
        if (activity == null) {
            return null;
        }
        return (T) activity.findViewById(id);
    }
}
