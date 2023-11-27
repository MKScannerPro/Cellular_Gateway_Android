package com.moko.lw006.activity.filter;

import android.os.Bundle;
import android.text.InputFilter;
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
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityFilterAdvNameBinding;
import com.moko.lw006.utils.ToastUtils;
import com.moko.support.lw006.LoRaLW006MokoSupport;
import com.moko.support.lw006.OrderTaskAssembler;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterAdvNameActivity extends Lw006BaseActivity {
    private final String FILTER_ASCII = "[ -~]*";
    private Lw006ActivityFilterAdvNameBinding mBind;
    private boolean savedParamsError;
    private ArrayList<String> filterAdvName;
    private InputFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityFilterAdvNameBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        filterAdvName = new ArrayList<>();
        filter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }
            return null;
        };
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterNamePrecise());
        orderTasks.add(OrderTaskAssembler.getFilterNameReverse());
        orderTasks.add(OrderTaskAssembler.getFilterNameRules());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_NAME_PRECISE:
                                case KEY_FILTER_NAME_REVERSE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_NAME_RULES:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(FilterAdvNameActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_NAME_PRECISE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbPreciseMatch.setChecked(enable == 1);
                                    }
                                    break;
                                case KEY_FILTER_NAME_REVERSE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbReverseFilter.setChecked(enable == 1);
                                    }
                                    break;
                                case KEY_FILTER_NAME_RULES:
                                    if (length > 0) {
                                        filterAdvName.clear();
                                        byte[] nameBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        for (int i = 0, l = nameBytes.length; i < l; ) {
                                            int nameLength = nameBytes[i] & 0xFF;
                                            i++;
                                            filterAdvName.add(new String(Arrays.copyOfRange(nameBytes, i, i + nameLength)));
                                            i += nameLength;
                                        }
                                        for (int i = 0, l = filterAdvName.size(); i < l; i++) {
                                            String advName = filterAdvName.get(i);
                                            View v = LayoutInflater.from(FilterAdvNameActivity.this).inflate(R.layout.lw006_item_adv_name_filter, mBind.llDavName, false);
                                            TextView title = v.findViewById(R.id.tv_adv_name_title);
                                            EditText etAdvName = v.findViewById(R.id.et_adv_name);
                                            etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
                                            title.setText(String.format("ADV Name%d", i + 1));
                                            etAdvName.setText(advName);
                                            etAdvName.setSelection(etAdvName.getText().length());
                                            mBind.llDavName.addView(v);
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
        if (isWindowLocked())
            return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    public void onAdd(View view) {
        if (isWindowLocked())
            return;
        int count = mBind.llDavName.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.lw006_item_adv_name_filter, mBind.llDavName, false);
        TextView title = v.findViewById(R.id.tv_adv_name_title);
        title.setText(String.format("ADV Name%d", count + 1));
        EditText etAdvName = v.findViewById(R.id.et_adv_name);
        etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
        mBind.llDavName.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked())
            return;
        final int c = mBind.llDavName.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llDavName.getChildCount();
        if (count > 0) {
            mBind.llDavName.removeViewAt(count - 1);
        }
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterNamePrecise(mBind.cbPreciseMatch.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterNameReverse(mBind.cbReverseFilter.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterNameRules(filterAdvName));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        final int c = mBind.llDavName.getChildCount();
        filterAdvName.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llDavName.getChildAt(i);
                EditText etAdvName = v.findViewById(R.id.et_adv_name);
                final String advName = etAdvName.getText().toString();
                if (TextUtils.isEmpty(advName)) {
                    return false;
                }
                int length = advName.length();
                if (length > 20) {
                    return false;
                }
                filterAdvName.add(advName);
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
}
