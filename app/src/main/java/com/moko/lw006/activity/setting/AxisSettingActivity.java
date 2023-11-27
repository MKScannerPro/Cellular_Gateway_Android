package com.moko.lw006.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityAxisSettingBinding;
import com.moko.lw006.utils.ToastUtils;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class AxisSettingActivity extends Lw006BaseActivity {
    private Lw006ActivityAxisSettingBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityAxisSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAccWakeupCondition());
        orderTasks.add(OrderTaskAssembler.getAccMotionCondition());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_ACC_WAKEUP_CONDITION:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_ACC_MOTION_CONDITION:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(AxisSettingActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_ACC_WAKEUP_CONDITION:
                                    if (length == 2) {
                                        int threshold = value[4] & 0xFF;
                                        int duration = value[5] & 0xFF;
                                        mBind.etWakeupThreshold.setText(String.valueOf(threshold));
                                        mBind.etWakeupDuration.setText(String.valueOf(duration));
                                        mBind.etWakeupThreshold.setSelection(mBind.etWakeupThreshold.getText().length());
                                        mBind.etWakeupDuration.setSelection(mBind.etWakeupDuration.getText().length());
                                    }
                                    break;
                                case KEY_ACC_MOTION_CONDITION:
                                    if (length == 2) {
                                        int threshold = value[4] & 0xFF;
                                        int duration = value[5] & 0xFF;
                                        mBind.etMotionThreshold.setText(String.valueOf(threshold));
                                        mBind.etMotionDuration.setText(String.valueOf(duration));
                                        mBind.etMotionThreshold.setSelection(mBind.etMotionThreshold.getText().length());
                                        mBind.etMotionDuration.setSelection(mBind.etMotionDuration.getText().length());
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        setResult(RESULT_OK);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etWakeupThreshold.getText())) return false;
        final String wakeUpThresholdStr = mBind.etWakeupThreshold.getText().toString();
        final int wakeUpThreshold = Integer.parseInt(wakeUpThresholdStr);
        if (wakeUpThreshold < 1 || wakeUpThreshold > 20) return false;

        if (TextUtils.isEmpty(mBind.etWakeupDuration.getText())) return false;
        final String wakeUpDurationStr = mBind.etWakeupDuration.getText().toString();
        final int wakeUpDuration = Integer.parseInt(wakeUpDurationStr);
        if (wakeUpDuration < 1 || wakeUpDuration > 10) return false;

        if (TextUtils.isEmpty(mBind.etMotionThreshold.getText())) return false;
        final String motionThresholdStr = mBind.etMotionThreshold.getText().toString();
        final int motionThreshold = Integer.parseInt(motionThresholdStr);
        if (motionThreshold < 10 || motionThreshold > 250) return false;

        if (TextUtils.isEmpty(mBind.etMotionDuration.getText())) return false;
        final String motionDurationStr = mBind.etMotionDuration.getText().toString();
        final int motionDuration = Integer.parseInt(motionDurationStr);
        return motionDuration >= 1 && motionDuration <= 15;
    }

    private void saveParams() {
        final String wakeUpThresholdStr = mBind.etWakeupThreshold.getText().toString();
        final int wakeUpThreshold = Integer.parseInt(wakeUpThresholdStr);
        final String wakeUpDurationStr = mBind.etWakeupDuration.getText().toString();
        final int wakeUpDuration = Integer.parseInt(wakeUpDurationStr);
        final String motionThresholdStr = mBind.etMotionThreshold.getText().toString();
        final int motionThreshold = Integer.parseInt(motionThresholdStr);
        final String motionDurationStr = mBind.etMotionDuration.getText().toString();
        final int motionDuration = Integer.parseInt(motionDurationStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAccWakeupCondition(wakeUpThreshold, wakeUpDuration));
        orderTasks.add(OrderTaskAssembler.setAccMotionCondition(motionThreshold, motionDuration));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
