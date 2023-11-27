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
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityMotionModeBinding;
import com.moko.lw006.dialog.BottomDialog;
import com.moko.lw006.utils.ToastUtils;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MotionModeActivity extends Lw006BaseActivity {
    private Lw006ActivityMotionModeBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private ArrayList<String> mValues;
    private int mStartSelected;
    private int mTripSelected;
    private int mEndSelected;
    private int stationarySelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityMotionModeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mValues = new ArrayList<>();
        mValues.add("WIFI");
        mValues.add("BLE");
        mValues.add("GPS");
        mValues.add("WIFI+GPS");
        mValues.add("BLE+GPS");
        mValues.add("WIFI+BLE");
        mValues.add("WIFI+BLE+GPS");
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getMotionModeEvent());
        orderTasks.add(OrderTaskAssembler.getMotionModeStartNumber());
        orderTasks.add(OrderTaskAssembler.getMotionStartPosStrategy());
        orderTasks.add(OrderTaskAssembler.getMotionTripInterval());
        orderTasks.add(OrderTaskAssembler.getMotionTripPosStrategy());
        orderTasks.add(OrderTaskAssembler.getMotionEndTimeout());
        orderTasks.add(OrderTaskAssembler.getMotionEndNumber());
        orderTasks.add(OrderTaskAssembler.getMotionEndInterval());
        orderTasks.add(OrderTaskAssembler.getMotionEndPosStrategy());
        orderTasks.add(OrderTaskAssembler.getMotionStationaryPosStrategy());
        orderTasks.add(OrderTaskAssembler.getMotionStationaryReportInterval());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                        if (header != 0xED)
                            return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_MOTION_MODE_START_NUMBER:
                                case KEY_MOTION_MODE_START_POS_STRATEGY:
                                case KEY_MOTION_MODE_TRIP_REPORT_INTERVAL:
                                case KEY_MOTION_MODE_TRIP_POS_STRATEGY:
                                case KEY_MOTION_MODE_END_TIMEOUT:
                                case KEY_MOTION_MODE_END_NUMBER:
                                case KEY_MOTION_MODE_END_REPORT_INTERVAL:
                                case KEY_MOTION_MODE_END_POS_STRATEGY:
                                case KEY_MOTION_MODE_STATIONARY_POS_STRATEGY:
                                case KEY_MOTION_MODE_STATIONARY_REPORT_INTERVAL:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_MOTION_MODE_EVENT:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_MOTION_MODE_EVENT:
                                    if (length > 0) {
                                        int modeEvent = value[4] & 0xFF;
                                        mBind.cbNotifyOnStart.setChecked((modeEvent & 1) == 1);
                                        mBind.cbFixOnStart.setChecked((modeEvent & 2) == 2);
                                        mBind.cbNotifyInTrip.setChecked((modeEvent & 4) == 4);
                                        mBind.cbFixInTrip.setChecked((modeEvent & 8) == 8);
                                        mBind.cbNotifyOnEnd.setChecked((modeEvent & 16) == 16);
                                        mBind.cbFixOnEnd.setChecked((modeEvent & 32) == 32);
                                        mBind.cbFixOnStationaryState.setChecked((modeEvent & 64) == 64);
                                    }
                                    break;
                                case KEY_MOTION_MODE_START_NUMBER:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etFixOnStartNumber.setText(String.valueOf(number));
                                        mBind.etFixOnStartNumber.setSelection(mBind.etFixOnStartNumber.getText().length());
                                    }
                                    break;
                                case KEY_MOTION_MODE_START_POS_STRATEGY:
                                    if (length > 0) {
                                        mStartSelected = value[4] & 0xFF;
                                        mBind.tvPosStrategyOnStart.setText(mValues.get(mStartSelected));
                                    }
                                    break;
                                case KEY_MOTION_MODE_TRIP_REPORT_INTERVAL:
                                    if (length > 0) {
                                        byte[] intervalBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        int interval = MokoUtils.toInt(intervalBytes);
                                        mBind.etReportIntervalInTrip.setText(String.valueOf(interval));
                                        mBind.etReportIntervalInTrip.setSelection(mBind.etReportIntervalInTrip.getText().length());
                                    }
                                    break;
                                case KEY_MOTION_MODE_TRIP_POS_STRATEGY:
                                    if (length > 0) {
                                        mTripSelected = value[4] & 0xFF;
                                        mBind.tvPosStrategyInTrip.setText(mValues.get(mTripSelected));
                                    }
                                    break;
                                case KEY_MOTION_MODE_END_TIMEOUT:
                                    if (length > 0) {
                                        int timeout = value[4] & 0xFF;
                                        mBind.etTripEndTimeout.setText(String.valueOf(timeout));
                                        mBind.etTripEndTimeout.setSelection(mBind.etTripEndTimeout.getText().length());
                                    }
                                    break;
                                case KEY_MOTION_MODE_END_NUMBER:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etFixOnEndNumber.setText(String.valueOf(number));
                                        mBind.etFixOnEndNumber.setSelection(mBind.etFixOnEndNumber.getText().length());
                                    }
                                    break;
                                case KEY_MOTION_MODE_END_REPORT_INTERVAL:
                                    if (length > 0) {
                                        byte[] intervalBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        int interval = MokoUtils.toInt(intervalBytes);
                                        mBind.etReportIntervalOnEnd.setText(String.valueOf(interval));
                                        mBind.etReportIntervalOnEnd.setSelection(mBind.etReportIntervalOnEnd.getText().length());
                                    }
                                    break;
                                case KEY_MOTION_MODE_END_POS_STRATEGY:
                                    if (length > 0) {
                                        mEndSelected = value[4] & 0xFF;
                                        mBind.tvPosStrategyOnEnd.setText(mValues.get(mEndSelected));
                                    }
                                    break;

                                case KEY_MOTION_MODE_STATIONARY_POS_STRATEGY:
                                    if (length > 0) {
                                        stationarySelected = value[4] & 0xff;
                                        mBind.tvPosStrategyOnStationary.setText(mValues.get(stationarySelected));
                                    }
                                    break;

                                case KEY_MOTION_MODE_STATIONARY_REPORT_INTERVAL:
                                    if (length == 2) {
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etReportIntervalOnStationary.setText(String.valueOf(interval));
                                        mBind.etReportIntervalOnStationary.setSelection(mBind.etReportIntervalOnStationary.getText().length());
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
        if (TextUtils.isEmpty(mBind.etFixOnStartNumber.getText())) return false;
        final String startNumberStr = mBind.etFixOnStartNumber.getText().toString();
        final int startNumber = Integer.parseInt(startNumberStr);
        if (startNumber < 1 || startNumber > 255) return false;
        if (TextUtils.isEmpty(mBind.etReportIntervalInTrip.getText())) return false;
        final String intervalTripStr = mBind.etReportIntervalInTrip.getText().toString();
        final int intervalTrip = Integer.parseInt(intervalTripStr);
        if (intervalTrip < 10 || intervalTrip > 86400) return false;
        if (TextUtils.isEmpty(mBind.etTripEndTimeout.getText())) return false;
        final String endTimeoutStr = mBind.etTripEndTimeout.getText().toString();
        final int endTimeout = Integer.parseInt(endTimeoutStr);
        if (endTimeout < 1 || endTimeout > 180) return false;
        if (TextUtils.isEmpty(mBind.etFixOnEndNumber.getText())) return false;
        final String endNumberStr = mBind.etFixOnEndNumber.getText().toString();
        final int endNumber = Integer.parseInt(endNumberStr);
        if (endNumber < 1 || endNumber > 255) return false;
        if (TextUtils.isEmpty(mBind.etReportIntervalOnEnd.getText())) return false;
        final String endIntervalStr = mBind.etReportIntervalOnEnd.getText().toString();
        final int endInterval = Integer.parseInt(endIntervalStr);
        if (endInterval < 10 || endInterval > 300) return false;
        if (TextUtils.isEmpty(mBind.etReportIntervalOnStationary.getText())) return false;
        int reportStationaryInterval = Integer.parseInt(mBind.etReportIntervalOnStationary.getText().toString());
        return reportStationaryInterval >= 1 && reportStationaryInterval <= 14400;
    }

    private void saveParams() {
        final String startNumberStr = mBind.etFixOnStartNumber.getText().toString();
        final int startNumber = Integer.parseInt(startNumberStr);
        final String intervalTripStr = mBind.etReportIntervalInTrip.getText().toString();
        final int intervalTrip = Integer.parseInt(intervalTripStr);
        final String endTimeoutStr = mBind.etTripEndTimeout.getText().toString();
        final int endTimeout = Integer.parseInt(endTimeoutStr);
        final String endNumberStr = mBind.etFixOnEndNumber.getText().toString();
        final int endNumber = Integer.parseInt(endNumberStr);
        final String endIntervalStr = mBind.etReportIntervalOnEnd.getText().toString();
        final int endInterval = Integer.parseInt(endIntervalStr);
        int reportStationaryInterval = Integer.parseInt(mBind.etReportIntervalOnStationary.getText().toString());

        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setMotionModeStartNumber(startNumber));
        orderTasks.add(OrderTaskAssembler.setMotionStartPosStrategy(mStartSelected));
        orderTasks.add(OrderTaskAssembler.setMotionTripInterval(intervalTrip));
        orderTasks.add(OrderTaskAssembler.setMotionTripPosStrategy(mTripSelected));
        orderTasks.add(OrderTaskAssembler.setMotionEndTimeout(endTimeout));
        orderTasks.add(OrderTaskAssembler.setMotionEndNumber(endNumber));
        orderTasks.add(OrderTaskAssembler.setMotionEndInterval(endInterval));
        orderTasks.add(OrderTaskAssembler.setMotionEndPosStrategy(mEndSelected));
        orderTasks.add(OrderTaskAssembler.setMotionStationaryPosStrategy(stationarySelected));
        orderTasks.add(OrderTaskAssembler.setMotionStationaryReportInterval(reportStationaryInterval));
        int motionMode = (mBind.cbNotifyOnStart.isChecked() ? 1 : 0)
                | (mBind.cbFixOnStart.isChecked() ? 2 : 0)
                | (mBind.cbNotifyInTrip.isChecked() ? 4 : 0)
                | (mBind.cbFixInTrip.isChecked() ? 8 : 0)
                | (mBind.cbNotifyOnEnd.isChecked() ? 16 : 0)
                | (mBind.cbFixOnEnd.isChecked() ? 32 : 0)
                | (mBind.cbFixOnStationaryState.isChecked() ? 64 : 0);
        orderTasks.add(OrderTaskAssembler.setMotionModeEvent(motionMode));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void selectPosStrategyStart(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mStartSelected);
        dialog.setListener(value -> {
            mStartSelected = value;
            mBind.tvPosStrategyOnStart.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void selectPosStrategyTrip(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mTripSelected);
        dialog.setListener(value -> {
            mTripSelected = value;
            mBind.tvPosStrategyInTrip.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void selectPosStrategyEnd(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mEndSelected);
        dialog.setListener(value -> {
            mEndSelected = value;
            mBind.tvPosStrategyOnEnd.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void selectPosStrategyStationary(View view){
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, stationarySelected);
        dialog.setListener(value -> {
            stationarySelected = value;
            mBind.tvPosStrategyOnStationary.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }
}
