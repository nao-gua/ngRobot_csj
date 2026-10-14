package com.chzu.ngRobot_csj.utils;

import android.text.TextUtils;

import com.csjbot.coshandler.util.ConfInfoUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/** 语音鉴权相关只读诊断，不改 SDK。 */
public final class SpeechDiagnose {
    private SpeechDiagnose() {}

    public static String dump() {
        StringBuilder sb = new StringBuilder();
        String sn = safe(ConfInfoUtil.getSN());
        sb.append("SN=").append(TextUtils.isEmpty(sn) ? "(空)" : sn).append('\n');

        File keyFile = new File("/sdcard/.robot_info/aiuikey.txt");
        sb.append("aiuikey路径=").append(keyFile.getAbsolutePath()).append('\n');
        sb.append("aiuikey存在=").append(keyFile.exists()).append('\n');
        if (keyFile.exists()) {
            String content = readFile(keyFile);
            sb.append("aiuikey内容=").append(content).append('\n');
            try {
                JSONObject o = new JSONObject(content);
                String appId = o.optString("appId", "");
                String appKey = o.optString("appKey", "");
                sb.append("appId=").append(TextUtils.isEmpty(appId) ? "(空)" : appId).append('\n');
                sb.append("appKey=").append(TextUtils.isEmpty(appKey) ? "(空)" : mask(appKey)).append('\n');
            } catch (Exception e) {
                sb.append("aiuikey解析失败: ").append(e.getMessage()).append('\n');
            }
        } else {
            sb.append("结论: 无 aiuikey，讯飞 AIUI 会报 InvalidAppIdError(90003)，说话不会出 ASR。\n");
        }

        File snFile = new File("/sdcard/.robot_info/.sys.txt");
        sb.append("SN文件存在=").append(snFile.exists()).append('\n');
        if (snFile.exists()) {
            sb.append("SN文件内容=").append(readFile(snFile)).append('\n');
        }
        return sb.toString().trim();
    }

    private static String mask(String key) {
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String readFile(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "(读失败: " + e.getMessage() + ")";
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception ignored) {
            }
        }
    }
}