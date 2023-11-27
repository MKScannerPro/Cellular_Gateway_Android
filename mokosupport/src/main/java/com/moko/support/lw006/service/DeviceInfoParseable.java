package com.moko.support.lw006.service;

import com.moko.support.lw006.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
