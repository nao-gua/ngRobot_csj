package com.chzu.ngRobot_csj.chat.llm;

import com.chzu.ngRobot_csj.chat.ChatMessage;

import java.util.List;

/** Mock 语音回复本地桩 */
public class MockLlmClient implements LlmClient {
    @Override
    public void chat(String userText, List<ChatMessage> history, LlmCallback callback) {
        if (callback == null) {
            return;
        }
        String text = userText == null ? "" : userText.trim();
        if (text.isEmpty()) {
            callback.onError("empty user text");
            return;
        }
        callback.onSuccess("我听到了：" + text, "mock");
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}