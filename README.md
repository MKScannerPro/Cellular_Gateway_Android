# MKGW4 Android

Android configuration app for the MKGW4 cellular gateway. network (cellular) params, device-side MQTT settings, scan filters, positioning, and function settings. After provisioning, the gateway connects to the cellular network and MQTT broker on its own.

Cross-platform reference (same protocol): [MKGW4_Flutter](https://github.com/MKScannerPro/MKGW4_Flutter).


---

## 1. Overview

```
┌────────────┐  BLE only   ┌──────────┐  Cellular + MQTT  ┌─────────────┐
│  MKGW4     │◄───────────►│  MKGW4   │──────────────────►│ MQTT Broker │
│   APP      │  provision  │  device  │                   └─────────────┘
└────────────┘             └────┬─────┘
                                │ BLE scan
                                ▼
                          Nearby Beacons
                          (scan & upload config)
```

Main flow:

1. **Scan and connect** — Scan for Service Data `0xAA11`; connect to a connectable gateway and verify password if enabled.
2. **Configure network** — Write cellular params (APN, network priority, PIN, region) and device-side MQTT settings over BLE.
3. **Configure scanner** — Set scan report mode, filters, and upload payload formats.
4. **Configure positioning** — Set fix mode (periodic / motion / GPS), axis parameters, and related payloads.
5. **Configure functions** — LED, BLE advertising, heartbeat report, system time, battery management, etc.
6. **Device leaves BLE session** — Gateway applies settings and connects to cellular network / MQTT independently.

---

## 2. Project Structure

| Module | Description |
|--------|-------------|
| `app` | UI and business flow: BLE scan, connect, tabbed device config |
| `mokosupport` | SDK: BLE scan/command assembly, `ParamsKeyEnum`, entities and callbacks |

Package names: `com.moko.mkgw4` (app), `com.moko.support.mkgw4` (SDK).

Initialize in `MKGW4MainActivity`:

```java
MokoSupport.getInstance().init(getApplicationContext());
```

Include the module:

```
include ':app', ':mokosupport'
```

```gradle
implementation project(path: ':mokosupport')
```

---

## 3. Business Flow

### 3.1 BLE Scan & Connect

Entry: `MKGW4MainActivity`

Scan for gateways advertising Service Data UUID `0xAA11`. Identification:

- Service Data payload ≥ 8 bytes; byte `[0]` = device type (`0`–`2`)
- Byte `[7]` = password verify enable (`1` = password required)
- Only **connectable** devices can be selected
- Password: entered by user on connect (last value remembered locally)

Custom BLE service `0xAA00`:

| Characteristic | Purpose |
|----------------|---------|
| `0xAA00` | Password verification (required when enabled) |
| `0xAA01` | Disconnect reason notify |
| `0xAA03` | Device parameter R/W |
| `0xAA04` | Debug log read |

After connect and password verification, navigate to `DeviceInfoActivity` — the main configuration hub with four bottom tabs.

### 3.2 Network Configuration

Tab: **Network** (`NetworkFragment`)

Configure how the gateway joins the cellular network and connects to its MQTT broker. These MQTT settings are written **to the device firmware**, not used by the Android APP.

| Screen | Purpose |
|--------|---------|
| `NetworkSettingsActivity` | APN, username/password, network priority (eMTC / NB-IoT / GSM), PIN, region, connect timeout |
| `MqttSettingsActivity` | Host, Port, ClientId, credentials, QoS, topics, Clean Session, Keepalive, SSL certificates |
| `SyncTimeFromNtpActivity` | NTP server, sync interval, timezone |

Status reads (via BLE): network status, MQTT connect status.

> **Note:** MKGW4 uses **cellular (4G)** networking, not Wi‑Fi. There is no Wi‑Fi configuration screen.

### 3.3 Scanner Configuration

Tab: **Scanner** (`ScannerFragment`)

| Screen | Purpose |
|--------|---------|
| `ScanReportModeActivity` | Scan & report mode, mode auto-switch |
| `RealScanPeriodicReportActivity` | Real-time scan periodic report interval |
| `PeriodicScanImmediateReportActivity` | Periodic scan immediate report |
| `PeriodicScanPeriodicReportActivity` | Periodic scan periodic report |
| `ScannerFilterSettingsActivity` | Filter hub → RSSI, PHY, MAC, Adv Name, Raw data, iBeacon, Eddystone UID/URL/TLM, BXP Tag/Button/Sensor, MK-PIR, MK-ToF, Nano, Other |
| `PayloadSettingsActivity` | Upload payload format hub → per-type payload pages |

Filter and payload subpages live under `filter/` and `payload/`.

### 3.4 Positioning Configuration

Tab: **Position** (`PositionFragment`)

| Screen | Purpose |
|--------|---------|
| `FixModeActivity` | Fix mode: OFF / Periodic fix / Motion fix |
| `PeriodicFixActivity` | Periodic fix interval and conditions |
| `MotionFixActivity` | Motion-triggered fix parameters |
| `GpsFixActivity` | GPS timeout, PDOP, payload settings |
| `AxisParameterActivity` | 3-axis ACC wakeup / motion conditions |

Position-related upload payloads are available for cellular device variants (`deviceType > 0`).

### 3.5 Function / System Configuration

Tab: **Settings** (`SettingsFragment`)

| Screen | Purpose |
|--------|---------|
| `LedSettingsActivity` | LED indicator |
| `BleParametersActivity` | Gateway BLE advertising params |
| `HeartReportSettingActivity` | Heartbeat report interval |
| `MkGw4SystemTimeActivity` | System time / NTP |
| `BatteryManagementActivity` | Battery mode, low-power notify, power-loss notify, auto power-on |
| `SystemInfoActivity` | Device info, OTA (DFU) |
| `LogDataActivity` | Debug log export |
| `UpPayloadSettingsActivity` | Device status upload payload |

System control (reboot, power-off, factory reset, delete buffer) is also available from the Settings tab.

---

## 4. BLE Parameter Protocol Summary

All configuration uses `CHAR_PARAMS` (`0xAA03`) with key-byte addressing defined in `ParamsKeyEnum`.

Common frame envelope:

**Single packet** `HEAD=0xED`: `HEAD + FLAG + KEY + LEN + DATA`  
**Multi packet** `HEAD=0xEE`: adds `PACKET_NUM` / `PACKET_SEQ` (MQTT credentials, filter rules, certificates, etc.)

| FLAG | Meaning |
|------|---------|
| `0x00` | Read |
| `0x01` | Write |
| `0x02` | Device notify (disconnect reason on `0xAA01`) |

Write reply: success when response flag is `0x01`. Constants: `mokosupport/.../entity/ParamsKeyEnum.java`.

Key parameter groups:

| Key range | Category | Examples |
|-----------|----------|----------|
| `0x01`–`0x1E` | System / power / NTP | Reboot, LED, NTP, password, battery notify, power-on method |
| `0x20`–`0x2D` | Device MQTT | Host, Port, ClientId, topics, QoS, SSL certs |
| `0x30`–`0x36` | Cellular network | APN, network priority, PIN, region, connect timeout |
| `0x40`–`0x46` | Scan report | Scan mode, report intervals, upload priority |
| `0x50`–`0x7F` | Scan filters | RSSI, PHY, MAC/name rules, iBeacon, Eddystone, BXP, PIR, ToF, Nano, Other |
| `0x80`–`0x88` | Gateway BLE adv | Adv name, iBeacon UUID/major/minor |
| `0x90`–`0x9D` | Positioning | Fix mode, periodic/motion fix, GPS timeout/PDOP, ACC conditions |
| `0xA0`–`0xAF` | Upload payloads | Per beacon/sensor type payload formats |
| `0xC0`–`0xC9` | Read-only status | Battery, network status, MQTT connect status, buffer count |

---

## 5. BLE Frame Format Summary

**Password** (`CHAR_PASSWORD`):

```
[0xED, 0x01, 0x01, len, passwordBytes]
```

**Param read** (short):

```
[0xED, 0x00, key, 0x00]
```

**Param read** (long — MQTT username/password, filter rules, file reads):

```
[0xEE, 0x00, key, 0x00]
```

**Param write**:

```
[0xED, 0x01, key, length, payload...]
```

**File/chunk write** (TLS certificates):

```
[0xEE, packetCount, packetIndex, chunkLen, data...]
```

Command assembly: `OrderTaskAssembler` / `ParamsTask`.

---

## 6. SDK Quick Start

### 6.1 BLE Scan

```java
mokoBleScanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override public void onStartScan() { }
    @Override public void onScanDevice(DeviceInfo device) { }
    @Override public void onStopScan() { }
});
```

Scan filter uses service UUID `0xAA11` (`OrderServices.SERVICE_ADV`). Adv parsing: `AdvInfoAnalysisImpl`.

### 6.2 BLE Connect & Orders

- Connection status via EventBus `ConnectStatusEvent` (discover success / disconnect)
- Send: `MokoSupport.getInstance().sendOrder(OrderTask...)`
- Response: `OrderTaskResponseEvent` (timeout / finish / result)

Typical flow: connect → verify password (`SetPasswordTask`) → read/write params via `OrderTaskAssembler` → disconnect.

Example:

```java
// Read MQTT host
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getMqttHost());

// Write APN
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setApn(apn));
```

### 6.3 Logging

Based on [xLog](https://github.com/elvishew/xLog). Folder and file names are configured in `BaseApplication` (e.g. `MKGW4` / `MKGW4.txt`). Keeps today and yesterday (`.bak`).

Device debug logs can be exported via `LogDataActivity` (`CHAR_LOG`).

```java
LogModule.d("log info");
```

---

## 7. Main Screen Index

```
GuideActivity                     Splash & permissions
MKGW4MainActivity                 BLE scan & connect
DeviceInfoActivity                Config hub (4 tabs)
  Network tab                     Cellular + device MQTT
    NetworkSettingsActivity       APN / network priority / PIN / region
    MqttSettingsActivity          Device-side MQTT
    SyncTimeFromNtpActivity         NTP / timezone
  Scanner tab                     Scan mode / filters / payloads
    ScanReportModeActivity          Scan & report mode
    ScannerFilterSettingsActivity   Filter hub
    filter/*                        Individual filter pages
    PayloadSettingsActivity         Payload hub
    payload/*                       Per-type payload pages
  Position tab                    Fix mode / GPS / axis
    FixModeActivity                 Fix mode selection
    PeriodicFixActivity             Periodic fix params
    MotionFixActivity               Motion fix params
    GpsFixActivity                  GPS params
    AxisParameterActivity           3-axis ACC conditions
  Settings tab                    System / function config
    LedSettingsActivity             LED indicator
    BleParametersActivity           BLE advertising
    HeartReportSettingActivity      Heartbeat report
    MkGw4SystemTimeActivity         System time
    BatteryManagementActivity       Battery / power management
    SystemInfoActivity              Device info / OTA
    LogDataActivity                 Debug log export
    UpPayloadSettingsActivity       Status upload payload
AboutActivity                     About / app info
```

---

## 8. References

- Parameter keys: `mokosupport/src/main/java/com/moko/support/mkgw4/entity/ParamsKeyEnum.java`
- Order assembly: `mokosupport/src/main/java/com/moko/support/mkgw4/OrderTaskAssembler.java`
- BLE UUIDs: `mokosupport/src/main/java/com/moko/support/mkgw4/entity/OrderServices.java`, `OrderCHAR.java`
