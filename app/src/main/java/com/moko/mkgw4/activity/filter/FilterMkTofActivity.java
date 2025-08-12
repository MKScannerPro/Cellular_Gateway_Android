package com.moko.mkgw4.activity.filter;

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
import com.moko.lib.scannerui.utils.ToastUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterMkTofMkgw4Binding;
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

/**
 * @author: jun.liu
 * @date: 2023/11/30 14:19
 * @des:
 */
public class FilterMkTofActivity extends BaseActivity {
    private ActivityFilterMkTofMkgw4Binding mBind;
    private boolean savedParamsError;
    private final List<String> filterTof = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterMkTofMkgw4Binding.inflate(getLayoutInflater());
        EventBus.getDefault().register(this);
        setContentView(mBind.getRoot());
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getFilterMkTofEnable());
        orderTasks.add(OrderTaskAssembler.getFilterMkTofRules());
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
                                case KEY_FILTER_MK_TOF_ENABLE:
                                    if (result != 1) savedParamsError = true;
                                    break;
                                case KEY_FILTER_MK_TOF_RULES:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_MK_TOF_RULES:
                                    if (length > 0) {
                                        filterTof.clear();
                                        byte[] bytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        int index = 0;
                                        for (int i = 0; i < bytes.length / 2; i++) {
                                            //0  2;2  4;4  6
                                            filterTof.add(MokoUtils.bytesToHexString(Arrays.copyOfRange(bytes, index, index + 2)));
                                            index += 2;
                                        }
                                        for (int i = 0, l = filterTof.size(); i < l; i++) {
                                            String macAddress = filterTof.get(i);
                                            View v = LayoutInflater.from(this).inflate(R.layout.item_mk_tof_filter_mkgw4, mBind.llMkTof, false);
                                            TextView title = v.findViewById(R.id.tvTofTitle);
                                            EditText etCode = v.findViewById(R.id.etCode);
                                            title.setText(String.format(Locale.getDefault(), "Code %d", i + 1));
                                            etCode.setText(macAddress);
                                            etCode.setSelection(etCode.getText().length());
                                            mBind.llMkTof.addView(v);
                                        }
                                    }
                                    break;

                                case KEY_FILTER_MK_TOF_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbEnable.setChecked(enable == 1);
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
        final int c = mBind.llMkTof.getChildCount();
        filterTof.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llMkTof.getChildAt(i);
                EditText etCode = v.findViewById(R.id.etCode);
                final String macAddress = etCode.getText().toString();
                if (TextUtils.isEmpty(macAddress)) return false;
                int length = macAddress.length();
                if (length != 4) return false;
                filterTof.add(macAddress);
            }
        }
        return true;
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.setFilterMkTofEnable(mBind.cbEnable.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterMkTofRules(filterTof));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llMkTof.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.item_mk_tof_filter_mkgw4, mBind.llMkTof, false);
        TextView title = v.findViewById(R.id.tvTofTitle);
        title.setText(String.format(Locale.getDefault(), "Code %d", count + 1));
        mBind.llMkTof.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
        final int c = mBind.llMkTof.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llMkTof.getChildCount();
        if (count > 0) mBind.llMkTof.removeViewAt(count - 1);
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
