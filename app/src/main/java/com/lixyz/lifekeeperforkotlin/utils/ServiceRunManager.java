package com.lixyz.lifekeeperforkotlin.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.lixyz.lifekeeperforkotlin.service.GuardService;

import java.util.List;

public class ServiceRunManager {
    public static ServiceRunManager serviceRunManager = null;

    private ServiceRunManager() {

    }

    public static ServiceRunManager getInstance() {
        if (serviceRunManager == null) {
            serviceRunManager = new ServiceRunManager();
        }
        return serviceRunManager;
    }

    public boolean isServiceRunning(Context context) {
        boolean isRunning = false;

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);


        if (serviceList == null || serviceList.size() == 0) {
            return false;
        }


        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(GuardService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
