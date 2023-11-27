package com.moko.lw006.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.databinding.Lw006ActivityPosGpsLr1110Binding;
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

public class PosGpsLR1110FixActivity extends Lw006BaseActivity {
    private Lw006ActivityPosGpsLr1110Binding mBind;
    private boolean savedParamsError;
    private ArrayList<String> mValues;
    private int mSelected;
    private ArrayList<String> mGpsPosSystemValues;
    private int mGpsPosSystemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw006ActivityPosGpsLr1110Binding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mValues = new ArrayList<>();
        mValues.add("DAS");
        mValues.add("Customer");
        mGpsPosSystemValues = new ArrayList<>();
        mGpsPosSystemValues.add("GPS");
        mGpsPosSystemValues.add("Beidou");
        mGpsPosSystemValues.add("GPS&Beidou");
        mBind.cbAutonomousAiding.setOnCheckedChangeListener((buttonView, isChecked) -> mBind.clAutonomousParams.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getGPSPosTimeout());
        orderTasks.add(OrderTaskAssembler.getGPSPosSatelliteThreshold());
        orderTasks.add(OrderTaskAssembler.getGPSPosDataType());
        orderTasks.add(OrderTaskAssembler.getGPSPosSystem());
        orderTasks.add(OrderTaskAssembler.getGPSPosAutoEnable());
        orderTasks.add(OrderTaskAssembler.getGPSPosAuxiliaryLatLon());
        orderTasks.add(OrderTaskAssembler.getGPSPosEphemerisNotifyEnable());
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
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
                        if (configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_GPS_POS_TIMEOUT:
                                case KEY_GPS_POS_SATELLITE_THRESHOLD:
                                case KEY_GPS_POS_SYSTEM:
                                case KEY_GPS_POS_DATA_TYPE:
                                case KEY_GPS_POS_AUTONMOUS_AIDING_ENABLE:
                                case KEY_GPS_POS_AUXILIARY_LAT_LON:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    break;
                                case KEY_GPS_POS_EPHEMERIS_NOTIFY_ENABLE:
                                    if (result != 1) {
                                        savedParamsError = true;
                                    }
                                    if (savedParamsError) {
                                        ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                    } else {
                                        ToastUtils.showToast(this, "Save Successfully！");
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x00) {
                            // read
                            switch (configKeyEnum) {
                                case KEY_GPS_POS_TIMEOUT:
                                    if (length > 0) {
                                        int timeout = value[4] & 0xFF;
                                        mBind.etPosTimeout.setText(String.valueOf(timeout));
                                        mBind.etPosTimeout.setSelection(mBind.etPosTimeout.getText().length());
                                    }
                                    break;
                                case KEY_GPS_POS_SATELLITE_THRESHOLD:
                                    if (length > 0) {
                                        int threshold = value[4] & 0xFF;
                                        mBind.etSatelliteThreshold.setText(String.valueOf(threshold));
                                        mBind.etSatelliteThreshold.setSelection(mBind.etSatelliteThreshold.getText().length());
                                    }
                                    break;
                                case KEY_GPS_POS_DATA_TYPE:
                                    if (length > 0) {
                                        mSelected = value[4] & 0xFF;
                                        mBind.tvGpsDataType.setText(mValues.get(mSelected));
                                    }
                                    break;
                                case KEY_GPS_POS_SYSTEM:
                                    if (length > 0) {
                                        mGpsPosSystemSelected = value[4] & 0xFF;
                                        mBind.tvGpsPosSystem.setText(mGpsPosSystemValues.get(mGpsPosSystemSelected));
                                    }
                                    break;
                                case KEY_GPS_POS_AUTONMOUS_AIDING_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbAutonomousAiding.setChecked(enable == 0);
                                        mBind.clAutonomousParams.setVisibility(enable == 0 ? View.VISIBLE : View.GONE);
                                    }
                                    break;
                                case KEY_GPS_POS_AUXILIARY_LAT_LON:
                                    if (length == 8) {
                                        byte[] latBytes = Arrays.copyOfRange(value, 4, 8);
                                        int lat = MokoUtils.toIntSigned(latBytes);
                                        byte[] lonBytes = Arrays.copyOfRange(value, 8, 12);
                                        int lon = MokoUtils.toIntSigned(lonBytes);
                                        mBind.etAutonomousLat.setText(String.valueOf(lat));
                                        mBind.etAutonomousLon.setText(String.valueOf(lon));
                                        mBind.etAutonomousLat.setSelection(mBind.etAutonomousLat.getText().length());
                                        mBind.etAutonomousLon.setSelection(mBind.etAutonomousLon.getText().length());
                                    }
                                    break;
                                case KEY_GPS_POS_EPHEMERIS_NOTIFY_ENABLE:
                                    if (length > 0) {
                                        int enable = value[4] & 0xFF;
                                        mBind.cbEphemerisStartNotify.setChecked((enable & 0x01) == 1);
                                        mBind.cbEphemerisEndNotify.setChecked((enable >> 1 & 0x01) == 1);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void onGPSDataType(View view) {
        if (isWindowLocked()) return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            mBind.tvGpsDataType.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onGPSPosSystem(View view) {
        if (isWindowLocked())
            return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mGpsPosSystemValues, mGpsPosSystemSelected);
        dialog.setListener(value -> {
            mGpsPosSystemSelected = value;
            mBind.tvGpsPosSystem.setText(mGpsPosSystemValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
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
        if (TextUtils.isEmpty(mBind.etPosTimeout.getText())) return false;
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        if (posTimeout < 5 || posTimeout > 50) {
            return false;
        }
        if (TextUtils.isEmpty(mBind.etSatelliteThreshold.getText())) return false;
        final String thresholdStr = mBind.etSatelliteThreshold.getText().toString();
        final int threshold = Integer.parseInt(thresholdStr);
        if (threshold < 4 || threshold > 10) {
            return false;
        }
        if (!mBind.cbAutonomousAiding.isChecked()) return true;
        if (TextUtils.isEmpty(mBind.etAutonomousLat.getText())) return false;
        final String latStr = mBind.etAutonomousLat.getText().toString();
        final int lat = Integer.parseInt(latStr);
        if (lat < -9000000 || lat > 9000000) {
            return false;
        }
        if (TextUtils.isEmpty(mBind.etAutonomousLon.getText())) return false;
        final String lonStr = mBind.etAutonomousLon.getText().toString();
        final int lon = Integer.parseInt(lonStr);
        return lon >= -18000000 && lon <= 18000000;
    }

    private void saveParams() {
        final String posTimeoutStr = mBind.etPosTimeout.getText().toString();
        final int posTimeout = Integer.parseInt(posTimeoutStr);
        final String thresholdStr = mBind.etSatelliteThreshold.getText().toString();
        final int threshold = Integer.parseInt(thresholdStr);
        final String latStr = mBind.etAutonomousLat.getText().toString();
        final int lat = Integer.parseInt(latStr);
        final String lonStr = mBind.etAutonomousLon.getText().toString();
        final int lon = Integer.parseInt(lonStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setGPSPosTimeout(posTimeout));
        orderTasks.add(OrderTaskAssembler.setGPSPosSatelliteThreshold(threshold));
        orderTasks.add(OrderTaskAssembler.setGPSPosDataType(mSelected));
        orderTasks.add(OrderTaskAssembler.setGPSPosSystem(mGpsPosSystemSelected));
        orderTasks.add(OrderTaskAssembler.setGPSPosAutonmousAidingEnable(mBind.cbAutonomousAiding.isChecked() ? 0 : 1));
        orderTasks.add(OrderTaskAssembler.setGPSPosAuxiliaryLatLon(lat, lon));
        int type = (mBind.cbEphemerisStartNotify.isChecked() ? 1 : 0) | (mBind.cbEphemerisEndNotify.isChecked() ? 2 : 0);
        orderTasks.add(OrderTaskAssembler.setGPSPosEphemerisNotifyEnable(type));
        LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
