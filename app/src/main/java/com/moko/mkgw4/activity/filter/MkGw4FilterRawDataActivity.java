package com.moko.mkgw4.activity.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterRawDataMkgw4Binding;
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

public class MkGw4FilterRawDataActivity extends MkGw4BaseActivity {
    private ActivityFilterRawDataMkgw4Binding mBind;
    private boolean savedParamsError;
    private boolean isBXPDeviceOpen;
    private boolean isBXPAccOpen;
    private boolean isBXPTHOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterRawDataMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getFilterRawData());
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
                                case KEY_FILTER_BXP_ACC:
                                case KEY_FILTER_BXP_TH:
                                case KEY_FILTER_BXP_DEVICE:
                                    if (result != 1) savedParamsError = true;
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_RAW_DATA) {
                                if (length == 2) {
                                    int data = MokoUtils.toInt(Arrays.copyOfRange(value, 4, value.length));
                                    mBind.tvFilterByIbeacon.setText((data & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByUid.setText((data >> 1 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByUrl.setText((data >> 2 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByTlm.setText((data >> 3 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.ivFilterByBxpDevice.setImageResource((data >> 4 & 0x01) == 1 ? R.drawable.ic_checked : R.drawable.ps101_ic_unchecked);
                                    mBind.ivFilterByBxpAcc.setImageResource((data >> 5 & 0x01) == 1 ? R.drawable.ic_checked : R.drawable.ps101_ic_unchecked);
                                    mBind.ivFilterByBxpTh.setImageResource((data >> 6 & 0x01) == 1 ? R.drawable.ic_checked : R.drawable.ps101_ic_unchecked);
                                    mBind.tvFilterByBxpButton.setText((data >> 7 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByBxpTag.setText((data >> 8 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByPir.setText((data >> 9 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByMkTof.setText((data >> 10 & 0x01) == 1 ? "ON" : "OFF");
                                    mBind.tvFilterByOther.setText((data >> 11 & 0x01) == 1 ? "ON" : "OFF");
                                    isBXPDeviceOpen = (data >> 4 & 0x01) == 1;
                                    isBXPAccOpen = (data >> 5 & 0x01) == 1;
                                    isBXPTHOpen = (data >> 6 & 0x01) == 1;
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
        finish();
    }

    private void startActivity(Class<?> clz) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, clz);
        launcher.launch(intent);
    }

    public void onFilterByIBeacon(View view) {
        startActivity(MkGw4FilterIBeaconActivity.class);
    }

    public void onFilterByUid(View view) {
        startActivity(MkGw4FilterUIDActivity.class);
    }

    public void onFilterByUrl(View view) {
        startActivity(MkGw4FilterUrlActivity.class);
    }

    public void onFilterByTlm(View view) {
        startActivity(MkGw4FilterTLMActivity.class);
    }

    public void onFilterByBXPDevice(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPDeviceOpen = !isBXPDeviceOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPDeviceEnable(isBXPDeviceOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPAcc(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPAccOpen = !isBXPAccOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPAccEnable(isBXPAccOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPTH(View view) {
        if (isWindowLocked()) return;
        showSyncingProgressDialog();
        isBXPTHOpen = !isBXPTHOpen;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterBXPTHEnable(isBXPTHOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getFilterRawData());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onFilterByBXPButton(View view) {
        startActivity(MkGw4FilterBXPButtonActivity.class);
    }

    public void onFilterByBXPTag(View view) {
        startActivity(MkGw4FilterBXPTagActivity.class);
    }

    public void onFilterByPir(View view) {
        startActivity(MkGw4FilterMkPirActivity.class);
    }

    public void onFilterByMkTof(View view){
        startActivity(MkGw4FilterMkTofActivity.class);
    }

    public void onFilterByOther(View view) {
        startActivity(MkGw4FilterOtherActivity.class);
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        showSyncingProgressDialog();
        mBind.tvFilterByPir.postDelayed(() -> MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getFilterRawData()), 200);
    });
}
