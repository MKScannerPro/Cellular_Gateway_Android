package com.moko.support.ps101m;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.ps101m.entity.ParamsKeyEnum;
import com.moko.support.ps101m.task.GetFirmwareRevisionTask;
import com.moko.support.ps101m.task.GetHardwareRevisionTask;
import com.moko.support.ps101m.task.GetManufacturerNameTask;
import com.moko.support.ps101m.task.GetModelNumberTask;
import com.moko.support.ps101m.task.GetSerialNumberTask;
import com.moko.support.ps101m.task.GetSoftwareRevisionTask;
import com.moko.support.ps101m.task.ParamsReadTask;
import com.moko.support.ps101m.task.ParamsWriteTask;
import com.moko.support.ps101m.task.SetPasswordTask;

import java.io.File;
import java.util.ArrayList;

public class OrderTaskAssembler {
    ///////////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask getManufacturer() {
        GetManufacturerNameTask getManufacturerTask = new GetManufacturerNameTask();
        return getManufacturerTask;
    }

    public static OrderTask getDeviceModel() {
        GetModelNumberTask getDeviceModelTask = new GetModelNumberTask();
        return getDeviceModelTask;
    }

    public static OrderTask getSerialNumber() {
        GetSerialNumberTask getSerialNumberTask = new GetSerialNumberTask();
        return getSerialNumberTask;
    }

    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask getHardwareVersionTask = new GetHardwareRevisionTask();
        return getHardwareVersionTask;
    }

    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask getFirmwareVersionTask = new GetFirmwareRevisionTask();
        return getFirmwareVersionTask;
    }

    public static OrderTask getSoftwareVersion() {
        GetSoftwareRevisionTask getSoftwareVersionTask = new GetSoftwareRevisionTask();
        return getSoftwareVersionTask;
    }

    public static OrderTask getTimeZone() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_TIME_ZONE);
        return task;
    }

    public static OrderTask getTimeUTC() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_TIME_UTC);
        return task;
    }

    public static OrderTask getLowPowerPercent() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_LOW_POWER_PERCENT);
        return task;
    }

    public static OrderTask getDeviceMode() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_DEVICE_MODE);
        return task;
    }

    public static OrderTask getIndicatorStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_INDICATOR_STATUS);
        return task;
    }

    public static OrderTask getHeartBeatInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_HEARTBEAT_INTERVAL);
        return task;
    }

    public static OrderTask getCustomManufacturer() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MANUFACTURER);
        return task;
    }

    public static OrderTask getShutdownPayloadEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_SHUTDOWN_PAYLOAD_ENABLE);
        return task;
    }

    public static OrderTask getOffByButtonEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_OFF_BY_BUTTON);
        return task;
    }

    public static OrderTask getAutoPowerOn() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_AUTO_POWER_ON_ENABLE);
        return task;
    }

    public static OrderTask getLowPowerPayloadEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_LOW_POWER_PAYLOAD_ENABLE);
        return task;
    }

    public static OrderTask getBuzzerSoundChoose() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_BUZZER_SOUND_CHOOSE);
        return task;
    }

    public static OrderTask getVibrationIntensity() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_VIBRATION_INTENSITY);
        return task;
    }

    public static OrderTask getBattery() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_BATTERY_POWER);
        return task;
    }

    public static OrderTask getMacAddress() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_CHIP_MAC);
        return task;
    }

    public static OrderTask getPCBAStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_PCBA_STATUS);
        return task;
    }

    public static OrderTask getSelfTestStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_SELFTEST_STATUS);
        return task;
    }

    public static OrderTask getMotorState() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTOR_STATE);
        return task;
    }

    public static OrderTask resetMotorState() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.resetMotorState();
        return task;
    }

    public static OrderTask getPasswordVerifyEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_PASSWORD_VERIFY_ENABLE);
        return task;
    }

    public static OrderTask getAdvTimeout() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ADV_TIMEOUT);
        return task;
    }


    public static OrderTask getAdvTxPower() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ADV_TX_POWER);
        return task;
    }

    public static OrderTask getAdvName() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ADV_NAME);
        return task;
    }

    public static OrderTask getAdvInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ADV_INTERVAL);
        return task;
    }

    public static OrderTask getPeriodicPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_PERIODIC_MODE_POS_STRATEGY);
        return task;
    }

    public static OrderTask getPeriodicReportInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_PERIODIC_MODE_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getTimePosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_TIME_MODE_POS_STRATEGY);
        return task;
    }

    public static OrderTask getStandbyPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_STANDBY_MODE_POS_STRATEGY);
        return task;
    }

    public static OrderTask getTimePosReportPoints() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_TIME_MODE_REPORT_TIME_POINT);
        return task;
    }

    public static OrderTask getMotionModeEvent() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_EVENT);
        return task;
    }

    public static OrderTask getMotionStartPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_START_POS_STRATEGY);
        return task;
    }

    public static OrderTask getMotionTripInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_TRIP_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getMotionTripPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_TRIP_POS_STRATEGY);
        return task;
    }


    public static OrderTask getMotionEndTimeout() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_END_TIMEOUT);
        return task;
    }

    public static OrderTask getMotionEndNumber() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_END_NUMBER);
        return task;
    }

    public static OrderTask getMotionEndInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_END_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getMotionEndPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_END_POS_STRATEGY);
        return task;
    }

    public static OrderTask getMotionStationaryPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_STATIONARY_POS_STRATEGY);
        return task;
    }

    public static OrderTask getMotionStationaryReportInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_MODE_STATIONARY_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getWifiRssiFilter() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_WIFI_RSSI_FILTER);
        return task;
    }

    public static OrderTask getWifiPosMechanism() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_WIFI_POS_MECHANISM);
        return task;
    }

    public static OrderTask getWifiPosTimeout() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_WIFI_POS_TIMEOUT);
        return task;
    }

    public static OrderTask getWifiPosBSSIDNumber() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_WIFI_POS_BSSID_NUMBER);
        return task;
    }

    public static OrderTask getBlePosTimeout() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_BLE_POS_TIMEOUT);
        return task;
    }

    public static OrderTask getBlePosNumber() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_BLE_POS_MAC_NUMBER);
        return task;
    }

    public static OrderTask getFilterRSSI() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RSSI);
        return task;
    }

    public static OrderTask getFilterBleScanPhy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BLE_SCAN_PHY);
        return task;
    }

    public static OrderTask getFilterRelationship() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RELATIONSHIP);
        return task;
    }

    public static OrderTask getFilterMacPrecise() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_PRECISE);
        return task;
    }

    public static OrderTask getFilterMacReverse() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_REVERSE);
        return task;
    }

    public static OrderTask getFilterMacRules() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_RULES);
        return task;
    }

    public static OrderTask getFilterNamePrecise() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_NAME_PRECISE);
        return task;
    }

    public static OrderTask getFilterNameReverse() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_NAME_REVERSE);
        return task;
    }

    public static OrderTask getFilterNameRules() {
        ParamsReadTask task = new ParamsReadTask();
        task.getFilterName();
        return task;
    }

    public static OrderTask getFilterRawData() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RAW_DATA);
        return task;
    }

    public static OrderTask getFilterIBeaconEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_ENABLE);
        return task;
    }

    public static OrderTask getFilterIBeaconMajorRange() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_MAJOR_RANGE);
        return task;
    }

    public static OrderTask getFilterIBeaconMinorRange() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_MINOR_RANGE);
        return task;
    }

    public static OrderTask getFilterIBeaconUUID() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_UUID);
        return task;
    }

    public static OrderTask getFilterBXPIBeaconEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_ENABLE);
        return task;
    }

    public static OrderTask getFilterBXPIBeaconMajorRange() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_MAJOR_RANGE);
        return task;
    }

    public static OrderTask getFilterBXPIBeaconMinorRange() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_MINOR_RANGE);
        return task;
    }

    public static OrderTask getFilterBXPIBeaconUUID() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_UUID);
        return task;
    }

    public static OrderTask getFilterBXPTagEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_ENABLE);
        return task;
    }

    public static OrderTask getFilterBXPTagPrecise() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_PRECISE);
        return task;
    }

    public static OrderTask getFilterBXPTagReverse() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_REVERSE);
        return task;
    }

    public static OrderTask getFilterBXPTagRules() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES);
        return task;
    }

    public static OrderTask getMkPirEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_ENABLE);
        return task;
    }

    public static OrderTask getMkPirSensorDetectionStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DETECTION_STATUS);
        return task;
    }

    public static OrderTask getMkPirSensorSensitivity() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY);
        return task;
    }

    public static OrderTask getMkPirDoorStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DOOR_STATUS);
        return task;
    }

    public static OrderTask getMkPirDelayResStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DELAY_RES_STATUS);
        return task;
    }

    public static OrderTask getMkPirMajor() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_MAJOR);
        return task;
    }

    public static OrderTask getMkPirMinor() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_MINOR);
        return task;
    }

    public static OrderTask getFilterBXPButtonEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_ENABLE);
        return task;
    }

    public static OrderTask getFilterBXPButtonRules() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_RULES);
        return task;
    }

    public static OrderTask getBlePosMechanism() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_BLE_POS_MECHANISM);
        return task;
    }

    public static OrderTask getFilterEddystoneUidEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneUidNamespace() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE);
        return task;
    }

    public static OrderTask getFilterEddystoneUidInstance() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE);
        return task;
    }

    public static OrderTask getFilterEddystoneUrlEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneUrl() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL);
        return task;
    }

    public static OrderTask getFilterEddystoneTlmEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneTlmVersion() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_VERSION);
        return task;
    }

    public static OrderTask getFilterBXPAcc() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_ACC);
        return task;
    }

    public static OrderTask getFilterBXPTH() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TH);
        return task;
    }

    public static OrderTask getFilterBXPDevice() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_DEVICE);
        return task;
    }

    public static OrderTask getFilterOtherEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_ENABLE);
        return task;
    }

    public static OrderTask getFilterOtherRelationship() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_RELATIONSHIP);
        return task;
    }

    public static OrderTask getFilterOtherRules() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_RULES);
        return task;
    }

    public static OrderTask getGPSPosTimeoutL76() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_GPS_POS_TIMEOUT_L76C);
        return task;
    }

    public static OrderTask getGPSPDOPLimitL76() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_GPS_PDOP_LIMIT_L76C);
        return task;
    }

    public static OrderTask getDownLinkPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_DOWN_LINK_POS_STRATEGY);
        return task;
    }

    public static OrderTask getAccWakeupCondition() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ACC_WAKEUP_CONDITION);
        return task;
    }

    public static OrderTask getAccMotionCondition() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ACC_MOTION_CONDITION);
        return task;
    }

    public static OrderTask getManDownDetectionEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_ENABLE);
        return task;
    }

    public static OrderTask getManDownDetectionTimeout() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_TIMEOUT);
        return task;
    }

    public static OrderTask getManDownPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MAN_DOWN_POS_STRATEGY);
        return task;
    }

    public static OrderTask getManDownReportInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getAlarmType() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_TYPE);
        return task;
    }

    public static OrderTask getAlarmExitTime() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_EXIT_TIME);
        return task;
    }

    public static OrderTask getAlarmAlertTriggerType() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_ALERT_TRIGGER_TYPE);
        return task;
    }

    public static OrderTask getAlarmAlertPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_ALERT_POS_STRATEGY);
        return task;
    }

    public static OrderTask getAlarmAlertNotifyEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_ALERT_NOTIFY_ENABLE);
        return task;
    }

    public static OrderTask getAlarmSosTriggerType() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_SOS_TRIGGER_TYPE);
        return task;
    }

    public static OrderTask getAlarmSosPosStrategy() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_SOS_POS_STRATEGY);
        return task;
    }

    public static OrderTask getAlarmSosReportInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_SOS_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getAlarmSosNotifyEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_ALARM_SOS_NOTIFY_ENABLE);
        return task;
    }

    public static OrderTask getNetworkStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_NETWORK_STATUS);
        return task;
    }

    public static OrderTask getMqttConnectionStatus() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CONNECT_STATUS);
        return task;
    }

    public static OrderTask getNetworkReconnectInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_NETWORK_RECONNECT_INTERVAL);
        return task;
    }

    public static OrderTask getMQTTHost() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_HOST);
        return task;
    }

    public static OrderTask getMQTTPort() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_PORT);
        return task;
    }

    public static OrderTask getMQTTClientId() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CLIENT_ID);
        return task;
    }

    public static OrderTask getMQTTSubscribeTopic() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_SUBSCRIBE_TOPIC);
        return task;
    }

    public static OrderTask getMQTTPublishTopic() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_PUBLISH_TOPIC);
        return task;
    }

    public static OrderTask getMQTTCleanSession() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CLEAN_SESSION);
        return task;
    }

    public static OrderTask getMQTTQos() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_QOS);
        return task;
    }

    public static OrderTask getMQTTKeepAlive() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_KEEP_ALIVE);
        return task;
    }

    public static OrderTask getApn() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_APN);
        return task;
    }

    public static OrderTask getNetworkFormat() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_NETWORK_FORMAT);
        return task;
    }

    public static OrderTask getMQTTUsername() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_USERNAME);
        return task;
    }

    public static OrderTask getMQTTPassword() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_PASSWORD);
        return task;
    }

    public static OrderTask getMQTTConnectMode() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_CONNECT_MODE);
        return task;
    }

    public static OrderTask getMQTTLwtEnable() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_LWT_ENABLE);
        return task;
    }

    public static OrderTask getMQTTLwtRetain() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_LWT_RETAIN);
        return task;
    }

    public static OrderTask getMQTTLwtQos() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_LWT_QOS);
        return task;
    }

    public static OrderTask getMQTTLwtTopic() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_LWT_TOPIC);
        return task;
    }

    public static OrderTask getMQTTLwtPayload() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_LWT_PAYLOAD);
        return task;
    }

    public static OrderTask getAxisDataReportInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_AXIS_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getDataFormat() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_DATA_COMMUNICATION_TYPE);
        return task;
    }

    public static OrderTask getNtpServer() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_NTP_SERVER);
        return task;
    }

    public static OrderTask getNtpSyncInterval() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.KEY_NTP_SYNC_INTERVAL);
        return task;
    }


    ///////////////////////////////////////////////////////////////////////////
    // WRITE
    ///////////////////////////////////////////////////////////////////////////
    public static OrderTask setNtpServer(@Nullable String ntpServer) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setNtpServer(ntpServer);
        return task;
    }

    public static OrderTask setNtpSyncInterval(@IntRange(from = 0, to = 720) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setNtpSyncInterval(interval);
        return task;
    }

    public static OrderTask setDataFormat(@IntRange(from = 0, to = 1) int format) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDataFormat(format);
        return task;
    }

    public static OrderTask setAxisDataReportInterval(@IntRange(from = 0, to = 65535) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAxisDataReportInterval(interval);
        return task;
    }

    public static OrderTask setMQTTHost(String mqttHost) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTHost(mqttHost);
        return task;
    }

    public static OrderTask setMQTTPort(@IntRange(from = 1, to = 65535) int port) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTPort(port);
        return task;
    }

    public static OrderTask setMQTTClientId(String clientId) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTClientId(clientId);
        return task;
    }

    public static OrderTask setMQTTSubscribeTopic(String subtopic) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTSubscribeTopic(subtopic);
        return task;
    }

    public static OrderTask setMQTTPublishTopic(String publishTopic) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTPublishTopic(publishTopic);
        return task;
    }

    public static OrderTask setMQTTCleanSession(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTCleanSession(enable);
        return task;
    }

    public static OrderTask setMQTTQos(@IntRange(from = 0, to = 2) int qos) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTQos(qos);
        return task;
    }

    public static OrderTask setMQTTKeepAlive(@IntRange(from = 10, to = 120) int keepAlive) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTKeepAlive(keepAlive);
        return task;
    }

    public static OrderTask setApn(@Nullable String apn) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setApn(apn);
        return task;
    }

    public static OrderTask setNetworkFormat(@IntRange(from = 0, to = 3) int networkFormat) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setNetworkFormat(networkFormat);
        return task;
    }

    public static OrderTask setMQTTUsername(@Nullable String userName) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTUsername(userName);
        return task;
    }

    public static OrderTask setMQTTPassword(@Nullable String password) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTPassword(password);
        return task;
    }

    public static OrderTask setMQTTConnectMode(@IntRange(from = 0, to = 3) int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTConnectMode(mode);
        return task;
    }

    public static OrderTask setMQTTLwtEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTLwtEnable(enable);
        return task;
    }

    public static OrderTask setMQTTLwtRetain(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTLwtRetain(enable);
        return task;
    }

    public static OrderTask setMQTTLwtQos(@IntRange(from = 0, to = 2) int qos) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTLwtQos(qos);
        return task;
    }

    public static OrderTask setMQTTLwtTopic(String lwtTopic) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTLwtTopic(lwtTopic);
        return task;
    }

    public static OrderTask setMQTTLwtPayload(String lwtPayload) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMQTTLwtPayload(lwtPayload);
        return task;
    }

    public static OrderTask setCA(@Nullable File file) throws Exception {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CA, file);
        return task;
    }

    public static OrderTask setClientCert(@Nullable File file) throws Exception {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CLIENT_CERT, file);
        return task;
    }

    public static OrderTask setClientKey(@Nullable File file) throws Exception {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CLIENT_KEY, file);
        return task;
    }

    public static OrderTask setNetworkReconnectInterval(@IntRange(from = 0, to = 100) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setNetworkReconnectInterval(interval);
        return task;
    }

    public static OrderTask setPassword(String password) {
        SetPasswordTask task = new SetPasswordTask();
        task.setData(password);
        return task;
    }

    public static OrderTask close() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.close();
        return task;
    }

    public static OrderTask restart() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.reboot();
        return task;
    }

    public static OrderTask restore() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.reset();
        return task;
    }

    public static OrderTask setTime() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setTime();
        return task;
    }

    public static OrderTask setTimeZone(@IntRange(from = -24, to = 28) int timeZone) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setTimeZone(timeZone);
        return task;
    }

    public static OrderTask setLowPowerPercent(@IntRange(from = 0, to = 5) int percent) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setLowPowerPercent(percent);
        return task;
    }

    public static OrderTask setBuzzerSound(@IntRange(from = 0, to = 2) int buzzer) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setBuzzerSound(buzzer);
        return task;
    }

    public static OrderTask setVibrationIntensity(@IntRange(from = 0, to = 100) int intensity) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setVibrationIntensity(intensity);
        return task;
    }

    public static OrderTask setDeviceMode(@IntRange(from = 0, to = 3) int mode) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDeviceMode(mode);
        return task;
    }

    public static OrderTask setIndicatorStatus(int status) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setIndicatorStatus(status);
        return task;
    }

    public static OrderTask setHeartBeatInterval(@IntRange(from = 1, to = 14400) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setHeartBeatInterval(interval);
        return task;
    }

    public static OrderTask setManufacturer(String manufacturer) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setManufacturer(manufacturer);
        return task;
    }

    public static OrderTask setShutdownInfoReport(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setShutdownInfoReport(enable);
        return task;
    }

    public static OrderTask setLowPowerReportEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setLowPowerReportEnable(enable);
        return task;
    }

    public static OrderTask setPasswordVerifyEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setPasswordVerifyEnable(enable);
        return task;
    }

    public static OrderTask changePassword(String password) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.changePassword(password);
        return task;
    }

    public static OrderTask setAdvTimeout(@IntRange(from = 1, to = 60) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvTimeout(timeout);
        return task;
    }

    public static OrderTask setAdvTxPower(@IntRange(from = -40, to = 8) int txPower) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvTxPower(txPower);
        return task;
    }

    public static OrderTask setAdvName(@Nullable String advName) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvName(advName);
        return task;
    }

    public static OrderTask setAdvInterval(@IntRange(from = 1, to = 100) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAdvInterval(interval);
        return task;
    }

    public static OrderTask setPeriodicPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setPeriodicPosStrategy(strategy);
        return task;
    }

    public static OrderTask setPeriodicReportInterval(@IntRange(from = 1, to = 14400) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setPeriodicReportInterval(interval);
        return task;
    }

    public static OrderTask setTimePosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setTimePosStrategy(strategy);
        return task;
    }

    public static OrderTask setStandbyPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setStandbyPosStrategy(strategy);
        return task;
    }

    public static OrderTask setTimePosReportPoints(@Nullable ArrayList<Integer> timePoints) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setTimePosReportPoints(timePoints);
        return task;
    }

    public static OrderTask setMotionModeEvent(@IntRange(from = 0, to = 31) int event) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionModeEvent(event);
        return task;
    }

    public static OrderTask setMotionStartPosStrategy(@IntRange(from = 0, to = 2) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionStartPosStrategy(strategy);
        return task;
    }


    public static OrderTask setMotionTripInterval(@IntRange(from = 10, to = 86400) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionTripInterval(interval);
        return task;
    }

    public static OrderTask setMotionTripPosStrategy(@IntRange(from = 0, to = 2) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionTripPosStrategy(strategy);
        return task;
    }

    public static OrderTask setMotionEndTimeout(@IntRange(from = 1, to = 180) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionEndTimeout(timeout);
        return task;
    }

    public static OrderTask setMotionEndNumber(@IntRange(from = 1, to = 255) int number) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionEndNumber(number);
        return task;
    }

    public static OrderTask setMotionEndInterval(@IntRange(from = 10, to = 300) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionEndInterval(interval);
        return task;
    }

    public static OrderTask setMotionEndPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionEndPosStrategy(strategy);
        return task;
    }

    public static OrderTask setMotionStationaryPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionStationaryPosStrategy(strategy);
        return task;
    }

    public static OrderTask setMotionStationaryReportInterval(@IntRange(from = 1, to = 14400) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setMotionStationaryReportInterval(interval);
        return task;
    }

    public static OrderTask setWifiRssiFilter(@IntRange(from = -127, to = 0) int rssi) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setWifiRssiFilter(rssi);
        return task;
    }

    public static OrderTask setWifiPosMechanism(@IntRange(from = 0, to = 1) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setWifiPosMechanism(type);
        return task;
    }

    public static OrderTask setWifiPosTimeout(@IntRange(from = 1, to = 4) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setWifiPosTimeout(timeout);
        return task;
    }

    public static OrderTask setWifiPosBSSIDNumber(@IntRange(from = 1, to = 15) int number) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setWifiPosBSSIDNumber(number);
        return task;
    }

    public static OrderTask setBlePosTimeout(@IntRange(from = 1, to = 10) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setBlePosTimeout(timeout);
        return task;
    }

    public static OrderTask setBlePosNumber(@IntRange(from = 1, to = 15) int number) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setBlePosNumber(number);
        return task;
    }

    public static OrderTask setFilterRSSI(@IntRange(from = -127, to = 0) int rssi) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterRSSI(rssi);
        return task;
    }

    public static OrderTask setFilterBleScanPhy(@IntRange(from = 0, to = 3) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBleScanPhy(type);
        return task;
    }

    public static OrderTask setFilterRelationship(@IntRange(from = 0, to = 6) int relationship) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterRelationship(relationship);
        return task;
    }

    public static OrderTask setFilterMacPrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMacPrecise(enable);
        return task;
    }

    public static OrderTask setFilterMacReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMacReverse(enable);
        return task;
    }

    public static OrderTask setFilterMacRules(ArrayList<String> filterMacRules) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMacRules(filterMacRules);
        return task;
    }

    public static OrderTask setFilterNamePrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterNamePrecise(enable);
        return task;
    }

    public static OrderTask setFilterNameReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterNameReverse(enable);
        return task;
    }

    public static OrderTask setFilterRawData(int unknown, int ibeacon,
                                             int eddystone_uid, int eddystone_url, int eddystone_tlm,
                                             int bxp_acc, int bxp_th,
                                             int mkibeacon, int mkibeacon_acc) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterRawData(unknown, ibeacon,
                eddystone_uid, eddystone_url, eddystone_tlm,
                bxp_acc, bxp_th,
                mkibeacon, mkibeacon_acc);
        return task;
    }

    public static OrderTask setFilterIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterIBeaconEnable(enable);
        return task;
    }

    public static OrderTask setFilterIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                                       @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterIBeaconMajorRange(min, max);
        return task;
    }

    public static OrderTask setFilterIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                                       @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterIBeaconMinorRange(min, max);
        return task;
    }

    public static OrderTask setFilterIBeaconUUID(String uuid) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterIBeaconUUID(uuid);
        return task;
    }

    public static OrderTask setFilterMKIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPIBeaconEnable(enable);
        return task;
    }

    public static OrderTask setFilterMKIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                                         @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPIBeaconMajorRange(min, max);
        return task;
    }

    public static OrderTask setFilterMKIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                                         @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPIBeaconMinorRange(min, max);
        return task;
    }

    public static OrderTask setFilterMKIBeaconUUID(String uuid) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPIBeaconUUID(uuid);
        return task;
    }

    public static OrderTask setFilterBXPTagEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPTagEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagPrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPTagPrecise(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPTagReverse(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagRules(ArrayList<String> filterBXPTagRules) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPTagRules(filterBXPTagRules);
        return task;
    }

    public static OrderTask setFilterMkPirEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirEnable(enable);
        return task;
    }

    public static OrderTask setFilterMkPirSensorDetectionStatus(@IntRange(from = 0, to = 2) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirSensorDetectionStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirSensorSensitivity(@IntRange(from = 0, to = 3) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirSensorSensitivity(type);
        return task;
    }

    public static OrderTask setFilterMkPirDoorStatus(@IntRange(from = 0, to = 2) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirDoorStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirDelayResStatus(@IntRange(from = 0, to = 3) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirDelayResStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirMajorRange(@IntRange(from = 0, to = 65535) int min,
                                                     @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirMajorRange(min, max);
        return task;
    }

    public static OrderTask setFilterMkPirMinorRange(@IntRange(from = 0, to = 65535) int min,
                                                     @IntRange(from = 0, to = 65535) int max) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterMkPirMinorRange(min, max);
        return task;
    }

    public static OrderTask setFilterBXPButtonEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPButtonEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPButtonRules(@IntRange(from = 0, to = 1) int singleEnable,
                                                    @IntRange(from = 0, to = 1) int doubleEnable,
                                                    @IntRange(from = 0, to = 1) int longEnable,
                                                    @IntRange(from = 0, to = 1) int abnormalEnable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPButtonRules(singleEnable, doubleEnable, longEnable, abnormalEnable);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneUIDEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDNamespace(String namespace) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneUIDNamespace(namespace);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDInstance(String instance) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneUIDInstance(instance);
        return task;
    }

    public static OrderTask setFilterEddystoneUrlEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneUrlEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneUrl(String url) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneUrl(url);
        return task;
    }

    public static OrderTask setFilterEddystoneTlmEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneTlmEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneTlmVersion(@IntRange(from = 0, to = 2) int version) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterEddystoneTlmVersion(version);
        return task;
    }

    public static OrderTask setFilterBXPDeviceEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPDeviceEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPAccEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPAccEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPTHEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterBXPTHEnable(enable);
        return task;
    }

    public static OrderTask setFilterOtherEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterOtherEnable(enable);
        return task;
    }

    public static OrderTask setFilterOtherRelationship(@IntRange(from = 0, to = 5) int relationship) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterOtherRelationship(relationship);
        return task;
    }

    public static OrderTask setFilterOtherRules(ArrayList<String> filterOtherRules) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterOtherRules(filterOtherRules);
        return task;
    }

    public static OrderTask setFilterNameRules(ArrayList<String> filterOtherRules) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setFilterNameRules(filterOtherRules);
        return task;
    }

    public static OrderTask setGPSPosTimeoutL76C(@IntRange(from = 60, to = 600) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setGPSPosTimeoutL76(timeout);
        return task;
    }


    public static OrderTask setGPSPDOPLimitL76C(@IntRange(from = 25, to = 100) int limit) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setGPSPDOPLimitL76(limit);
        return task;
    }

    public static OrderTask setBlePosMechanism(@IntRange(from = 0, to = 1) int mechanism) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setBlePosMechanism(mechanism);
        return task;
    }

    public static OrderTask setDownLinkPosStrategy(@IntRange(from = 0, to = 2) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setDownLinkPosStrategy(strategy);
        return task;
    }

    public static OrderTask setAccWakeupCondition(@IntRange(from = 1, to = 20) int threshold,
                                                  @IntRange(from = 1, to = 10) int duration) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAccWakeupCondition(threshold, duration);
        return task;
    }

    public static OrderTask setAccMotionCondition(@IntRange(from = 10, to = 250) int threshold,
                                                  @IntRange(from = 1, to = 15) int duration) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAccMotionCondition(threshold, duration);
        return task;
    }

    public static OrderTask setShutdownPayloadEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setShutdownInfoReport(enable);
        return task;
    }

    public static OrderTask setOffByButton(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setOffByButton(enable);
        return task;
    }

    public static OrderTask setAutoPowerOn(@IntRange(from = 0, to = 1) int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAutoPowerOn(enable);
        return task;
    }

    public static OrderTask setManDownDetectionEnable(int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setManDownDetectionEnable(enable);
        return task;
    }

    public static OrderTask setManDownDetectionTimeout(@IntRange(from = 1, to = 120) int timeout) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setManDownDetectionTimeout(timeout);
        return task;
    }

    public static OrderTask setManDownPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setManDownPosStrategy(strategy);
        return task;
    }

    public static OrderTask setManDownReportInterval(@IntRange(from = 10, to = 600) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setManDownReportInterval(interval);
        return task;
    }

    public static OrderTask setAlarmType(@IntRange(from = 0, to = 2) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmType(type);
        return task;
    }

    public static OrderTask setAlarmExitTime(@IntRange(from = 5, to = 15) int time) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmExitTime(time);
        return task;
    }

    public static OrderTask setAlarmAlertTriggerType(@IntRange(from = 0, to = 4) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmAlertTriggerType(type);
        return task;
    }

    public static OrderTask setAlarmAlertPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmAlertPosStrategy(strategy);
        return task;
    }

    public static OrderTask setAlarmAlertNotifyEnable(int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmAlertNotifyEnable(enable);
        return task;
    }

    //
    public static OrderTask setAlarmSosTriggerType(@IntRange(from = 0, to = 4) int type) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmSosTriggerType(type);
        return task;
    }

    public static OrderTask setAlarmSosPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmSosPosStrategy(strategy);
        return task;
    }

    public static OrderTask setAlarmSosReportInterval(@IntRange(from = 10, to = 600) int interval) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmSosReportInterval(interval);
        return task;
    }

    public static OrderTask setAlarmSosNotifyEnable(int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAlarmSosNotifyEnable(enable);
        return task;
    }

    public static OrderTask readStorageData(int time) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.readStorageData(time);
        return task;
    }

    public static OrderTask setSyncEnable(int enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setSyncEnable(enable);
        return task;
    }

    public static OrderTask clearStorageData() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.clearStorageData();
        return task;
    }

}
