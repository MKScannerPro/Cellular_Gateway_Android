package com.moko.support.mkgw4.callback;

import com.moko.support.mkgw4.entity.DeviceInfo;

public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
