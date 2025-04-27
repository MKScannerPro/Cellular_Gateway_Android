package com.moko.mkgw4.activity.filter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterMacMkgw4Binding;
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

public class FilterMacAddressActivity extends BaseActivity {
    private ActivityFilterMacMkgw4Binding mBind;
    private boolean savedParamsError;
    private ArrayList<String> filterMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterMacMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        filterMacAddress = new ArrayList<>();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterMacPrecise());
        orderTasks.add(OrderTaskAssembler.getFilterMacReverse());
        orderTasks.add(OrderTaskAssembler.getFilterMacRules());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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

    @SuppressLint("DefaultLocale")
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
                                case KEY_FILTER_MAC_PRECISE:
                                case KEY_FILTER_MAC_REVERSE:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_FILTER_MAC_RULES:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        }else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_MAC_PRECISE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbPreciseMatch.setChecked(enable == 1);
                                    }
                                    break;
                                case KEY_FILTER_MAC_REVERSE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbReverseFilter.setChecked(enable == 1);
                                    }
                                    break;
                                case KEY_FILTER_MAC_RULES:
                                    if (length > 0) {
                                        filterMacAddress.clear();
                                        byte[] macBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        for (int i = 0, l = macBytes.length; i < l; ) {
                                            int macLength = macBytes[i] & 0xFF;
                                            i++;
                                            filterMacAddress.add(MokoUtils.bytesToHexString(Arrays.copyOfRange(macBytes, i, i + macLength)));
                                            i += macLength;
                                        }
                                        for (int i = 0, l = filterMacAddress.size(); i < l; i++) {
                                            String macAddress = filterMacAddress.get(i);
                                            View v = LayoutInflater.from(FilterMacAddressActivity.this).inflate(R.layout.item_mac_filter_mkgw4, mBind.llMacAddress, false);
                                            TextView title = v.findViewById(R.id.tv_mac_address_title);
                                            EditText etMacAddress = v.findViewById(R.id.et_mac_address);
                                            title.setText(String.format("MAC %d", i + 1));
                                            etMacAddress.setText(macAddress);
                                            etMacAddress.setSelection(etMacAddress.getText().length());
                                            mBind.llMacAddress.addView(v);
                                        }
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

    @SuppressLint("DefaultLocale")
    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llMacAddress.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.item_mac_filter_mkgw4, mBind.llMacAddress, false);
        TextView title = v.findViewById(R.id.tv_mac_address_title);
        title.setText(String.format("MAC %d", count + 1));
        mBind.llMacAddress.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
        final int c = mBind.llMacAddress.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llMacAddress.getChildCount();
        if (count > 0) {
            mBind.llMacAddress.removeViewAt(count - 1);
        }
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterMacPrecise(mBind.cbPreciseMatch.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterMacReverse(mBind.cbReverseFilter.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterMacRules(filterMacAddress));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        final int c = mBind.llMacAddress.getChildCount();
        filterMacAddress.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llMacAddress.getChildAt(i);
                EditText etMacAddress = v.findViewById(R.id.et_mac_address);
                final String macAddress = etMacAddress.getText().toString();
                if (TextUtils.isEmpty(macAddress)) {
                    return false;
                }
                int length = macAddress.length();
                if (length % 2 != 0 || length > 12) {
                    return false;
                }
                filterMacAddress.add(macAddress);
            }
        }
        return true;
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
