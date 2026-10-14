package com.chzu.ngRobot_csj.chat.llm;

import com.chzu.ngRobot_csj.chat.ChatMessage;

import java.util.List;

/** 大模型对话接口。新厂商实现此接口，并在 LlmClientFactory 注册。 */
public interface LlmClient {
    void chat(String userText, List<ChatMessage> history, LlmCallback callback);

    void cancel();

    /** 是否已具备调用条件（一般看 apiKey） */
    boolean isConfigured();
}