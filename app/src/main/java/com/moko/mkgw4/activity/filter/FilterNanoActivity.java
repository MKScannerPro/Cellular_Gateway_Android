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
import com.moko.lib.scannerui.dialog.BottomDialog;
import com.moko.lib.scannerui.utils.ToastUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterNanoMkgw4Binding;
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

public class FilterNanoActivity extends BaseActivity {
    private ActivityFilterNanoMkgw4Binding mBind;
    private boolean savedParamsError;
    private final List<String> filterNano = new ArrayList<>();

    private final String[] mTriggerType = {"All", "Normal type", "Trigger type"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterNanoMkgw4Binding.inflate(getLayoutInflater());
        EventBus.getDefault().register(this);
        setContentView(mBind.getRoot());

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(1);
        orderTasks.add(OrderTaskAssembler.getFilterNanoRules());
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
                            if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_NANO_RULES) {
                                if (result != 1) savedParamsError = true;
                                ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                            }
                        } else if (flag == 0x00) {
                            // read
                            if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_NANO_RULES) {
                                if (length > 0) {
                                    int enable = value[4];
                                    mBind.cbNano.setChecked(enable == 1);
                                    int type = value[5] & 0xFF;
                                    mBind.tvTriggerType.setTag(type);
                                    mBind.tvTriggerType.setText(mTriggerType[type]);
                                    filterNano.clear();
                                    byte[] bytes = Arrays.copyOfRange(value, 6, 4 + length);
                                    int index = 0;
                                    for (int i = 0; i < bytes.length / 2; i++) {
                                        //0  2;2  4;4  6
                                        filterNano.add(MokoUtils.bytesToHexString(Arrays.copyOfRange(bytes, index, index + 2)));
                                        index += 2;
                                    }
                                    for (int i = 0, l = filterNano.size(); i < l; i++) {
                                        String id = filterNano.get(i);
                                        View v = LayoutInflater.from(this).inflate(R.layout.item_nano_filter_mkgw4, mBind.llId, false);
                                        TextView title = v.findViewById(R.id.tvTitle);
                                        EditText etId = v.findViewById(R.id.etId);
                                        title.setText(String.format(Locale.getDefault(), "ID %d", i + 1));
                                        etId.setText(id);
                                        etId.setSelection(etId.getText().length());
                                        mBind.llId.addView(v);
                                    }
                                }
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


    public void onTriggerType(View view) {
        if (isWindowLocked()) return;
        int selected = (int) view.getTag();
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(new ArrayList<>(Arrays.asList(mTriggerType)), selected);
        dialog.setListener(value -> {
            view.setTag(value);
            mBind.tvTriggerType.setText(mTriggerType[value]);
        });
        dialog.show(getSupportFragmentManager());
    }

    private boolean isValid() {
        final int c = mBind.llId.getChildCount();
        filterNano.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llId.getChildAt(i);
                EditText etId = v.findViewById(R.id.etId);
                final String id = etId.getText().toString();
                if (TextUtils.isEmpty(id)) return false;
                int length = id.length();
                if (length != 4) return false;
                filterNano.add(id);
            }
        }
        return true;
    }

    private void saveParams() {
        savedParamsError = false;
        int type = (int) mBind.tvTriggerType.getTag();
        List<OrderTask> orderTasks = new ArrayList<>(1);
        orderTasks.add(OrderTaskAssembler.setFilterNanoRules(mBind.cbNano.isChecked() ? 1 : 0, type, filterNano));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llId.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.item_nano_filter_mkgw4, mBind.llId, false);
        TextView title = v.findViewById(R.id.tvTitle);
        title.setText(String.format(Locale.getDefault(), "ID %d", count + 1));
        mBind.llId.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
        final int c = mBind.llId.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llId.getChildCount();
        if (count > 0) mBind.llId.removeViewAt(count - 1);
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
