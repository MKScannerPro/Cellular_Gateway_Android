package com.moko.mkgw4.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.filter.MkGw4FilterAdvNameActivity;
import com.moko.mkgw4.activity.filter.MkGw4FilterMacAddressActivity;
import com.moko.mkgw4.activity.filter.MkGw4FilterRawDataActivity;
import com.moko.mkgw4.databinding.ActivityScannerFilterSettingsBinding;
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
import java.util.Locale;

public class ScannerFilterSettingsActivity extends MkGw4BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ActivityScannerFilterSettingsBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private final String[] mRelationshipValues = {"Null", "Only MAC", "Only ADV Name", "Only Raw Data",
            "ADV Name&Raw Data", "MAC&ADV Name&Raw Data", "ADV Name | Raw Data", "ADV NAME & MAC"};
    private int mRelationshipSelected;
    private final String[] mFilterTypeValues = {"1M PHY(V4.2)", "1M PHY(V5.0)", "1M PHY(V4.2) & 1M PHY(V5.0)", "Coded PHY(V5.0)"};
    private int mFilterTypeSelected;
    private final String[] duplicateDataValues = {"None", "MAC", "MAC+Data type", "MAC+Raw data"};
    private int duplicateDataSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityScannerFilterSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        String advName = getIntent().getStringExtra("advName");
        mBind.sbRssiFilter.setOnSeekBarChangeListener(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
        mBind.tvTitle.setText(advName);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getFilterRSSI());
        orderTasks.add(OrderTaskAssembler.getFilterBleScanPhy());
        orderTasks.add(OrderTaskAssembler.getFilterRelationship());
        orderTasks.add(OrderTaskAssembler.getFilterDuplicateData());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        initListener();
    }

    private void initListener() {
        mBind.tvFilterType.setOnClickListener(v -> onFilterType());
        mBind.tvFilterRelationship.setOnClickListener(v -> onFilterRelationship());
        mBind.tvFilterDuplicate.setOnClickListener(v -> onDuplicateFilter());
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
                                case KEY_FILTER_RSSI:
                                case KEY_FILTER_PHY:
                                case KEY_FILTER_RELATIONSHIP:
                                    if (result != 1) savedParamsError = true;
                                    break;

                                case KEY_DUPLICATE_DATA_FILTER:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_RSSI:
                                    if (length > 0) {
                                        final int rssi = value[4];
                                        int progress = rssi + 127;
                                        mBind.sbRssiFilter.setProgress(progress);
                                        mBind.tvRssiFilterTips.setText(getString(R.string.ble_fix_filter, rssi));
                                    }
                                    break;

                                case KEY_FILTER_PHY:
                                    if (length > 0) {
                                        int type = value[4] & 0xFF;
                                        mFilterTypeSelected = type;
                                        mBind.tvFilterType.setText(mFilterTypeValues[type]);
                                    }
                                    break;

                                case KEY_FILTER_RELATIONSHIP:
                                    if (length > 0) {
                                        int relationship = value[4] & 0xFF;
                                        mRelationshipSelected = relationship;
                                        mBind.tvFilterRelationship.setText(mRelationshipValues[relationship]);
                                    }
                                    break;

                                case KEY_DUPLICATE_DATA_FILTER:
                                    if (length > 0) {
                                        duplicateDataSelected = value[4] & 0xff;
                                        mBind.tvFilterDuplicate.setText(duplicateDataValues[duplicateDataSelected]);
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
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.setFilterRSSI(mBind.sbRssiFilter.getProgress() - 127));
        orderTasks.add(OrderTaskAssembler.setFilterBleScanPhy(mFilterTypeSelected));
        orderTasks.add(OrderTaskAssembler.setFilterRelationship(mRelationshipSelected));
        orderTasks.add(OrderTaskAssembler.setFilterDuplicateData(duplicateDataSelected));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
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

    public void onDuplicateFilter() {
        if (isWindowLocked()) return;
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(duplicateDataValues)), duplicateDataSelected);
        dialog.setListener(value -> {
            duplicateDataSelected = value;
            mBind.tvFilterDuplicate.setText(duplicateDataValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterType() {
        if (isWindowLocked()) return;
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mFilterTypeValues)), mFilterTypeSelected);
        dialog.setListener(value -> {
            mFilterTypeSelected = value;
            mBind.tvFilterType.setText(mFilterTypeValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterRelationship() {
        if (isWindowLocked()) return;
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mRelationshipValues)), mRelationshipSelected);
        dialog.setListener(value -> {
            mRelationshipSelected = value;
            mBind.tvFilterRelationship.setText(mRelationshipValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterByMac(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, MkGw4FilterMacAddressActivity.class);
        startActivity(intent);
    }

    public void onFilterByName(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, MkGw4FilterAdvNameActivity.class);
        startActivity(intent);
    }

    public void onFilterByRawData(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, MkGw4FilterRawDataActivity.class);
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        int rssi = progress - 127;
        mBind.tvRssiFilterValue.setText(String.format(Locale.getDefault(), "%ddBm", rssi));
        mBind.tvRssiFilterTips.setText(getString(R.string.ble_fix_filter, rssi));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
