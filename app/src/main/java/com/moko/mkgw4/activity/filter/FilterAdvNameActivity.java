package com.moko.mkgw4.activity.filter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.R;
import com.moko.mkgw4.activity.BaseActivity;
import com.moko.mkgw4.databinding.ActivityFilterAdvNameBinding;
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

public class FilterAdvNameActivity extends BaseActivity {
    private final String FILTER_ASCII = "[ -~]*";
    private ActivityFilterAdvNameBinding mBind;
    private boolean savedParamsError;
    private ArrayList<String> filterAdvName;
    private InputFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityFilterAdvNameBinding.inflate(getLayoutInflater());
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
                        int header = value[0] & 0xff;
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        if (header == 0xEE) {
                            if (flag == 0x01) {
                                if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_NAME_RULES) {
                                    if ((value[4] & 0xff) != 1) savedParamsError = true;
                                }
                            } else if (flag == 0x00) {
                                int length = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                                if (configKeyEnum == ParamsKeyEnum.KEY_FILTER_NAME_RULES) {
                                    if (length > 0) {
                                        filterAdvName.clear();
                                        byte[] nameBytes = Arrays.copyOfRange(value, 5, 5 + length);
                                        for (int i = 0, l = nameBytes.length; i < l; ) {
                                            int nameLength = nameBytes[i] & 0xFF;
                                            i++;
                                            filterAdvName.add(new String(Arrays.copyOfRange(nameBytes, i, i + nameLength)));
                                            i += nameLength;
                                        }
                                        for (int i = 0, l = filterAdvName.size(); i < l; i++) {
                                            String advName = filterAdvName.get(i);
                                            View v = LayoutInflater.from(this).inflate(R.layout.ps101m_item_adv_name_filter, mBind.llDavName, false);
                                            TextView title = v.findViewById(R.id.tv_adv_name_title);
                                            EditText etAdvName = v.findViewById(R.id.et_adv_name);
                                            etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
                                            title.setText(String.format("ADV Name%d", i + 1));
                                            etAdvName.setText(advName);
                                            etAdvName.setSelection(etAdvName.getText().length());
                                            mBind.llDavName.addView(v);
                                        }
                                    }
                                }
                            }
                        } else if (header == 0xED) {
                            int length = value[3] & 0xff;
                            if (flag == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_FILTER_NAME_PRECISE:
                                        break;
                                    case KEY_FILTER_NAME_REVERSE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        ToastUtils.showToast(this, !savedParamsError ? "Setup succeed" : "Setup failed");
                                        break;
                                }
                            } else if (flag == 0x00) {
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

    @SuppressLint("DefaultLocale")
    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llDavName.getChildCount();
        if (count > 9) {
            ToastUtils.showToast(this, "You can set up to 10 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.ps101m_item_adv_name_filter, mBind.llDavName, false);
        TextView title = v.findViewById(R.id.tv_adv_name_title);
        title.setText(String.format("ADV Name%d", count + 1));
        EditText etAdvName = v.findViewById(R.id.et_adv_name);
        etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
        mBind.llDavName.addView(v);
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
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
        orderTasks.add(OrderTaskAssembler.setFilterNameRules(filterAdvName));
        orderTasks.add(OrderTaskAssembler.setFilterNamePrecise(mBind.cbPreciseMatch.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterNameReverse(mBind.cbReverseFilter.isChecked() ? 1 : 0));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        final int c = mBind.llDavName.getChildCount();
        filterAdvName.clear();
        if (c > 0) {
            for (int i = 0; i < c; i++) {
                View v = mBind.llDavName.getChildAt(i);
                EditText etAdvName = v.findViewById(R.id.et_adv_name);
                final String advName = etAdvName.getText().toString();
                if (TextUtils.isEmpty(advName)) return false;
                int length = advName.length();
                if (length > 20) return false;
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
        finish();
    }
}
