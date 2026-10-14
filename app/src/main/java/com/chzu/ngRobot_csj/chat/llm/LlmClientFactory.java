package com.chzu.ngRobot_csj.chat.llm;

import android.content.Context;
import android.text.TextUtils;

import com.chzu.ngRobot_csj.utils.AppLogger;

/**
 * 按 llm_settings.json 的 provider 创建对应实现。
 *
 * 已支持：
 * - zhipu         智谱（OpenAI 兼容）
 * - openai_compat 通用 OpenAI 兼容（填 baseUrl/apiKey/model 即可）
 * - xunfei_maas   讯飞星辰 MaaS（类已占位，按官方文档补请求即可）
 * - mock          本地复读
 *
 * 加新厂商：
 * 1. 新建 XxxLlmClient implements LlmClient
 * 2. 在本工厂 switch 里加一个 case
 * 3. llm_settings.json 把 provider 改成对应名字
 */
public final class LlmClientFactory {
    private static final String TAG = "LlmClientFactory";

    private LlmClientFactory() {}

    public static LlmClient createPrimary(Context context) {
        LlmSettings s = LlmSettings.load(context);
        return createPrimary(s);
    }

    public static LlmClient createPrimary(LlmSettings s) {
        String p = s.provider.toLowerCase();
        AppLogger.i(TAG, "create provider=" + p + " model=" + s.model);

        switch (p) {
            case "zhipu":
            case "zhiyu":
            case "glm":
                return HttpLlmClient.zhipu(
                        s.apiKey,
                        TextUtils.isEmpty(s.model) ? "glm-4-flash" : s.model,
                        s.systemPrompt
                );

            case "openai":
            case "openai_compat":
            case "compatible":
                return new HttpLlmClient(
                        s.baseUrl,
                        s.apiKey,
                        s.model,
                        s.systemPrompt,
                        "openai_compat",
                        1024
                );

            case "xunfei":
            case "xunfei_maas":
            case "xfyun_maas":
            case "spark_maas":
                return new XunfeiMaasLlmClient(s);

            case "mock":
                return new MockLlmClient();

            default:
                AppLogger.w(TAG, "unknown provider=" + s.provider + ", fallback openai_compat");
                return new HttpLlmClient(
                        s.baseUrl,
                        s.apiKey,
                        s.model,
                        s.systemPrompt,
                        "openai_compat",
                        1024
                );
        }
    }

    /** 主模型 + 官方 NLP + Mock */
    public static LlmClient createComposite(Context context) {
        LlmSettings s = LlmSettings.load(context);
        LlmClient primary = createPrimary(s);
        return new CompositeLlmClient(primary, new SdkNlpLlmClient());
    }
}