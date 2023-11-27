package com.moko.support.lw006;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleLib;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw006.entity.ExportData;
import com.moko.support.lw006.entity.OrderCHAR;
import com.moko.support.lw006.entity.ParamsKeyEnum;
import com.moko.support.lw006.handler.MokoCharacteristicHandler;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class LoRaLW006MokoSupport extends MokoBleLib {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    private static volatile LoRaLW006MokoSupport INSTANCE;

    private Context mContext;

    private MokoBleConfig mBleConfig;

    private LoRaLW006MokoSupport() {
        //no instance
    }

    public static LoRaLW006MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (LoRaLW006MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoRaLW006MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context;
        super.init(context);
    }

    @Override
    public MokoBleManager getMokoBleManager() {
        mBleConfig = new MokoBleConfig(mContext, this);
        return mBleConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    // connect
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        mCharacteristicMap = new MokoCharacteristicHandler().getCharacteristics(gatt);
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(Enum orderCHAR) {
        return mCharacteristicMap.get(orderCHAR);
    }

    ///////////////////////////////////////////////////////////////////////////
    // order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCHARNull() {
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            disConnectBle();
            return true;
        }
        return false;
    }

    @Override
    public void orderFinish() {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_FINISH);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderTimeout(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    @Override
    public void orderResult(OrderTaskResponse response) {
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }

    private int packetCount;
    private int packetIndex;
    private int dataLength;
    private byte[] dataBytes;
    private StringBuilder dataSb;

    @Override
    public boolean orderResponseValid(BluetoothGattCharacteristic characteristic, OrderTask orderTask) {
        final UUID responseUUID = characteristic.getUuid();
        final OrderCHAR orderCHAR = (OrderCHAR) orderTask.orderCHAR;
        if (responseUUID.equals(orderCHAR.getUuid()) && responseUUID.equals(OrderCHAR.CHAR_PARAMS.getUuid())) {
            byte[] value = characteristic.getValue();
            final int header = value[0] & 0xFF;
            final int flag = value[1] & 0xFF;
            if (header == 0xEE && flag == 0x00) {
                // 分包读取时特殊处理
                final int cmd = value[2] & 0xFF;
                packetCount = value[3] & 0xFF;
                packetIndex = value[4] & 0xFF;
                final int length = value[5] & 0xFF;
                if (packetIndex == 0) {
                    // 第一包
                    dataLength = 0;
                    dataSb = new StringBuilder();
                }
                ParamsKeyEnum keyEnum = ParamsKeyEnum.fromParamKey(cmd);
                switch (keyEnum) {
                    case KEY_FILTER_NAME_RULES:
                        if (length > 0) {
                            dataLength += length;
                            byte[] responseData = Arrays.copyOfRange(value, 6, 6 + length);
                            dataSb.append(MokoUtils.bytesToHexString(responseData));
                        }
                        if (packetIndex == (packetCount - 1)) {
                            if (!TextUtils.isEmpty(dataSb.toString()))
                                dataBytes = MokoUtils.hex2bytes(dataSb.toString());
                            byte[] responseValue = new byte[4 + dataLength];
                            responseValue[0] = (byte) 0xED;
                            responseValue[1] = (byte) 0x00;
                            responseValue[2] = (byte) cmd;
                            responseValue[3] = (byte) dataLength;
                            for (int i = 0; i < dataLength; i++) {
                                responseValue[4 + i] = dataBytes[i];
                            }
                            dataSb = null;
                            dataBytes = null;
                            // 最后一包
                            orderTask.orderStatus = 1;
                            orderTask.response.responseValue = responseValue;
                            pollTask();
                            executeTask();
                            orderResult(orderTask.response);
                        }
                        break;
                }
                return false;
            }
            return true;
        }
        return responseUUID.equals(orderCHAR.getUuid());
    }

    @Override
    public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
        final UUID responseUUID = characteristic.getUuid();
        OrderCHAR orderCHAR = null;
        if (responseUUID.equals(OrderCHAR.CHAR_DISCONNECTED_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_DISCONNECTED_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_STORAGE_DATA_NOTIFY.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_STORAGE_DATA_NOTIFY;
        }
        if (responseUUID.equals(OrderCHAR.CHAR_LOG.getUuid())) {
            orderCHAR = OrderCHAR.CHAR_LOG;
        }
        if (orderCHAR == null)
            return false;
        XLog.i(orderCHAR.name());
        OrderTaskResponse response = new OrderTaskResponse();
        response.orderCHAR = orderCHAR;
        response.responseValue = value;
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_CURRENT_DATA);
        event.setResponse(response);
        EventBus.getDefault().post(event);
        return true;
    }

    public ArrayList<ExportData> exportDatas;
    public StringBuilder storeString;
    public int startTime;
    public int sum;

    public void enableLogNotify() {
        if (mBleConfig != null)
            mBleConfig.enableLogNotify();
    }

    public void disableLogNotify() {
        if (mBleConfig != null)
            mBleConfig.disableLogNotify();
    }
}
