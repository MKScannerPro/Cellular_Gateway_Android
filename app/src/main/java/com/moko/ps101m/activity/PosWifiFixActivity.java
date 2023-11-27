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
import com.moko.ps101m.databinding.Ps101mActivityPosWifiBinding;
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

public class PosWifiFixActivity extends PS101BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private Ps101mActivityPosWifiBinding mBind;
    private boolean mReceiverTag = false;
    private final String[] posMechanism = {"RSSI Priority", "Time Priority"};
    private int posMechanismIndex;
    private int posTimeoutFlag;
    private int numBssidFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivityPosWifiBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
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
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getWifiPosTimeout());
        orderTasks.add(OrderTaskAssembler.getWifiPosBSSIDNumber());
        orderTasks.add(OrderTaskAssembler.getWifiPosMechanism());
        orderTasks.add(OrderTaskAssembler.getWifiRssiFilter());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        mBind.tvWifiFixMechanism.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(new ArrayList<>(Arrays.asList(posMechanism)), posMechanismIndex);
            dialog.setListener(value -> {
                posMechanismIndex = value;
                mBind.tvWifiFixMechanism.setText(posMechanism[value]);
            });
            dialog.show(getSupportFragmentManager());
        });
        mBind.sbRssiFilter.setOnSeekBarChangeListener(this);
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
                                case KEY_WIFI_POS_TIMEOUT:
                                    posTimeoutFlag = result;
                                    break;

                                case KEY_WIFI_POS_BSSID_NUMBER:
                                    numBssidFlag = result;
                                    break;

                                case KEY_WIFI_POS_MECHANISM:
                                    if (posTimeoutFlag == 1 && numBssidFlag == 1 && result == 1) {
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
                                case KEY_WIFI_POS_TIMEOUT:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etPosTimeout.setText(String.valueOf(number));
                                        mBind.etPosTimeout.setSelection(mBind.etPosTimeout.getText().length());
                                    }
                                    break;

                                case KEY_WIFI_POS_BSSID_NUMBER:
                                    if (length > 0) {
                                        int number = value[4] & 0xFF;
                                        mBind.etBssidNumber.setText(String.valueOf(number));
                                        mBind.etBssidNumber.setSelection(mBind.etBssidNumber.getText().length());
                                    }
                                    break;

                                case KEY_WIFI_POS_MECHANISM:
                                    if (length == 1) {
                                        posMechanismIndex = value[4] & 0xff;
                                        mBind.tvWifiFixMechanism.setText(posMechanism[posMechanismIndex]);
                                    }
                                    break;

                                case KEY_WIFI_RSSI_FILTER:
                                    if (length == 1) {
                                        final int rssi = value[4];
                                        int progress = rssi + 127;
                                        mBind.sbRssiFilter.setProgress(progress);
                                        mBind.tvRssiFilterTips.setText(getString(R.string.wifi_fix_filter, rssi));
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
            numBssidFlag = 0;
            posTimeoutFlag = 0;
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etPosTimeout.getText())) return false;
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 1 || posTimeout > 10) return false;
        if (TextUtils.isEmpty(mBind.etBssidNumber.getText())) return false;
        final String numberStr = mBind.etBssidNumber.getText().toString();
        final int number = Integer.parseInt(numberStr);
        return number >= 1 && number <= 15;
    }

    private void saveParams() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final String numberStr = mBind.etBssidNumber.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final int number = Integer.parseInt(numberStr);
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.setWifiPosTimeout(posTimeout));
        orderTasks.add(OrderTaskAssembler.setWifiPosBSSIDNumber(number));
        orderTasks.add(OrderTaskAssembler.setWifiPosMechanism(posMechanismIndex));
        orderTasks.add(OrderTaskAssembler.setWifiRssiFilter(mBind.sbRssiFilter.getProgress() - 127));
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int rssi = progress - 127;
        mBind.tvRssiFilterValue.setText(String.format(Locale.getDefault(), "%ddBm", rssi));
        mBind.tvRssiFilterTips.setText(getString(R.string.wifi_fix_filter, rssi));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
