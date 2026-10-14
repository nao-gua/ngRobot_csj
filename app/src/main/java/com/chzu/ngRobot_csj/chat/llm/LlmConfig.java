package com.chzu.ngRobot_csj.chat.llm;

/**
 * @deprecated 配置已外置，请改机器人上的文件：
 * /storage/emulated/0/com.chzu/ngRobot_csj/llm_settings.json
 * 否则读取并拷贝工程内 assets/llm_settings.json
 *
 * 保留此类仅作提示，避免旧引用编译失败。
 */
@Deprecated
public final class LlmConfig {
    private LlmConfig() {}

    /** 请改 /storage/emulated/0/com.chzu/ngRobot_csj/llm_settings.json */
    public static final String ZHIPU_API_KEY = "";
    public static final String ZHIPU_MODEL = "";
    public static final String ZHIPU_BASE_URL = "https://open.bigmodel.cn/api/paas/v4";
    public static final String SYSTEM_PROMPT = "";

    public static boolean isZhipuReady() {
        return false;
    }
}