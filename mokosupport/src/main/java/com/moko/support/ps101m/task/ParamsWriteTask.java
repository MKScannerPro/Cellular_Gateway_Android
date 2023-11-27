package com.moko.support.ps101m.task;

import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.ps101m.MokoSupport;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class ParamsWriteTask extends OrderTask {
    public byte[] data;

    public ParamsWriteTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setNtpSyncInterval(@IntRange(from = 0, to = 720) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        int length = bytes.length;
        response.responseValue = data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_NTP_SYNC_INTERVAL.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
    }

    public void setNtpServer(@Nullable String ntpServer) {
        if (TextUtils.isEmpty(ntpServer)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_NTP_SERVER.getParamsKey();
            data[3] = (byte) 0;
        } else {
            byte[] bytes = ntpServer.getBytes();
            int length = bytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_NTP_SERVER.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(bytes, 0, data, 4, bytes.length);
        }
        response.responseValue = data;
    }

    public void setDataFormat(@IntRange(from = 0, to = 1) int format) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DATA_COMMUNICATION_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) format
        };
    }

    public void setAxisDataReportInterval(@IntRange(from = 0, to = 65535) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    //mqtt参数
    public void setMQTTHost(String mqttHost) {
        byte[] bytes = mqttHost.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MQTT_HOST.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setMQTTPort(@IntRange(from = 1, to = 65535) int port) {
        byte[] bytes = MokoUtils.toByteArray(port, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_PORT.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }


    public void setMQTTClientId(String clientId) {
        byte[] bytes = clientId.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MQTT_CLIENT_ID.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setMQTTSubscribeTopic(String subtopic) {
        byte[] bytes = subtopic.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SUBSCRIBE_TOPIC.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setMQTTPublishTopic(String publishTopic) {
        byte[] bytes = publishTopic.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_PUBLISH_TOPIC.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setMQTTCleanSession(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_CLEAN_SESSION.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setMQTTQos(@IntRange(from = 0, to = 2) int qos) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_QOS.getParamsKey(),
                (byte) 0x01,
                (byte) qos
        };
    }

    public void setMQTTKeepAlive(@IntRange(from = 10, to = 120) int keepAlive) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_KEEP_ALIVE.getParamsKey(),
                (byte) 0x01,
                (byte) keepAlive
        };
    }

    public void setApn(@Nullable String apn) {
        if (!TextUtils.isEmpty(apn)) {
            byte[] bytes = apn.getBytes();
            int length = bytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_APN.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(bytes, 0, data, 4, bytes.length);
        } else {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_APN.getParamsKey(),
                    (byte) 0x00
            };
        }
        response.responseValue = data;
    }

    public void setNetworkFormat(@IntRange(from = 0, to = 3) int networkFormat) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_NETWORK_FORMAT.getParamsKey(),
                (byte) 0x01,
                (byte) networkFormat
        };
    }

    public void setMQTTUsername(@Nullable String userName) {
        if (!TextUtils.isEmpty(userName)) {
            byte[] bytes = userName.getBytes();
            int length = bytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_MQTT_USERNAME.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(bytes, 0, data, 4, bytes.length);
            response.responseValue = data;
        } else {
            response.responseValue = data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_MQTT_USERNAME.getParamsKey(),
                    (byte) 0x00
            };
        }
    }

    public void setMQTTPassword(@Nullable String password) {
        if (!TextUtils.isEmpty(password)) {
            byte[] bytes = password.getBytes();
            int length = bytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_MQTT_PASSWORD.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(bytes, 0, data, 4, bytes.length);
            response.responseValue = data;
        } else {
            response.responseValue = data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_MQTT_PASSWORD.getParamsKey(),
                    (byte) 0x00
            };
        }
    }

    public void setMQTTConnectMode(@IntRange(from = 0, to = 3) int mode) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CONNECT_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
    }

    public void setMQTTLwtEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_LWT_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setMQTTLwtRetain(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_LWT_RETAIN.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setMQTTLwtQos(@IntRange(from = 0, to = 2) int qos) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MQTT_LWT_QOS.getParamsKey(),
                (byte) 0x01,
                (byte) qos
        };
    }

    public void setMQTTLwtTopic(String lwtTopic) {
        byte[] bytes = lwtTopic.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MQTT_LWT_TOPIC.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setMQTTLwtPayload(String lwtPayload) {
        byte[] bytes = lwtPayload.getBytes();
        int length = bytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MQTT_LWT_PAYLOAD.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(bytes, 0, data, 4, bytes.length);
        response.responseValue = data;
    }

    public void setFile(ParamsKeyEnum key, @Nullable File file) throws Exception {
        if (null == file) {
            data = new byte[dataLength + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) key.getParamsKey();
            data[3] = 0;
            data[4] = 0;
            data[5] = 0;
        } else {
            FileInputStream inputSteam = new FileInputStream(file);
            dataBytes = new byte[(int) file.length()];
            inputSteam.read(dataBytes);
            dataLength = dataBytes.length;
            if (dataLength % DATA_LENGTH_MAX > 0) {
                packetCount = dataLength / DATA_LENGTH_MAX + 1;
            } else {
                packetCount = dataLength / DATA_LENGTH_MAX;
            }
            remainPack = packetCount - 1;
            packetIndex = 0;
            delayTime = DEFAULT_DELAY_TIME + 500 * packetCount;
            if (packetCount > 1) {
                data = new byte[DATA_LENGTH_MAX + 6];
                data[0] = (byte) 0xEE;
                data[1] = (byte) 0x01;
                data[2] = (byte) key.getParamsKey();
                data[3] = (byte) packetCount;
                data[4] = (byte) packetIndex;
                data[5] = (byte) DATA_LENGTH_MAX;
                for (int i = 0; i < DATA_LENGTH_MAX; i++, dataOrigin++) {
                    data[i + 6] = dataBytes[dataOrigin];
                }
            } else {
                data = new byte[dataLength + 6];
                data[0] = (byte) 0xEE;
                data[1] = (byte) 0x01;
                data[2] = (byte) key.getParamsKey();
                data[3] = (byte) packetCount;
                data[4] = (byte) packetIndex;
                data[5] = (byte) dataLength;
                for (int i = 0; i < dataLength; i++) {
                    data[i + 6] = dataBytes[i];
                }
            }
        }
    }

    public void setNetworkReconnectInterval(@IntRange(from = 0, to = 100) int interval) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_NETWORK_RECONNECT_INTERVAL.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    /**
     * 上行配置参数
     *
     * @param flag  0 1
     * @param times 1-4
     */
    public void setPayloadInfo(@IntRange(from = 0, to = 1) int flag, @IntRange(from = 1, to = 4) int times, int key) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) key,
                (byte) 0x02,
                (byte) flag,
                (byte) times
        };
    }

    public void close() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CLOSE.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void reboot() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REBOOT.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void reset() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RESET.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void setTime() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        calendar.setTimeZone(timeZone);
        long time = calendar.getTimeInMillis() / 1000;
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; ++i) {
            bytes[i] = (byte) (time >> 8 * (3 - i) & 255);
        }
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TIME_UTC.getParamsKey(),
                (byte) 0x04,
                bytes[0],
                bytes[1],
                bytes[2],
                bytes[3],
        };
    }

    public void setTimeZone(@IntRange(from = -24, to = 28) int timeZone) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TIME_ZONE.getParamsKey(),
                (byte) 0x01,
                (byte) timeZone
        };
        response.responseValue = data;
    }

    public void setLowPowerPercent(@IntRange(from = 0, to = 5) int percent) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LOW_POWER_PERCENT.getParamsKey(),
                (byte) 0x01,
                (byte) percent
        };
    }

    public void setBuzzerSound(@IntRange(from = 0, to = 2) int buzzer) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUZZER_SOUND_CHOOSE.getParamsKey(),
                (byte) 0x01,
                (byte) buzzer
        };
    }

    public void setVibrationIntensity(@IntRange(from = 0, to = 100) int intensity) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_VIBRATION_INTENSITY.getParamsKey(),
                (byte) 0x01,
                (byte) intensity
        };
    }

    public void setDeviceMode(@IntRange(from = 0, to = 3) int mode) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DEVICE_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
        response.responseValue = data;
    }

    public void setIndicatorStatus(int status) {
        byte[] bytes = MokoUtils.toByteArray(status, 3);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_INDICATOR_STATUS.getParamsKey(),
                (byte) 0x03,
                bytes[0],
                bytes[1],
                bytes[2]
        };
    }

    public void setHeartBeatInterval(@IntRange(from = 1, to = 14400) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HEARTBEAT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setManufacturer(String manufacturer) {
        byte[] manufacturerBytes = manufacturer.getBytes();
        int length = manufacturerBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_MANUFACTURER.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < manufacturerBytes.length; i++) {
            data[i + 4] = manufacturerBytes[i];
        }
        response.responseValue = data;
    }


    public void setShutdownInfoReport(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SHUTDOWN_PAYLOAD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setOffByButton(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_OFF_BY_BUTTON.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setAutoPowerOn(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AUTO_POWER_ON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void resetMotorState() {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RESET_MOTOR_STATE.getParamsKey(),
                (byte) 0x00
        };
    }

    public void setLowPowerReportEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_LOW_POWER_PAYLOAD_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setPasswordVerifyEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PASSWORD_VERIFY_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void changePassword(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_PASSWORD.getParamsKey();
        data[3] = (byte) length;
        System.arraycopy(passwordBytes, 0, data, 4, passwordBytes.length);
        response.responseValue = data;
    }

    public void setAdvTimeout(@IntRange(from = 1, to = 60) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }

    public void setAdvTxPower(@IntRange(from = -40, to = 8) int txPower) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_TX_POWER.getParamsKey(),
                (byte) 0x01,
                (byte) txPower
        };
        response.responseValue = data;
    }

    public void setAdvName(@Nullable String advName) {
        if (TextUtils.isEmpty(advName)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_ADV_NAME.getParamsKey();
            data[3] = (byte) 0;
        } else {
            byte[] advNameBytes = advName.getBytes();
            int length = advNameBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_ADV_NAME.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(advNameBytes, 0, data, 4, advNameBytes.length);
        }
        response.responseValue = data;
    }

    public void setAdvInterval(@IntRange(from = 1, to = 100) int interval) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_INTERVAL.getParamsKey(),
                (byte) 0x01,
                (byte) interval
        };
    }

    public void setPeriodicPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PERIODIC_MODE_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setPeriodicReportInterval(@IntRange(from = 1, to = 14400) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_PERIODIC_MODE_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setTimePosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TIME_MODE_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setStandbyPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_STANDBY_MODE_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setTimePosReportPoints(@Nullable ArrayList<Integer> timePoints) {
        if (timePoints == null || timePoints.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_TIME_MODE_REPORT_TIME_POINT.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = timePoints.size();
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_TIME_MODE_REPORT_TIME_POINT.getParamsKey();
            data[3] = (byte) length;
            for (int i = 0; i < length; i++) {
                data[4 + i] = timePoints.get(i).byteValue();
            }
        }
        response.responseValue = data;
    }

    public void setMotionModeEvent(@IntRange(from = 0, to = 31) int event) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_EVENT.getParamsKey(),
                (byte) 0x01,
                (byte) event
        };
        response.responseValue = data;
    }

    public void setMotionStartPosStrategy(@IntRange(from = 0, to = 2) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_START_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setMotionTripInterval(@IntRange(from = 10, to = 86400) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 4);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_TRIP_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x04,
                intervalBytes[0],
                intervalBytes[1],
                intervalBytes[2],
                intervalBytes[3],
        };
        response.responseValue = data;
    }

    public void setMotionTripPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_TRIP_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setMotionEndTimeout(@IntRange(from = 1, to = 180) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_END_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }


    public void setMotionEndNumber(@IntRange(from = 1, to = 255) int number) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_END_NUMBER.getParamsKey(),
                (byte) 0x01,
                (byte) number
        };
        response.responseValue = data;
    }


    public void setMotionEndInterval(@IntRange(from = 10, to = 300) int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_END_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setMotionEndPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_END_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setMotionStationaryPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_STATIONARY_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
    }

    public void setMotionStationaryReportInterval(@IntRange(from = 1, to = 14400) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MOTION_MODE_STATIONARY_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    public void setWifiRssiFilter(@IntRange(from = -127, to = 0) int rssi) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_WIFI_RSSI_FILTER.getParamsKey(),
                (byte) 0x01,
                (byte) rssi
        };
    }

    public void setWifiPosMechanism(@IntRange(from = 0, to = 1) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_WIFI_POS_MECHANISM.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setWifiPosTimeout(@IntRange(from = 1, to = 4) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_WIFI_POS_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }

    public void setWifiPosBSSIDNumber(@IntRange(from = 1, to = 15) int number) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_WIFI_POS_BSSID_NUMBER.getParamsKey(),
                (byte) 0x01,
                (byte) number
        };
        response.responseValue = data;
    }

    public void setBlePosTimeout(@IntRange(from = 1, to = 10) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_POS_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }

    public void setBlePosNumber(@IntRange(from = 1, to = 15) int number) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_POS_MAC_NUMBER.getParamsKey(),
                (byte) 0x01,
                (byte) number
        };
        response.responseValue = data;
    }

    public void setFilterRSSI(@IntRange(from = -127, to = 0) int rssi) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RSSI.getParamsKey(),
                (byte) 0x01,
                (byte) rssi
        };
        response.responseValue = data;
    }

    public void setFilterBleScanPhy(@IntRange(from = 0, to = 4) int type) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BLE_SCAN_PHY.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
        response.responseValue = data;
    }

    public void setFilterRelationship(@IntRange(from = 0, to = 6) int relationship) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RELATIONSHIP.getParamsKey(),
                (byte) 0x01,
                (byte) relationship
        };
        response.responseValue = data;
    }

    public void setFilterMacPrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MAC_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterMacReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MAC_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterMacRules(ArrayList<String> filterMacRules) {
        if (filterMacRules == null || filterMacRules.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_MAC_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String mac : filterMacRules) {
                length += 1;
                length += mac.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_MAC_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterMacRules.size(); i < size; i++) {
                String mac = filterMacRules.get(i);
                byte[] macBytes = MokoUtils.hex2bytes(mac);
                int l = macBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = macBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setFilterNamePrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterNameReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterRawData(int unknown, int ibeacon,
                                 int eddystone_uid, int eddystone_url, int eddystone_tlm,
                                 int bxp_acc, int bxp_th,
                                 int mkibeacon, int mkibeacon_acc) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_RAW_DATA.getParamsKey(),
                (byte) 0x09,
                (byte) unknown,
                (byte) ibeacon,
                (byte) eddystone_uid,
                (byte) eddystone_url,
                (byte) eddystone_tlm,
                (byte) bxp_acc,
                (byte) bxp_th,
                (byte) mkibeacon,
                (byte) mkibeacon_acc
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_IBEACON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                           @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_IBEACON_MAJOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                           @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_IBEACON_MINOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterIBeaconUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_IBEACON_UUID.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
            int length = uuidBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_IBEACON_UUID.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(uuidBytes, 0, data, 4, uuidBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconMajorRange(@IntRange(from = 0, to = 65535) int min,
                                              @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_MAJOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconMinorRange(@IntRange(from = 0, to = 65535) int min,
                                              @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_MINOR_RANGE.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
        response.responseValue = data;
    }

    public void setFilterBXPIBeaconUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_UUID.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
            int length = uuidBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_IBEACON_UUID.getParamsKey();
            data[3] = (byte) length;
            for (int i = 0; i < uuidBytes.length; i++) {
                data[i + 4] = uuidBytes[i];
            }
        }
        response.responseValue = data;
    }

    public void setFilterBXPTagEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagPrecise(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_PRECISE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagReverse(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_REVERSE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTagRules(ArrayList<String> filterBXPTagRules) {
        if (filterBXPTagRules == null || filterBXPTagRules.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String mac : filterBXPTagRules) {
                length += 1;
                length += mac.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_BXP_TAG_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterBXPTagRules.size(); i < size; i++) {
                String mac = filterBXPTagRules.get(i);
                byte[] macBytes = MokoUtils.hex2bytes(mac);
                int l = macBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = macBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setFilterMkPirEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setFilterMkPirSensorDetectionStatus(@IntRange(from = 0, to = 2) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DETECTION_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirSensorSensitivity(@IntRange(from = 0, to = 3) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirDoorStatus(@IntRange(from = 0, to = 2) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DOOR_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirDelayResStatus(@IntRange(from = 0, to = 3) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_DELAY_RES_STATUS.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setFilterMkPirMajorRange(@IntRange(from = 0, to = 65535) int min,
                                         @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_MAJOR.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
    }

    public void setFilterMkPirMinorRange(@IntRange(from = 0, to = 65535) int min,
                                         @IntRange(from = 0, to = 65535) int max) {
        byte[] minBytes = MokoUtils.toByteArray(min, 2);
        byte[] maxBytes = MokoUtils.toByteArray(max, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_MK_PIR_MINOR.getParamsKey(),
                (byte) 0x04,
                minBytes[0],
                minBytes[1],
                maxBytes[0],
                maxBytes[1]
        };
    }

    public void setFilterBXPButtonEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPButtonRules(@IntRange(from = 0, to = 1) int singleEnable,
                                        @IntRange(from = 0, to = 1) int doubleEnable,
                                        @IntRange(from = 0, to = 1) int longEnable,
                                        @IntRange(from = 0, to = 1) int abnormalEnable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_BUTTON_RULES.getParamsKey(),
                (byte) 0x04,
                (byte) singleEnable,
                (byte) doubleEnable,
                (byte) longEnable,
                (byte) abnormalEnable,
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDNamespace(String namespace) {
        if (TextUtils.isEmpty(namespace)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = MokoUtils.hex2bytes(namespace);
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_NAMESPACE.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(dataBytes, 0, data, 4, dataBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneUIDInstance(String instance) {
        if (TextUtils.isEmpty(instance)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = MokoUtils.hex2bytes(instance);
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_UID_INSTANCE.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(dataBytes, 0, data, 4, dataBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneUrlEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            data = new byte[4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL.getParamsKey();
            data[3] = (byte) 0x00;
        } else {
            byte[] dataBytes = url.getBytes();
            int length = dataBytes.length;
            data = new byte[length + 4];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_URL.getParamsKey();
            data[3] = (byte) length;
            System.arraycopy(dataBytes, 0, data, 4, dataBytes.length);
        }
        response.responseValue = data;
    }

    public void setFilterEddystoneTlmEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterEddystoneTlmVersion(@IntRange(from = 0, to = 2) int version) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_EDDYSTONE_TLM_VERSION.getParamsKey(),
                (byte) 0x01,
                (byte) version
        };
        response.responseValue = data;
    }

    public void setFilterBXPAccEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_ACC.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPTHEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_TH.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterBXPDeviceEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_BXP_DEVICE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterOtherEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_OTHER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setFilterOtherRelationship(@IntRange(from = 0, to = 5) int relationship) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RELATIONSHIP.getParamsKey(),
                (byte) 0x01,
                (byte) relationship
        };
        response.responseValue = data;
    }

    public void setFilterOtherRules(ArrayList<String> filterOtherRules) {
        if (filterOtherRules == null || filterOtherRules.size() == 0) {
            data = new byte[]{
                    (byte) 0xED,
                    (byte) 0x01,
                    (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RULES.getParamsKey(),
                    (byte) 0x00
            };
        } else {
            int length = 0;
            for (String other : filterOtherRules) {
                length += 1;
                length += other.length() / 2;
            }
            data = new byte[4 + length];
            data[0] = (byte) 0xED;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_OTHER_RULES.getParamsKey();
            data[3] = (byte) length;
            int index = 0;
            for (int i = 0, size = filterOtherRules.size(); i < size; i++) {
                String rule = filterOtherRules.get(i);
                byte[] ruleBytes = MokoUtils.hex2bytes(rule);
                int l = ruleBytes.length;
                data[4 + index] = (byte) l;
                index++;
                for (int j = 0; j < l; j++, index++) {
                    data[4 + index] = ruleBytes[j];
                }
            }
        }
        response.responseValue = data;
    }

    public void setGPSPosTimeoutL76(@IntRange(from = 60, to = 600) int timeout) {
        byte[] timeoutBytes = MokoUtils.toByteArray(timeout, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_GPS_POS_TIMEOUT_L76C.getParamsKey(),
                (byte) 0x02,
                timeoutBytes[0],
                timeoutBytes[1]
        };
        response.responseValue = data;
    }


    public void setGPSPDOPLimitL76(@IntRange(from = 25, to = 100) int limit) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_GPS_PDOP_LIMIT_L76C.getParamsKey(),
                (byte) 0x01,
                (byte) limit,
        };
        response.responseValue = data;
    }

    public void setBlePosMechanism(@IntRange(from = 0, to = 1) int mechanism) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_POS_MECHANISM.getParamsKey(),
                (byte) 0x01,
                (byte) mechanism
        };
        response.responseValue = data;
    }

    public void setDownLinkPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DOWN_LINK_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
        response.responseValue = data;
    }

    public void setAccWakeupCondition(@IntRange(from = 1, to = 20) int threshold,
                                      @IntRange(from = 1, to = 10) int duration) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ACC_WAKEUP_CONDITION.getParamsKey(),
                (byte) 0x02,
                (byte) threshold,
                (byte) duration
        };
        response.responseValue = data;
    }

    public void setAccMotionCondition(@IntRange(from = 10, to = 250) int threshold,
                                      @IntRange(from = 1, to = 15) int duration) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ACC_MOTION_CONDITION.getParamsKey(),
                (byte) 0x02,
                (byte) threshold,
                (byte) duration
        };
        response.responseValue = data;
    }

    public void setManDownDetectionEnable(int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setManDownDetectionTimeout(@IntRange(from = 1, to = 120) int timeout) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_TIMEOUT.getParamsKey(),
                (byte) 0x01,
                (byte) timeout
        };
        response.responseValue = data;
    }

    public void setManDownPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MAN_DOWN_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
    }

    public void setManDownReportInterval(@IntRange(from = 10, to = 600) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_MAN_DOWN_DETECTION_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    public void setAlarmType(@IntRange(from = 0, to = 2) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setAlarmExitTime(@IntRange(from = 5, to = 15) int time) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_EXIT_TIME.getParamsKey(),
                (byte) 0x01,
                (byte) time
        };
    }

    public void setAlarmAlertTriggerType(@IntRange(from = 0, to = 4) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_ALERT_TRIGGER_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setAlarmAlertPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_ALERT_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
    }

    public void setAlarmAlertNotifyEnable(int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_ALERT_NOTIFY_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    //
    public void setAlarmSosTriggerType(@IntRange(from = 0, to = 4) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_SOS_TRIGGER_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
    }

    public void setAlarmSosPosStrategy(@IntRange(from = 0, to = 6) int strategy) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_SOS_POS_STRATEGY.getParamsKey(),
                (byte) 0x01,
                (byte) strategy
        };
    }

    public void setAlarmSosReportInterval(@IntRange(from = 10, to = 600) int interval) {
        byte[] bytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_SOS_REPORT_INTERVAL.getParamsKey(),
                (byte) 0x02,
                bytes[0],
                bytes[1]
        };
    }

    public void setAlarmSosNotifyEnable(int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ALARM_SOS_NOTIFY_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void readStorageData(@IntRange(from = 1, to = 65535) int time) {
        byte[] rawDataBytes = MokoUtils.toByteArray(time, 2);
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_READ_STORAGE_DATA.getParamsKey(),
                (byte) 0x02,
                rawDataBytes[0],
                rawDataBytes[1]
        };
    }

    public void clearStorageData() {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CLEAR_STORAGE_DATA.getParamsKey(),
                (byte) 0x00
        };
    }

    public void setSyncEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SYNC_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
    }

    public void setFilterNameRules(ArrayList<String> filterNameRules) {
        int length = 0;
        for (String name : filterNameRules) {
            length += 1;
            length += name.length();
        }
        dataBytes = new byte[length];
        int index = 0;
        for (int i = 0, size = filterNameRules.size(); i < size; i++) {
            String name = filterNameRules.get(i);
            byte[] nameBytes = name.getBytes();
            int l = nameBytes.length;
            dataBytes[index] = (byte) l;
            index++;
            for (int j = 0; j < l; j++, index++) {
                dataBytes[index] = nameBytes[j];
            }
        }
        dataLength = dataBytes.length;
        if (dataLength != 0) {
            if (dataLength % DATA_LENGTH_MAX > 0) {
                packetCount = dataLength / DATA_LENGTH_MAX + 1;
            } else {
                packetCount = dataLength / DATA_LENGTH_MAX;
            }
        } else {
            packetCount = 1;
        }
        remainPack = packetCount - 1;
        packetIndex = 0;
        delayTime = DEFAULT_DELAY_TIME + 500 * packetCount;
        if (packetCount > 1) {
            data = new byte[DATA_LENGTH_MAX + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey();
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) DATA_LENGTH_MAX;
            for (int i = 0; i < DATA_LENGTH_MAX; i++, dataOrigin++) {
                data[i + 6] = dataBytes[dataOrigin];
            }
        } else {
            data = new byte[dataLength + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey();
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) dataLength;
            for (int i = 0; i < dataLength; i++) {
                data[i + 6] = dataBytes[i];
            }
        }
    }

    private int packetCount;
    private int packetIndex;
    private int remainPack;
    private int dataLength;
    private int dataOrigin;
    private byte[] dataBytes;
    private static final int DATA_LENGTH_MAX = 176;

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        if (header == 0xED)
            return true;
        final int cmd = value[2] & 0xFF;
        final int result = value[4] & 0xFF;
        if (result == 1) {
            remainPack--;
            packetIndex++;
            if (remainPack >= 0) {
                assembleRemainData(cmd);
                return false;
            }
            return true;
        }
        return false;
    }

    private void assembleRemainData(int cmd) {
        int length = dataLength - dataOrigin;
        if (length > DATA_LENGTH_MAX) {
            data = new byte[DATA_LENGTH_MAX + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) cmd;
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) DATA_LENGTH_MAX;
            for (int i = 0; i < DATA_LENGTH_MAX; i++, dataOrigin++) {
                data[i + 6] = dataBytes[dataOrigin];
            }
        } else {
            data = new byte[length + 6];
            data[0] = (byte) 0xEE;
            data[1] = (byte) 0x01;
            data[2] = (byte) cmd;
            data[3] = (byte) packetCount;
            data[4] = (byte) packetIndex;
            data[5] = (byte) length;
            for (int i = 0; i < length; i++, dataOrigin++) {
                data[i + 6] = dataBytes[dataOrigin];
            }
        }
        MokoSupport.getInstance().sendDirectOrder(this);
    }
}
