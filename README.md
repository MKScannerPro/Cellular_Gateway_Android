# LW006 Android SDK Guide（English）

## Intro

Please read the part of this document which you need.

* We will explain the important classes in the SDK.

* will help developers to get started.

* will explain notes in your developing progress.

## Design instructions

We divide the communications between SDK and devices into three stages: Scanning stage, Connection
stage, Communication stage. For ease of understanding, let's take a look at the related classes and
the relationships between them.

### 1.Scanning stage

**`com.moko.support.LW006.MokoBleScanner`**

Scanning processing class, support to open scan, close scan and get the raw data of the scanned
device.

**`com.moko.support.LW006.callback.MokoScanDeviceCallback`**

Scanning callback interface,this interface can be used to obtain the scan status and device data.

**`com.moko.support.LW006.service.DeviceInfoParseable`**

Parsed data interface,this interface can parsed the device broadcast frame, get the specific data.
the implementation can refer to `BeaconInfoParseableImpl` in the project,the `DeviceInfo` will be
parsed to `BeaconInfo`.

### 2.Connection stage

**`com.moko.support.LW006.LoRaLW006MokoSupport`**

BLE operation core class, extends from `Mokoblelib`.It can connect the device, disconnect the
device, send the device connection status, turn on Bluetooth, turn off Bluetooth, judge whether
Bluetooth is on or not, receive data from the device and send data to the device, notify the page
data update, turn on and off characteristic notification.

### 3.Communication stage

**`com.moko.support.LW006.OrderTaskAssembler`**

We assemble read data and write data to `OrderTask`, send the task to the device
through `LoRaLW006MokoSupport `, and receive the resopnse.

**`com.moko.ble.lib.event.ConnectStatusEvent`**

The connection status is notified by `EventBus`, the device connection status and disconnection
status are obtained from this event.

**`com.moko.ble.lib.event.OrderTaskResponseEvent`**

The response is notified by `EventBus`, we can get result when we send task to device from this
event,distinguish between function via `OrderTaskResponse`.

## Get Started

### Prepare

**Development environment:**

* Android Studio 3.6.+

* minSdkVersion 18

**Import to Project**

Copy the module mokosupport into the project root directory and add dependencies in build.gradle. As
shown below:

```
dependencies {
    ...
    implementation project(path: ':mokosupport')
}
```

add mokosupport in settings.gradle.As shown below:

```
include ':app', ':mokosupport'
```

### Start Developing

**Initialize**

First of all, you should initialize the LoRaLW006MokoSupport.We recommend putting it in Application.

```
LoRaLW006MokoSupport.getInstance().init(getApplicationContext());
```

**Scan devices**

Before operating the Bluetooth scanning device, we need to apply for permission, which we have added
in LoRaLW006MokoSupport `AndroidManifest.xml`

```
...
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
...
```

Start scanning task to find devices around you, then you can get their advertisement content,
connect to device and change parameters.

```
MokoBleScanner mokoBleScanner = new MokoBleScanner(this);
mokoBleScanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override
    public void onStartScan() {
    }

    @Override
    public void onScanDevice(DeviceInfo device) {
    }

    @Override
    public void onStopScan() {
    }
});
```

at the sometime, you can stop the scanning task in this way:

```
mokoBleScanner.stopScanDevice();
```

You can use BeaconInfoParseableImpl to parsed advertisement data to the frame data, such as
deviceType,battery,T&H and etc...

```
int battery = 0;
int deviceType = 0;
String tempStr = "";
String humiStr = "";
Iterator iterator = map.keySet().iterator();
if (iterator.hasNext()) {
    ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
    if (parcelUuid.toString().startsWith("0000aa00")) {
        byte[] bytes = map.get(parcelUuid);
        if (bytes != null) {
            deviceType = bytes[0] & 0xFF;
        }
    } else {
        return null;
    }
}
battery = manufacturerSpecificDataByte[6] & 0xFF;
byte[] tempBytes = Arrays.copyOfRange(manufacturerSpecificDataByte, 7, 9);
byte[] humiBytes = Arrays.copyOfRange(manufacturerSpecificDataByte, 9, 11);
tempStr = MokoUtils.getDecimalFormat("#.##").format(MokoUtils.toIntSigned(tempBytes) * 0.01);
humiStr = MokoUtils.getDecimalFormat("#.##").format(MokoUtils.toInt(humiBytes) * 0.01);
```

**Connect to devices**

Connect to the device in order to do more operations(change parameter, OTA),the only parameter
required is the MAC address.

```
LoRaLW006MokoSupport.getInstance().connDevice(beaconXInfo.mac);
```

You can get the connection status through `ConnectStatusEvent`,remember to register `EventBus`

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onConnectStatusEvent(ConnectStatusEvent event) {
    String action = event.getAction();
    if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
    // connect failed
    ...
    }
    if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
    // connect success
    ...
    }
}
```

You will find that when connect to device password may need, so ,we need set password first.

```
LoRaLW006MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword(password));

```

You can get the response result from device through `OrderTaskResponseEvent`,

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
    // the task timout
    }
    if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
    // finish all task
    }
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
    // get the task result
        OrderTaskResponse response = event.getResponse();
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        int responseType = response.responseType;
        byte[] value = response.responseValue;
        ...
    }
    if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    // notify data
    }
}
```

> `ACTION_ORDER_RESULT`
>
> After the task is sent to the device, the data returned by the device can be obtained by using the `OrderTaskResponse`, and you can determine which task is being returned as a resultis according to the `response.orderCHAR`. The `response.responseValue` is the returned data.

> `ACTION_ORDER_TIMEOUT`
>
> Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. You can determine which task is being returned as a resultis according to the `response.orderCHAR` function and then the next task continues.

> `ACTION_ORDER_FINISH`
>
> When the task in the queue is empty, `onOrderFinish` will be called back.

> `ACTION_CURRENT_DATA`
>
> The data from device notify.

**Communication with the device**

All the read data and write data is encapsulated into `OrderTask` in `OrderTaskAssembler`, and sent
to the device in a **QUEUE** way. SDK gets task status from task callback `OrderTaskResponse` after
sending tasks successfully.

For example, if you want to get the lora region, please refer to the code example below.

```
// read lora region
LoRaLW006MokoSupport.getInstance().sendOrder(derTaskAssembler.getLoraRegion());
...
// get result
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
        OrderTaskResponse response = event.getResponse();
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        int responseType = response.responseType;
        byte[] value = response.responseValue;
        switch (orderCHAR) {
	        case CHAR_PARAMS:
		        if (value.length >= 4) {
		            int header = value[0] & 0xFF;// 0xED
		            int flag = value[1] & 0xFF;// read or write
		            int cmd = value[2] & 0xFF;
		            if (header != 0xED)
		                return;
		            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
		            if (configKeyEnum == null) {
		                return;
		            }
		            int length = value[3] & 0xFF;
		            if (flag == 0x00) {
	                    // read
	                    switch (configKeyEnum) {
							        case KEY_LORA_REGION:
						                if (length > 0) {
						                    final int region = value[4] & 0xFF;
						                }
						                break;
						     }
					  }
			    }
    	 }
    }
}
// read params of device
ArrayList<OrderTask> orderTasks = new ArrayList<>();
orderTasks.add(OrderTaskAssembler.getBattery());
orderTasks.add(OrderTaskAssembler.getMacAddress());
orderTasks.add(OrderTaskAssembler.getDeviceModel());
orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
orderTasks.add(OrderTaskAssembler.getHardwareVersion());
orderTasks.add(OrderTaskAssembler.getManufacturer());
LoRaLW006MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

```

How to parse the returned results, please refer to the code of the sample project and documentation.

The current data of storage are sent to APP by notification. we have on the notification function of
characteristic after connected device.

**OTA**

We used the Nordic DFU for the OTA,dependencies have been added to build.gradle.

```
dependencies {
    api 'no.nordicsemi.android:dfu:0.6.2'
}
```

The OTA requires three important parameters:the path of firmware file,the adv name of device and the
mac address of device.You can use it like this:

```
DfuServiceInitiator starter = new DfuServiceInitiator(deviceMac)
    .setDeviceName(deviceName)
    .setKeepBond(false)
    .setDisableNotification(true);
starter.setZip(null, firmwareFilePath);
starter.start(this, DfuService.class);
```

you can get progress of OTA through `DfuProgressListener`,the examples can be referred to demo
project.

At the end of this part, you can refer all code above to develop. If there is something new, we will
update this document.

## Notes

1.In Android-6.0 or later, Bluetooth scanning requires dynamic application for location permissions,
as follows:

```
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
!= PackageManager.PERMISSION_GRANTED) {
ActivityCompat.requestPermissions(this,
                                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
} 
```

2.`EventBus` is used in the SDK and can be modified in `LoRaLW006MokoSupport` if you want to use
other communication methods.

```
@Override
public void orderFinish() {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_FINISH);
    EventBus.getDefault().post(event);
}

@Override
public void orderTimeout(OrderTaskResponse response) {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
    event.setResponse(response);
    EventBus.getDefault().post(event);
}

@Override
public void orderResult(OrderTaskResponse response) {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_RESULT);
    event.setResponse(response);
    EventBus.getDefault().post(event);
}

@Override
public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
    ...
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_CURRENT_DATA);
    event.setResponse(response);
    EventBus.getDefault().post(event);
    ...
}
```

3.In order to record log files, `XLog` is used in the SDK, and the
permission `WRITE_EXTERNAL_STORAGE` is applied. If you do not want to use it, you can modify it
in `BaseApplication`, and only keep `XLog.init(config)`.

## Change log

* 2021.03.11 mokosupport version:1.0
    * First commit
