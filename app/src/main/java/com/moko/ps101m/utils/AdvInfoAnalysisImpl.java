package com.moko.ps101m.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.entity.AdvInfo;
import com.moko.support.ps101m.entity.DeviceInfo;
import com.moko.support.ps101m.entity.OrderServices;
import com.moko.support.ps101m.service.DeviceInfoParseable;

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
        if (null == bytes || bytes.length != 16) return null;
        int txPower = bytes[7];
        int powerPercent = bytes[8] & 0xff;
        int batteryVoltage = MokoUtils.toInt(Arrays.copyOfRange(bytes, 9, 11));
        boolean verifyEnable = (bytes[11] & 0xff) == 1;
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.powerPercent = powerPercent;
            advInfo.batteryVoltage = batteryVoltage;
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
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.txPower = txPower;
            advInfo.verifyEnable = verifyEnable;
            advInfo.connectable = result.isConnectable();
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
