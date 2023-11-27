package com.moko.ps101m.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ps101m.R;
import com.moko.ps101m.activity.filter.FilterAdvNameActivity;
import com.moko.ps101m.activity.filter.FilterMacAddressActivity;
import com.moko.ps101m.activity.filter.FilterRawDataSwitchActivity;
import com.moko.ps101m.databinding.Ps101mActivityPosBleBinding;
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
import java.util.Locale;

public class PosBleFixActivity extends PS101BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private Ps101mActivityPosBleBinding mBind;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;
    private final String[] mRelationshipValues = {"Null", "Only MAC", "Only ADV Name", "Only Raw Data", "ADV Name&Raw Data", "MAC&ADV Name&Raw Data", "ADV Name | Raw Data"};
    private int mRelationshipSelected;
    private final String[] mScanningTypeValues = {"1M PHY(BLE 4.x)", "1M PHY(BLE 5)", "1M PHY(BLE 4.x + BLE 5)", "Coded PHY(BLE 5)"};
    private int mScanningTypeSelected;
    private final String[] mBleFixMechanismValues = {"RSSI Priority", "Time Priority"};
    private int mBleFixMechanismSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivityPosBleBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mBind.sbRssiFilter.setOnSeekBarChangeListener(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getBlePosTimeout());
        orderTasks.add(OrderTaskAssembler.getBlePosNumber());
        orderTasks.add(OrderTaskAssembler.getBlePosMechanism());
        orderTasks.add(OrderTaskAssembler.getFilterBleScanPhy());
        orderTasks.add(OrderTaskAssembler.getFilterRSSI());
        orderTasks.add(OrderTaskAssembler.getFilterRelationship());
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
                            switch (configKeyEnum) {
                                case KEY_BLE_POS_TIMEOUT:
                                case KEY_BLE_POS_MAC_NUMBER:
                                case KEY_FILTER_BLE_SCAN_PHY:
                                case KEY_BLE_POS_MECHANISM:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_RELATIONSHIP:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(PosBleFixActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_BLE_POS_TIMEOUT:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etPosTimeout.setText(String.valueOf(number));
                                        mBind.etPosTimeout.setSelection(mBind.etPosTimeout.getText().length());
                                    }
                                    break;
                                case KEY_BLE_POS_MAC_NUMBER:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etMacNumber.setText(String.valueOf(number));
                                        mBind.etMacNumber.setSelection(mBind.etMacNumber.getText().length());
                                    }
                                    break;
                                case KEY_BLE_POS_MECHANISM:
                                    if (length > 0) {
                                        int mechanism = value[4] & 0xFF;
                                        mBleFixMechanismSelected = mechanism;
                                        mBind.tvBleFixMechanism.setText(mBleFixMechanismValues[mechanism]);
                                    }
                                    break;
                                case KEY_FILTER_RSSI:
                                    if (length > 0) {
                                        final int rssi = value[4];
                                        int progress = rssi + 127;
                                        mBind.sbRssiFilter.setProgress(progress);
                                        mBind.tvRssiFilterTips.setText(getString(R.string.ble_fix_filter, rssi));
                                    }
                                    break;
                                case KEY_FILTER_BLE_SCAN_PHY:
                                    if (length > 0) {
                                        int type = value[4] & 0xFF;
                                        mScanningTypeSelected = type;
                                        mBind.tvScanningType.setText(mScanningTypeValues[type]);
                                    }
                                    break;
                                case KEY_FILTER_RELATIONSHIP:
                                    if (length > 0) {
                                        int relationship = value[4] & 0xFF;
                                        mRelationshipSelected = relationship;
                                        mBind.tvFilterRelationship.setText(mRelationshipValues[relationship]);
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
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        if (TextUtils.isEmpty(posTimeoutStr)) return false;
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 1 || posTimeout > 10) return false;
        final String numberStr = mBind.etMacNumber.getText().toString();
        if (TextUtils.isEmpty(numberStr)) return false;
        final int number = Integer.parseInt(numberStr);
        return number >= 1 && number <= 15;
    }


    private void saveParams() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final String numberStr = mBind.etMacNumber.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final int number = Integer.parseInt(numberStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setBlePosTimeout(posTimeout));
        orderTasks.add(OrderTaskAssembler.setBlePosNumber(number));
        orderTasks.add(OrderTaskAssembler.setBlePosMechanism(mBleFixMechanismSelected));
        orderTasks.add(OrderTaskAssembler.setFilterRSSI(mBind.sbRssiFilter.getProgress() - 127));
        orderTasks.add(OrderTaskAssembler.setFilterBleScanPhy(mScanningTypeSelected));
        orderTasks.add(OrderTaskAssembler.setFilterRelationship(mRelationshipSelected));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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

    public void onBleFixMechanism(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mBleFixMechanismValues)), mBleFixMechanismSelected);
        dialog.setListener(value -> {
            mBleFixMechanismSelected = value;
            mBind.tvBleFixMechanism.setText(mBleFixMechanismValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onScanningType(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mScanningTypeValues)), mScanningTypeSelected);
        dialog.setListener(value -> {
            mScanningTypeSelected = value;
            mBind.tvScanningType.setText(mScanningTypeValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterRelationship(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mRelationshipValues)), mRelationshipSelected);
        dialog.setListener(value -> {
            mRelationshipSelected = value;
            mBind.tvFilterRelationship.setText(mRelationshipValues[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onFilterByMac(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, FilterMacAddressActivity.class);
        startActivity(intent);
    }

    public void onFilterByName(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, FilterAdvNameActivity.class);
        startActivity(intent);
    }

    public void onFilterByRawData(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, FilterRawDataSwitchActivity.class);
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
