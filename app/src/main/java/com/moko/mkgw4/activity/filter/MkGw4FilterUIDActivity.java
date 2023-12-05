package com.moko.mkgw4.activity.filter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.activity.MkGw4BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterUidMkgw4Binding;
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

public class MkGw4FilterUIDActivity extends MkGw4BaseActivity {
    private ActivityFilterUidMkgw4Binding mBind;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterUidMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidEnable());
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidNamespace());
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidInstance());
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
                                case KEY_FILTER_EDDYSTONE_UID_NAMESPACE:
                                case KEY_FILTER_EDDYSTONE_UID_INSTANCE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_EDDYSTONE_UID_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_EDDYSTONE_UID_NAMESPACE:
                                    if (length > 0) {
                                        String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etUidNamespace.setText(String.valueOf(uuid));
                                        mBind.etUidNamespace.setSelection(mBind.etUidNamespace.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_EDDYSTONE_UID_INSTANCE:
                                    if (length > 0) {
                                        String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etUidInstanceId.setText(String.valueOf(uuid));
                                        mBind.etUidInstanceId.setSelection(mBind.etUidInstanceId.getText().length());
                                    }
                                    break;
                                case KEY_FILTER_EDDYSTONE_UID_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbUid.setChecked(enable == 1);
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
        if (!TextUtils.isEmpty(mBind.etUidNamespace.getText())) {
            final String namespace = mBind.etUidNamespace.getText().toString();
            int length = namespace.length();
            if (length % 2 != 0) return false;
        }
        if (!TextUtils.isEmpty(mBind.etUidInstanceId.getText())) {
            final String instanceId = mBind.etUidInstanceId.getText().toString();
            int length = instanceId.length();
            return length % 2 == 0;
        }
        return true;
    }

    private void saveParams() {
        final String namespace = !TextUtils.isEmpty(mBind.etUidNamespace.getText()) ? mBind.etUidNamespace.getText().toString() : null;
        final String instanceId = !TextUtils.isEmpty(mBind.etUidInstanceId.getText()) ? mBind.etUidInstanceId.getText().toString() : null;
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDNamespace(namespace));
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDInstance(instanceId));
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDEnable(mBind.cbUid.isChecked() ? 1 : 0));
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
