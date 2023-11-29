package com.moko.ps101m.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.activity.PS101BaseActivity;
import com.moko.ps101m.databinding.ActivityPeriodicScanImmediateReportBinding;
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

/**
 * @author: jun.liu
 * @date: 2023/11/29 14:54
 * @des:
 */
public class PeriodicScanImmediateReportActivity extends PS101BaseActivity {
    private ActivityPeriodicScanImmediateReportBinding mBind;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityPeriodicScanImmediateReportBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getPeriodicScanImmediateReport());
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_PERIODIC_SCAN_IMMEDIATE_REPORT) {
                                int result = value[4] & 0xFF;
                                ToastUtils.showToast(this, result == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_PERIODIC_SCAN_IMMEDIATE_REPORT) {
                                if (length == 6) {
                                    int duration = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                    int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 6, value.length));
                                    mBind.etScanDuration.setText(String.valueOf(duration));
                                    mBind.etScanDuration.setSelection(mBind.etScanDuration.getText().length());
                                    mBind.etScanInetval.setText(String.valueOf(interval));
                                    mBind.etScanInetval.setSelection(mBind.etScanInetval.getText().length());
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
        if (isValid()) {
            int duration = Integer.parseInt(mBind.etScanDuration.getText().toString());
            int interval = Integer.parseInt(mBind.etScanInetval.getText().toString());
            if (interval < duration) {
                ToastUtils.showToast(this, "Bluetooth scan interval shouldn't be less than Bluetooth scan duration");
                return;
            }
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setPeriodicScanImmediateReport(duration, interval));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etScanDuration.getText())) return false;
        int duration = Integer.parseInt(mBind.etScanDuration.getText().toString());
        if (duration < 3 || duration > 3600) return false;
        if (TextUtils.isEmpty(mBind.etScanInetval.getText())) return false;
        int interval = Integer.parseInt(mBind.etScanInetval.getText().toString());
        return interval >= 600 && interval <= 86400;
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
