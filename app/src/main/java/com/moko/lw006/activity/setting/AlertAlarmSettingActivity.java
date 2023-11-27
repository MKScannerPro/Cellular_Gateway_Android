package com.moko.lw006.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.ActivityAlartAlermSettingBinding;
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
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2023/6/8 15:14
 * @des:
 */
public class AlertAlarmSettingActivity extends Lw006BaseActivity {
    private ActivityAlartAlermSettingBinding mBind;
    private boolean mReceiverTag;
    private final ArrayList<String> mValues = new ArrayList<>(8);
    private final ArrayList<String> triggerMode = new ArrayList<>(6);
    private int mSelectedPos;
    private int mSelectedMode;
    private int modeFlag, posFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityAlartAlermSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues.add("WIFI");
        mValues.add("BLE");
        mValues.add("GPS");
        mValues.add("WIFI+GPS");
        mValues.add("BLE+GPS");
        mValues.add("WIFI+BLE");
        mValues.add("WIFI+BLE+GPS");
        triggerMode.add("Single Click");
        triggerMode.add("Double Click");
        triggerMode.add("Long Press 1s");
        triggerMode.add("Long Press 2s");
        triggerMode.add("Long Press 3s");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getAlarmAlertTriggerType());
        orderTasks.add(OrderTaskAssembler.getAlarmAlertPosStrategy());
        orderTasks.add(OrderTaskAssembler.getAlarmAlertNotifyEnable());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

        mBind.tvTriggerMode.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(triggerMode, mSelectedMode);
            dialog.setListener(value -> {
                mSelectedMode = value;
                mBind.tvTriggerMode.setText(triggerMode.get(value));
            });
            dialog.show(getSupportFragmentManager());
        });

        mBind.tvPosStrategy.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(mValues, mSelectedPos);
            dialog.setListener(value -> {
                mSelectedPos = value;
                mBind.tvPosStrategy.setText(mValues.get(value));
            });
            dialog.show(getSupportFragmentManager());
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || null == configKeyEnum) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            switch (configKeyEnum) {
                                case KEY_ALARM_ALERT_TRIGGER_TYPE:
                                    modeFlag = value[4] & 0xff;
                                    break;

                                case KEY_ALARM_ALERT_POS_STRATEGY:
                                    posFlag = value[4] & 0xff;
                                    break;

                                case KEY_ALARM_ALERT_NOTIFY_ENABLE:
                                    if ((value[4] & 0xff) == 1 && modeFlag == 1 && posFlag == 1) {
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
                                case KEY_ALARM_ALERT_TRIGGER_TYPE:
                                    if (length == 1) {
                                        mSelectedMode = value[4] & 0xff;
                                        mBind.tvTriggerMode.setText(triggerMode.get(mSelectedMode));
                                    }
                                    break;

                                case KEY_ALARM_ALERT_POS_STRATEGY:
                                    if (length == 1) {
                                        mSelectedPos = value[4] & 0xff;
                                        mBind.tvPosStrategy.setText(mValues.get(mSelectedPos));
                                    }
                                    break;

                                case KEY_ALARM_ALERT_NOTIFY_ENABLE:
                                    if (length == 1) {
                                        int result = value[4] & 0xff;
                                        mBind.cbNotifyAlertStart.setChecked((result & 0x01) == 1);
                                        mBind.cbNotifyAlertEnd.setChecked((result >> 1 & 0x01) == 1);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        int type = (mBind.cbNotifyAlertStart.isChecked() ? 1 : 0) | (mBind.cbNotifyAlertEnd.isChecked() ? 2 : 0);
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.setAlarmAlertTriggerType(mSelectedMode));
        orderTasks.add(OrderTaskAssembler.setAlarmAlertPosStrategy(mSelectedPos));
        orderTasks.add(OrderTaskAssembler.setAlarmAlertNotifyEnable(type));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
        finish();
    }
}
