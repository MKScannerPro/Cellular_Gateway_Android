package com.moko.support.ps101m.callback;

import com.moko.support.ps101m.entity.DeviceInfo;

public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
