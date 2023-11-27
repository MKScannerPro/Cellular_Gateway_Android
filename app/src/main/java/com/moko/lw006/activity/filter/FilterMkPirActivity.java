package com.moko.lw006.activity.filter;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.activity.Lw006BaseActivity;
import com.moko.lw006.databinding.Lw006ActivityFilterMkpirBinding;
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

/**
 * @author: jun.liu
 * @date: 2023/6/7 10:43
 * @des:
 */
public class FilterMkPirActivity extends Lw006BaseActivity {
    private Lw006ActivityFilterMkpirBinding mBind;
    private final String[] detectionStatusArray = {"No motion detected", "Motion detected", "All"};
    private final String[] sensorSensitivityArray = {"Low", "Medium", "High", "All"};
    private final String[] doorStatusArray = {"Close", "Open", "All"};
    private final String[] delayResStatusArray = {"Low delay", "Medium delay", "High delay", "All"};
    private int detectionStatusIndex;
    private int sensorSensitivityIndex;
    private int doorStatusIndex;
    private int delayResStatusIndex;
    private int mkPirEnableFlag;
    private int detectionStatusFlag;
    private int sensorSensitivityFlag;
    private int doorStatusFlag;
    private int delayResStatusFlag;
    private int majorFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityFilterMkpirBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.getMkPirEnable());
        orderTasks.add(OrderTaskAssembler.getMkPirSensorDetectionStatus());
        orderTasks.add(OrderTaskAssembler.getMkPirSensorSensitivity());
        orderTasks.add(OrderTaskAssembler.getMkPirDoorStatus());
        orderTasks.add(OrderTaskAssembler.getMkPirDelayResStatus());
        orderTasks.add(OrderTaskAssembler.getMkPirMajor());
        orderTasks.add(OrderTaskAssembler.getMkPirMinor());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        setListener();
    }

    private void setListener() {
        mBind.tvDetectionStatus.setOnClickListener(v -> showBottomDialog(new ArrayList<>(Arrays.asList(detectionStatusArray)),
                detectionStatusIndex, mBind.tvDetectionStatus, 1));
        mBind.tvSensorSensitivity.setOnClickListener(v -> showBottomDialog(new ArrayList<>(Arrays.asList(sensorSensitivityArray)),
                sensorSensitivityIndex, mBind.tvSensorSensitivity, 2));
        mBind.tvDoorStatus.setOnClickListener(v -> showBottomDialog(new ArrayList<>(Arrays.asList(doorStatusArray)),
                doorStatusIndex, mBind.tvDoorStatus, 3));
        mBind.tvDelayResStatus.setOnClickListener(v -> showBottomDialog(new ArrayList<>(Arrays.asList(delayResStatusArray)),
                delayResStatusIndex, mBind.tvDelayResStatus, 4));
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
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length >= 4) {
                        int header = value[0] & 0xFF;// 0xED
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        if (header != 0xED) return;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_FILTER_MK_PIR_ENABLE:
                                    mkPirEnableFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_DETECTION_STATUS:
                                    detectionStatusFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY:
                                    sensorSensitivityFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_DOOR_STATUS:
                                    doorStatusFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_DELAY_RES_STATUS:
                                    delayResStatusFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_MAJOR:
                                    majorFlag = result;
                                    break;

                                case KEY_FILTER_MK_PIR_MINOR:
                                    if (mkPirEnableFlag == 1 && detectionStatusFlag == 1 && sensorSensitivityFlag == 1 &&
                                            doorStatusFlag == 1 && delayResStatusFlag == 1 && majorFlag == 1 && result == 1) {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    } else {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_FILTER_MK_PIR_ENABLE:
                                    if (length == 1) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbMkPir.setChecked(enable == 1);
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_DETECTION_STATUS:
                                    if (length == 1) {
                                        detectionStatusIndex = value[4] & 0xff;
                                        mBind.tvDetectionStatus.setText(detectionStatusArray[detectionStatusIndex]);
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY:
                                    if (length == 1) {
                                        sensorSensitivityIndex = value[4] & 0xff;
                                        mBind.tvSensorSensitivity.setText(sensorSensitivityArray[sensorSensitivityIndex]);
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_DOOR_STATUS:
                                    if (length == 1) {
                                        doorStatusIndex = value[4] & 0xff;
                                        mBind.tvDoorStatus.setText(doorStatusArray[doorStatusIndex]);
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_DELAY_RES_STATUS:
                                    if (length == 1) {
                                        delayResStatusIndex = value[4] & 0xff;
                                        mBind.tvDelayResStatus.setText(delayResStatusArray[delayResStatusIndex]);
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_MAJOR:
                                    if (length == 4) {
                                        int majorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        int majorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        mBind.etMajorMin.setText(String.valueOf(majorMin));
                                        mBind.etMajorMax.setText(String.valueOf(majorMax));
                                        mBind.etMajorMin.setSelection(mBind.etMajorMin.getText().length());
                                        mBind.etMajorMax.setSelection(mBind.etMajorMax.getText().length());
                                    }
                                    break;

                                case KEY_FILTER_MK_PIR_MINOR:
                                    if (length == 4) {
                                        int minorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        int minorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                        mBind.etMinorMin.setText(String.valueOf(minorMin));
                                        mBind.etMinorMax.setText(String.valueOf(minorMax));
                                        mBind.etMinorMin.setSelection(mBind.etMinorMin.getText().length());
                                        mBind.etMinorMax.setSelection(mBind.etMinorMax.getText().length());
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

    private void saveParams() {
        int majorMin;
        int majorMax;
        int minorMin;
        int minorMax;
        if (TextUtils.isEmpty(mBind.etMajorMin.getText()) && TextUtils.isEmpty(mBind.etMajorMax.getText())) {
            majorMin = 0;
            majorMax = 0xffff;
        } else {
            majorMin = Integer.parseInt(mBind.etMajorMin.getText().toString());
            majorMax = Integer.parseInt(mBind.etMajorMax.getText().toString());
        }

        if (TextUtils.isEmpty(mBind.etMinorMin.getText()) && TextUtils.isEmpty(mBind.etMinorMax.getText())) {
            minorMin = 0;
            minorMax = 0xffff;
        } else {
            minorMin = Integer.parseInt(mBind.etMinorMin.getText().toString());
            minorMax = Integer.parseInt(mBind.etMinorMax.getText().toString());
        }
        List<OrderTask> orderTasks = new ArrayList<>(8);
        orderTasks.add(OrderTaskAssembler.setFilterMkPirEnable(mBind.cbMkPir.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirSensorDetectionStatus(detectionStatusIndex));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirSensorSensitivity(sensorSensitivityIndex));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirDoorStatus(doorStatusIndex));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirDelayResStatus(delayResStatusIndex));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirMajorRange(majorMin, majorMax));
        orderTasks.add(OrderTaskAssembler.setFilterMkPirMinorRange(minorMin, minorMax));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void showBottomDialog(ArrayList<String> mValues, int mSelected, TextView textView, int type) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            textView.setText(mValues.get(value));
            if (type == 1) detectionStatusIndex = value;
            else if (type == 2) sensorSensitivityIndex = value;
            else if (type == 3) doorStatusIndex = value;
            else if (type == 4) delayResStatusIndex = value;
        });
        dialog.show(getSupportFragmentManager());
    }

    private boolean isValid() {
        if (!TextUtils.isEmpty(mBind.etMajorMin.getText()) && !TextUtils.isEmpty(mBind.etMajorMax.getText())) {
            final String majorMin = mBind.etMajorMin.getText().toString();
            final String majorMax = mBind.etMajorMax.getText().toString();
            if (Integer.parseInt(majorMin) > 65535) return false;
            if (Integer.parseInt(majorMax) > 65535) return false;
            if (Integer.parseInt(majorMax) < Integer.parseInt(majorMin)) return false;
        } else if (!TextUtils.isEmpty(mBind.etMajorMin.getText()) && TextUtils.isEmpty(mBind.etMajorMax.getText())) {
            return false;
        } else if (TextUtils.isEmpty(mBind.etMajorMin.getText()) && !TextUtils.isEmpty(mBind.etMajorMax.getText())) {
            return false;
        }
        if (!TextUtils.isEmpty(mBind.etMinorMin.getText()) && !TextUtils.isEmpty(mBind.etMinorMax.getText())) {
            final String minorMin = mBind.etMinorMin.getText().toString();
            final String minorMax = mBind.etMinorMax.getText().toString();
            if (Integer.parseInt(minorMin) > 65535) return false;
            if (Integer.parseInt(minorMax) > 65535) return false;
            return Integer.parseInt(minorMax) >= Integer.parseInt(minorMin);
        } else if (!TextUtils.isEmpty(mBind.etMinorMin.getText()) && TextUtils.isEmpty(mBind.etMinorMax.getText())) {
            return false;
        } else
            return !TextUtils.isEmpty(mBind.etMinorMin.getText()) || TextUtils.isEmpty(mBind.etMinorMax.getText());
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
