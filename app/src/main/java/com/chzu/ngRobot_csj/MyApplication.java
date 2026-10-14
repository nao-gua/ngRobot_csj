package com.chzu.ngRobot_csj;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.chzu.ngRobot_csj.utils.AppLogger;
import com.chzu.ngRobot_csj.utils.SharedPreferencesSDCard;
import com.chzu.ngRobot_csj.utils.Utils;
import com.csjbot.coshandler.aiui.aiui_soft.send_msg.AIUIConstants;
import com.csjbot.coshandler.core.CsjRobot;
import com.csjbot.coshandler.listener.OnAuthenticationListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 对齐可运行官方 demo(sdkdemo240326) 的初始化关键点：
 * - RobotType.TIMO（可运行 demo 用 TIMO，不是 SNOW）
 * - enableAsr(true)
 * - 启动前确保 aiuikey 文件有效（高 targetSdk 读 /sdcard 失败时，AIUI cfg 里 appid 为空会 90003）
 */
public class MyApplication extends MultiDexApplication {
    // 来自可运行 demo assets/cfg 的默认 AIUI 登录信息（仅作文件兜底，不改 SDK）
    private static final String FALLBACK_AIUI_APP_ID = "54a8a8d6";
    private static final String FALLBACK_AIUI_APP_KEY = "e1338b41d53014f92fb81b9d87ab4259";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.init(this);
        AppLogger.installCrashHandler();
        AIUIConstants.isIn18 = Utils.isI18n();

        ensureAiuiKeyFile();

        //TODO 上传前确保擦掉 APIkey 和 UserKey
        CsjRobot.authentication(this, "cd1393d0-75bd-40ff-aedb-95094777691c", "417EC70A1A80534CF97E4696402E0379", new OnAuthenticationListener() {
            @Override
            public void success() {
                Log.d("TAG", "application授权成功！Authorization succeeded!");
                AppLogger.i("MyApplication", "CSJ authentication success");
            }

            @Override
            public void error() {
                Log.d("TAG", "application授权失败！privilege grant failed!");
                AppLogger.e("MyApplication", "CSJ authentication failed");
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 与可运行 demo 一致
        CsjRobot.enableAsr(true);
        CsjRobot.enableFace(true);
        CsjRobot.enableSlam(true);

        // 可运行 demo: RobotType.TIMO
        CsjRobot.setRobotType(CsjRobot.RobotType.TIMO);
        CsjRobot.getInstance().setPersonCheckType(true, true, true);

        CsjRobot.getInstance().init(this);
        SharedPreferencesSDCard.getInstance().init(this);
        AppLogger.i("MyApplication", "init done, robotType=TIMO");
    }

    /**
     * 若 aiuikey 不存在/无效，写入可运行 demo 同款兜底 key。
     * 已有有效内容则不覆盖。
     */
    private void ensureAiuiKeyFile() {
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), ".robot_info");
            if (!dir.exists()) {
                // noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            File keyFile = new File(dir, "aiuikey.txt");
            boolean needWrite = true;
            if (keyFile.exists() && keyFile.length() > 0) {
                try {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(new FileInputStream(keyFile), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    JSONObject o = new JSONObject(sb.toString());
                    String appId = o.optString("appId", "");
                    String appKey = o.optString("appKey", "");
                    if (appId.length() > 0 && appKey.length() > 0) {
                        needWrite = false;
                        AppLogger.i("MyApplication", "aiuikey ok, appId=" + appId);
                    }
                } catch (Exception parseErr) {
                    AppLogger.w("MyApplication", "aiuikey parse fail, will rewrite: " + parseErr.getMessage());
                }
            }
            if (needWrite) {
                JSONObject o = new JSONObject();
                o.put("appId", FALLBACK_AIUI_APP_ID);
                o.put("appKey", FALLBACK_AIUI_APP_KEY);
                FileOutputStream fos = new FileOutputStream(keyFile, false);
                fos.write(o.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.close();
                AppLogger.i("MyApplication", "wrote fallback aiuikey to " + keyFile.getAbsolutePath());
            }
        } catch (Exception e) {
            AppLogger.e("MyApplication", "ensureAiuiKeyFile failed (storage permission?).", e);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}