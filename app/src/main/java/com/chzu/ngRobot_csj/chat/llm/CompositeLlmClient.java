package com.chzu.ngRobot_csj.chat.llm;

import com.chzu.ngRobot_csj.chat.ChatMessage;
import com.chzu.ngRobot_csj.utils.AppLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 优先级：配置的主模型 -> 官方 NLP -> Mock。
 */
public class CompositeLlmClient implements LlmClient {
    private static final String TAG = "CompositeLlm";

    private final LlmClient primary;
    private final SdkNlpLlmClient sdkNlpLlmClient;
    private final MockLlmClient mockLlmClient = new MockLlmClient();
    private final AtomicBoolean done = new AtomicBoolean(false);

    public CompositeLlmClient(LlmClient primary, SdkNlpLlmClient sdkNlpLlmClient) {
        this.primary = primary;
        this.sdkNlpLlmClient = sdkNlpLlmClient;
    }

    @Override
    public boolean isConfigured() {
        return primary != null && primary.isConfigured();
    }

    @Override
    public void chat(String userText, List<ChatMessage> history, LlmCallback callback) {
        done.set(false);
        if (primary != null && primary.isConfigured()) {
            AppLogger.i(TAG, "try primary " + primary.getClass().getSimpleName());
            primary.chat(userText, history, new LlmCallback() {
                @Override
                public void onSuccess(String reply, String source) {
                    finishOk(callback, reply, source);
                }

                @Override
                public void onError(String error) {
                    AppLogger.w(TAG, "primary failed: " + error);
                    trySdk(userText, history, callback);
                }
            });
            return;
        }
        trySdk(userText, history, callback);
    }

    private void trySdk(String userText, List<ChatMessage> history, LlmCallback callback) {
        AppLogger.i(TAG, "try sdk nlp");
        sdkNlpLlmClient.chat(userText, history, new LlmCallback() {
            @Override
            public void onSuccess(String reply, String source) {
                finishOk(callback, reply, source);
            }

            @Override
            public void onError(String error) {
                AppLogger.w(TAG, "sdk nlp failed: " + error);
                mockLlmClient.chat(userText, history, new LlmCallback() {
                    @Override
                    public void onSuccess(String reply, String source) {
                        finishOk(callback, reply, source);
                    }

                    @Override
                    public void onError(String e2) {
                        finishErr(callback, e2);
                    }
                });
            }
        });
    }

    private void finishOk(LlmCallback callback, String reply, String source) {
        if (!done.compareAndSet(false, true)) {
            return;
        }
        if (callback != null) {
            callback.onSuccess(reply, source);
        }
    }

    private void finishErr(LlmCallback callback, String err) {
        if (!done.compareAndSet(false, true)) {
            return;
        }
        if (callback != null) {
            callback.onError(err);
        }
    }

    @Override
    public void cancel() {
        done.set(true);
        if (primary != null) {
            primary.cancel();
        }
        if (sdkNlpLlmClient != null) {
            sdkNlpLlmClient.cancel();
        }
        mockLlmClient.cancel();
    }
}