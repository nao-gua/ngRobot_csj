package com.csjbot.sdkDemo.future;

import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import com.csjbot.coshandler.global.REQConstants;
import com.csjbot.sdkDemo.R;
import com.csjbot.sdkDemo.utils.AutoTestManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ActionActivity extends BaseActivity {
    private TextView asr_result, nlp_result, wakeup_angle, double_door_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        initLogShow();

        double_door_state = (TextView) findViewById(R.id.double_door_state);

        AutoTestManager.getInstance().setOnMsgListener(new AutoTestManager.OnMsgListener() {
            @Override
            public void msg(String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }


    public void AliceNormal(View view) {
        mCsjBot.getExpression().normal();
    }

    public void AliceHappy(View view) {
        mCsjBot.getExpression().happy();
    }

    public void AliceSad(View view) {
        mCsjBot.getExpression().sadness();
    }

    public void AliceAngry(View view) {
        mCsjBot.getExpression().angry();
    }

    public void AliceCharge(View view) {
        mCsjBot.getExpression().lightning();
    }

    public void AliceSleepy(View view) {
        mCsjBot.getExpression().sleepiness();
    }

    public void aliceReset(View view) {
        mCsjBot.getAction().resetV2();
    }

    public void aliceUpHead(View view) {
        mCsjBot.getAction().AliceNewActionHeadUpDownCtrl(100);
    }

    public void aliceDownHead(View view) {
        mCsjBot.getAction().AliceNewActionHeadUpDownCtrl(0);
    }

    public void aliceLeftHead(View view) {
        mCsjBot.getAction().AliceNewActionHeadLeftRightCtrl(30);
    }

    public void aliceRightHead(View view) {
        mCsjBot.getAction().AliceNewActionHeadLeftRightCtrl(70);
    }

    public void aliceMiddleHead(View view) {
        mCsjBot.getAction().AliceNewActionHeadLeftRightCtrl(50);
    }

    public void aliceDownLeft(View view) {
        mCsjBot.getAction().AliceNewActionLeftHandCtrl(20);

    }


    public void aliceUpLeft(View view) {
        mCsjBot.getAction().AliceNewActionLeftHandCtrl(60);

    }

    String rotation = "-1";

    public void aliceUpRight(View view) {
        mCsjBot.getAction().AliceNewActionRightHandCtrl(60);
    }

    public void aliceDownRight(View view) {
        mCsjBot.getAction().AliceNewActionRightHandCtrl(20);
    }


    public void aliceAutoTestOpen(View view) {
        mCsjBot.getAction().startWaveHands(3000);
    }

    public void aliceAutoTestClose(View view) {
        mCsjBot.getAction().stopWaveHands();
    }

    public void openDoubleDoor(View view) {
        mCsjBot.getAction().openDoubleDoor(null);
    }

    public void closeOpenDoor(View view) {
        mCsjBot.getAction().closeDoubleDoor(null);
    }


    boolean auto_run = false;

    public void arm_auto_run(View view) {
        auto_run = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (auto_run) {
                    mCsjBot.getAction().AliceRightArmUp();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mCsjBot.getAction().AliceRightArmDown();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mCsjBot.getAction().AliceLeftArmUp();


                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mCsjBot.getAction().AliceLeftArmDown();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void arm_auto_run_stop(View view) {
        auto_run = false;
    }


    boolean isSnowSwingArms = false;

    public void snow_start_arm_swing(View view) {
        if (isSnowSwingArms) {
            return;
        }
        isSnowSwingArms = true;
        JSONObject jo = new JSONObject();
        try {
            jo.put("msg_id", REQConstants.BodyAction.ROBOT_ARM_LOOP_START_REQ);
            jo.put("interval_time", 1500);
            mCsjBot.sendDirectMessage(jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void snow_stop_arm_swing(View view) {
        JSONObject jo = new JSONObject();
        isSnowSwingArms = false;

        try {
            jo.put("msg_id", REQConstants.BodyAction.ROBOT_ARM_LOOP_STOP_REQ);
            mCsjBot.sendDirectMessage(jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void robotReboot(View view) {
        mCsjBot.getState().reboot();
    }
}
