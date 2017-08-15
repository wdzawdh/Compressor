package com.cw.library.compressor.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Cw
 * @date 17/8/15
 */
public class Utils {

    /**
     * android 5.0之后获取当前运行的应用包名
     *
     * @param context context
     * @return pkgName
     */
    public static String getCurrentPkgName(Context context) {
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        int START_TASK_TO_FRONT = 2;
        String pkgName = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : processes) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (state != null && state == START_TASK_TO_FRONT) {
                    currentInfo = app;
                    break;
                }
            }
        }
        if (currentInfo != null) {
            pkgName = currentInfo.processName;
        }
        return pkgName;
    }

}
