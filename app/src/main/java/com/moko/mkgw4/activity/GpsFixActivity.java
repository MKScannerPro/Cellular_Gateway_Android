package com.moko.mkgw4.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.databinding.ActivityGpsFixMkgw4Binding;
import com.moko.lib.scannerui.utils.ToastUtils;
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

public class GpsFixActivity extends BaseActivity {
    private ActivityGpsFixMkgw4Binding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityGpsFixMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(2);
        orderTasks.add(OrderTaskAssembler.getGPSTimeout());
        orderTasks.add(OrderTaskAssembler.getGPSPDOP());
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
                                case KEY_GPS_TIMEOUT:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_GPS_PDOP:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, savedParamsError ? "Setup failed！" : "Setup succeed！");
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_GPS_TIMEOUT:
                                    if (length > 0) {
                                        byte[] timeoutBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        int timeout = MokoUtils.toInt(timeoutBytes);
                                        mBind.etTimeout.setText(String.valueOf(timeout));
                                        mBind.etTimeout.setSelection(mBind.etTimeout.getText().length());
                                    }
                                    break;
                                case KEY_GPS_PDOP:
                                    if (length > 0) {
                                        int limit = value[4] & 0xFF;
                                        mBind.etPdop.setText(String.valueOf(limit));
                                        mBind.etPdop.setSelection(mBind.etPdop.getText().length());
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
        if (TextUtils.isEmpty(mBind.etTimeout.getText())) return false;
        final String posTimeoutStr = mBind.etTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 60 || posTimeout > 600) return false;
        if (TextUtils.isEmpty(mBind.etPdop.getText())) return false;
        final String limitStr = mBind.etPdop.getText().toString();
        final int limit = Integer.parseInt(limitStr);
        return limit >= 25 && limit <= 100;
    }

    private void saveParams() {
        final String posTimeoutStr = mBind.etTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final String limitStr = mBind.etPdop.getText().toString();
        final int limit = Integer.parseInt(limitStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setGPSTimeout(posTimeout));
        orderTasks.add(OrderTaskAssembler.setGPSPDOP(limit));
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
