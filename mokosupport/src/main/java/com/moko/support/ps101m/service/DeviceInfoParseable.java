package com.moko.support.ps101m.service;

import com.moko.support.ps101m.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
