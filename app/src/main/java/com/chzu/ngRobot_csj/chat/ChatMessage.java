package com.chzu.ngRobot_csj.chat;

public class ChatMessage {
    public static final int ROLE_USER = 0;
    public static final int ROLE_BOT = 1;
    public static final int ROLE_SYSTEM = 2;

    public final int role;
    public final String text;
    public final long timeMs;

    public ChatMessage(int role, String text) {
        this.role = role;
        this.text = text == null ? "" : text;
        this.timeMs = System.currentTimeMillis();
    }
}