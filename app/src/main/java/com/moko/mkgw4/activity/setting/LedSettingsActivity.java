package com.moko.mkgw4.activity.setting;

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
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityLedSettingsBinding;
import com.moko.mkgw4.utils.ToastUtils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author: jun.liu
 * @date: 2023/12/4 10:43
 * @des:
 */
public class LedSettingsActivity extends BaseActivity {
    private ActivityLedSettingsBinding mBind;
    private boolean mReceiverTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityLedSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLedIndicator());
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_LED_INDICATOR) {
                                ToastUtils.showToast(this, (value[4] & 0xff) == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_LED_INDICATOR) {
                                if (length == 1) {
                                    int indicator = value[4] & 0xff;
                                    mBind.cbPowerIndicator.setChecked((indicator & 0x01) == 1);
                                    mBind.cbSwitchIndicator.setChecked((indicator >> 1 & 0x01) == 1);
                                    mBind.cbNetworkIndicator.setChecked((indicator >> 2 & 0x01) == 1);
                                    mBind.cbGpsIndicator.setChecked((indicator >> 3 & 0x01) == 1);
                                }
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
        int indicator = (mBind.cbPowerIndicator.isChecked() ? 1 : 0) | (mBind.cbSwitchIndicator.isChecked() ? 1 << 1 : 0) |
                (mBind.cbNetworkIndicator.isChecked() ? 1 << 2 : 0) | (mBind.cbGpsIndicator.isChecked() ? 1 << 3 : 0);
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLedIndicator(indicator));
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
