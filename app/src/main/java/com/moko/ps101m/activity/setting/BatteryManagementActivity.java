package com.moko.ps101m.activity.setting;

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
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.ActivityBatteryManagementBinding;
import com.moko.ps101m.dialog.BottomDialog;
import com.moko.ps101m.utils.ToastUtils;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.OrderTaskAssembler;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatteryManagementActivity extends PS101BaseActivity {
    private ActivityBatteryManagementBinding mBind;
    private boolean mReceiverTag = false;
    private final String[] mValues = {"10%", "20%", "30%", "40%", "50%"};
    private int mSelected;
    private boolean saveParamError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityBatteryManagementBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getLowPowerNotifyEnable());
        orderTasks.add(OrderTaskAssembler.getLowPowerPercent());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

        mBind.tvLowPowerPercent.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(mValues)), mSelected);
            dialog.setListener(value -> {
                mSelected = value;
                mBind.tvLowPowerPercent.setText(mValues[value]);
            });
            dialog.show(getSupportFragmentManager());
        });
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
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_LOW_POWER_PERCENT:
                                    if (result != 1) saveParamError = true;
                                    break;

                                case KEY_LOW_POWER_NOTIFY_ENABLE:
                                    if (result != 1) saveParamError = true;
                                    ToastUtils.showToast(this, !saveParamError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_LOW_POWER_NOTIFY_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbLowPowerNotify.setChecked(enable == 1);
                                    }
                                    break;
                                case KEY_LOW_POWER_PERCENT:
                                    if (length > 0) {
                                        mSelected = value[4] & 0xff;
                                        mBind.tvLowPowerPercent.setText(mValues[mSelected]);
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
        if (EventBus.getDefault().isRegistered(this))
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
        EventBus.getDefault().unregister(this);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        List<OrderTask> orderTasks = new ArrayList<>(2);
        if (mBind.cbLowPowerNotify.isChecked()) {
            orderTasks.add(OrderTaskAssembler.setLowPowerPercent(mSelected));
        }
        orderTasks.add(OrderTaskAssembler.setLowPowerReportEnable(mBind.cbLowPowerNotify.isChecked() ? 1 : 0));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
    }
}
