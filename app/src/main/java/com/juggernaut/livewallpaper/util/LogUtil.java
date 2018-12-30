package com.juggernaut.livewallpaper.util;

import android.util.Log;

import com.juggernaut.livewallpaper.BuildConfig;

public class LogUtil {

    public static void i(String text) {
        i(LogUtil.class.getSimpleName(), text);
    }

    public static void i(String tag, String text) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, text);
        }
    }

}
