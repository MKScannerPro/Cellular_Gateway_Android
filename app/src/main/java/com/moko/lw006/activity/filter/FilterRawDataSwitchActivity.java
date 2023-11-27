package com.moko.lw006.activity.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw006.AppConstants;
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityFilterRawDataSwitchBinding;
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

public class FilterRawDataSwitchActivity extends Lw006BaseActivity {
    private Lw006ActivityFilterRawDataSwitchBinding mBind;
    private boolean savedParamsError;
    private boolean isBXPDeviceOpen;
    private boolean isBXPAccOpen;
    private boolean isBXPTHOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityFilterRawDataSwitchBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getFilterRawData());
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

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                        if (header != 0xED)
                            return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_BXP_ACC:
                                case KEY_FILTER_BXP_TH:
                                case KEY_FILTER_BXP_DEVICE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(FilterRawDataSwitchActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_RAW_DATA) {
                                if (length == 12) {
                                    dismissSyncProgressDialog();
                                    mBind.tvFilterByIbeacon.setText(value[4] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByUid.setText(value[5] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByUrl.setText(value[6] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByTlm.setText(value[7] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByBxpIbeacon.setText(value[8] == 1 ? "ON" : "OFF");
                                    mBind.ivFilterByBxpDevice.setImageResource(value[9] == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                    mBind.ivFilterByBxpAcc.setImageResource(value[10] == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                    mBind.ivFilterByBxpTh.setImageResource(value[11] == 1 ? R.drawable.lw006_ic_checked : R.drawable.lw006_ic_unchecked);
                                    mBind.tvFilterByBxpButton.setText(value[12] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByBxpTag.setText(value[13] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByMkPir.setText(value[14] == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByOther.setText(value[15] == 1 ? "ON" : "OFF");
                                    isBXPDeviceOpen = value[9] == 1;
                                    isBXPAccOpen = value[10] == 1;
                                    isBXPTHOpen = value[11] == 1;
                                }
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

    public void onFilterByIBeacon(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterIBeaconActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByUid(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterUIDActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByUrl(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterUrlActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByTlm(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterTLMActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByBXPiBeacon(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterBXPIBeaconActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByBXPDevice(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPDeviceOpen = !isBXPDeviceOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPDeviceEnable(isBXPDeviceOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPAcc(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPAccOpen = !isBXPAccOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPAccEnable(isBXPAccOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPTH(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPTHOpen = !isBXPTHOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPTHEnable(isBXPTHOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPButton(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterBXPButtonActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByBXPTag(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterBXPTagIdActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByMkPir(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterMkPirActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    public void onFilterByOther(View view) {
        if (isWindowLocked()) return;
        Intent i = new Intent(this, FilterOtherActivity.class);
        startActivityForResult(i, AppConstants.REQUEST_CODE_FILTER_RAW_DATA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_FILTER_RAW_DATA) {
            showSyncingProgressDialog();
            mBind.tvFilterByMkPir.postDelayed(()-> LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getFilterRawData()),200);
        }
    }
}
