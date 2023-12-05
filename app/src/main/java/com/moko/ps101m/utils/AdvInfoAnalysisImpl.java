package com.moko.ps101m.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.ps101m.entity.AdvInfo;
import com.moko.support.ps101m.entity.DeviceInfo;
import com.moko.support.ps101m.entity.OrderServices;
import com.moko.support.ps101m.service.DeviceInfoParseable;

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
        byte[] manufacturerSpecificData = record.getManufacturerSpecificData(0xAA11);
        byte[] bytes = record.getServiceData(new ParcelUuid(OrderServices.SERVICE_ADV.getUuid()));
        if (null == manufacturerSpecificData || manufacturerSpecificData.length != 14) return null;
        if ((manufacturerSpecificData[0] & 0xff) != 0) return null;
        boolean verifyEnable = (manufacturerSpecificData[7] & 0xff) == 1;
        String uuid = null;
        String major = null;
        String minor = null;
        String rssi1M = null;
        if (null != bytes && bytes.length == 23) {
            String data = MokoUtils.bytesToHexString(bytes);
            StringBuilder stringBuilder = new StringBuilder(data.substring(4, 36).toLowerCase());
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            uuid = stringBuilder.toString();
            major = String.valueOf(Integer.parseInt(data.substring(36, 40), 16));
            minor = String.valueOf(Integer.parseInt(data.substring(40, 44), 16));
            int rssi = Integer.parseInt(data.substring(44, 46), 16);
            rssi1M = (byte) rssi + "";
        }
        AdvInfo advInfo;
        if (advInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = advInfoHashMap.get(deviceInfo.mac);
            if (null == advInfo) return null;
            advInfo.name = deviceInfo.name;
            advInfo.rssi = deviceInfo.rssi;
            long currentTime = SystemClock.elapsedRealtime();
            advInfo.intervalTime = currentTime - advInfo.scanTime;
            advInfo.scanTime = currentTime;
            advInfo.verifyEnable = verifyEnable;
            advInfo.connectable = result.isConnectable();
            advInfo.uuid = uuid;
            advInfo.major = major;
            advInfo.minor = minor;
            advInfo.rssi1M = rssi1M;
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.verifyEnable = verifyEnable;
            advInfo.connectable = result.isConnectable();
            advInfo.uuid = uuid;
            advInfo.major = major;
            advInfo.minor = minor;
            advInfo.rssi1M = rssi1M;
            advInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        return advInfo;
    }
}
