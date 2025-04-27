package com.moko.mkgw4.activity.payload;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.adapter.OtherPayloadAdapter;
import com.moko.mkgw4.databinding.ActivityOtherPayloadMkgw4Binding;
import com.moko.mkgw4.entity.OtherTypePayloadBean;
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
 * @date: 2023/12/1 12:19
 * @des:
 */
public class OtherPayloadActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    private ActivityOtherPayloadMkgw4Binding mBind;
    private boolean isParamsError;
    private final List<OtherTypePayloadBean> otherData = new ArrayList<>();
    private OtherPayloadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityOtherPayloadMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(4);
        orderTasks.add(OrderTaskAssembler.getOtherPayload());
        orderTasks.add(OrderTaskAssembler.getOtherPayloadData());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        mBind.tvBack.setOnClickListener(v -> finish());
        mBind.ivSave.setOnClickListener(v -> onSave());
        adapter = new OtherPayloadAdapter();
        mBind.rvList.setAdapter(adapter);
        adapter.replaceData(otherData);
        adapter.setOnItemChildClickListener(this);
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
                            int result = value[4] & 0xff;
                            switch (configKeyEnum) {
                                case KEY_OTHER_PAYLOAD:
                                    if (result != 1) isParamsError = true;
                                    break;

                                case KEY_OTHER_PAYLOAD_DATA:
                                    if (result != 1) isParamsError = true;
                                    ToastUtils.showToast(this, !isParamsError ? "Setup succeed" : "Setup failed");
                                    break;
                            }
                        } else if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_OTHER_PAYLOAD:
                                    if (length > 0) {
                                        int data = value[4] & 0xff;
                                        mBind.cbRssi.setChecked((data & 0x01) == 1);
                                        mBind.cbTimestamp.setChecked((data >> 1 & 0x01) == 1);
                                        mBind.cbRawDataAdv.setChecked((data >> 2 & 0x01) == 1);
                                        mBind.cbRawDataRes.setChecked((data >> 3 & 0x01) == 1);
                                    }
                                    break;

                                case KEY_OTHER_PAYLOAD_DATA:
                                    if (length > 0) {
                                        otherData.clear();
                                        byte[] bytes = Arrays.copyOfRange(value, 4, length + 4);
                                        int index = 0;
                                        for (int i = 0; i < bytes.length / 3; i++) {
                                            otherData.add(new OtherTypePayloadBean(MokoUtils.bytesToHexString(Arrays.copyOfRange(bytes, index, index + 3))));
                                            index += 3;//3  6
                                        }
                                        adapter.replaceData(otherData);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onAdd(View view) {
        if (isWindowLocked()) return;
        if (otherData.size() > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        otherData.add(new OtherTypePayloadBean("000000"));
        adapter.addData(otherData.size() - 1, otherData.get(otherData.size() - 1));
    }

    private final List<String> payloadOther = new ArrayList<>();

    private void onSave() {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            isParamsError = false;
            int payload = (mBind.cbRssi.isChecked() ? 1 : 0) | (mBind.cbTimestamp.isChecked() ? 1 << 1 : 0) |
                    (mBind.cbRawDataAdv.isChecked() ? 1 << 2 : 0) | (mBind.cbRawDataRes.isChecked() ? 1 << 3 : 0);
            List<OrderTask> orderTasks = new ArrayList<>(4);
            orderTasks.add(OrderTaskAssembler.setOtherPayload(payload));
            orderTasks.add(OrderTaskAssembler.setOtherPayloadData(payloadOther));
            orderTasks.add(OrderTaskAssembler.getOtherPayloadData());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        } else {
            ToastUtils.showToast(this, "Parameter Error");
        }
    }

    private boolean isValid() {
        final int count = otherData.size();
        if (count > 0) {
            // 发送设置的过滤RawData
            payloadOther.clear();
            for (int i = 0; i < count; i++) {
                EditText etDataType = (EditText) adapter.getViewByPosition(mBind.rvList, i, R.id.et_data_type);
                EditText etMin = (EditText) adapter.getViewByPosition(mBind.rvList, i, R.id.et_min);
                EditText etMax = (EditText) adapter.getViewByPosition(mBind.rvList, i, R.id.et_max);
                final String dataTypeStr = !TextUtils.isEmpty(etDataType.getText()) ? etDataType.getText().toString() : null;
                final String minStr = !TextUtils.isEmpty(etMin.getText()) ? etMin.getText().toString() : null;
                final String maxStr = !TextUtils.isEmpty(etMax.getText()) ? etMax.getText().toString() : null;
                if (TextUtils.isEmpty(minStr) || TextUtils.isEmpty(maxStr)) return false;
                final int dataType = TextUtils.isEmpty(dataTypeStr) ? 0 : Integer.parseInt(dataTypeStr, 16);
                if (dataType < 0 || dataType > 0xFF) return false;
                int min = 0;
                int max = 0;
                if (!TextUtils.isEmpty(minStr))
                    min = Integer.parseInt(minStr);
                if (!TextUtils.isEmpty(maxStr))
                    max = Integer.parseInt(maxStr);
                if (min < 1 || max < 1) return false;
                if (min > 29 || max > 29) return false;
                if (max < min) return false;
                String sb = MokoUtils.int2HexString(dataType) +
                        MokoUtils.int2HexString(min) +
                        MokoUtils.int2HexString(max);
                payloadOther.add(sb);
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        otherData.remove(position);
        this.adapter.remove(position);
//        this.adapter.replaceData(otherData);
    }
}
