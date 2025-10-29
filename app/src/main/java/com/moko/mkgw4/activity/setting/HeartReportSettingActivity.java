package com.moko.mkgw4.activity.setting;

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
import com.moko.lib.scannerui.utils.ToastUtils;
import com.moko.mkgw4.AppConstants;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityHeartReportSettingMkgw4Binding;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: jun.liu
 * @date: 2023/12/4 17:36
 * @des:
 */
public class HeartReportSettingActivity extends BaseActivity {
    private ActivityHeartReportSettingMkgw4Binding mBind;
    private boolean mReceiverTag;
    private boolean saveParError;
    private int deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityHeartReportSettingMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        deviceType = getIntent().getIntExtra(AppConstants.DEVICE_TYPE, 0);
        if (deviceType > 0) mBind.cbSequenceNum.setVisibility(View.VISIBLE);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getDevicePayloadInterval());
        orderTasks.add(OrderTaskAssembler.getDeviceStatusChoose());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

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
                    if (null != value && value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || null == configKeyEnum) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xff;
                            switch (configKeyEnum) {
                                case KEY_DEVICE_PAYLOAD_INTERVAL:
                                    if (result != 1) saveParError = true;
                                    break;

                                case KEY_DEVICE_STATUS_CHOOSE:
                                    if (result != 1) saveParError = true;
                                    ToastUtils.showToast(this, !saveParError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_DEVICE_PAYLOAD_INTERVAL:
                                    if (length == 4) {
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etInterval.setText(String.valueOf(interval));
                                        mBind.etInterval.setSelection(mBind.etInterval.getText().length());
                                    }
                                    break;

                                case KEY_DEVICE_STATUS_CHOOSE:
                                    if (length == 1) {
                                        int status = value[4] & 0xff;
                                        mBind.cbBattery.setChecked((status & 0x01) == 1);
                                        mBind.cbAcc.setChecked((status >> 1 & 0x01) == 1);
                                        mBind.cbVehicleStatus.setChecked((status >> 2 & 0x01) == 1);
                                        if (deviceType > 0) {
                                            mBind.cbSequenceNum.setChecked((status >> 3 & 0x01) == 1);
                                        }
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
        if (isValid()) {
            showSyncingProgressDialog();
            saveParError = false;
            int interval = Integer.parseInt(mBind.etInterval.getText().toString().trim());
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setDevicePayloadInterval(interval));
            int status;
            if (deviceType > 0) {
                status = (mBind.cbBattery.isChecked() ? 1 : 0) | (mBind.cbAcc.isChecked() ? 1 << 1 : 0) |
                        (mBind.cbVehicleStatus.isChecked() ? 1 << 2 : 0) | (mBind.cbSequenceNum.isChecked() ? 1 << 3 : 0);
            } else {
                status = (mBind.cbBattery.isChecked() ? 1 : 0) | (mBind.cbAcc.isChecked() ? 1 << 1 : 0) | (mBind.cbVehicleStatus.isChecked() ? 1 << 2 : 0);
            }
            orderTasks.add(OrderTaskAssembler.setDeviceStatusChoose(status));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etInterval.getText())) return false;
        int interval = Integer.parseInt(mBind.etInterval.getText().toString());
        return (interval >= 30 && interval <= 86400) || interval == 0;
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
}
