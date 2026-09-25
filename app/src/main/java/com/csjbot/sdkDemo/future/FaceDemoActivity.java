package com.csjbot.sdkDemo.future;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.csjbot.coshandler.listener.OnDetectPersonListener;
import com.csjbot.coshandler.listener.OnFaceListener;
import com.csjbot.coshandler.listener.OnFaceSaveListener;
import com.csjbot.coshandler.listener.OnGetAllFaceListener;
import com.csjbot.coshandler.listener.OnSnapshotoListener;
import com.csjbot.coshandler.log.BaseLogger;
import com.csjbot.sdkDemo.R;
import com.csjbot.sdkDemo.entity.PersonDetectBean;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class FaceDemoActivity extends BaseActivity {
    private ImageView cameraPreview;
    private EditText register_name;
    private TextView person_state, person_selected;
    private ListView allFaceListLisView;
    private String selectedPersonId = "";
    private TextView tv_show_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_demo);
        cameraPreview = (ImageView) findViewById(R.id.cameraPreview);
        register_name = (EditText) findViewById(R.id.register_name);
        person_state = (TextView) findViewById(R.id.person_state);
        person_selected = (TextView) findViewById(R.id.person_selected);
        allFaceListLisView = (ListView) findViewById(R.id.allFaceListLisView);
        tv_show_name = (TextView) findViewById(R.id.tv_show_name);

//        mCsjBot.registerDetectPersonListener(personListener);
        initCountDownTimer();
        initLogShow();
    }

    private final OnDetectPersonListener onDetectPersonListener = new OnDetectPersonListener() {
        @Override
        public void response(int state) {
            BaseLogger.debug("getPerson === " + state);

        }
    };

    private final OnFaceListener faceListener = new OnFaceListener() {

        @Override
        public void personInfo(String s) {
            // There is only one person here
            BaseLogger.error("takePicture === " + s);
            PersonDetectBean bean = new Gson().fromJson(s, PersonDetectBean.class);
            PersonDetectBean.FaceListBean.FaceRecgBean recgBean = bean.getFace_list().get(0).getFace_recg();

            int confidence = recgBean.getConfidence();
            if (confidence > 60) {
                speechSpeak.startSpeaking(getString(R.string.hello) + recgBean.getName(), null);
                runOnUiThread(() -> tv_show_name.setText(getString(R.string.hello) + recgBean.getName()));
            }
        }

        @Override
        public void personNear(boolean b) {
            String person = b ? getString(R.string.have_person) : getString(R.string.have_no_person);
            showMsgInTextView(person_state, person);
            BaseLogger.error("personNear === " + b);
        }
    };

//    private OnDetectPersonListener personListener = new OnDetectPersonListener() {
//        @Override
//        public void response(int i) {
//            String person = i == 0 ? "无人" : "有人";
//            showMsgInTextView(person_state, person);
////            BaseLogger.error("takePicture" + i);
//        }
//    };


    public void startContinuousIdentification(View view) {
        Intent intent = new Intent("com.csjbot.forceSearchFace");
        sendBroadcast(intent);
    }

    public void stopContinuousIdentification(View view) {
    }

    @Override
    protected void onResume() {
        mCsjBot.registerFaceListener(faceListener);
        mCsjBot.registerDetectPersonListener(onDetectPersonListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        dismissPreview();
        mCsjBot.getFace().resumePreview(FaceDemoActivity.this);
        mCsjBot.unRegisterFaceListener(faceListener);
        mCsjBot.unRegisterDetectPersonListener(onDetectPersonListener);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissPreview();
        mCsjBot.getFace().resumePreview(FaceDemoActivity.this);

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void openPreView(View view) {
        showPreview();
    }

    public void closePreView(View view) {
        dismissPreview();
    }

    /**
     * set x, y to your view
     */
    private void showPreview() {
        int[] viewLocation = new int[2];
        cameraPreview.getLocationInWindow(viewLocation);
        int preViewX = viewLocation[0]; // x
        int preViewY = viewLocation[1]; // y

        if (isPlus()) {
            mCsjBot.getFace().openPreView(this, 240, 240, 640, 480);
        } else {
            mCsjBot.getFace().openPreView(this, 240, 240, 640, 480);
        }
    }

    private boolean isPlus() {
        return false;
    }

    private void dismissPreview() {
        mCsjBot.getFace().closePreView(this);
    }

    private boolean snapshotOK = false;
    private static final int COUNT_DOWNCOUNT_MAX = 3;
    private int countDownCount = COUNT_DOWNCOUNT_MAX;
    private CountDownTimer countDownTimer;

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(COUNT_DOWNCOUNT_MAX * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!FaceDemoActivity.this.isFinishing()) {
                    countDownCount--;
                    speechSpeak.startSpeaking(String.valueOf(countDownCount), null);
                }
            }

            /**
             *Called after the countdown ends
             */
            @Override
            public void onFinish() {
                if (snapshotOK) {
                    mCsjBot.getFace().pausePreview(FaceDemoActivity.this);
                }
                countDownCount = COUNT_DOWNCOUNT_MAX;
            }

        };
    }

    public void takePicture(View view) {
    }

    public void cancelRegisterFace(View view) {
    }

    public void registerFace(View view) {
    }

    public void getAllFaces(View view) {
    }

    public void delChoosePerson(View view) {
    }
}
