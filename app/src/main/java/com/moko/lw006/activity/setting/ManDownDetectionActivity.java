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
import com.moko.lw006.databinding.Lw006ActivityManDownDetectionBinding;
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

public class ManDownDetectionActivity extends Lw006BaseActivity {
    private Lw006ActivityManDownDetectionBinding mBind;
    private boolean mReceiverTag = false;
    private final ArrayList<String> mValues = new ArrayList<>();
    private int mSelected;
    private int enableFlag;
    private int timeoutFlag;
    private int strategyFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityManDownDetectionBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues.add("WIFI");
        mValues.add("BLE");
        mValues.add("GPS");
        mValues.add("WIFI+GPS");
        mValues.add("BLE+GPS");
        mValues.add("WIFI+BLE");
        mValues.add("WIFI+BLE+GPS");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getManDownDetectionEnable());
        orderTasks.add(OrderTaskAssembler.getManDownDetectionTimeout());
        orderTasks.add(OrderTaskAssembler.getManDownPosStrategy());
        orderTasks.add(OrderTaskAssembler.getManDownReportInterval());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

        mBind.tvPosStrategy.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(mValues, mSelected);
            dialog.setListener(value -> {
                mSelected = value;
                mBind.tvPosStrategy.setText(mValues.get(value));
            });
            dialog.show(getSupportFragmentManager());
        });
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
                                case KEY_MAN_DOWN_DETECTION_ENABLE:
                                    enableFlag = result;
                                    break;
                                case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                    timeoutFlag = result;
                                    break;
                                case KEY_MAN_DOWN_POS_STRATEGY:
                                    strategyFlag = result;
                                    break;
                                case KEY_MAN_DOWN_DETECTION_REPORT_INTERVAL:
                                    if (enableFlag == 1 && timeoutFlag == 1 && strategyFlag == 1 && result == 1) {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_MAN_DOWN_DETECTION_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbManDownDetection.setChecked((enable & 0x01) == 1);
                                        mBind.cbNotifyManDownStart.setChecked((enable >> 1 & 0x01) == 1);
                                        mBind.cbNotifyManDownEnd.setChecked((enable >> 2 & 0x01) == 1);
                                    }
                                    break;
                                case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                    if (length > 0) {
                                        int timeout = value[4] & 0xff;
                                        mBind.etDetectionTimeout.setText(String.valueOf(timeout));
                                        mBind.etDetectionTimeout.setSelection(mBind.etDetectionTimeout.getText().length());
                                    }
                                    break;

                                case KEY_MAN_DOWN_POS_STRATEGY:
                                    if (length == 1) {
                                        mSelected = value[4] & 0xff;
                                        mBind.tvPosStrategy.setText(mValues.get(mSelected));
                                    }
                                    break;

                                case KEY_MAN_DOWN_DETECTION_REPORT_INTERVAL:
                                    if (length == 2) {
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etReportInterval.setText(String.valueOf(interval));
                                        mBind.etReportInterval.setSelection(mBind.etReportInterval.getText().length());
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
        if (TextUtils.isEmpty(mBind.etDetectionTimeout.getText())) return false;
        final String timeoutStr = mBind.etDetectionTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        if (timeout < 1 || timeout > 120) return false;
        if (TextUtils.isEmpty(mBind.etReportInterval.getText())) return false;
        int interval = Integer.parseInt(mBind.etReportInterval.getText().toString());
        return interval >= 10 && interval <= 600;
    }

    private void saveParams() {
        final String timeoutStr = mBind.etDetectionTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        int interval = Integer.parseInt(mBind.etReportInterval.getText().toString());
        int flag = (mBind.cbManDownDetection.isChecked() ? 1 : 0) | (mBind.cbNotifyManDownStart.isChecked() ? 2 : 0) | (mBind.cbNotifyManDownEnd.isChecked() ? 4 : 0);
        enableFlag = 0;
        strategyFlag = 0;
        timeoutFlag = 0;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setManDownDetectionEnable(flag));
        orderTasks.add(OrderTaskAssembler.setManDownDetectionTimeout(timeout));
        orderTasks.add(OrderTaskAssembler.setManDownPosStrategy(mSelected));
        orderTasks.add(OrderTaskAssembler.setManDownReportInterval(interval));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
