package com.chzu.ngRobot_csj.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chzu.ngRobot_csj.MainActivity;
import com.chzu.ngRobot_csj.R;
import com.chzu.ngRobot_csj.chat.llm.LlmClientFactory;
import com.chzu.ngRobot_csj.chat.llm.LlmSettings;
import com.chzu.ngRobot_csj.chat.llm.LlmCallback;
import com.chzu.ngRobot_csj.chat.llm.LlmClient;
import com.chzu.ngRobot_csj.future.BaseActivity;
import com.chzu.ngRobot_csj.utils.AppLogger;
import com.chzu.ngRobot_csj.utils.SpeechDiagnose;
import com.csjbot.coshandler.core.CsjRobot;
import com.csjbot.coshandler.core.Speech;
import com.csjbot.coshandler.listener.OnSpeakListener;
import com.csjbot.coshandler.listener.OnSpeechErrorListener;
import com.csjbot.coshandler.listener.OnSpeechListener;
import com.csjbot.coshandler.listener.SpeechError;
import com.csjbot.coshandler.tts.ISpeechSpeak;
import com.csjbot.coshandler.tts.SpeechFactory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 单轮对话：开始 → 听一句完整话 → 回答/播报 → 自动回待机（只显示开始按钮）。
 */
public class ChatActivity extends BaseActivity {
    private static final String TAG = "ChatActivity";

    private enum State {
        IDLE, LISTENING, THINKING, SPEAKING
    }

    private RecyclerView rvChat;
    private TextView tvStatus;
    private TextView tvEngine;
    private TextView tvPartial;
    private EditText etManual;
    private Button btnStart;
    private Button btnStop;
    private Button btnClear;
    private Button btnDemo;
    private Button btnManualSend;

    private ChatMessageAdapter adapter;
    private final List<ChatMessage> history = new ArrayList<>();
    private final Handler ui = new Handler(Looper.getMainLooper());

    private State state = State.IDLE;
    /** 当前这一轮是否在听（点开始后到收完整句前） */
    private boolean sessionActive = false;
    private boolean asrEngineBad = false;
    private String lastErrorTip = "";
    private long lastErrorAt = 0;
    private final AtomicBoolean waitingAnswer = new AtomicBoolean(false);
    private final Runnable forceIdleRunnable = () -> {
        AppLogger.w(TAG, "force idle (tts timeout or stuck)");
        finishTurnToIdle();
    };

    private LlmClient llmClient;
    private LlmClient primaryLlmClient;
    private ISpeechSpeak tts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 大模型：读 /storage/emulated/0/com.chzu/ngRobot_csj/llm_settings.json（无则用 assets 并自动拷出）
        LlmSettings llmSettings = LlmSettings.load(this);
        primaryLlmClient = LlmClientFactory.createPrimary(llmSettings);
        llmClient = LlmClientFactory.createComposite(this);
        AppLogger.i(TAG, "llm " + llmSettings.summary());

        if (speechSpeak == null) {
            speechSpeak = SpeechFactory.createSpeech(this, SpeechFactory.SpeechType.GOOGLE);
        }
        tts = speechSpeak;

        rvChat = findViewById(R.id.rv_chat);
        tvStatus = findViewById(R.id.tv_status);
        tvEngine = findViewById(R.id.tv_engine);
        tvPartial = findViewById(R.id.tv_partial);
        etManual = findViewById(R.id.et_manual);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnClear = findViewById(R.id.btn_clear);
        btnDemo = findViewById(R.id.btn_demo);
        btnManualSend = findViewById(R.id.btn_manual_send);

        adapter = new ChatMessageAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);

        btnStart.setOnClickListener(v -> startSession());
        btnStop.setOnClickListener(v -> stopSession("已取消"));
        btnClear.setOnClickListener(v -> clearChat());
        btnDemo.setOnClickListener(v ->
                startActivity(new Intent(ChatActivity.this, MainActivity.class)));
        btnManualSend.setOnClickListener(v -> sendManual());
        tvEngine.setOnLongClickListener(v -> {
            showDiagnose();
            return true;
        });

        ensureMicPermission();
        setState(State.IDLE);
        addSystem(getString(R.string.chat_hint_start));
        addSystem(buildServiceRouteTip(llmSettings));
        // 诊断改为长按状态栏，避免占对话区
        tvStatus.setOnLongClickListener(v -> {
            showDiagnose();
            return true;
        });
        AppLogger.i(TAG, "onCreate single-turn mode, log=" + AppLogger.getLogPath());
        ui.post(this::checkDiagnoseQuiet);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCsjBot.registerSpeechListener(speechListener);
        mCsjBot.registerSpeechErrorListener(speechErrorListener);
    }

    @Override
    protected void onStop() {
        if (isFinishing()) {
            stopSession(null);
        }
        mCsjBot.unRegisterSpeechListener(speechListener);
        mCsjBot.registerSpeechErrorListener(null);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopSession(null);
        if (llmClient != null) {
            llmClient.cancel();
        }
        super.onDestroy();
    }

    private void ensureMicPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
        }
    }

    private void checkDiagnoseQuiet() {
        String dump = SpeechDiagnose.dump();
        AppLogger.i(TAG, "diagnose:\n" + dump);
        if (dump.contains("aiuikey存在=false") || dump.contains("appId=(空)")) {
            asrEngineBad = true;
            setEngineBanner("识别引擎可能未就绪。长按状态栏查看诊断。");
        }
    }

    private void showDiagnose() {
        String dump = SpeechDiagnose.dump();
        AppLogger.i(TAG, "diagnose manual:\n" + dump);
        addSystem("语音诊断：\n" + dump);
    }

    private void setEngineBanner(String msg) {
        if (TextUtils.isEmpty(msg)) {
            tvEngine.setVisibility(View.GONE);
            return;
        }
        tvEngine.setVisibility(View.VISIBLE);
        tvEngine.setText(msg);
    }

    /** 点开始：只听一轮 */
    private void startSession() {
        // 若卡在思考/播报，先强制收尾，再开新一轮
        if (state == State.THINKING || state == State.SPEAKING) {
            AppLogger.w(TAG, "start while busy state=" + state + ", force idle first");
            finishTurnToIdle();
        }
        if (sessionActive && state == State.LISTENING) {
            return;
        }
        sessionActive = true;
        waitingAnswer.set(false);
        AppLogger.i(TAG, "startSession (single turn)");
        try {
            CsjRobot.getInstance().getSpeech().startSpeechService();
            ui.postDelayed(() -> {
                if (!sessionActive) {
                    return;
                }
                CsjRobot.getInstance().getSpeech().startIsr();
                setState(State.LISTENING);
                if (asrEngineBad) {
                    addSystem("注意：识别引擎可能异常，若无转写请用底部输入框。");
                }
            }, 500);
        } catch (Exception e) {
            AppLogger.e(TAG, "startSession failed", e);
            sessionActive = false;
            setState(State.IDLE);
            addSystem("启动语音失败: " + e.getMessage());
        }
        updateButtons();
    }

    /**
     * 结束本轮并回待机。
     * @param tip 可选系统提示，null 则不提示
     */
    private void stopSession(String tip) {
        ui.removeCallbacks(forceIdleRunnable);
        AppLogger.i(TAG, "stopSession tip=" + tip);
        sessionActive = false;
        waitingAnswer.set(false);
        if (llmClient != null) {
            llmClient.cancel();
        }
        try {
            if (tts != null) {
                tts.stopSpeaking();
            }
            CsjRobot.getInstance().getSpeech().stopIsr();
            CsjRobot.getInstance().getSpeech().closeSpeechService();
        } catch (Exception e) {
            AppLogger.e(TAG, "stopSession error", e);
        }
        tvPartial.setText("");
        setState(State.IDLE);
        if (!TextUtils.isEmpty(tip)) {
            addSystem(tip);
        }
        updateButtons();
    }

    private void clearChat() {
        history.clear();
        adapter.clear();
        tvPartial.setText("");
        addSystem(getString(R.string.chat_cleared));
    }

    private void sendManual() {
        String text = etManual.getText() == null ? "" : etManual.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        etManual.setText("");
        // 手动也走单轮：不依赖听筒会话
        onFinalAsr(text, null);
    }

    private final OnSpeechErrorListener speechErrorListener = json -> ui.post(() -> {
        AppLogger.e(TAG, "speech error: " + json);
        String tip = "语音识别错误";
        try {
            JSONObject o = new JSONObject(json);
            tip = o.optString("desc", tip);
            if (o.has("error_code")) {
                tip = tip + " (" + o.optInt("error_code") + ")";
            }
            if ("InvalidAppIdError".equals(o.optString("desc")) || o.optInt("error_code") == 90003) {
                asrEngineBad = true;
                setEngineBanner("InvalidAppIdError(90003)。长按状态栏看诊断。");
            }
        } catch (Exception ignored) {
        }
        long now = System.currentTimeMillis();
        if (tip.equals(lastErrorTip) && now - lastErrorAt < 2000) {
            return;
        }
        lastErrorTip = tip;
        lastErrorAt = now;
        addSystem(tip);
        // 听的过程中出错：直接回待机
        if (sessionActive && state == State.LISTENING) {
            stopSession(null);
        } else {
            refreshStatus();
        }
    });

    private final OnSpeechListener speechListener = new OnSpeechListener() {
        @Override
        public void speechInfo(String json, int type) {
            ui.post(() -> handleSpeech(json, type));
        }

        @Override
        public void onAudio(byte[] audioData) {
        }
    };

    private void handleSpeech(String json, int type) {
        if (!sessionActive) {
            return;
        }
        if (state == State.SPEAKING || state == State.THINKING) {
            return;
        }
        try {
            if (type == Speech.SPEECH_RECOGNITION_RESULT) {
                JSONObject obj = new JSONObject(json);
                String text = obj.optString("text", "");
                boolean isLast = obj.optBoolean("isLast", false);
                if (!isLast) {
                    tvPartial.setText(TextUtils.isEmpty(text) ? "" : ("识别中: " + text));
                    return;
                }
                tvPartial.setText("");
                onFinalAsr(text, null);
            } else if (type == Speech.SPEECH_RECOGNITION_AND_ANSWER_RESULT
                    || type == Speech.SPEECH_RECOGNITION_AIUI_ANSWER_RESULT) {
                JSONObject obj = new JSONObject(json);
                String text = "";
                String say = null;
                if (obj.has("result")) {
                    JSONObject result = obj.getJSONObject("result");
                    text = result.optString("text", "");
                    if (result.has("data")) {
                        JSONObject data = result.getJSONObject("data");
                        say = data.optString("say", data.optString("answer", null));
                    }
                }
                if (TextUtils.isEmpty(text)) {
                    text = obj.optString("text", "");
                }
                tvPartial.setText("");
                AppLogger.i(TAG, "last result text=" + text + " say=" + say);
                onFinalAsr(text, say);
            }
        } catch (Exception e) {
            AppLogger.e(TAG, "handleSpeech error json=" + json, e);
        }
    }

    private void onFinalAsr(String text, String sdkSay) {
        String t = text == null ? "" : text.trim();
        if (t.length() < 1) {
            return;
        }
        if (!waitingAnswer.compareAndSet(false, true)) {
            AppLogger.w(TAG, "drop asr, busy: " + t);
            return;
        }
        AppLogger.i(TAG, "final ASR (single turn): " + t);
        addMessage(new ChatMessage(ChatMessage.ROLE_USER, t));

        // 收到完整一句：先停听，本轮不再继续 ASR
        sessionActive = false;
        try {
            CsjRobot.getInstance().getSpeech().stopIsr();
        } catch (Exception e) {
            AppLogger.w(TAG, "stopIsr: " + e.getMessage());
        }

        setState(State.THINKING);
        updateButtons();

        if (!TextUtils.isEmpty(sdkSay) && !isHttpLlmConfigured()) {
            onAnswer(sdkSay, "sdk-nlp-push");
            return;
        }

        List<ChatMessage> hist = new ArrayList<>(history);
        if (!hist.isEmpty() && hist.get(hist.size() - 1).role == ChatMessage.ROLE_USER) {
            hist = new ArrayList<>(hist.subList(0, hist.size() - 1));
        }
        final String fallbackSay = sdkSay;
        llmClient.chat(t, hist, new LlmCallback() {
            @Override
            public void onSuccess(String reply, String source) {
                ui.post(() -> onAnswer(reply, source));
            }

            @Override
            public void onError(String error) {
                ui.post(() -> {
                    if (!TextUtils.isEmpty(fallbackSay)) {
                        onAnswer(fallbackSay, "sdk-nlp-fallback");
                    } else {
                        // Mock 已在 Composite 里；这里兜底
                        onAnswer("我听到了：" + t, "mock-local");
                    }
                });
            }
        });
    }

    private boolean isHttpLlmConfigured() {
        return primaryLlmClient != null && primaryLlmClient.isConfigured();
    }

    private void onAnswer(String reply, String source) {
        AppLogger.i(TAG, "answer source=" + source + " text=" + reply);
        addMessage(new ChatMessage(ChatMessage.ROLE_BOT, reply == null ? "" : reply));
        speakThenIdle(reply);
    }

    /** 播报完（或失败/超时）后关闭语音服务，回到待机 */
    private void speakThenIdle(String reply) {
        setState(State.SPEAKING);
        updateButtons();
        if (tts == null || TextUtils.isEmpty(reply)) {
            finishTurnToIdle();
            return;
        }
        // TTS 回调经常丢，必须有超时兜底，否则会卡在「播报中」且开始按钮消失
        ui.removeCallbacks(forceIdleRunnable);
        long timeoutMs = Math.min(60000L, Math.max(4000L, (reply == null ? 0 : reply.length()) * 400L + 3000L));
        ui.postDelayed(forceIdleRunnable, timeoutMs);
        try {
            tts.startSpeaking(reply, new OnSpeakListener() {
                @Override
                public void onSpeakBegin() {
                    AppLogger.d(TAG, "tts begin");
                }

                @Override
                public void onCompleted(SpeechError speechError) {
                    AppLogger.d(TAG, "tts completed err=" + speechError);
                    ui.post(() -> {
                        ui.removeCallbacks(forceIdleRunnable);
                        finishTurnToIdle();
                    });
                }
            });
        } catch (Exception e) {
            AppLogger.e(TAG, "tts failed", e);
            ui.removeCallbacks(forceIdleRunnable);
            finishTurnToIdle();
        }
    }

    private void finishTurnToIdle() {
        ui.removeCallbacks(forceIdleRunnable);
        waitingAnswer.set(false);
        sessionActive = false;
        try {
            if (tts != null) {
                tts.stopSpeaking();
            }
            CsjRobot.getInstance().getSpeech().stopIsr();
            CsjRobot.getInstance().getSpeech().closeSpeechService();
        } catch (Exception e) {
            AppLogger.w(TAG, "close after turn: " + e.getMessage());
        }
        tvPartial.setText("");
        if (state != State.IDLE) {
            setState(State.IDLE);
            addSystem(getString(R.string.chat_turn_done));
        } else {
            updateButtons();
        }
        AppLogger.i(TAG, "turn done -> IDLE");
    }

    /** Startup service routing tip. */
    private String buildServiceRouteTip(LlmSettings s) {
        boolean primaryOk = primaryLlmClient != null && primaryLlmClient.isConfigured();
        String primaryName = primaryOk
                ? (s.provider + " / " + s.model)
                : "未配置（将跳过）";
        String answerChain = primaryOk
                ? "优先自研大模型 → 失败再用官方 NLP 知识库 → 再失败本地Mock"
                : "官方 NLP 知识库 → 失败则本地Mock";

        StringBuilder sb = new StringBuilder();
        sb.append("【服务路由】\n");
        sb.append("ASR 语音识别：官方 CSJ Speech（底层 AIUI/讯飞，读机器人 aiuikey）\n");
        sb.append("TTS 语音播报：官方 SDK 封装 SpeechFactory.GOOGLE\n");
        sb.append("对话控制：半双工状态机\n");
        sb.append("应答链路：").append(answerChain).append('\n');
        sb.append("├ 主模型：自研接入 ").append(primaryName).append('\n');
        sb.append("├ 备用 NLP：官方 Speech.getResult 知识库\n");
        sb.append("└ Mock：复读\n");
        sb.append("机型设定：CsjRobot.setRobotType = TIMO\n");
        sb.append("大模型配置：").append(s.loadedFrom).append('\n');
        String log = AppLogger.getLogPath();
        if (!TextUtils.isEmpty(log)) {
            sb.append("运行日志：").append(log);
        }
        return sb.toString();
    }

    private void addMessage(ChatMessage msg) {
        history.add(msg);
        adapter.add(msg);
        rvChat.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void addSystem(String text) {
        addMessage(new ChatMessage(ChatMessage.ROLE_SYSTEM, text));
    }

    private void setState(State s) {
        state = s;
        refreshStatus();
        updateButtons();
    }

    private void refreshStatus() {
        String label;
        switch (state) {
            case LISTENING:
                label = getString(R.string.chat_status_listening);
                break;
            case THINKING:
                label = getString(R.string.chat_status_thinking);
                break;
            case SPEAKING:
                label = getString(R.string.chat_status_speaking);
                break;
            case IDLE:
            default:
                label = getString(R.string.chat_status_idle);
                break;
        }
        if (asrEngineBad) {
            label = label + " | 引擎异常";
        }
        tvStatus.setText(label);
    }

    private void updateButtons() {
        boolean listening = state == State.LISTENING;
        boolean busy = state == State.THINKING || state == State.SPEAKING;

        // 非聆听都显示「开始」；卡在播报/思考时也可点，用来强制回待机再开一轮
        btnStart.setVisibility(listening ? View.GONE : View.VISIBLE);
        btnStart.setEnabled(true);

        btnStop.setVisibility(listening ? View.VISIBLE : View.GONE);
        btnStop.setEnabled(listening);

        btnClear.setEnabled(!listening);
        btnManualSend.setEnabled(!listening);
    }
}