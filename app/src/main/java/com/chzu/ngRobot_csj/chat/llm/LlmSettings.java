package com.chzu.ngRobot_csj.chat.llm;

import android.content.Context;
import android.text.TextUtils;

import com.chzu.ngRobot_csj.utils.AppLogger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 大模型外部配置。
 * 优先读：/storage/emulated/0/com.chzu/ngRobot_csj/llm_settings.json（改完重启 App 即可，不用重编译）
 * 没有则用 assets/llm_settings.json，并自动拷一份到上述目录方便改。
 *
 * 字段：
 * provider   - zhipu / openai_compat / xunfei_maas / mock
 * baseUrl    - 接口根路径（一般到 v4，不含 chat/completions）
 * apiKey     - 密钥
 * model      - 模型名
 * systemPrompt - 人设
 */
public class LlmSettings {
    private static final String TAG = "LlmSettings";
    public static final String FILE_NAME = "llm_settings.json";
    /** 机器人上本项目配置目录 */
    public static final String EXTERNAL_DIR = "/storage/emulated/0/com.chzu/ngRobot_csj";

    public final String provider;
    public final String baseUrl;
    public final String apiKey;
    public final String model;
    public final String systemPrompt;
    public final String loadedFrom;

    public LlmSettings(String provider, String baseUrl, String apiKey, String model,
                       String systemPrompt, String loadedFrom) {
        this.provider = provider == null ? "" : provider.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "" : model.trim();
        this.systemPrompt = TextUtils.isEmpty(systemPrompt)
                ? "你是机器人前台助手，回答简洁口语化，适合语音播报。"
                : systemPrompt.trim();
        this.loadedFrom = loadedFrom;
    }

    public static File externalFile() {
        return new File(EXTERNAL_DIR, FILE_NAME);
    }

    public static LlmSettings load(Context context) {
        File ext = externalFile();
        try {
            if (!ext.exists()) {
                // 首次把 assets 拷出去，之后只改外部文件即可
                copyAssetToExternal(context, ext);
            }
            if (ext.exists() && ext.length() > 0) {
                String json = readFile(ext);
                LlmSettings s = parse(json, ext.getAbsolutePath());
                AppLogger.i(TAG, "loaded " + s.summary());
                return s;
            }
        } catch (Exception e) {
            AppLogger.e(TAG, "read external failed", e);
        }

        try {
            String json = readAsset(context, FILE_NAME);
            LlmSettings s = parse(json, "assets/" + FILE_NAME);
            AppLogger.i(TAG, "loaded " + s.summary());
            return s;
        } catch (Exception e) {
            AppLogger.e(TAG, "read assets failed", e);
        }

        AppLogger.w(TAG, "fallback empty settings");
        return new LlmSettings("mock", "", "", "", "", "fallback");
    }

    public String summary() {
        return "from=" + loadedFrom
                + " provider=" + provider
                + " model=" + model
                + " key=" + (TextUtils.isEmpty(apiKey) ? "(empty)" : "ok");
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(apiKey);
    }

    private static LlmSettings parse(String json, String from) throws Exception {
        JSONObject o = new JSONObject(json);
        return new LlmSettings(
                o.optString("provider", "openai_compat"),
                o.optString("baseUrl", ""),
                o.optString("apiKey", ""),
                o.optString("model", ""),
                o.optString("systemPrompt", ""),
                from
        );
    }

    private static void copyAssetToExternal(Context context, File dest) {
        InputStream in = null;
        FileOutputStream out = null;
        try {
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                // noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
            in = context.getAssets().open(FILE_NAME);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            out.flush();
            AppLogger.i(TAG, "copied assets to " + dest.getAbsolutePath());
        } catch (Exception e) {
            AppLogger.w(TAG, "copy asset failed: " + e.getMessage());
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
        }
    }

    private static String readAsset(Context context, String name) throws Exception {
        InputStream in = context.getAssets().open(name);
        return readStream(in);
    }

    private static String readFile(File file) throws Exception {
        return readStream(new FileInputStream(file));
    }

    private static String readStream(InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}