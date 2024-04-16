package com.moko.support.mkgw4;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.mkgw4.entity.ParamsKeyEnum;
import com.moko.support.mkgw4.task.GetFirmwareRevisionTask;
import com.moko.support.mkgw4.task.GetHardwareRevisionTask;
import com.moko.support.mkgw4.task.GetManufacturerNameTask;
import com.moko.support.mkgw4.task.GetModelNumberTask;
import com.moko.support.mkgw4.task.GetSerialNumberTask;
import com.moko.support.mkgw4.task.GetSoftwareRevisionTask;
import com.moko.support.mkgw4.task.ParamsTask;
import com.moko.support.mkgw4.task.SetPasswordTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_TIME_ZONE);
        return task;
    }

    public static OrderTask getLowPowerPercent() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_LOW_POWER_PERCENT);
        return task;
    }

    public static OrderTask getCustomManufacturer() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MANUFACTURER);
        return task;
    }

    public static OrderTask getAutoPowerOn() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_AUTO_POWER_ON_ENABLE);
        return task;
    }

    public static OrderTask getLowPowerNotifyEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_LOW_POWER_NOTIFY_ENABLE);
        return task;
    }

    public static OrderTask getBattery() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BATTERY_POWER);
        return task;
    }

    public static OrderTask getPowerLossNotify() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_POWER_LOSS_NOTIFY);
        return task;
    }

    public static OrderTask getLedIndicator() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_LED_INDICATOR);
        return task;
    }

    public static OrderTask getMacAddress() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CHIP_MAC);
        return task;
    }

    public static OrderTask getIMEI() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IMEI);
        return task;
    }

    public static OrderTask getIccId() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ICC_ID);
        return task;
    }

    public static OrderTask getPCBAStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PCBA_STATUS);
        return task;
    }

    public static OrderTask getSelfTestStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_SELFTEST_STATUS);
        return task;
    }

    public static OrderTask getPasswordVerifyEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PASSWORD_VERIFY_ENABLE);
        return task;
    }

    public static OrderTask getAdvTimeout() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ADV_TIMEOUT);
        return task;
    }

    public static OrderTask getDevicePayloadInterval() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DEVICE_PAYLOAD_INTERVAL);
        return task;
    }

    public static OrderTask getDeviceStatusChoose() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DEVICE_STATUS_CHOOSE);
        return task;
    }

    public static OrderTask getAdvTxPower() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ADV_TX_POWER);
        return task;
    }

    public static OrderTask getAdvName() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ADV_NAME);
        return task;
    }

    public static OrderTask getAdvResponse() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ADV_RESPONSE);
        return task;
    }

    public static OrderTask getAdvInterval() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ADV_INTERVAL);
        return task;
    }

    public static OrderTask getRealScanPeriodicReportInterval() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_REAL_SCAN_PERIODIC_REPORT_INTERVAL);
        return task;
    }

    public static OrderTask getPeriodicScanImmediateReport() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PERIODIC_SCAN_IMMEDIATE_REPORT);
        return task;
    }

    public static OrderTask getPeriodicScanPeriodicReport() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PERIODIC_SCAN_PERIODIC_REPORT);
        return task;
    }

    public static OrderTask getDataRetentionPriority() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DATA_RETENTION_PRIORITY);
        return task;
    }

    public static OrderTask getFixMode() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FIX_MODE);
        return task;
    }

    public static OrderTask getPeriodicFixInterval() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PERIODIC_FIX_INTERVAL);
        return task;
    }

    public static OrderTask getMotionFixEnableStart() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_ENABLE_WHEN_START);
        return task;
    }

    public static OrderTask getMotionFixEnableInTrip() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_ENABLE_WHEN_TRIP);
        return task;
    }

    public static OrderTask getMotionFixIntervalInTrip() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_INTERVAL_WHEN_TRIP);
        return task;
    }

    public static OrderTask getMotionFixEnableStop() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_ENABLE_WHEN_STOP);
        return task;
    }

    public static OrderTask getMotionFixTimeoutStop() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_TIMEOUT_WHEN_STOP);
        return task;
    }

    public static OrderTask getMotionFixEnableStationary() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_ENABLE_WHEN_STATIONARY);
        return task;
    }

    public static OrderTask getMotionFixIntervalStationary() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_FIX_INTERVAL_WHEN_STATIONARY);
        return task;
    }


    public static OrderTask getFilterRSSI() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RSSI);
        return task;
    }

    public static OrderTask getFilterBleScanPhy() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_PHY);
        return task;
    }

    public static OrderTask getFilterRelationship() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RELATIONSHIP);
        return task;
    }

    public static OrderTask getFilterDuplicateData() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DUPLICATE_DATA_FILTER);
        return task;
    }

    public static OrderTask getFilterMacPrecise() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_PRECISE);
        return task;
    }

    public static OrderTask getFilterMacReverse() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_REVERSE);
        return task;
    }

    public static OrderTask getFilterMacRules() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MAC_RULES);
        return task;
    }

    public static OrderTask getFilterNamePrecise() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_NAME_PRECISE);
        return task;
    }

    public static OrderTask getFilterNameReverse() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_NAME_REVERSE);
        return task;
    }

    public static OrderTask getFilterNameRules() {
        ParamsTask task = new ParamsTask();
        task.getFilterName();
        return task;
    }

    public static OrderTask getFilterRawData() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_RAW_DATA);
        return task;
    }

    public static OrderTask getFilterIBeaconEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_ENABLE);
        return task;
    }

    public static OrderTask getFilterIBeaconMajorRange() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_MAJOR_RANGE);
        return task;
    }

    public static OrderTask getFilterIBeaconMinorRange() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_MINOR_RANGE);
        return task;
    }

    public static OrderTask getFilterIBeaconUUID() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_IBEACON_UUID);
        return task;
    }

    public static OrderTask getIBeaconMajor() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IBEACON_MAJOR);
        return task;
    }

    public static OrderTask getIBeaconMinor() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IBEACON_MINOR);
        return task;
    }

    public static OrderTask getIBeaconUUID() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IBEACON_UUID);
        return task;
    }

    public static OrderTask getRssi1M() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IBEACON_RSSI1M);
        return task;
    }

    public static OrderTask getFilterBXPTagEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_ENABLE);
        return task;
    }

    public static OrderTask getFilterBXPTagPrecise() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_PRECISE);
        return task;
    }

    public static OrderTask getFilterBXPTagReverse() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_REVERSE);
        return task;
    }

    public static OrderTask getFilterBXPTagRules() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES);
        return task;
    }

    public static OrderTask getMkPirEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_ENABLE);
        return task;
    }

    public static OrderTask getMkPirSensorDetectionStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DETECTION_STATUS);
        return task;
    }

    public static OrderTask getMkPirSensorSensitivity() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY);
        return task;
    }

    public static OrderTask getMkPirDoorStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DOOR_STATUS);
        return task;
    }

    public static OrderTask getMkPirDelayResStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_DELAY_RES_STATUS);
        return task;
    }

    public static OrderTask getMkPirMajor() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_MAJOR);
        return task;
    }

    public static OrderTask getMkPirMinor() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_PIR_MINOR);
        return task;
    }

    public static OrderTask getFilterBXPButtonEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_ENABLE);
        return task;
    }

    public static OrderTask getFilterBXPButtonRules() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_RULES);
        return task;
    }

    public static OrderTask getFilterEddystoneUidEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneUidNamespace() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE);
        return task;
    }

    public static OrderTask getFilterEddystoneUidInstance() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE);
        return task;
    }

    public static OrderTask getFilterEddystoneUrlEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneUrl() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL);
        return task;
    }

    public static OrderTask getFilterEddystoneTlmEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_ENABLE);
        return task;
    }

    public static OrderTask getFilterEddystoneTlmVersion() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_VERSION);
        return task;
    }

    public static OrderTask getFilterBXPAcc() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_ACC);
        return task;
    }

    public static OrderTask getFilterBXPTH() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_TH);
        return task;
    }

    public static OrderTask getFilterBXPDevice() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_BXP_DEVICE);
        return task;
    }

    public static OrderTask getFilterOtherEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_ENABLE);
        return task;
    }

    public static OrderTask getFilterMkTofEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_TOF_ENABLE);
        return task;
    }

    public static OrderTask getFilterMkTofRules() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_MK_TOF_RULES);
        return task;
    }

    public static OrderTask getIBeaconPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_IBEACON_PAYLOAD);
        return task;
    }

    public static OrderTask getEddystoneUidPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_EDDYSTONE_UID_PAYLOAD);
        return task;
    }

    public static OrderTask getEddystoneUrlPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_EDDYSTONE_URL_PAYLOAD);
        return task;
    }

    public static OrderTask getEddystoneTlmPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_EDDYSTONE_TLM_PAYLOAD);
        return task;
    }

    public static OrderTask getBxpDeviceInfoPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BXP_DEVICE_INFO_PAYLOAD);
        return task;
    }

    public static OrderTask getBxpAccPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BXP_ACC_PAYLOAD);
        return task;
    }

    public static OrderTask getBxpThPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BXP_TH_PAYLOAD);
        return task;
    }

    public static OrderTask getBxpButtonPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BXP_BUTTON_PAYLOAD);
        return task;
    }

    public static OrderTask getBxpTagPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_BXP_TAG_PAYLOAD);
        return task;
    }

    public static OrderTask getPirPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PIR_PAYLOAD);
        return task;
    }

    public static OrderTask getTofPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_TOF_PAYLOAD);
        return task;
    }

    public static OrderTask getOtherPayload() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_OTHER_PAYLOAD);
        return task;
    }

    public static OrderTask getOtherPayloadData() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_OTHER_PAYLOAD_DATA);
        return task;
    }

    public static OrderTask getFilterOtherRelationship() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_RELATIONSHIP);
        return task;
    }

    public static OrderTask getFilterOtherRules() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_FILTER_OTHER_RULES);
        return task;
    }

    public static OrderTask getGPSTimeout() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_GPS_TIMEOUT);
        return task;
    }

    public static OrderTask getGPSPDOP() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_GPS_PDOP);
        return task;
    }

    public static OrderTask getAccWakeupCondition() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ACC_WAKEUP_CONDITION);
        return task;
    }

    public static OrderTask getAccMotionCondition() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_ACC_MOTION_CONDITION);
        return task;
    }

    public static OrderTask getNetworkStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_NETWORK_STATUS);
        return task;
    }

    public static OrderTask getMqttConnectionStatus() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CONNECT_STATUS);
        return task;
    }

    public static OrderTask getDeviceType(){
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DEVICE_MODE);
        return task;
    }

    public static OrderTask getScanReportEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_SCAN_REPORT_ENABLE);
        return task;
    }

    public static OrderTask getScanReportMode() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_SCAN_REPORT_MODE);
        return task;
    }

    public static OrderTask getUploadPriority() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_UPLOAD_PRIORITY);
        return task;
    }

    public static OrderTask getMQTTHost() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_HOST);
        return task;
    }

    public static OrderTask getMQTTPort() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_PORT);
        return task;
    }

    public static OrderTask getMQTTClientId() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CLIENT_ID);
        return task;
    }

    public static OrderTask getMQTTSubscribeTopic() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_SUBSCRIBE_TOPIC);
        return task;
    }

    public static OrderTask getMQTTPublishTopic() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PUBLISH_TOPIC);
        return task;
    }

    public static OrderTask getMQTTCleanSession() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_CLEAN_SESSION);
        return task;
    }

    public static OrderTask getMQTTQos() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_QOS);
        return task;
    }

    public static OrderTask getMQTTKeepAlive() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MQTT_KEEP_ALIVE);
        return task;
    }

    public static OrderTask getApn() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_APN);
        return task;
    }

    public static OrderTask getApnUsername() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_APN_NAME);
        return task;
    }

    public static OrderTask getApnPassword() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_APN_PASSWORD);
        return task;
    }

    public static OrderTask getNetworkConnectTimeout() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CONNECT_NETWORK_TIMEOUT);
        return task;
    }

    public static OrderTask getNetworkPriority() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_NETWORK_PRIORITY);
        return task;
    }

    public static OrderTask getMQTTUsername() {
        ParamsTask task = new ParamsTask();
        task.getLongData(ParamsKeyEnum.KEY_MQTT_USERNAME);
        return task;
    }

    public static OrderTask getMQTTPassword() {
        ParamsTask task = new ParamsTask();
        task.getLongData(ParamsKeyEnum.KEY_MQTT_PASSWORD);
        return task;
    }

    public static OrderTask getMQTTConnectMode() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CONNECT_MODE);
        return task;
    }


    public static OrderTask getNtpServer() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_NTP_SERVER);
        return task;
    }

    public static OrderTask getNtpSyncInterval() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_NTP_SYNC_INTERVAL);
        return task;
    }

    public static OrderTask getPassword() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_PASSWORD);
        return task;
    }

    public static OrderTask getNtpEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_NTP_SWITCH);
        return task;
    }


    ///////////////////////////////////////////////////////////////////////////
    // WRITE
    ///////////////////////////////////////////////////////////////////////////
    public static OrderTask setPowerLossNotify(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setPowerLossNotify(enable);
        return task;
    }

    public static OrderTask setLedIndicator(int indicator) {
        ParamsTask task = new ParamsTask();
        task.setLedIndicator(indicator);
        return task;
    }

    public static OrderTask setNtpEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setNtpEnable(enable);
        return task;
    }

    public static OrderTask setNtpServer(@Nullable String ntpServer) {
        ParamsTask task = new ParamsTask();
        task.setNtpServer(ntpServer);
        return task;
    }

    public static OrderTask setNtpSyncInterval(@IntRange(from = 1, to = 720) int interval) {
        ParamsTask task = new ParamsTask();
        task.setNtpSyncInterval(interval);
        return task;
    }

    public static OrderTask setMQTTHost(String mqttHost) {
        ParamsTask task = new ParamsTask();
        task.setMQTTHost(mqttHost);
        return task;
    }

    public static OrderTask setMQTTPort(@IntRange(from = 1, to = 65535) int port) {
        ParamsTask task = new ParamsTask();
        task.setMQTTPort(port);
        return task;
    }

    public static OrderTask setMQTTClientId(String clientId) {
        ParamsTask task = new ParamsTask();
        task.setMQTTClientId(clientId);
        return task;
    }

    public static OrderTask setMQTTSubscribeTopic(String subtopic) {
        ParamsTask task = new ParamsTask();
        task.setMQTTSubscribeTopic(subtopic);
        return task;
    }

    public static OrderTask setMQTTPublishTopic(String publishTopic) {
        ParamsTask task = new ParamsTask();
        task.setMQTTPublishTopic(publishTopic);
        return task;
    }

    public static OrderTask setMQTTCleanSession(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setMQTTCleanSession(enable);
        return task;
    }

    public static OrderTask setMQTTQos(@IntRange(from = 0, to = 2) int qos) {
        ParamsTask task = new ParamsTask();
        task.setMQTTQos(qos);
        return task;
    }

    public static OrderTask setMQTTKeepAlive(@IntRange(from = 10, to = 120) int keepAlive) {
        ParamsTask task = new ParamsTask();
        task.setMQTTKeepAlive(keepAlive);
        return task;
    }

    public static OrderTask setApn(@Nullable String apn) {
        ParamsTask task = new ParamsTask();
        task.setApn(apn);
        return task;
    }

    public static OrderTask setApnUsername(@Nullable String apnUsername) {
        ParamsTask task = new ParamsTask();
        task.setApnUsername(apnUsername);
        return task;
    }

    public static OrderTask setApnPassword(@Nullable String apnPassword) {
        ParamsTask task = new ParamsTask();
        task.setApnPassword(apnPassword);
        return task;
    }

    public static OrderTask setNetworkConnectTimeout(@IntRange(from = 30, to = 600) int timeout) {
        ParamsTask task = new ParamsTask();
        task.setNetworkConnectTimeout(timeout);
        return task;
    }

    public static OrderTask setNetworkPriority(@IntRange(from = 0, to = 10) int networkFormat) {
        ParamsTask task = new ParamsTask();
        task.setNetworkPriority(networkFormat);
        return task;
    }

    public static OrderTask setMQTTUsername(@Nullable String userName) {
        ParamsTask task = new ParamsTask();
        task.setMQTTUsername(userName);
        return task;
    }

    public static OrderTask setMQTTPassword(@Nullable String password) {
        ParamsTask task = new ParamsTask();
        task.setMQTTPassword(password);
        return task;
    }

    public static OrderTask setMQTTConnectMode(@IntRange(from = 0, to = 3) int mode) {
        ParamsTask task = new ParamsTask();
        task.setMQTTConnectMode(mode);
        return task;
    }

    public static OrderTask setScanReportEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setScanReportEnable(enable);
        return task;
    }

    public static OrderTask setScanReportMode(@IntRange(from = 0, to = 4) int mode) {
        ParamsTask task = new ParamsTask();
        task.setScanReportMode(mode);
        return task;
    }

    public static OrderTask setUploadPriority(@IntRange(from = 0, to = 1) int priority) {
        ParamsTask task = new ParamsTask();
        task.setUploadPriority(priority);
        return task;
    }

    public static OrderTask setCA(@Nullable File file) throws Exception {
        ParamsTask task = new ParamsTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CA, file);
        return task;
    }

    public static OrderTask setClientCert(@Nullable File file) throws Exception {
        ParamsTask task = new ParamsTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CLIENT_CERT, file);
        return task;
    }

    public static OrderTask setClientKey(@Nullable File file) throws Exception {
        ParamsTask task = new ParamsTask();
        task.setFile(ParamsKeyEnum.KEY_MQTT_CLIENT_KEY, file);
        return task;
    }


    public static OrderTask setPassword(String password) {
        SetPasswordTask task = new SetPasswordTask();
        task.setData(password);
        return task;
    }

    public static OrderTask close() {
        ParamsTask task = new ParamsTask();
        task.close();
        return task;
    }

    public static OrderTask reboot() {
        ParamsTask task = new ParamsTask();
        task.reboot();
        return task;
    }

    public static OrderTask reset() {
        ParamsTask task = new ParamsTask();
        task.reset();
        return task;
    }

    public static OrderTask setTimeZone(@IntRange(from = -24, to = 28) int timeZone) {
        ParamsTask task = new ParamsTask();
        task.setTimeZone(timeZone);
        return task;
    }

    public static OrderTask setLowPowerPercent(@IntRange(from = 0, to = 4) int percent) {
        ParamsTask task = new ParamsTask();
        task.setLowPowerPercent(percent);
        return task;
    }

    public static OrderTask deleteBufferData() {
        ParamsTask task = new ParamsTask();
        task.deleteBufferData();
        return task;
    }

    public static OrderTask setManufacturer(String manufacturer) {
        ParamsTask task = new ParamsTask();
        task.setManufacturer(manufacturer);
        return task;
    }

    public static OrderTask setLowPowerReportEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setLowPowerReportEnable(enable);
        return task;
    }

    public static OrderTask setPasswordVerifyEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setPasswordVerifyEnable(enable);
        return task;
    }

    public static OrderTask changePassword(String password) {
        ParamsTask task = new ParamsTask();
        task.changePassword(password);
        return task;
    }

    public static OrderTask setAdvTimeout(@IntRange(from = 0, to = 60) int timeout) {
        ParamsTask task = new ParamsTask();
        task.setAdvTimeout(timeout);
        return task;
    }

    public static OrderTask setDevicePayloadInterval(int interval) {
        ParamsTask task = new ParamsTask();
        task.setDevicePayloadInterval(interval);
        return task;
    }

    public static OrderTask setDeviceStatusChoose(int status) {
        ParamsTask task = new ParamsTask();
        task.setDeviceStatusChoose(status);
        return task;
    }

    public static OrderTask setAdvTxPower(@IntRange(from = -40, to = 8) int txPower) {
        ParamsTask task = new ParamsTask();
        task.setAdvTxPower(txPower);
        return task;
    }

    public static OrderTask setAdvName(String advName) {
        ParamsTask task = new ParamsTask();
        task.setAdvName(advName);
        return task;
    }

    public static OrderTask setAdvResponse(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setAdvResponse(enable);
        return task;
    }

    public static OrderTask setAdvInterval(@IntRange(from = 1, to = 100) int interval) {
        ParamsTask task = new ParamsTask();
        task.setAdvInterval(interval);
        return task;
    }

    public static OrderTask setRealScanPeriodicReportInterval(@IntRange(from = 10, to = 86400) int interval) {
        ParamsTask task = new ParamsTask();
        task.setRealScanPeriodicReportInterval(interval);
        return task;
    }

    public static OrderTask setPeriodicScanImmediateReport(@IntRange(from = 3, to = 3600) int duration,
                                                           @IntRange(from = 10, to = 86400) int interval) {
        ParamsTask task = new ParamsTask();
        task.setPeriodicScanImmediateReport(duration, interval);
        return task;
    }

    public static OrderTask setPeriodicScanPeriodicReport(@IntRange(from = 3, to = 3600) int duration,
                                                          @IntRange(from = 10, to = 86400) int interval,
                                                          @IntRange(from = 10, to = 86400) int reportInterval) {
        ParamsTask task = new ParamsTask();
        task.setPeriodicScanPeriodicReport(duration, interval, reportInterval);
        return task;
    }

    public static OrderTask setDataRetentionPriority(@IntRange(from = 0, to = 1) int priority) {
        ParamsTask task = new ParamsTask();
        task.setDataRetentionPriority(priority);
        return task;
    }

    public static OrderTask setFixMode(@IntRange(from = 0, to = 2) int mode) {
        ParamsTask task = new ParamsTask();
        task.setFixMode(mode);
        return task;
    }

    public static OrderTask setPeriodicFixInterval(@IntRange(from = 10, to = 86400) int interval) {
        ParamsTask task = new ParamsTask();
        task.setPeriodicFixInterval(interval);
        return task;
    }

    public static OrderTask setMotionFixEnableStart(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixEnableStart(enable);
        return task;
    }

    public static OrderTask setMotionFixEnableTrip(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixEnableTrip(enable);
        return task;
    }

    public static OrderTask setMotionFixIntervalTrip(@IntRange(from = 10, to = 86400) int interval) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixIntervalTrip(interval);
        return task;
    }

    public static OrderTask setMotionFixEnableStop(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixEnableStop(enable);
        return task;
    }

    public static OrderTask setMotionFixTimeoutStop(@IntRange(from = 3, to = 180) int timeout) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixTimeoutStop(timeout);
        return task;
    }

    public static OrderTask setMotionFixEnableStationary(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixEnableStationary(enable);
        return task;
    }

    public static OrderTask setMotionFixIntervalStationary(@IntRange(from = 1, to = 1440) int interval) {
        ParamsTask task = new ParamsTask();
        task.setMotionFixIntervalStationary(interval);
        return task;
    }

    public static OrderTask setFilterRSSI(@IntRange(from = -127, to = 0) int rssi) {
        ParamsTask task = new ParamsTask();
        task.setFilterRSSI(rssi);
        return task;
    }

    public static OrderTask setFilterBleScanPhy(@IntRange(from = 0, to = 3) int type) {
        ParamsTask task = new ParamsTask();
        task.setFilterBleScanPhy(type);
        return task;
    }

    public static OrderTask setFilterRelationship(@IntRange(from = 0, to = 7) int relationship) {
        ParamsTask task = new ParamsTask();
        task.setFilterRelationship(relationship);
        return task;
    }

    public static OrderTask setFilterDuplicateData(@IntRange(from = 0, to = 3) int filterDuplicateData) {
        ParamsTask task = new ParamsTask();
        task.setFilterDuplicateData(filterDuplicateData);
        return task;
    }

    public static OrderTask setFilterMacPrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterMacPrecise(enable);
        return task;
    }

    public static OrderTask setFilterMacReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterMacReverse(enable);
        return task;
    }

    public static OrderTask setFilterMacRules(ArrayList<String> filterMacRules) {
        ParamsTask task = new ParamsTask();
        task.setFilterMacRules(filterMacRules);
        return task;
    }

    public static OrderTask setFilterNamePrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterNamePrecise(enable);
        return task;
    }

    public static OrderTask setFilterNameReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterNameReverse(enable);
        return task;
    }

    public static OrderTask setFilterRawData(int unknown, int ibeacon,
                                             int eddystone_uid, int eddystone_url, int eddystone_tlm,
                                             int bxp_acc, int bxp_th,
                                             int mkibeacon, int mkibeacon_acc) {
        ParamsTask task = new ParamsTask();
        task.setFilterRawData(unknown, ibeacon,
                eddystone_uid, eddystone_url, eddystone_tlm,
                bxp_acc, bxp_th,
                mkibeacon, mkibeacon_acc);
        return task;
    }

    public static OrderTask setFilterIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterIBeaconEnable(enable);
        return task;
    }

    public static OrderTask setFilterIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                                       @IntRange(from = 0, to = 65535) int max) {
        ParamsTask task = new ParamsTask();
        task.setFilterIBeaconMajorRange(min, max);
        return task;
    }

    public static OrderTask setFilterIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                                       @IntRange(from = 0, to = 65535) int max) {
        ParamsTask task = new ParamsTask();
        task.setFilterIBeaconMinorRange(min, max);
        return task;
    }

    public static OrderTask setFilterIBeaconUUID(String uuid) {
        ParamsTask task = new ParamsTask();
        task.setFilterIBeaconUUID(uuid);
        return task;
    }

    public static OrderTask setIBeaconMajor(@IntRange(from = 0, to = 65535) int major) {
        ParamsTask task = new ParamsTask();
        task.setIBeaconMajor(major);
        return task;
    }

    public static OrderTask setIBeaconMinor(@IntRange(from = 0, to = 65535) int minor) {
        ParamsTask task = new ParamsTask();
        task.setIBeaconMinor(minor);
        return task;
    }

    public static OrderTask setIBeaconUUID(String uuid) {
        ParamsTask task = new ParamsTask();
        task.setIBeaconUUID(uuid);
        return task;
    }

    public static OrderTask setRssi1M(@IntRange(from = -100, to = 0) int rssi1M) {
        ParamsTask task = new ParamsTask();
        task.setRssi1M(rssi1M);
        return task;
    }

    public static OrderTask setFilterBXPTagEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPTagEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagPrecise(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPTagPrecise(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagReverse(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPTagReverse(enable);
        return task;
    }

    public static OrderTask setFilterBXPTagRules(ArrayList<String> filterBXPTagRules) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPTagRules(filterBXPTagRules);
        return task;
    }

    public static OrderTask setFilterMkPirEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirEnable(enable);
        return task;
    }

    public static OrderTask setFilterMkPirSensorDetectionStatus(@IntRange(from = 0, to = 2) int type) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirSensorDetectionStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirSensorSensitivity(@IntRange(from = 0, to = 3) int type) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirSensorSensitivity(type);
        return task;
    }

    public static OrderTask setFilterMkPirDoorStatus(@IntRange(from = 0, to = 2) int type) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirDoorStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirDelayResStatus(@IntRange(from = 0, to = 3) int type) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirDelayResStatus(type);
        return task;
    }

    public static OrderTask setFilterMkPirMajorRange(@IntRange(from = 0, to = 65535) int min,
                                                     @IntRange(from = 0, to = 65535) int max) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirMajorRange(min, max);
        return task;
    }

    public static OrderTask setFilterMkPirMinorRange(@IntRange(from = 0, to = 65535) int min,
                                                     @IntRange(from = 0, to = 65535) int max) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkPirMinorRange(min, max);
        return task;
    }

    public static OrderTask setFilterBXPButtonEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPButtonEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPButtonRules(int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPButtonRules(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneUIDEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDNamespace(String namespace) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneUIDNamespace(namespace);
        return task;
    }

    public static OrderTask setFilterEddystoneUIDInstance(String instance) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneUIDInstance(instance);
        return task;
    }

    public static OrderTask setFilterEddystoneUrlEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneUrlEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneUrl(String url) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneUrl(url);
        return task;
    }

    public static OrderTask setFilterEddystoneTlmEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneTlmEnable(enable);
        return task;
    }

    public static OrderTask setFilterEddystoneTlmVersion(@IntRange(from = 0, to = 2) int version) {
        ParamsTask task = new ParamsTask();
        task.setFilterEddystoneTlmVersion(version);
        return task;
    }

    public static OrderTask setFilterBXPDeviceEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPDeviceEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPAccEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPAccEnable(enable);
        return task;
    }

    public static OrderTask setFilterBXPTHEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterBXPTHEnable(enable);
        return task;
    }

    public static OrderTask setFilterOtherEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterOtherEnable(enable);
        return task;
    }

    public static OrderTask setFilterMkTofEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkTofEnable(enable);
        return task;
    }

    public static OrderTask setFilterMkTofRules(List<String> filterTofRules) {
        ParamsTask task = new ParamsTask();
        task.setFilterMkTofRules(filterTofRules);
        return task;
    }

    public static OrderTask setFilterOtherRelationship(@IntRange(from = 0, to = 5) int relationship) {
        ParamsTask task = new ParamsTask();
        task.setFilterOtherRelationship(relationship);
        return task;
    }

    public static OrderTask setFilterOtherRules(ArrayList<String> filterOtherRules) {
        ParamsTask task = new ParamsTask();
        task.setFilterOtherRules(filterOtherRules);
        return task;
    }

    public static OrderTask setIBeaconPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setIBeaconPayload(payload);
        return task;
    }

    public static OrderTask setEddystoneUidPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setEddystoneUidPayload(payload);
        return task;
    }

    public static OrderTask setEddystoneUrlPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setEddystoneUrlPayload(payload);
        return task;
    }

    public static OrderTask setEddystoneTlmPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setEddystoneTlmPayload(payload);
        return task;
    }

    public static OrderTask setBxpDeviceInfoPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setBxpDeviceInfoPayload(payload);
        return task;
    }

    public static OrderTask setBxpAccPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setBxpAccPayload(payload);
        return task;
    }

    public static OrderTask setBxpThPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setBxpThPayload(payload);
        return task;
    }

    public static OrderTask setBxpTagPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setBxpTagPayload(payload);
        return task;
    }

    public static OrderTask setBxpButtonPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setBxpButtonPayload(payload);
        return task;
    }

    public static OrderTask setPirPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setPirPayload(payload);
        return task;
    }

    public static OrderTask setTofPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setTofPayload(payload);
        return task;
    }

    public static OrderTask setOtherPayload(int payload) {
        ParamsTask task = new ParamsTask();
        task.setOtherPayload(payload);
        return task;
    }

    public static OrderTask setOtherPayloadData(List<String> otherRules) {
        ParamsTask task = new ParamsTask();
        task.setOtherPayloadData(otherRules);
        return task;
    }

    public static OrderTask setFilterNameRules(ArrayList<String> filterOtherRules) {
        ParamsTask task = new ParamsTask();
        task.setFilterNameRules(filterOtherRules);
        return task;
    }

    public static OrderTask setGPSTimeout(@IntRange(from = 60, to = 600) int timeout) {
        ParamsTask task = new ParamsTask();
        task.setGPSTimeout(timeout);
        return task;
    }


    public static OrderTask setGPSPDOP(@IntRange(from = 25, to = 100) int limit) {
        ParamsTask task = new ParamsTask();
        task.setGPSPDOP(limit);
        return task;
    }

    public static OrderTask setAccWakeupCondition(@IntRange(from = 1, to = 20) int threshold,
                                                  @IntRange(from = 1, to = 10) int duration) {
        ParamsTask task = new ParamsTask();
        task.setAccWakeupCondition(threshold, duration);
        return task;
    }

    public static OrderTask setAccMotionCondition(@IntRange(from = 10, to = 250) int threshold,
                                                  @IntRange(from = 1, to = 15) int duration) {
        ParamsTask task = new ParamsTask();
        task.setAccMotionCondition(threshold, duration);
        return task;
    }

    public static OrderTask setAutoPowerOn(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setAutoPowerOn(enable);
        return task;
    }
}
