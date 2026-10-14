package com.chzu.ngRobot_csj.chat.llm;

import android.text.TextUtils;

import com.chzu.ngRobot_csj.chat.ChatMessage;
import com.chzu.ngRobot_csj.utils.AppLogger;

import java.util.List;

/**
 * 讯飞星辰 MaaS。
 *
 * 若控制台给的是 OpenAI 兼容地址：直接在 llm_settings.json 填 baseUrl/apiKey/model，
 * provider 设为 xunfei_maas 即可（本类默认走 HttpLlmClient 兼容协议）。
 *
 * 若鉴权/请求体特殊：在 doCustomChat() 里按官方文档改，不要改 ChatActivity。
 */
public class XunfeiMaasLlmClient implements LlmClient {
    private static final String TAG = "XunfeiMaasLlm";

    private final LlmSettings settings;
    private final LlmClient delegate;
    private final boolean useCustom;

    public XunfeiMaasLlmClient(LlmSettings settings) {
        this.settings = settings;
        // 默认按 OpenAI 兼容转发；需要定制协议时把 useCustom 改为 true
        this.useCustom = false;
        if (!useCustom) {
            String url = TextUtils.isEmpty(settings.baseUrl)
                    ? "https://maas-api.cn-huabei-1.xf-yun.com/v1"
                    : settings.baseUrl;
            this.delegate = new HttpLlmClient(
                    url,
                    settings.apiKey,
                    settings.model,
                    settings.systemPrompt,
                    "xunfei_maas",
                    1024
            );
        } else {
            this.delegate = null;
        }
        AppLogger.i(TAG, "init custom=" + useCustom + " model=" + settings.model);
    }

    @Override
    public boolean isConfigured() {
        if (!useCustom) {
            return delegate != null && delegate.isConfigured();
        }
        return settings.hasKey();
    }

    @Override
    public void chat(String userText, List<ChatMessage> history, LlmCallback callback) {
        if (!useCustom) {
            delegate.chat(userText, history, callback);
            return;
        }
        doCustomChat(userText, history, callback);
    }

    /** 非兼容协议时在这里实现讯飞星辰专用请求 */
    private void doCustomChat(String userText, List<ChatMessage> history, LlmCallback callback) {
        AppLogger.w(TAG, "custom protocol not implemented yet");
        if (callback != null) {
            callback.onError("xunfei_maas custom protocol not implemented");
        }
    }

    @Override
    public void cancel() {
        if (delegate != null) {
            delegate.cancel();
        }
    }
}