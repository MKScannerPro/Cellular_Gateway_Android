package com.moko.support.lw006.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    //// 系统相关参数
    KEY_CLOSE(0x10),
    KEY_REBOOT(0x11),
    KEY_RESET(0x12),
    // 时间同步
    KEY_TIME_UTC(0x13),
    // 时区
    KEY_TIME_ZONE(0x14),
    // 芯片MAC
    KEY_CHIP_MAC(0x15),
    // 自检状态
    KEY_SELFTEST_STATUS(0x16),
    // 产测状态
    KEY_PCBA_STATUS(0x17),
    // 读取当前需求版本
    KEY_DEMAND_VERSION(0x18),
    // 电池电量
    KEY_BATTERY_POWER(0x19),
    // 厂家信息
    KEY_MANUFACTURER(0x1A),
    // 工作模式选择
    KEY_DEVICE_MODE(0x1B),
    // 关机信息上报
    KEY_SHUTDOWN_PAYLOAD_ENABLE(0x1C),
    //按键关机
    KEY_OFF_BY_BUTTON(0x1D),
    // 低电触发心跳开关
    KEY_LOW_POWER_PAYLOAD_ENABLE(0x1E),
    //低电百分比
    KEY_LOW_POWER_PERCENT(0x1F),
    // 设备心跳间隔
    KEY_HEARTBEAT_INTERVAL(0x20),
    // 三轴唤醒条件
    KEY_ACC_WAKEUP_CONDITION(0x21),
    // 运动检测判断
    KEY_ACC_MOTION_CONDITION(0x22),
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
    // 电池信息信息
    KEY_BATTERY_INFO(0x28),
    // 电池信息清除
    KEY_BATTERY_RESET(0x29),
    //充电自动开机
    KEY_AUTO_POWER_ON_ENABLE(0x2B),
    //硬件版本
    KEY_HARDWARE_VERSION(0x2C),


    //// 蓝牙相关参数
    // 登录是否需要密码
    KEY_PASSWORD_VERIFY_ENABLE(0x30),
    //连接密码
    KEY_PASSWORD(0x31),
    //蓝牙广播超时时间
    KEY_ADV_TIMEOUT(0x32),
    //蓝牙 TX power
    KEY_ADV_TX_POWER(0x33),
    //广播名称
    KEY_ADV_NAME(0x34),
    //广播间隔
    KEY_ADV_INTERVAL(0x35),


    //// 模式相关参数
    //待机模式定位策略
    KEY_STANDBY_MODE_POS_STRATEGY(0x3F),
    // 定期模式定位策略
    KEY_PERIODIC_MODE_POS_STRATEGY(0x40),
    // 定期模式上报间隔
    KEY_PERIODIC_MODE_REPORT_INTERVAL(0x41),
    // 定时模式定位策略
    KEY_TIME_MODE_POS_STRATEGY(0x42),
    // 定时模式时间点
    KEY_TIME_MODE_REPORT_TIME_POINT(0x43),
    // 运动模式事件
    KEY_MOTION_MODE_EVENT(0x44),
    // 运动开始定位上报次数
    KEY_MOTION_MODE_START_NUMBER(0x45),
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



    ////蓝牙扫描过滤参数
    // 蓝牙扫描PHY选择
    KEY_FILTER_BLE_SCAN_PHY(0x50),
    // RSSI过滤规则
    KEY_FILTER_RSSI(0x51),
    // 广播内容过滤逻辑
    KEY_FILTER_RELATIONSHIP(0x52),
    // 精准过滤MAC开关
    KEY_FILTER_MAC_PRECISE(0x53),
    // 反向过滤MAC开关
    KEY_FILTER_MAC_REVERSE(0x54),
    // MAC过滤规则
    KEY_FILTER_MAC_RULES(0x55),
    // 精准过滤ADV Name开关
    KEY_FILTER_NAME_PRECISE(0x56),
    // 反向过滤ADV Name开关
    KEY_FILTER_NAME_REVERSE(0x57),
    // NAME过滤规则
    KEY_FILTER_NAME_RULES(0x58),
    // 过滤设备类型开关
    KEY_FILTER_RAW_DATA(0x59),
    // iBeacon类型过滤开关
    KEY_FILTER_IBEACON_ENABLE(0x5A),
    // iBeacon类型Major范围
    KEY_FILTER_IBEACON_MAJOR_RANGE(0x5B),
    // iBeacon类型Minor范围
    KEY_FILTER_IBEACON_MINOR_RANGE(0x5C),
    // iBeacon类型UUID
    KEY_FILTER_IBEACON_UUID(0x5D),
    // eddystone-UID类型过滤开关
    KEY_FILTER_EDDYSTONE_UID_ENABLE(0x5E),
    // eddystone-UID类型Namespace
    KEY_FILTER_EDDYSTONE_UID_NAMESPACE(0x5F),
    // eddystone-UID类型Instance
    KEY_FILTER_EDDYSTONE_UID_INSTANCE(0x60),
    // eddystone-URL类型过滤开关
    KEY_FILTER_EDDYSTONE_URL_ENABLE(0x61),
    // eddystone-URL类型URL
    KEY_FILTER_EDDYSTONE_URL(0x62),
    // eddystone-TLM类型过滤开关
    KEY_FILTER_EDDYSTONE_TLM_ENABLE(0x63),
    // eddystone- TLM类型TLMVersion
    KEY_FILTER_EDDYSTONE_TLM_VERSION(0x64),
    // BXP-iBeacon类型过滤开关
    KEY_FILTER_BXP_IBEACON_ENABLE(0x65),
    // BXP-iBeacon类型Major范围
    KEY_FILTER_BXP_IBEACON_MAJOR_RANGE(0x66),
    // BXP-iBeacon类型Minor范围
    KEY_FILTER_BXP_IBEACON_MINOR_RANGE(0x67),
    // BXP-iBeacon类型UUID
    KEY_FILTER_BXP_IBEACON_UUID(0x68),
    // BXP-Device类型过滤开关
    KEY_FILTER_BXP_DEVICE(0x69),
    // BeaconX Pro-ACC设备过滤开关
    KEY_FILTER_BXP_ACC(0x6A),
    // BeaconX Pro-T&H设备过滤开关
    KEY_FILTER_BXP_TH(0x6B),
    // BXP-Button类型过滤开关
    KEY_FILTER_BXP_BUTTON_ENABLE(0x6C),
    // BXP-Button类型过滤规则
    KEY_FILTER_BXP_BUTTON_RULES(0x6D),
    // BXP-Tag开关类型过滤开关
    KEY_FILTER_BXP_TAG_ENABLE(0x6E),
    // 精准过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_PRECISE(0x6F),
    // 反向过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_REVERSE(0x70),
    // BXP-Tag过滤规则
    KEY_FILTER_BXP_TAG_RULES(0x71),
    //MK-PIR 设备过滤开关
    KEY_FILTER_MK_PIR_ENABLE(0x72),
    //MK-PIR 设备过滤
    //sensor_detection_status
    KEY_FILTER_MK_PIR_DETECTION_STATUS(0x73),
    //MK-PIR 设备过滤
    //sensor_sensitivity
    KEY_FILTER_MK_PIR_SENSOR_SENSITIVITY(0x74),
    //MK-PIR 设备过滤
    //door_status
    KEY_FILTER_MK_PIR_DOOR_STATUS(0x75),
    //MK-PIR 设备过滤
    //delay_response_status
    KEY_FILTER_MK_PIR_DELAY_RES_STATUS(0x76),
    //MK-PIR 设备
    //Major 过滤范围
    KEY_FILTER_MK_PIR_MAJOR(0x77),
    //MK-PIR 设备
    //Minor 过滤范围
    KEY_FILTER_MK_PIR_MINOR(0x78),
    // Unknown设备过滤开关
    KEY_FILTER_OTHER_ENABLE(0x79),
    // 3组unknown过滤规则逻辑
    KEY_FILTER_OTHER_RELATIONSHIP(0x7A),
    // unknown类型过滤规则
    KEY_FILTER_OTHER_RULES(0x7B),


    //// 定位参数
    // WIFI定位数据格式
    KEY_WIFI_POS_DATA_TYPE(0x7D),
    //wifi定位机制
    KEY_WIFI_POS_MECHANISM(0x7E),
    // WIFI定位超时时间
    KEY_WIFI_POS_TIMEOUT(0x7F),
    // WIFI定位成功BSSID数量
    KEY_WIFI_POS_BSSID_NUMBER(0x80),
    // 蓝牙定位机制选择
    KEY_BLE_POS_MECHANISM(0x81),
    // 蓝牙定位超时时间
    KEY_BLE_POS_TIMEOUT(0x82),
    // 蓝牙定位成功MAC数量
    KEY_BLE_POS_MAC_NUMBER(0x83),
    //gps型号选择
    KEY_GPS_MODULE(0x84),
    // GPS极限上传模式（L76版本）
    KEY_GPS_EXTREME_MODE_L76C(0x85),
    // GPS定位超时时间（L76版本）
    KEY_GPS_POS_TIMEOUT_L76C(0x86),
    // GPS位置精度因子PDOP（L76版本）
    KEY_GPS_PDOP_LIMIT_L76C(0x87),
    // GPS定位数据格式（LR1110版本）
    KEY_GPS_POS_DATA_TYPE(0x88),
    // GPS定位超时时间（LR1110版本）
    KEY_GPS_POS_TIMEOUT(0x89),
    // GPS搜星数量（LR1110版本）
    KEY_GPS_POS_SATELLITE_THRESHOLD(0x8A),
    // GPS定位星座（LR1110版本）
    KEY_GPS_POS_SYSTEM(0x8B),
    // 定位方式选择（LR1110版本）
    KEY_GPS_POS_AUTONMOUS_AIDING_ENABLE(0x8C),
    // 辅助定位经纬度（LR1110版本）
    KEY_GPS_POS_AUXILIARY_LAT_LON(0x8D),
    // 星历开始更新事件开关
    KEY_GPS_POS_EPHEMERIS_NOTIFY_ENABLE(0x8E),
    // 离线定位功能开关
    KEY_OFFLINE_LOCATION_ENABLE(0x8F),



    //// LoRaWAN参数
    // LoRaWAN网络状态
    KEY_LORA_NETWORK_STATUS(0x90),
    // 频段
    KEY_LORA_REGION(0x91),
    // 入网类型
    KEY_LORA_MODE(0x92),
    KEY_LORA_DEV_EUI(0x93),
    KEY_LORA_APP_EUI(0x94),
    KEY_LORA_APP_KEY(0x95),
    KEY_LORA_DEV_ADDR(0x96),
    KEY_LORA_APP_SKEY(0x97),
    KEY_LORA_NWK_SKEY(0x98),
    // CH
    KEY_LORA_CH(0x99),
    // 入网DR
    KEY_LORA_DR(0x9A),
    // 数据发送策略
    KEY_LORA_UPLINK_STRATEGY(0x9B),
    // DUTYCYCLE
    KEY_LORA_DUTYCYCLE(0x9C),
    //ADR_ACK_LIMIT
    KEY_LORA_ADR_ACK_LIMIT(0x9D),
    //ADR_ACK_DELAY
    KEY_LORA_ADR_ACK_DELAY(0x9E),
    //devtime同步间隔
    KEY_LORA_TIME_SYNC_INTERVAL(0x9F),
    // 网络检查间隔
    KEY_LORA_NETWORK_CHECK_INTERVAL(0xA0),
    //设备信息包上行配置
    KEY_DEVICE_INFO_PAYLOAD(0xA1),
    //心跳数据包上行配置
    KEY_HEARTBEAT_PAYLOAD(0xA2),
    //低电状态数据包上行配置
    KEY_LOW_POWER_PAYLOAD(0xA3),
    //事件信息包上行配置
    KEY_EVENT_PAYLOAD(0xA4),
    //GPS极限定位数据包上行配置
    KEY_GPS_LIMIT_PAYLOAD(0xA5),
    //定位数据包上行配置
    KEY_POSITIONING_PAYLOAD(0xA6),



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
