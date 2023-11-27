package com.moko.lw006.activity.device;

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
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.ActivityOnOffSettingsBinding;
import com.moko.lw006.dialog.AlertMessageDialog;
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
 * @date: 2023/6/12 17:39
 * @des:
 */
public class OnOffSettingsActivity extends Lw006BaseActivity {
    private ActivityOnOffSettingsBinding mBind;
    private boolean mReceiverTag;
    private boolean shutdownPayloadOpen;
    private boolean offByButtonOpen;
    private boolean autoPowerOnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityOnOffSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!LoRaLW006MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            LoRaLW006MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.getShutdownPayloadEnable());
            orderTasks.add(OrderTaskAssembler.getOffByButtonEnable());
            orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        setListener();
    }

    private void setListener() {
        mBind.ivShutdownPayload.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setShutdownInfoReport(shutdownPayloadOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getShutdownPayloadEnable());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        mBind.ivOffByButton.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setOffByButton(offByButtonOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getOffByButtonEnable());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });

        mBind.ivAutoPowerOn.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(2);
            orderTasks.add(OrderTaskAssembler.setAutoPowerOn(autoPowerOnOpen ? 0 : 1));
            orderTasks.add(OrderTaskAssembler.getAutoPowerOn());
            LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });

        mBind.ivPowerOff.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning!");
            dialog.setMessage("Are you sure to turn off the device? Please make sure the device has a button to turn on!");
            dialog.setConfirm("OK");
            dialog.setOnAlertConfirmListener(() -> {
                showSyncingProgressDialog();
                LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.close());
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
                                case KEY_SHUTDOWN_PAYLOAD_ENABLE:
                                case KEY_OFF_BY_BUTTON:
                                case KEY_AUTO_POWER_ON_ENABLE:
                                    if (result == 1) {
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
                                case KEY_SHUTDOWN_PAYLOAD_ENABLE:
                                    if (length == 1) {
                                        int enable = value[4] & 0xFF;
                                        shutdownPayloadOpen = enable == 1;
                                        mBind.ivShutdownPayload.setImageResource(enable == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                    }
                                    break;
                                case KEY_OFF_BY_BUTTON:
                                    if (length == 1) {
                                        int enable = value[4] & 0xFF;
                                        offByButtonOpen = enable == 1;
                                        mBind.ivOffByButton.setImageResource(enable == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                    }
                                    break;
                                case KEY_AUTO_POWER_ON_ENABLE:
                                    if (length == 1) {
                                        int enable = value[4] & 0xFF;
                                        autoPowerOnOpen = enable == 1;
                                        mBind.ivAutoPowerOn.setImageResource(enable == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
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
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }
}
