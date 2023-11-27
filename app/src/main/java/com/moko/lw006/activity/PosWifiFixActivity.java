package com.moko.lw006.activity;

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
import com.moko.lw006.databinding.Lw006ActivityPosWifiBinding;
import com.moko.lw006.dialog.BottomDialog;
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

public class PosWifiFixActivity extends Lw006BaseActivity {
    private Lw006ActivityPosWifiBinding mBind;
    private final ArrayList<String> mValues = new ArrayList<>(2);
    private int mSelected;
    private boolean mReceiverTag = false;
    private final ArrayList<String> posMechanism = new ArrayList<>(2);
    private int posMechanismIndex;
    private int posTimeoutFlag;
    private int numBssidFlag;
    private int wifiDataTypeFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityPosWifiBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues.add("DAS");
        mValues.add("Customer");
        posMechanism.add("RSSI Priority");
        posMechanism.add("Time Priority");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getWifiPosTimeout());
        orderTasks.add(OrderTaskAssembler.getWifiPosBSSIDNumber());
        orderTasks.add(OrderTaskAssembler.getWifiPosDataType());
        orderTasks.add(OrderTaskAssembler.getWifiPosMechanism());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        mBind.tvWifiFixMechanism.setOnClickListener(v -> {
            if (isWindowLocked()) return;
            BottomDialog dialog = new BottomDialog();
            dialog.setDatas(posMechanism, posMechanismIndex);
            dialog.setListener(value -> {
                posMechanismIndex = value;
                mBind.tvWifiFixMechanism.setText(posMechanism.get(value));
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

                                case KEY_WIFI_POS_DATA_TYPE:
                                    wifiDataTypeFlag = result;
                                    break;

                                case KEY_WIFI_POS_MECHANISM:
                                    if (posTimeoutFlag == 1 && numBssidFlag == 1 && wifiDataTypeFlag == 1 && result == 1) {
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
                                case KEY_WIFI_POS_DATA_TYPE:
                                    if (length > 0) {
                                        mSelected = value[4] & 0xFF;
                                        mBind.tvWifiDataType.setText(mValues.get(mSelected));
                                    }
                                    break;

                                case KEY_WIFI_POS_MECHANISM:
                                    if (length == 1) {
                                        posMechanismIndex = value[4] & 0xff;
                                        mBind.tvWifiFixMechanism.setText(posMechanism.get(posMechanismIndex));
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onWifiDataType(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvWifiDataType.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            wifiDataTypeFlag = 0;
            numBssidFlag = 0;
            posTimeoutFlag = 0;
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        if (TextUtils.isEmpty(posTimeoutStr))
            return false;
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 1 || posTimeout > 10) {
            return false;
        }
        final String numberStr = mBind.etBssidNumber.getText().toString();
        if (TextUtils.isEmpty(numberStr)) return false;
        final int number = Integer.parseInt(numberStr);
        return number >= 1 && number <= 15;
    }


    private void saveParams() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final String numberStr = mBind.etBssidNumber.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final int number = Integer.parseInt(numberStr);
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setWifiPosTimeout(posTimeout));
        orderTasks.add(OrderTaskAssembler.setWifiPosBSSIDNumber(number));
        orderTasks.add(OrderTaskAssembler.setWifiPosDataType(mSelected));
        orderTasks.add(OrderTaskAssembler.setWifiPosMechanism(posMechanismIndex));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
}
