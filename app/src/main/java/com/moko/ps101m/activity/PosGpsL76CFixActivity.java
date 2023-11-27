package com.moko.ps101m.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.databinding.Ps101mActivityPosGpsBinding;
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

public class PosGpsL76CFixActivity extends PS101BaseActivity {
    private Ps101mActivityPosGpsBinding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Ps101mActivityPosGpsBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getGPSPosTimeoutL76());
        orderTasks.add(OrderTaskAssembler.getGPSPDOPLimitL76());
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
                                case KEY_GPS_POS_TIMEOUT_L76C:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_GPS_PDOP_LIMIT_L76C:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(PosGpsL76CFixActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_GPS_POS_TIMEOUT_L76C:
                                    if (length > 0) {
                                        byte[] timeoutBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        int timeout = MokoUtils.toInt(timeoutBytes);
                                        mBind.etPositionTimeout.setText(String.valueOf(timeout));
                                        mBind.etPositionTimeout.setSelection(mBind.etPositionTimeout.getText().length());
                                    }
                                    break;
                                case KEY_GPS_PDOP_LIMIT_L76C:
                                    if (length > 0) {
                                        int limit = value[4] & 0xFF;
                                        mBind.etPdopLimit.setText(String.valueOf(limit));
                                        mBind.etPdopLimit.setSelection(mBind.etPdopLimit.getText().length());
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
        final String posTimeoutStr = mBind.etPositionTimeout.getText().toString();
        if (TextUtils.isEmpty(posTimeoutStr)) return false;
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 30 || posTimeout > 600) return false;
        final String limitStr = mBind.etPdopLimit.getText().toString();
        if (TextUtils.isEmpty(limitStr)) return false;
        final int limit = Integer.parseInt(limitStr);
        return limit >= 25 && limit <= 100;
    }

    private void saveParams() {
        final String posTimeoutStr = mBind.etPositionTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final String limitStr = mBind.etPdopLimit.getText().toString();
        final int limit = Integer.parseInt(limitStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setGPSPosTimeoutL76C(posTimeout));
        orderTasks.add(OrderTaskAssembler.setGPSPDOPLimitL76C(limit));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBack(View view) {
        finish();
    }
}
