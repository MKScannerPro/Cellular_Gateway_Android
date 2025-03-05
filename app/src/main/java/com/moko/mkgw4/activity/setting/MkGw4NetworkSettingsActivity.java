package com.moko.mkgw4.activity.setting;

import static com.moko.mkgw4.AppConstants.TYPE_USB;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.mkgw4.AppConstants;
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
public class MkGw4NetworkSettingsActivity extends MkGw4BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private ActivityNetworkSettingsMkgw4Binding mBind;
    private final String FILTER_ASCII = "[ -~]*";
    private boolean mSavedParamsError;
    private int netPrioritySelect;
    private int cellularType;
    private final String[] netPriorityValue = {"eMTC->NB-IOT->GSM", "eMTC-> GSM -> NB-IOT", "NB-IOT->GSM-> eMTC", "NB-IOT-> eMTC-> GSM",
            "GSM -> NB-IOT-> eMTC", "GSM -> eMTC->NB-IOT", "eMTC->NB-IOT", "NB-IOT-> eMTC", "GSM", "NB-IOT", "eMTC"};
    private int deviceType;
    private final List<CompoundButton> checkList = new ArrayList<>(3);
    private boolean hasCheckFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityNetworkSettingsMkgw4Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        InputFilter inputFilter = (source, start, end, dest, dStart, dEnd) -> {
            if (!(source + "").matches(FILTER_ASCII)) return "";
            return null;
        };
        cellularType = getIntent().getIntExtra("cellularType", 0);
        deviceType = getIntent().getIntExtra(AppConstants.DEVICE_TYPE, 0);
        mBind.netGroup.setVisibility(cellularType == 0 ? View.VISIBLE : View.GONE);
        mBind.etApn.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100), inputFilter});
        mBind.etUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100), inputFilter});
        mBind.etPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100), inputFilter});
        if (deviceType == TYPE_USB) mBind.groupBand.setVisibility(View.VISIBLE);

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        if (cellularType == 0) orderTasks.add(OrderTaskAssembler.getNetworkPriority());
        orderTasks.add(OrderTaskAssembler.getApn());
        orderTasks.add(OrderTaskAssembler.getApnUsername());
        orderTasks.add(OrderTaskAssembler.getApnPassword());
        if (deviceType == TYPE_USB) {
            orderTasks.add(OrderTaskAssembler.getPin());
            orderTasks.add(OrderTaskAssembler.getRegion());
        }
        orderTasks.add(OrderTaskAssembler.getNetworkConnectTimeout());

        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[0]));
        mBind.tvNetworkPriority.setOnClickListener(v -> onNetPriorityClick());
        if (deviceType == TYPE_USB) {
            mBind.checkUs.setOnCheckedChangeListener(this);
            mBind.checkEurope.setOnCheckedChangeListener(this);
            mBind.checkKorea.setOnCheckedChangeListener(this);
            mBind.checkAustralia.setOnCheckedChangeListener(this);
            mBind.checkMiddleEast.setOnCheckedChangeListener(this);
            mBind.checkJapan.setOnCheckedChangeListener(this);
            mBind.checkChina.setOnCheckedChangeListener(this);
            mBind.checkAll.setOnCheckedChangeListener(this);
        }
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
                                case KEY_PIN:
                                case KEY_REGION:
                                case KEY_APN:
                                case KEY_APN_NAME:
                                case KEY_APN_PASSWORD:
                                    if (result != 1) mSavedParamsError = true;
                                    break;
                                case KEY_CONNECT_NETWORK_TIMEOUT:
                                    if (result != 1) mSavedParamsError = true;
                                    ToastUtils.showToast(this, mSavedParamsError ? "Setup failed！" : "Setup succeed！");
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
                                case KEY_PIN:
                                    mBind.etPin.setText(new String(Arrays.copyOfRange(value, 4, value.length)));
                                    mBind.etPin.setSelection(mBind.etPin.getText().length());
                                    break;
                                case KEY_REGION:
                                    int result = value[4] & 0xff;
                                    if ((result & 0x01) == 1) mBind.checkUs.setChecked(true);
                                    if ((result >> 1 & 0x01) == 1)
                                        mBind.checkEurope.setChecked(true);
                                    if ((result >> 2 & 0x01) == 1)
                                        mBind.checkKorea.setChecked(true);
                                    if ((result >> 3 & 0x01) == 1)
                                        mBind.checkAustralia.setChecked(true);
                                    if ((result >> 4 & 0x01) == 1)
                                        mBind.checkMiddleEast.setChecked(true);
                                    if ((result >> 5 & 0x01) == 1)
                                        mBind.checkJapan.setChecked(true);
                                    if ((result >> 6 & 0x01) == 1)
                                        mBind.checkChina.setChecked(true);
                                    if ((result >> 7 & 0x01) == 1) mBind.checkAll.setChecked(true);
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

    private void back() {
        EventBus.getDefault().unregister(this);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (isValid()) {
            showSyncingProgressDialog();
            mSavedParamsError = false;
            List<OrderTask> orderTasks = new ArrayList<>(8);
            if (cellularType == 0)
                orderTasks.add(OrderTaskAssembler.setNetworkPriority(netPrioritySelect));
            if (deviceType == TYPE_USB) {
                String pin = TextUtils.isEmpty(mBind.etPin.getText()) ? null : mBind.etPin.getText().toString();
                orderTasks.add(OrderTaskAssembler.setPin(pin));
                //频段设置
                orderTasks.add(OrderTaskAssembler.setRegion(getRegion()));
            }
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

    private int getRegion() {
        if (mBind.checkAll.isChecked()) return 128;
        //没有选择全部
        String[] array = {"0", "0", "0", "0", "0", "0", "0", "0"};
        if (mBind.checkUs.isChecked()) array[7] = "1";
        if (mBind.checkEurope.isChecked()) array[6] = "1";
        if (mBind.checkKorea.isChecked()) array[5] = "1";
        if (mBind.checkAustralia.isChecked()) array[4] = "1";
        if (mBind.checkMiddleEast.isChecked()) array[3] = "1";
        if (mBind.checkJapan.isChecked()) array[2] = "1";
        if (mBind.checkChina.isChecked()) array[1] = "1";
        StringBuilder result = new StringBuilder();
        for (String str : array) {
            result.append(str);
        }
        return Integer.parseInt(result.toString(), 2);
    }

    private boolean isValid() {
        if (deviceType == TYPE_USB) {
            if (!TextUtils.isEmpty(mBind.etPin.getText())) {
                int length = mBind.etPin.getText().length();
                if (length < 4 || length > 8) return false;
            }
            //设置频段地区
            if (!mBind.checkAll.isChecked() && checkList.isEmpty()) return false;
        }
        if (TextUtils.isEmpty(mBind.etConnectTimeout.getText())) return false;
        int timeout = Integer.parseInt(mBind.etConnectTimeout.getText().toString().trim());
        return timeout >= 30 && timeout <= 600;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (mBind.checkAll.isChecked() && !buttonView.getText().equals("All of them")) {
                mBind.checkAll.setChecked(false);
            }
            if (mBind.checkAll.isChecked()) {
                if (!checkList.isEmpty()) {
                    hasCheckFull = true;
                    for (CompoundButton view : checkList) {
                        view.setChecked(false);
                    }
                    checkList.clear();
                    hasCheckFull = false;
                }
            } else {
                checkList.add(buttonView);
                if (checkList.size() == 3) {
                    hasCheckFull = true;
                    checkList.get(0).setChecked(false);
                    checkList.remove(0);
                    hasCheckFull = false;
                }
            }
        } else {
            if (!checkList.isEmpty() && !hasCheckFull) {
                for (int i = 0; i < checkList.size(); i++) {
                    if (buttonView.getText().equals(checkList.get(i).getText())) {
                        //这里只会移除一个，所以不存在循环移除的错误风险
                        checkList.remove(i);
                        break;
                    }
                }
            }
        }
    }
}
