package com.moko.mkgw4.activity.filter;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterUrlMkgw4Binding;
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

public class MkGw4FilterUrlActivity extends MkGw4BaseActivity {
    private final String FILTER_ASCII = "[ -~]*";
    private ActivityFilterUrlMkgw4Binding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterUrlMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etUrl.setFilters(new InputFilter[]{new InputFilter.LengthFilter(37), inputFilter});
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUrlEnable());
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUrl());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                dismissSyncProgressDialog();
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
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xED || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_EDDYSTONE_URL:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_EDDYSTONE_URL_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        }else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_EDDYSTONE_URL:
                                    if (length > 0) {
                                        String url = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etUrl.setText(url);
                                        mBind.etUrl.setSelection(mBind.etUrl.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_EDDYSTONE_URL_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbUrl.setChecked(enable == 1);
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
        final String url = !TextUtils.isEmpty(mBind.etUrl.getText()) ? mBind.etUrl.getText().toString() : null;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUrl(url));
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUrlEnable(mBind.cbUrl.isChecked() ? 1 : 0));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        setResult(RESULT_OK);
        finish();
    }
}
