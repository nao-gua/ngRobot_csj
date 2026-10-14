package com.chzu.ngRobot_csj.chat.llm;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.chzu.ngRobot_csj.chat.ChatMessage;
import com.chzu.ngRobot_csj.utils.AppLogger;
import com.csjbot.coshandler.core.CsjRobot;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 走官方知识库 NLP（Speech.getResult）。
 * 没配知识库时通常返回兜底话术。
 */
public class SdkNlpLlmClient implements LlmClient {
    private static final String TAG = "SdkNlpLlm";
    private static final long TIMEOUT_MS = 8000;

    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private Runnable timeoutTask;

    @Override
    public void chat(String userText, List<ChatMessage> history, LlmCallback callback) {
        finished.set(false);
        if (TextUtils.isEmpty(userText) || callback == null) {
            if (callback != null) {
                callback.onError("empty text");
            }
            return;
        }
        AppLogger.i(TAG, "getResult: " + userText);
        try {
            CsjRobot.getInstance().getSpeech().getResult(userText, json -> {
                if (!finished.compareAndSet(false, true)) {
                    return;
                }
                main.removeCallbacks(timeoutTask);
                try {
                    String say = parseSay(json);
                    if (TextUtils.isEmpty(say)) {
                        callback.onError("empty nlp say");
                    } else {
                        callback.onSuccess(say, "sdk-nlp");
                    }
                } catch (Exception e) {
                    AppLogger.e(TAG, "parse nlp failed", e);
                    callback.onError(e.getMessage());
                }
            });
        } catch (Exception e) {
            AppLogger.e(TAG, "getResult call failed", e);
            callback.onError(e.getMessage());
            return;
        }

        timeoutTask = () -> {
            if (finished.compareAndSet(false, true)) {
                AppLogger.w(TAG, "nlp timeout");
                callback.onError("nlp timeout");
            }
        };
        main.postDelayed(timeoutTask, TIMEOUT_MS);
    }

    private String parseSay(String json) throws Exception {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        JSONObject root = new JSONObject(json);
        // 兼容多种返回结构
        if (root.has("result")) {
            JSONObject result = root.getJSONObject("result");
            if (result.has("data")) {
                JSONObject data = result.getJSONObject("data");
                if (data.has("say")) {
                    return data.getString("say");
                }
                if (data.has("answer")) {
                    return data.getString("answer");
                }
            }
            if (result.has("say")) {
                return result.getString("say");
            }
        }
        if (root.has("say")) {
            return root.getString("say");
        }
        if (root.has("answer")) {
            return root.getString("answer");
        }
        AppLogger.w(TAG, "unknown nlp json: " + json);
        return null;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public void cancel() {
        finished.set(true);
        if (timeoutTask != null) {
            main.removeCallbacks(timeoutTask);
        }
    }
}