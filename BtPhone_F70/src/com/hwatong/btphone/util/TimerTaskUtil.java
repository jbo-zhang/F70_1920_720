package com.hwatong.btphone.util;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangjinbo on 17-7-24.
 */

public class TimerTaskUtil {
    private static HashMap<String, Timer> mTimerMap = new HashMap<String, Timer>();

    public static synchronized void startTimer(String name, int when, int period, TimerTask task) {
        if(mTimerMap.get(name) != null) {
            mTimerMap.get(name).cancel();
            mTimerMap.remove(name);
        }
        Timer timer = new Timer();
        timer.schedule(task, when, period);
        mTimerMap.put(name, timer);
    }


    public static synchronized void cancelTimer(String name) {
        if(mTimerMap.get(name) != null) {
            mTimerMap.get(name).cancel();
            mTimerMap.remove(name);
        }
    }
}
