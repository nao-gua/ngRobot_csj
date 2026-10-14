package com.chzu.ngRobot_csj.chat.llm;

import android.text.TextUtils;

import com.chzu.ngRobot_csj.chat.ChatMessage;
import com.chzu.ngRobot_csj.utils.AppLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenAI 兼容 chat/completions（智谱 v4 同形态）。
 */
public class HttpLlmClient implements LlmClient {
    private static final String TAG = "HttpLlmClient";

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final String systemPrompt;
    private final String sourceName;
    private final int maxTokens;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> running;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    public HttpLlmClient(String baseUrl, String apiKey, String model, String systemPrompt) {
        this(baseUrl, apiKey, model, systemPrompt, "http-llm", 1024);
    }

    public HttpLlmClient(String baseUrl, String apiKey, String model, String systemPrompt,
                         String sourceName, int maxTokens) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = TextUtils.isEmpty(model) ? "glm-4-flash" : model;
        this.systemPrompt = TextUtils.isEmpty(systemPrompt)
                ? "你是机器人前台助手，回答简洁口语化。"
                : systemPrompt;
        this.sourceName = TextUtils.isEmpty(sourceName) ? "http-llm" : sourceName;
        this.maxTokens = maxTokens > 0 ? maxTokens : 1024;
    }

    /** 智谱一键构造 */
    public static HttpLlmClient zhipu(String apiKey, String model, String systemPrompt) {
        return new HttpLlmClient(
                LlmConfig.ZHIPU_BASE_URL,
                apiKey,
                model,
                systemPrompt,
                "zhipu",
                1024
        );
    }

    @Override
    public boolean isConfigured() {
        return !TextUtils.isEmpty(baseUrl) && !TextUtils.isEmpty(apiKey);
    }

    @Override
    public void chat(String userText, List<ChatMessage> history, LlmCallback callback) {
        canceled.set(false);
        if (!isConfigured()) {
            if (callback != null) {
                callback.onError("llm not configured");
            }
            return;
        }
        running = executor.submit(() -> doRequest(userText, history, callback));
    }

    private void doRequest(String userText, List<ChatMessage> history, LlmCallback callback) {
        HttpURLConnection conn = null;
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            if (history != null) {
                int start = Math.max(0, history.size() - 10);
                for (int i = start; i < history.size(); i++) {
                    ChatMessage m = history.get(i);
                    if (m.role == ChatMessage.ROLE_USER) {
                        messages.put(new JSONObject().put("role", "user").put("content", m.text));
                    } else if (m.role == ChatMessage.ROLE_BOT) {
                        messages.put(new JSONObject().put("role", "assistant").put("content", m.text));
                    }
                }
            }
            messages.put(new JSONObject().put("role", "user").put("content", userText));

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", maxTokens);
            body.put("stream", false);

            String endpoint = baseUrl.endsWith("/")
                    ? baseUrl + "chat/completions"
                    : baseUrl + "/chat/completions";
            AppLogger.i(TAG, "POST " + endpoint + " model=" + model);

            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream os = conn.getOutputStream();
            os.write(bytes);
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            if (canceled.get()) {
                return;
            }
            if (code < 200 || code >= 300) {
                AppLogger.e(TAG, "http " + code + " body=" + sb);
                notifyError(callback, "http " + code + ": " + trimErr(sb.toString()));
                return;
            }

            JSONObject resp = new JSONObject(sb.toString());
            String reply = resp.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();
            if (TextUtils.isEmpty(reply)) {
                notifyError(callback, "empty llm reply");
                return;
            }
            AppLogger.i(TAG, "ok source=" + sourceName + " len=" + reply.length());
            notifyOk(callback, reply);
        } catch (Exception e) {
            AppLogger.e(TAG, "request failed", e);
            if (!canceled.get()) {
                notifyError(callback, e.getMessage());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String trimErr(String s) {
        if (s == null) return "";
        s = s.replace('\n', ' ').trim();
        return s.length() > 180 ? s.substring(0, 180) + "..." : s;
    }

    private void notifyOk(LlmCallback callback, String reply) {
        if (callback == null) return;
        callback.onSuccess(reply, sourceName);
    }

    private void notifyError(LlmCallback callback, String err) {
        if (callback == null) return;
        callback.onError(err == null ? "unknown" : err);
    }

    @Override
    public void cancel() {
        canceled.set(true);
        Future<?> f = running;
        if (f != null) {
            f.cancel(true);
        }
    }
}