package com.csjbot.sdkDemo.utils;

import android.text.TextUtils;

import com.csjbot.sdkDemo.BuildConfig;

public class Utils {
    public static boolean isI18n() {
        return TextUtils.equals(BuildConfig.robotType, BuildConfig.ROBOT_TYPE_DEF_TIMO_i18n);
    }
}
