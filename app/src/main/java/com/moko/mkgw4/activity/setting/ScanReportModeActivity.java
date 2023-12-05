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
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityScanReportModeBinding;
import com.moko.mkgw4.dialog.MkGw4BottomDialog;
import com.moko.mkgw4.utils.ToastUtils;
import com.moko.support.mkgw4.MokoSupport;
import com.moko.support.mkgw4.OrderTaskAssembler;
import com.moko.support.mkgw4.entity.OrderCHAR;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

public class ScanReportModeActivity extends MkGw4BaseActivity {
    private ActivityScanReportModeBinding mBind;
    private boolean mReceiverTag;
    private final String[] mValues = {"turn off scan", "Real time scan& immediate report", "real time scan & periodic report",
            "periodic scan& immediate report", "periodic scan & periodic report"};
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityScanReportModeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getScanReportMode());
        initListener();
    }

    private void initListener() {
        mBind.tvModeSelection.setOnClickListener(v -> selectDeviceMode());
        mBind.ivSave.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setScanReportMode(mSelected));
        });
        mBind.tvRealScanPeriodicReport.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            startActivity(new Intent(this, RealScanPeriodicReportActivity.class));
        });
        mBind.tvPeriodicScanImmediateReport.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            startActivity(new Intent(this, PeriodicScanImmediateReportActivity.class));
        });
        mBind.tvPeriodicScanPeriodicReport.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            startActivity(new Intent(this, PeriodicScanPeriodicReportActivity.class));
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_SCAN_REPORT_MODE) {
                                ToastUtils.showToast(this, result == 1 ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_SCAN_REPORT_MODE) {
                                if (length > 0) {
                                    mSelected = value[4] & 0xff;
                                    mBind.tvModeSelection.setText(mValues[mSelected]);
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
        finish();
    }

    private void selectDeviceMode() {
        if (isWindowLocked()) return;
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mValues)), mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvModeSelection.setText(mValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }
}
