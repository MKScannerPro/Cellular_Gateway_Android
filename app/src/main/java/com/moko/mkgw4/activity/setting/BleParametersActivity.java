package com.moko.mkgw4.activity.setting;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityBleParametersBinding;
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

/**
 * @author: jun.liu
 * @date: 2023/6/8 15:14
 * @des:
 */
public class BleParametersActivity extends MkGw4BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ActivityBleParametersBinding mBind;
    private boolean mReceiverTag;
    private boolean isParamsError;
    private final int[] txPowerArray = {-40, -20, -16, -12, -8, -4, 0, 2, 3, 4, 5, 6, 7, 8};
    private final String FILTER_ASCII = "[ -~]*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityBleParametersBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(12);
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        orderTasks.add(OrderTaskAssembler.getAdvTimeout());
        orderTasks.add(OrderTaskAssembler.getAdvResponse());
        orderTasks.add(OrderTaskAssembler.getIBeaconMajor());
        orderTasks.add(OrderTaskAssembler.getIBeaconMinor());
        orderTasks.add(OrderTaskAssembler.getIBeaconUUID());
        orderTasks.add(OrderTaskAssembler.getRssi1M());
        orderTasks.add(OrderTaskAssembler.getAdvTxPower());
        orderTasks.add(OrderTaskAssembler.getPasswordVerifyEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        initData();
    }

    private void initData() {
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), inputFilter});
        mBind.etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), inputFilter});
        mBind.cbAdvResponse.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.group.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        mBind.cbPwdVerify.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.layoutPwd.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
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
                            int result = value[4] & 0xff;
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                case KEY_ADV_INTERVAL:
                                case KEY_ADV_TIMEOUT:
                                case KEY_ADV_RESPONSE:
                                case KEY_IBEACON_MAJOR:
                                case KEY_IBEACON_MINOR:
                                case KEY_IBEACON_UUID:
                                case KEY_IBEACON_RSSI1M:
                                case KEY_PASSWORD_VERIFY_ENABLE:
                                case KEY_PASSWORD:
                                    if (result != 1) isParamsError = true;
                                    break;

                                case KEY_ADV_TX_POWER:
                                    if (result != 1) isParamsError = true;
                                    ToastUtils.showToast(this, !isParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                    if (length > 0) {
                                        mBind.etAdvName.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                        mBind.etAdvName.setSelection(mBind.etAdvName.getText().length());
                                    }
                                    break;

                                case KEY_ADV_INTERVAL:
                                    if (length == 1) {
                                        mBind.etAdvInterval.setText(String.valueOf(value[4] & 0xff));
                                        mBind.etAdvInterval.setSelection(mBind.etAdvInterval.getText().length());
                                    }
                                    break;

                                case KEY_ADV_TIMEOUT:
                                    if (length == 1) {
                                        mBind.etAdvTimeout.setText(String.valueOf(value[4] & 0xff));
                                        mBind.etAdvTimeout.setSelection(mBind.etAdvTimeout.getText().length());
                                    }
                                    break;

                                case KEY_ADV_RESPONSE:
                                    if (length == 1) {
                                        int enable = value[4] & 0xff;
                                        mBind.cbAdvResponse.setChecked(enable == 1);
                                    }
                                    break;

                                case KEY_IBEACON_MAJOR:
                                    if (length == 2) {
                                        int major = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etMajor.setText(String.valueOf(major));
                                        mBind.etMajor.setSelection(mBind.etMajor.getText().length());
                                    }
                                    break;

                                case KEY_IBEACON_MINOR:
                                    if (length == 2) {
                                        int minor = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etMinor.setText(String.valueOf(minor));
                                        mBind.etMinor.setSelection(mBind.etMinor.getText().length());
                                    }
                                    break;

                                case KEY_IBEACON_UUID:
                                    if (length == 16) {
                                        String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etUUid.setText(uuid);
                                        mBind.etUUid.setSelection(mBind.etUUid.getText().length());
                                    }
                                    break;

                                case KEY_IBEACON_RSSI1M:
                                    if (length > 0) {
                                        int rssi = value[4];
                                        mBind.sbRssi.setProgress(rssi + 100);
                                        mBind.tvRssiValue.setText(rssi + "dBm");
                                    }
                                    break;

                                case KEY_ADV_TX_POWER:
                                    if (length > 0) {
                                        int txPower = value[4];
                                        int index = 0;
                                        for (int i = 0; i < txPowerArray.length; i++) {
                                            if (txPowerArray[i] == txPower) {
                                                index = i;
                                                break;
                                            }
                                        }
                                        mBind.sbTxPower.setProgress(index);
                                        mBind.tvTxPowerValue.setText(txPower + "dBm");
                                    }
                                    break;

                                case KEY_PASSWORD_VERIFY_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xff;
                                        mBind.cbPwdVerify.setChecked(enable == 1);
                                        if (enable == 1)
                                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getPassword());
                                    }
                                    break;

                                case KEY_PASSWORD:
                                    if (length > 0) {
                                        String password = new String(Arrays.copyOfRange(value, 4, value.length));
                                        mBind.etPwd.setText(password);
                                        mBind.etPwd.setSelection(mBind.etPwd.getText().length());
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
            isParamsError = false;
            List<OrderTask> orderTasks = new ArrayList<>(12);
            orderTasks.add(OrderTaskAssembler.setAdvName(mBind.etAdvName.getText().toString().trim()));
            int interval = Integer.parseInt(mBind.etAdvInterval.getText().toString());
            orderTasks.add(OrderTaskAssembler.setAdvInterval(interval));
            int timeout = Integer.parseInt(mBind.etAdvTimeout.getText().toString());
            orderTasks.add(OrderTaskAssembler.setAdvTimeout(timeout));
            orderTasks.add(OrderTaskAssembler.setAdvResponse(mBind.cbAdvResponse.isChecked() ? 1 : 0));
            if (mBind.cbAdvResponse.isChecked()) {
                int major = Integer.parseInt(mBind.etMajor.getText().toString());
                orderTasks.add(OrderTaskAssembler.setIBeaconMajor(major));
                int minor = Integer.parseInt(mBind.etMinor.getText().toString());
                orderTasks.add(OrderTaskAssembler.setIBeaconMinor(minor));
                orderTasks.add(OrderTaskAssembler.setIBeaconUUID(mBind.etUUid.getText().toString()));
                orderTasks.add(OrderTaskAssembler.setRssi1M(mBind.sbRssi.getProgress() - 100));
            }
            orderTasks.add(OrderTaskAssembler.setAdvTxPower(txPowerArray[mBind.sbTxPower.getProgress()]));
            orderTasks.add(OrderTaskAssembler.setPasswordVerifyEnable(mBind.cbPwdVerify.isChecked() ? 1 : 0));
            if (mBind.cbPwdVerify.isChecked()) {
                orderTasks.add(OrderTaskAssembler.changePassword(mBind.etPwd.getText().toString()));
            }
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etAdvName.getText())) return false;
        if (TextUtils.isEmpty(mBind.etAdvInterval.getText())) return false;
        int interval = Integer.parseInt(mBind.etAdvInterval.getText().toString());
        if (interval < 1 || interval > 100) return false;
        if (TextUtils.isEmpty(mBind.etAdvTimeout.getText())) return false;
        int timeout = Integer.parseInt(mBind.etAdvTimeout.getText().toString());
        if (timeout > 60) return false;
        if (mBind.cbAdvResponse.isChecked()) {
            if (TextUtils.isEmpty(mBind.etMajor.getText())) return false;
            int major = Integer.parseInt(mBind.etMajor.getText().toString());
            if (major > 65535) return false;
            if (TextUtils.isEmpty(mBind.etMinor.getText())) return false;
            int minor = Integer.parseInt(mBind.etMinor.getText().toString());
            if (minor > 65535) return false;
            if (TextUtils.isEmpty(mBind.etUUid.getText()) || mBind.etUUid.getText().length() != 32)
                return false;
        }
        if (mBind.cbPwdVerify.isChecked()) {
            if (TextUtils.isEmpty(mBind.etPwd.getText())) return false;
            int length = mBind.etPwd.getText().length();
            return length >= 6 && length <= 10;
        }
        return true;
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvRssiValue.setText(rssi + "dBm");
        } else if (seekBar.getId() == R.id.sbTxPower) {
            int txPower = txPowerArray[progress];
            mBind.tvTxPowerValue.setText(txPower + "dBm");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
