package com.moko.support.ps101m.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    //// 系统相关参数

    // 时区

    // 芯片MAC

    // 自检状态
    KEY_SELFTEST_STATUS(0x16),
    // 产测状态
    KEY_PCBA_STATUS(0x17),
    // 读取当前需求版本
    KEY_DEMAND_VERSION(0x18),
    // 电池电量

    // 厂家信息
    KEY_MANUFACTURER(0x1A),
    // 工作模式选择
    // 关机信息上报
    KEY_SHUTDOWN_PAYLOAD_ENABLE(0x1C),
    //按键关机
    KEY_OFF_BY_BUTTON(0x1D),
    // 低电触发心跳开关

    // 设备心跳间隔
    KEY_HEARTBEAT_INTERVAL(0x20),
    //蜂鸣器声效选择
    KEY_BUZZER_SOUND_CHOOSE(0x23),
    //马达震动强度选择
    KEY_VIBRATION_INTENSITY(0x24),
    //马达异常状态
    KEY_MOTOR_STATE(0x25),
    //清除马达异常状态
    KEY_RESET_MOTOR_STATE(0x26),
    // 指示灯开关
    KEY_INDICATOR_STATUS(0x27),
    //充电自动开机
    KEY_AUTO_POWER_ON_ENABLE(0x28),
    //网络状态
    KEY_NETWORK_STATUS(0xC4),
    //MQTT连接状态
    KEY_MQTT_CONNECT_STATUS(0xC5),
    //ntp服务器


    //// 蓝牙相关参数
    // 登录是否需要密码

    //蓝牙广播超时时间


    //// 模式相关参数
    //待机模式定位策略
    KEY_STANDBY_MODE_POS_STRATEGY(0x3F),
    // 定期模式定位策略
    KEY_PERIODIC_MODE_POS_STRATEGY(0x40),
    // 定期模式上报间隔
    // 定时模式定位策略
    KEY_TIME_MODE_POS_STRATEGY(0x42),
    // 定时模式时间点
    KEY_TIME_MODE_REPORT_TIME_POINT(0x43),
    // 运动模式事件
    KEY_MOTION_MODE_EVENT(0x44),
    // 运动开始定位策略
    KEY_MOTION_MODE_START_POS_STRATEGY(0x46),
    // 运动中定位间隔
    KEY_MOTION_MODE_TRIP_REPORT_INTERVAL(0x47),
    // 运动中定位策略
    KEY_MOTION_MODE_TRIP_POS_STRATEGY(0x48),
    // 运动结束判断时间
    KEY_MOTION_MODE_END_TIMEOUT(0x49),
    // 运动结束定位次数
    KEY_MOTION_MODE_END_NUMBER(0x4A),
    // 运动结束定位间隔
    KEY_MOTION_MODE_END_REPORT_INTERVAL(0x4B),
    // 运动结束定位策略
    KEY_MOTION_MODE_END_POS_STRATEGY(0x4C),
    //运动静止状态定位策略
    KEY_MOTION_MODE_STATIONARY_POS_STRATEGY(0x4D),
    //运动禁止状态上报间隔
    KEY_MOTION_MODE_STATIONARY_REPORT_INTERVAL(0x4E),


    KEY_FILTER_MAC_PRECISE(0x53),
    KEY_FILTER_MAC_REVERSE(0x54),
    KEY_FILTER_MAC_RULES(0x55),
    // 精准过滤ADV Name开关
    KEY_FILTER_NAME_PRECISE(0x56),
    // 反向过滤ADV Name开关
    KEY_FILTER_NAME_REVERSE(0x57),
    // NAME过滤规则
    KEY_FILTER_NAME_RULES(0x58),
    // 过滤设备类型开关
    KEY_FILTER_RAW_DATA(0x59),
    // BXP-iBeacon类型过滤开关
    KEY_FILTER_BXP_IBEACON_ENABLE(0x5A),
    // BXP-iBeacon类型Major范围


    //// 定位参数
    // WIFI定位数据格式
    KEY_FIX_MODE(0x90),
    KEY_PERIODIC_FIX_INTERVAL(0x91),
    // 三轴唤醒条件
    KEY_ACC_WAKEUP_CONDITION(0x92),
    // 运动检测判断
    KEY_ACC_MOTION_CONDITION(0x93),
    KEY_MOTION_FIX_ENABLE_WHEN_START(0x94),
    KEY_MOTION_FIX_ENABLE_WHEN_TRIP(0x95),
    KEY_MOTION_FIX_INTERVAL_WHEN_TRIP(0x96),
    KEY_MOTION_FIX_TIMEOUT_WHEN_STOP(0x97),
    KEY_MOTION_FIX_ENABLE_WHEN_STOP(0x98),
    KEY_MOTION_FIX_ENABLE_WHEN_STATIONARY(0x99),
    KEY_MOTION_FIX_INTERVAL_WHEN_STATIONARY(0x9A),
    KEY_GPS_TIMEOUT(0x9B),
    KEY_GPS_PDOP(0x9C),

    KEY_SCAN_REPORT_MODE(0x40),
    KEY_SCAN_REPORT_ENABLE(0x41),
    KEY_REAL_SCAN_PERIODIC_REPORT_INTERVAL(0x42),
    KEY_PERIODIC_SCAN_IMMEDIATE_REPORT(0x43),
    KEY_PERIODIC_SCAN_PERIODIC_REPORT(0x44),
    KEY_UPLOAD_PRIORITY(0x45),
    KEY_DATA_RETENTION_PRIORITY(0x46),

    KEY_FILTER_RSSI(0x50),
    KEY_FILTER_PHY(0x51),
    KEY_FILTER_RELATIONSHIP(0x52),
    KEY_DUPLICATE_DATA_FILTER(0x7A),
    KEY_FILTER_IBEACON_ENABLE(0x5A),
    KEY_FILTER_IBEACON_MAJOR_RANGE(0x5B),
    KEY_FILTER_IBEACON_MINOR_RANGE(0x5C),
    KEY_FILTER_IBEACON_UUID(0x5D),
    KEY_FILTER_EDDYSTONE_UID_ENABLE(0x5E),
    KEY_FILTER_EDDYSTONE_UID_NAMESPACE(0x5F),
    KEY_FILTER_EDDYSTONE_UID_INSTANCE(0x60),
    KEY_FILTER_EDDYSTONE_URL_ENABLE(0x61),
    KEY_FILTER_EDDYSTONE_URL(0x62),
    KEY_FILTER_EDDYSTONE_TLM_ENABLE(0x63),
    KEY_FILTER_EDDYSTONE_TLM_VERSION(0x64),
    KEY_FILTER_BXP_DEVICE(0x65),
    KEY_FILTER_BXP_ACC(0x66),
    KEY_FILTER_BXP_TH(0x67),
    KEY_FILTER_BXP_BUTTON_ENABLE(0x68),
    KEY_FILTER_BXP_BUTTON_RULES(0x69),
    KEY_FILTER_BXP_TAG_ENABLE(0x6A),
    // 精准过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_PRECISE(0x6B),
    // 反向过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_REVERSE(0x6C),
    // BXP-Tag过滤规则
    KEY_FILTER_BXP_TAG_RULES(0x6D),
    KEY_FILTER_MK_PIR_ENABLE(0x6E),
    KEY_FILTER_MK_PIR_DELAY_RES_STATUS(0x6F),
    KEY_FILTER_MK_PIR_DOOR_STATUS(0x70),
    KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY(0x71),
    KEY_FILTER_MK_PIR_DETECTION_STATUS(0x72),
    KEY_FILTER_MK_PIR_MAJOR(0x73),
    KEY_FILTER_MK_PIR_MINOR(0x74),
    KEY_FILTER_MK_TOF_ENABLE(0x75),
    KEY_FILTER_MK_TOF_RULES(0x76),
    KEY_FILTER_OTHER_ENABLE(0x77),
    KEY_FILTER_OTHER_RELATIONSHIP(0x78),
    KEY_FILTER_OTHER_RULES(0x79),
    KEY_IBEACON_PAYLOAD(0xA0),
    KEY_EDDYSTONE_UID_PAYLOAD(0xA1),
    KEY_EDDYSTONE_URL_PAYLOAD(0xA2),
    KEY_EDDYSTONE_TLM_PAYLOAD(0xA3),
    KEY_BXP_DEVICE_INFO_PAYLOAD(0xA4),
    KEY_BXP_ACC_PAYLOAD(0xA5),
    KEY_BXP_TH_PAYLOAD(0xA6),
    KEY_BXP_BUTTON_PAYLOAD(0xA7),
    KEY_BXP_TAG_PAYLOAD(0xA8),
    KEY_PIR_PAYLOAD(0xA9),
    KEY_TOF_PAYLOAD(0xAA),
    KEY_OTHER_PAYLOAD(0xAB),
    KEY_OTHER_PAYLOAD_DATA(0xAC),

    KEY_BATTERY_POWER(0xC0),
    KEY_POWER_LOSS_NOTIFY(0x13),
    KEY_LED_INDICATOR(0x0C),
    KEY_PASSWORD_VERIFY_ENABLE(0x15),
    KEY_PASSWORD(0x14),
    KEY_ADV_RESPONSE(0x80),
    KEY_ADV_NAME(0x81),
    KEY_IBEACON_MAJOR(0x82),
    KEY_IBEACON_MINOR(0x83),
    KEY_IBEACON_UUID(0x84),
    KEY_IBEACON_RSSI1M(0x85),
    KEY_ADV_INTERVAL(0x86),
    KEY_ADV_TX_POWER(0x87),
    KEY_ADV_TIMEOUT(0x88),
    KEY_DEVICE_PAYLOAD_INTERVAL(0x11),
    KEY_DEVICE_STATUS_CHOOSE(0x12),
    KEY_TIME_ZONE(0x10),
    KEY_NTP_SWITCH(0x0D),
    KEY_NTP_SYNC_INTERVAL(0x0E),
    KEY_NTP_SERVER(0x0F),
    KEY_LOW_POWER_NOTIFY_ENABLE(0x16),
    KEY_LOW_POWER_PERCENT(0x17),
    KEY_CHIP_MAC(0x06),
    KEY_IMEI(0xC1),
    KEY_ICC_ID(0xC2),
    KEY_DELETE_BUFFER_DATA(0x18),
    KEY_CLOSE(0x02),
    KEY_REBOOT(0x01),
    KEY_RESET(0x0B),








    KEY_WIFI_RSSI_FILTER(0x7F),
    //wifi定位机制
    KEY_WIFI_POS_MECHANISM(0x80),
    // WIFI定位超时时间
    KEY_WIFI_POS_TIMEOUT(0x81),
    // WIFI定位成功BSSID数量
    KEY_WIFI_POS_BSSID_NUMBER(0x82),
    // 蓝牙定位机制选择
    KEY_BLE_POS_MECHANISM(0x83),
    // 蓝牙定位超时时间
    KEY_BLE_POS_TIMEOUT(0x84),
    // 蓝牙定位成功MAC数量
    KEY_BLE_POS_MAC_NUMBER(0x85),


    ////网络通信参数
    KEY_NETWORK_PRIORITY(0x30),
    KEY_APN(0x31),
    KEY_APN_NAME(0x32),
    KEY_APN_PASSWORD(0x33),
    KEY_CONNECT_NETWORK_TIMEOUT(0x34),


    //////
    /////MQTT参数
    KEY_MQTT_HOST(0x20),
    KEY_MQTT_PORT(0x21),
    KEY_MQTT_CLIENT_ID(0x22),
    KEY_MQTT_USERNAME(0x23),
    KEY_MQTT_PASSWORD(0x24),
    KEY_MQTT_CLEAN_SESSION(0x25),
    KEY_MQTT_KEEP_ALIVE(0x26),
    KEY_MQTT_QOS(0x27),
    KEY_SUBSCRIBE_TOPIC(0x28),
    KEY_PUBLISH_TOPIC(0x29),
    KEY_CONNECT_MODE(0x2A),
    KEY_MQTT_CA(0x2B),
    KEY_MQTT_CLIENT_CERT(0x2C),
    KEY_MQTT_CLIENT_KEY(0x2D),


    //// 辅助功能参数
    // 下行请求定位策略
    KEY_DOWN_LINK_POS_STRATEGY(0xB0),
    // 闲置功能使能
    KEY_MAN_DOWN_DETECTION_ENABLE(0xB1),
    //ManDown 定位策略
    KEY_MAN_DOWN_POS_STRATEGY(0xB2),
    // 闲置超时时间
    KEY_MAN_DOWN_DETECTION_TIMEOUT(0xB3),
    //ManDown 定 位 数 据上报间隔
    KEY_MAN_DOWN_DETECTION_REPORT_INTERVAL(0xB4),
    //报警类型选择
    KEY_ALARM_TYPE(0xB5),
    //退出报警按键时间
    KEY_ALARM_EXIT_TIME(0xB6),
    //Alert 报警触发按键模式
    KEY_ALARM_ALERT_TRIGGER_TYPE(0xB7),
    //Alert 报警定位策略
    KEY_ALARM_ALERT_POS_STRATEGY(0xB8),
    //Alert 报警事件通知
    KEY_ALARM_ALERT_NOTIFY_ENABLE(0xB9),
    //SOS 报警触发按键
    KEY_ALARM_SOS_TRIGGER_TYPE(0xBA),
    //SOS 报警定位策略
    KEY_ALARM_SOS_POS_STRATEGY(0xBB),
    //SOS 定位数据上报间隔
    KEY_ALARM_SOS_REPORT_INTERVAL(0xBC),
    //SOS 报警事件通知
    KEY_ALARM_SOS_NOTIFY_ENABLE(0xBD),
    //三轴数据上报间隔
    KEY_AXIS_REPORT_INTERVAL(0xBE),


    //// 存储协议
    // 读取存储的数据
    KEY_READ_STORAGE_DATA(0xC0),
    //清除存储的所有数据
    KEY_CLEAR_STORAGE_DATA(0xC1),
    //暂停/恢复传输
    KEY_SYNC_ENABLE(0xC2),
    ;

    private final int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }

    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
