package com.chzu.ngRobot_csj.chat.llm;

public interface LlmCallback {
    void onSuccess(String reply, String source);
    void onError(String error);
}