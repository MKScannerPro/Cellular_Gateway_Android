package com.moko.support.mkgw4.service;

import com.moko.support.mkgw4.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
