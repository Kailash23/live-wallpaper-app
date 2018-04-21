package com.juggrnaut.livewallpaper.util;

import android.util.Log;

import com.juggrnaut.livewallpaper.BuildConfig;

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
