package com.chzu.ngRobot_csj.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 简单文件+Logcat日志，避免主界面刷原始报文。 */
public final class AppLogger {
    private static final String TAG = "ngRobot";
    private static final Object LOCK = new Object();
    private static File logFile;
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private AppLogger() {}

    public static void init(Context context) {
        try {
            File dir = new File(context.getExternalFilesDir(null), "logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            logFile = new File(dir, "app.log");
            i("AppLogger", "log file = " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "init log failed", e);
        }
    }

    public static String getLogPath() {
        return logFile == null ? "" : logFile.getAbsolutePath();
    }

    public static void d(String tag, String msg) {
        Log.d(TAG, tag + " | " + msg);
        write("D", tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + " | " + msg);
        write("I", tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + " | " + msg);
        write("W", tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " | " + msg);
        write("E", tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        Log.e(TAG, tag + " | " + msg, t);
        write("E", tag, msg + "\n" + stack(t));
    }

    public static void installCrashHandler() {
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e("CRASH", "uncaught in " + t.getName(), e);
            if (previous != null) {
                previous.uncaughtException(t, e);
            }
        });
    }

    private static String stack(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void write(String level, String tag, String msg) {
        if (logFile == null) {
            return;
        }
        synchronized (LOCK) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(logFile, true);
                fw.write(SDF.format(new Date()));
                fw.write(" ");
                fw.write(level);
                fw.write("/");
                fw.write(tag);
                fw.write(": ");
                fw.write(msg);
                fw.write("\n");
            } catch (IOException ignored) {
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }
}