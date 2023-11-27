package com.moko.lw006.activity.filter;

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
import com.moko.lw006.R;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityFilterOtherBinding;
import com.moko.lw006.dialog.BottomDialog;
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

public class FilterOtherActivity extends Lw006BaseActivity {
    private Lw006ActivityFilterOtherBinding mBind;
    private boolean savedParamsError;
    private ArrayList<String> filterOther;
    private ArrayList<String> mValues;
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityFilterOtherBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);

        filterOther = new ArrayList<>();
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterOtherEnable());
        orderTasks.add(OrderTaskAssembler.getFilterOtherRelationship());
        orderTasks.add(OrderTaskAssembler.getFilterOtherRules());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                                case KEY_FILTER_OTHER_RELATIONSHIP:
                                case KEY_FILTER_OTHER_RULES:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_FILTER_OTHER_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(FilterOtherActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_OTHER_RELATIONSHIP:
                                    if (length > 0) {
                                        int relationship = value[4] & 0xFF;
                                        if (relationship < 1) {
                                            mValues = new ArrayList<>();
                                            mValues.add("A");
                                            mSelected = 0;
                                        } else if (relationship < 3) {
                                            mValues = new ArrayList<>();
                                            mValues.add("A & B");
                                            mValues.add("A | B");
                                            mSelected = relationship - 1;
                                        } else if (relationship < 6) {
                                            mValues = new ArrayList<>();
                                            mValues.add("A & B & C");
                                            mValues.add("(A & B) | C");
                                            mValues.add("A | B | C");
                                            mSelected = relationship - 3;
                                        }
                                        mBind.tvOtherRelationship.setText(mValues.get(mSelected));
                                    }
                                    break;
                                case KEY_FILTER_OTHER_RULES:
                                    if (length > 0) {
                                        filterOther.clear();
                                        byte[] otherBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        for (int i = 0, l = otherBytes.length; i < l; ) {
                                            int otherLength = otherBytes[i] & 0xFF;
                                            i++;
                                            filterOther.add(MokoUtils.bytesToHexString(Arrays.copyOfRange(otherBytes, i, i + otherLength)));
                                            i += otherLength;
                                        }
                                        for (int i = 0, l = filterOther.size(); i < l; i++) {
                                            String other = filterOther.get(i);
                                            View v = LayoutInflater.from(this).inflate(R.layout.lw006_item_other_filter, mBind.llFilterCondition, false);
                                            TextView tvCondition = v.findViewById(R.id.tv_condition);
                                            EditText etDataType = v.findViewById(R.id.et_data_type);
                                            EditText etMin = v.findViewById(R.id.et_min);
                                            EditText etMax = v.findViewById(R.id.et_max);
                                            EditText etRawData = v.findViewById(R.id.et_raw_data);
                                            if (i == 0) {
                                                tvCondition.setText("Condition A");
                                            } else if (i == 1) {
                                                tvCondition.setText("Condition B");
                                            } else {
                                                tvCondition.setText("Condition C");
                                            }
                                            String dataTypeStr = other.substring(0, 2);
                                            if ("00".equals(dataTypeStr))
                                                etDataType.setText("");
                                            else
                                                etDataType.setText(dataTypeStr);
                                            etMin.setText(String.valueOf(Integer.parseInt(other.substring(2, 4), 16)));
                                            etMax.setText(String.valueOf(Integer.parseInt(other.substring(4, 6), 16)));
                                            etRawData.setText(other.substring(6));
                                            etDataType.setSelection(etDataType.getText().length());
                                            etMin.setSelection(etMin.getText().length());
                                            etMax.setSelection(etMax.getText().length());
                                            etRawData.setSelection(etRawData.getText().length());
                                            mBind.llFilterCondition.addView(v);
                                        }
                                        if (filterOther.size() > 0)
                                            mBind.clOtherRelationship.setVisibility(View.VISIBLE);
                                    }
                                    break;

                                case KEY_FILTER_OTHER_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbOther.setChecked(enable == 1);
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
        final int count = mBind.llFilterCondition.getChildCount();
        if (count > 0) {
            // 发送设置的过滤RawData
            filterOther.clear();
            for (int i = 0; i < count; i++) {
                View v = mBind.llFilterCondition.getChildAt(i);
                EditText etDataType = v.findViewById(R.id.et_data_type);
                EditText etMin = v.findViewById(R.id.et_min);
                EditText etMax = v.findViewById(R.id.et_max);
                EditText etRawData = v.findViewById(R.id.et_raw_data);
                final String dataTypeStr = etDataType.getText().toString();
                final String minStr = etMin.getText().toString();
                final String maxStr = etMax.getText().toString();
                final String rawDataStr = etRawData.getText().toString();

                final int dataType = TextUtils.isEmpty(dataTypeStr) ? 0 : Integer.parseInt(dataTypeStr, 16);
//                final DataTypeEnum dataTypeEnum = DataTypeEnum.fromDataType(dataType);
                if (dataType < 0 || dataType > 0xFF)
                    return false;
                if (TextUtils.isEmpty(rawDataStr)) {
                    return false;
                }
                int length = rawDataStr.length();
                if (length % 2 != 0) {
                    return false;
                }
                int min = 0;
                int max = 0;
                if (dataType != 0) {
                    if (!TextUtils.isEmpty(minStr))
                        min = Integer.parseInt(minStr);
                    if (!TextUtils.isEmpty(maxStr))
                        max = Integer.parseInt(maxStr);
                    if (min == 0 && max != 0) {
                        return false;
                    }
                    if (min > 29) {
                        return false;
                    }
                    if (max > 29) {
                        return false;
                    }
                    if (max < min) {
                        return false;
                    }
                    if (min > 0) {
                        int interval = max - min;
                        if (length != ((interval + 1) * 2)) {
                            return false;
                        }
                    }
                } else {
                    etMin.setText("0");
                    etMax.setText("0");
                }
                StringBuffer sb = new StringBuffer();
                sb.append(MokoUtils.int2HexString(dataType));
                sb.append(MokoUtils.int2HexString(min));
                sb.append(MokoUtils.int2HexString(max));
                sb.append(rawDataStr);
                filterOther.add(sb.toString());
            }
        } else {
            filterOther = new ArrayList<>();
        }
        return true;
    }

    private void saveParams() {
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterOtherRules(filterOther));
        int relationship = 0;
        if (filterOther.size() == 1) {
            relationship = 0;
        }
        if (filterOther.size() == 2) {
            relationship = mSelected + 1;
        }
        if (filterOther.size() == 3) {
            relationship = mSelected + 3;
        }
        orderTasks.add(OrderTaskAssembler.setFilterOtherRelationship(relationship));
        orderTasks.add(OrderTaskAssembler.setFilterOtherEnable(mBind.cbOther.isChecked() ? 1 : 0));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onAdd(View view) {
        if (isWindowLocked()) return;
        int count = mBind.llFilterCondition.getChildCount();
        if (count > 2) {
            ToastUtils.showToast(this, "You can set up to 3 filters!");
            return;
        }
        View v = LayoutInflater.from(this).inflate(R.layout.lw006_item_other_filter, mBind.llFilterCondition, false);
        TextView tvCondition = v.findViewById(R.id.tv_condition);
        if (count == 0) {
            tvCondition.setText("Condition A");
        } else if (count == 1) {
            tvCondition.setText("Condition B");
        } else {
            tvCondition.setText("Condition C");
        }
        mBind.llFilterCondition.addView(v);
        mBind.clOtherRelationship.setVisibility(View.VISIBLE);
        mValues = new ArrayList<>();
        if (count == 0) {
            mValues.add("A");
            mSelected = 0;
        }
        if (count == 1) {
            mValues.add("A & B");
            mValues.add("A | B");
            mSelected = 1;
        }
        if (count == 2) {
            mValues.add("A & B & C");
            mValues.add("(A & B) | C");
            mValues.add("A | B | C");
            mSelected = 2;
        }
        mBind.tvOtherRelationship.setText(mValues.get(mSelected));
    }

    public void onDel(View view) {
        if (isWindowLocked()) return;
        final int c = mBind.llFilterCondition.getChildCount();
        if (c == 0) {
            ToastUtils.showToast(this, "There are currently no filters to delete");
            return;
        }
        int count = mBind.llFilterCondition.getChildCount();
        if (count > 0) {
            mBind.llFilterCondition.removeViewAt(count - 1);
            mValues = new ArrayList<>();
            if (count == 1) {
                mBind.clOtherRelationship.setVisibility(View.GONE);
                return;
            }
            if (count == 2) {
                mValues.add("A");
                mSelected = 0;
            }
            if (count == 3) {
                mValues.add("A & B");
                mValues.add("A | B");
                mSelected = 1;
            }
            mBind.tvOtherRelationship.setText(mValues.get(mSelected));
        }
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

    public void onOtherRelationship(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvOtherRelationship.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }
}
