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
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityPeriodicScanPeriodicReportBinding;
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
import java.util.List;

public class PeriodicScanPeriodicReportActivity extends MkGw4BaseActivity {
    private ActivityPeriodicScanPeriodicReportBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private final String[] mValues = {"Next period", "Current period"};
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityPeriodicScanPeriodicReportBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getPeriodicScanPeriodicReport());
        orderTasks.add(OrderTaskAssembler.getDataRetentionPriority());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        mBind.tvDataRetentionPriority.setOnClickListener(v -> selectPriority());
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
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_PERIODIC_SCAN_PERIODIC_REPORT:
                                    if (result != 1) savedParamsError = true;
                                    break;

                                case KEY_DATA_RETENTION_PRIORITY:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_PERIODIC_SCAN_PERIODIC_REPORT:
                                    if (length == 10) {
                                        int scanDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        int scanInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 10));
                                        int reportInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 10, 14));
                                        mBind.etScanDuration.setText(String.valueOf(scanDuration));
                                        mBind.etScanDuration.setSelection(mBind.etScanDuration.getText().length());
                                        mBind.etScanInterval.setText(String.valueOf(scanInterval));
                                        mBind.etScanInterval.setSelection(mBind.etScanInterval.getText().length());
                                        mBind.etReportInterval.setText(String.valueOf(reportInterval));
                                        mBind.etReportInterval.setSelection(mBind.etReportInterval.getText().length());
                                    }
                                    break;

                                case KEY_DATA_RETENTION_PRIORITY:
                                    if (length > 0) {
                                        mSelected = value[4] & 0xff;
                                        mBind.tvDataRetentionPriority.setText(mValues[mSelected]);
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
        finish();
    }

    private void selectPriority() {
        if (isWindowLocked()) return;
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mValues)), mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvDataRetentionPriority.setText(mValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        savedParamsError = false;
        if (isValid()) {
            int scanDuration = Integer.parseInt(mBind.etScanDuration.getText().toString());
            int scanInterval = Integer.parseInt(mBind.etScanInterval.getText().toString());
            int reportInterval = Integer.parseInt(mBind.etReportInterval.getText().toString());
            if (scanInterval < scanDuration || reportInterval < scanInterval) {
                ToastUtils.showToast(this, "Report interval should be no less than scan interval, scan interval should be no less than scan duration");
                return;
            }
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setPeriodicScanPeriodicReport(scanDuration, scanInterval, reportInterval));
            orderTasks.add(OrderTaskAssembler.setDataRetentionPriority(mSelected));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etScanDuration.getText())) return false;
        int scanDuration = Integer.parseInt(mBind.etScanDuration.getText().toString());
        if (scanDuration < 3 || scanDuration > 3600) return false;
        if (TextUtils.isEmpty(mBind.etScanInterval.getText())) return false;
        int scanInterval = Integer.parseInt(mBind.etScanInterval.getText().toString());
        if (scanInterval < 10 || scanInterval > 86400) return false;
        if (TextUtils.isEmpty(mBind.etReportInterval.getText())) return false;
        int reportInterval = Integer.parseInt(mBind.etReportInterval.getText().toString());
        return reportInterval >= 10 && reportInterval <= 86400;
    }

}
