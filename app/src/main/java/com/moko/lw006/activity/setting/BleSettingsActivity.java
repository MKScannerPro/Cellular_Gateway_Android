package com.moko.lw006.activity.setting;

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
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityBleSettingsBinding;
import com.moko.lw006.dialog.ChangePasswordDialog;
import com.moko.lw006.entity.TxPowerEnum;
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
import java.util.Timer;
import java.util.TimerTask;

public class BleSettingsActivity extends Lw006BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private final String FILTER_ASCII = "[ -~]*";

    private Lw006ActivityBleSettingsBinding mBind;
    private boolean savedParamsError;
    private boolean mPasswordVerifyEnable;
    private boolean mPasswordVerifyDisable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityBleSettingsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), inputFilter});
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        orderTasks.add(OrderTaskAssembler.getAdvTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvTimeout());
        orderTasks.add(OrderTaskAssembler.getPasswordVerifyEnable());
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
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                case KEY_ADV_INTERVAL:
                                case KEY_ADV_TIMEOUT:
                                case KEY_ADV_TX_POWER:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_PASSWORD_VERIFY_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_ADV_NAME:
                                    if (length > 0) {
                                        mBind.etAdvName.setText(new String(Arrays.copyOfRange(value, 4, 4 + length)));
                                        mBind.etAdvName.setSelection(mBind.etAdvName.getText().length());
                                    }
                                    break;
                                case KEY_ADV_TIMEOUT:
                                    if (length > 0) {
                                        int timeout = value[4] & 0xFF;
                                        mBind.etAdvTimeout.setText(String.valueOf(timeout));
                                        mBind.etAdvTimeout.setSelection(mBind.etAdvTimeout.getText().length());
                                    }
                                    break;
                                case KEY_PASSWORD_VERIFY_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mPasswordVerifyEnable = enable == 1;
                                        mPasswordVerifyDisable = enable == 0;
                                        mBind.ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                        mBind.tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
                                    }
                                    break;
                                case KEY_ADV_TX_POWER:
                                    if (length > 0) {
                                        int txPower = value[4];
                                        int progress = TxPowerEnum.fromTxPower(txPower).ordinal();
                                        mBind.sbTxPower.setProgress(progress);
                                        mBind.tvTxPowerValue.setText(String.format("%ddBm", txPower));
                                    }
                                    break;

                                case KEY_ADV_INTERVAL:
                                    if (length > 0) {
                                        int interval = value[4] & 0xff;
                                        mBind.etAdInterval.setText(String.valueOf(interval));
                                        mBind.etAdInterval.setSelection(mBind.etAdInterval.getText().length());
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etAdvTimeout.getText())) return false;
        final String advTimeoutStr = mBind.etAdvTimeout.getText().toString();
        final int timeout = Integer.parseInt(advTimeoutStr);
        if (timeout < 1 || timeout > 60) return false;

        if (TextUtils.isEmpty(mBind.etAdInterval.getText())) return false;
        int interval = Integer.parseInt(mBind.etAdInterval.getText().toString().trim());
        return interval >= 1 && interval <= 100;
    }


    private void saveParams() {
        final String advName = mBind.etAdvName.getText().toString();
        final String timeoutStr = mBind.etAdvTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        final int progress = mBind.sbTxPower.getProgress();
        int interval = Integer.parseInt(mBind.etAdInterval.getText().toString().trim());
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAdvName(advName));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(interval));
        orderTasks.add(OrderTaskAssembler.setAdvTimeout(timeout));
        if (txPowerEnum != null) {
            orderTasks.add(OrderTaskAssembler.setAdvTxPower(txPowerEnum.getTxPower()));
        }
        orderTasks.add(OrderTaskAssembler.setPasswordVerifyEnable(mPasswordVerifyEnable ? 1 : 0));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onChangePassword(View view) {
        if (isWindowLocked()) return;
        if (mPasswordVerifyDisable) return;
        final ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setOnPasswordClicked(password -> {
            showSyncingProgressDialog();
            LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.changePassword(password));
        });
        dialog.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> dialog.showKeyboard());
            }
        }, 200);
    }

    public void onChangeLoginMode(View view) {
        if (isWindowLocked()) return;
        mPasswordVerifyEnable = !mPasswordVerifyEnable;
        mBind.ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
        mBind.tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        if (txPowerEnum == null) return;
        int txPower = txPowerEnum.getTxPower();
        mBind.tvTxPowerValue.setText(String.format("%ddBm", txPower));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
