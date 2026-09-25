package com.csjbot.sdkDemo.future;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.csjbot.coshandler.core.CsjRobot;
import com.csjbot.coshandler.core.Speech;
import com.csjbot.coshandler.core.State;
import com.csjbot.coshandler.listener.OnGoRotationListener;
import com.csjbot.coshandler.listener.OnSpeakListener;
import com.csjbot.coshandler.listener.OnSpeechListener;
import com.csjbot.coshandler.listener.OnWakeupListener;
import com.csjbot.coshandler.listener.SpeechError;
import com.csjbot.coshandler.log.Csjlogger;
import com.csjbot.coshandler.util.ShellUtil;
import com.csjbot.sdkDemo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class AsrNlpActivity extends BaseActivity {
    private TextView asr_result, nlp_result, wakeup_angle;

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr_nlp);

        initLogShow();

        asr_result = (TextView) findViewById(R.id.asr_result);
        nlp_result = (TextView) findViewById(R.id.nlp_result);
        wakeup_angle = (TextView) findViewById(R.id.wakeup_angle);

        mCsjBot.registerSpeechListener(onSpeechListener);

        mCsjBot.registerWakeupListener(onWakeupListener);
    }

    private final OnSpeechListener onSpeechListener = new OnSpeechListener() {
        @Override
        public void speechInfo(final String s, final int i) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Simple parsing example
                    Csjlogger.debug("speechInfo： type = " + i + ",msg=" + s);
                    if (Speech.SPEECH_RECOGNITION_RESULT == i) { // Identified information
                        try {
                            String text = new JSONObject(s).getString("text");
                            boolean last = new JSONObject(s).getBoolean("isLast");
                            if (last) {
                            }
                            asr_result.setText(text);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (Speech.SPEECH_RECOGNITION_AND_ANSWER_RESULT == i) {// Identified information and answers
                        try {
                            String say = new JSONObject(s).getJSONObject("result").getJSONObject("data").getString("say");
                            nlp_result.setText(say);
                            speechSpeak.startSpeaking(say, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onAudio(byte[] audioData) {
            // 获取语音识别的音频流数据

        }
    };

    private OnWakeupListener onWakeupListener = new OnWakeupListener() {
        @Override
        public void response(final int i) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    wakeup_angle.setText(String.format(Locale.getDefault(), getString(R.string.wakeup_angle), i));

                    speechSpeak.startSpeaking(getString(R.string.im_here), null);

                    // Turn around after locating the sound source
                    mCsjBot.getAction().moveAngle(i, new OnGoRotationListener() {
                        @Override
                        public void response(int i) {
                            if (i > 0 && i < 360) {
                                if (i <= 180) {
                                    if (mCsjBot.getState().getChargeState() == State.NOT_CHARGING) {
                                        mCsjBot.getAction().moveAngle(i, null);
                                    }
                                } else {
                                    if (mCsjBot.getState().getChargeState() == State.NOT_CHARGING) {
                                        mCsjBot.sendDirectMessage("{\"msg_id\":\"SET_WAKEUP_BEAM_REQ\",\"beam\":0}");
                                        mCsjBot.getAction().moveAngle(-(360 - i), null);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mCsjBot.unRegisterSpeechListener(onSpeechListener);
        mCsjBot.unRegisterWakeupListener(onWakeupListener);
        this.finish();
    }

    public void startAsrService(View view) {
        CsjRobot.getInstance().getSpeech().startSpeechService();
    }

    public void stopAsrService(View view) {
        CsjRobot.getInstance().getSpeech().closeSpeechService();
    }

    public void TurnOnMultipleASR(View view) {
        ShellUtil.execCmd("am force-stop com.demo.csjbot.csjsdkdemo  " +
                "&& am start -n com.demo.csjbot.csjsdkdemo/com.demo.csjbot.csjsdkdemo.SplashActivity ", true, false);
        CsjRobot.getInstance().getSpeech().startIsr();
    }

    public void TurnOffMultipleASR(View view) {
        CsjRobot.getInstance().getSpeech().stopIsr();
    }


    public void textToSpeech(View view) {
        String result = nlp_result.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = getString(R.string.content_is_null);
        }
        speechSpeak.startSpeaking(result, null);
    }

    boolean canLoopSpeak = false;

    private void speakMessage(String msg) {
        speechSpeak.startSpeaking(msg, new OnSpeakListener() {
            @Override
            public void onSpeakBegin() {

            }

            @Override
            public void onCompleted(SpeechError speechError) {
                if (canLoopSpeak) {
                    speakMessage(msg);
                }
            }
        });
    }
}
