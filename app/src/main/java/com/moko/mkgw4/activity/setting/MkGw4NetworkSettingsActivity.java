package com.moko.mkgw4.activity.setting;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityNetworkSettingsMkgw4Binding;
import com.moko.mkgw4.dialog.MkGw4BottomDialog;
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
 * @date: 2023/11/27 15:22
 * @des:
 */
public class MkGw4NetworkSettingsActivity extends MkGw4BaseActivity {
    private ActivityNetworkSettingsMkgw4Binding mBind;
    private final String FILTER_ASCII = "[ -~]*";
    private boolean mSavedParamsError;
    private int netPrioritySelect;
    private final String[] netPriorityValue = {"eMTC->NB-IOT->GSM", "eMTC-> GSM -> NB-IOT", "NB-IOT->GSM-> eMTC", "NB-IOT-> eMTC-> GSM",
            "GSM -> NB-IOT-> eMTC", "GSM -> eMTC->NB-IOT", "eMTC->NB-IOT", "NB-IOT-> eMTC", "GSM", "NB-IOT", "eMTC"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityNetworkSettingsMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        InputFilter inputFilter = (source, start, end, dest, dStart, dEnd) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        mBind.etApn.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100), inputFilter});
        mBind.etUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(127), inputFilter});
        mBind.etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(127), inputFilter});

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(6);
        orderTasks.add(OrderTaskAssembler.getNetworkPriority());
        orderTasks.add(OrderTaskAssembler.getApn());
        orderTasks.add(OrderTaskAssembler.getApnUsername());
        orderTasks.add(OrderTaskAssembler.getApnPassword());
        orderTasks.add(OrderTaskAssembler.getNetworkConnectTimeout());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        mBind.tvNetworkPriority.setOnClickListener(v -> onNetPriorityClick());
    }

    private void onNetPriorityClick() {
        MkGw4BottomDialog dialog = new MkGw4BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(netPriorityValue)), netPrioritySelect);
        dialog.setListener(value -> {
            netPrioritySelect = value;
            mBind.tvNetworkPriority.setText(netPriorityValue[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            runOnUiThread(() -> {
                dismissSyncProgressDialog();
                finish();
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
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
                        if (configKeyEnum == null || header != 0xED) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_NETWORK_PRIORITY:
                                case KEY_APN:
                                case KEY_APN_NAME:
                                case KEY_APN_PASSWORD:
                                    if (result != 1) {
                                        mSavedParamsError = true;
                                    }
                                    break;
                                case KEY_CONNECT_NETWORK_TIMEOUT:
                                    if (result != 1) {
                                        mSavedParamsError = true;
                                    }
                                    if (mSavedParamsError) {
                                        ToastUtils.showToast(this, "Setup failed！");
                                    } else {
                                        ToastUtils.showToast(this, "Setup succeed！");
                                    }
                                    break;
                            }
                        } else if (flag == 0x00) {
                            if (length == 0) return;
                            // read
                            switch (configKeyEnum) {
                                case KEY_NETWORK_PRIORITY:
                                    netPrioritySelect = value[4] & 0xff;
                                    mBind.tvNetworkPriority.setText(netPriorityValue[netPrioritySelect]);
                                    break;
                                case KEY_APN:
                                    mBind.etApn.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                    mBind.etApn.setSelection(mBind.etApn.getText().length());
                                    break;
                                case KEY_APN_NAME:
                                    String name = new String(Arrays.copyOfRange(value, 4, value.length));
                                    mBind.etUsername.setText(name);
                                    mBind.etUsername.setSelection(mBind.etUsername.getText().length());
                                    break;
                                case KEY_APN_PASSWORD:
                                    mBind.etPwd.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                    mBind.etPwd.setSelection(mBind.etPwd.getText().length());
                                    break;
                                case KEY_CONNECT_NETWORK_TIMEOUT:
                                    mBind.etConnectTimeout.setText(String.valueOf(MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length))));
                                    mBind.etConnectTimeout.setSelection(mBind.etConnectTimeout.getText().length());
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onBack(View view) {
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back(){
        EventBus.getDefault().unregister(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>(6);
            orderTasks.add(OrderTaskAssembler.setNetworkPriority(netPrioritySelect));
            String apn = TextUtils.isEmpty(mBind.etApn.getText()) ? null : mBind.etApn.getText().toString();
            String apnName = TextUtils.isEmpty(mBind.etUsername.getText()) ? null : mBind.etUsername.getText().toString();
            String apnPwd = TextUtils.isEmpty(mBind.etPwd.getText()) ? null : mBind.etPwd.getText().toString();
            int timeout = Integer.parseInt(mBind.etConnectTimeout.getText().toString());
            orderTasks.add(OrderTaskAssembler.setApn(apn));
            orderTasks.add(OrderTaskAssembler.setApnUsername(apnName));
            orderTasks.add(OrderTaskAssembler.setApnPassword(apnPwd));
            orderTasks.add(OrderTaskAssembler.setNetworkConnectTimeout(timeout));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "param error");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etConnectTimeout.getText())) return false;
        int timeout = Integer.parseInt(mBind.etConnectTimeout.getText().toString().trim());
        return timeout >= 30 && timeout <= 600;
    }

}
