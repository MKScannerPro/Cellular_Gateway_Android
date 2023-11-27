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
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityIndicatorSettingsBinding;
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

public class IndicatorSettingsActivity extends Lw006BaseActivity {
    private Lw006ActivityIndicatorSettingsBinding mBind;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityIndicatorSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getIndicatorStatus());
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
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            if (configKeyEnum == ParamsKeyEnum.KEY_INDICATOR_STATUS) {
                                if (result == 1) {
                                    ToastUtils.showToast(this, "Save Successfully！");
                                } else {
                                    ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                }
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_INDICATOR_STATUS) {
                                if (length == 2) {
                                    byte[] indicatorBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                    int indicator = MokoUtils.toInt(indicatorBytes);
                                    mBind.cbDeviceState.setChecked((indicator & 1) == 1);
                                    mBind.cbLowPower.setChecked((indicator & 2) == 2);
                                    mBind.cbCharging.setChecked((indicator & 4) == 4);
                                    mBind.cbFullCharge.setChecked((indicator & 8) == 8);
                                    mBind.cbBleConnection.setChecked((indicator & 16) == 16);
                                    mBind.cbNetworkCheck.setChecked((indicator & 32) == 32);
                                    mBind.cbInfix.setChecked((indicator & 64) == 64);
                                    mBind.cbFixSuccessful.setChecked((indicator & 128) == 128);
                                    mBind.cbFailToFix.setChecked((indicator & 256) == 256);
                                }
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
        int indicator = (mBind.cbDeviceState.isChecked() ? 1 : 0)
                | (mBind.cbLowPower.isChecked() ? 2 : 0)
                | (mBind.cbCharging.isChecked() ? 4 : 0)
                | (mBind.cbFullCharge.isChecked() ? 8 : 0)
                | (mBind.cbBleConnection.isChecked() ? 16 : 0)
                | (mBind.cbNetworkCheck.isChecked() ? 32 : 0)
                | (mBind.cbInfix.isChecked() ? 64 : 0)
                | (mBind.cbFixSuccessful.isChecked() ? 128 : 0)
                | (mBind.cbFailToFix.isChecked() ? 256 : 0)
                | 512 | 1024;
        showSyncingProgressDialog();
        LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setIndicatorStatus(indicator));
    }
}
