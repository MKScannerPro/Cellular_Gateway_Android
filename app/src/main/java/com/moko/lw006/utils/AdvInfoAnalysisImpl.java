package com.moko.lw006.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw006.entity.AdvInfo;
import com.moko.support.lw006.entity.DeviceInfo;
import com.moko.support.lw006.entity.OrderServices;
import com.moko.support.lw006.service.DeviceInfoParseable;

import java.util.Arrays;
import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class AdvInfoAnalysisImpl implements DeviceInfoParseable<AdvInfo> {
    private final HashMap<String, AdvInfo> advInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.advInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        if (null == record) return null;
        byte[] bytes = record.getServiceData(new ParcelUuid(OrderServices.SERVICE_ADV.getUuid()));
        if (null == bytes || bytes.length != 14) return null;
        // 0x00:LR1110,0x01:L76
        int deviceType = bytes[0] & 0xFF;
        int txPower = bytes[8];
        int powerPercent = bytes[9] & 0xff;
        int batteryVoltage = MokoUtils.toInt(Arrays.copyOfRange(bytes, 10, 12));
        boolean verifyEnable = ((bytes[12] & 0xff) >> 7 & 0x01) == 1;
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.powerPercent = powerPercent;
            advInfo.batteryVoltage = batteryVoltage;
            advInfo.deviceType = deviceType;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
            advInfo.txPower = txPower;
            advInfo.verifyEnable = verifyEnable;
            advInfo.connectable = result.isConnectable();
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.batteryVoltage = batteryVoltage;
            advInfo.powerPercent = powerPercent;
            advInfo.deviceType = deviceType;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.txPower = txPower;
            advInfo.verifyEnable = verifyEnable;
            advInfo.connectable = result.isConnectable();
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
