package com.moko.support.mkgw4.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.mkgw4.entity.OrderCHAR;

public class SetPasswordTask extends OrderTask {
    public byte[] data;

    public SetPasswordTask() {
        super(OrderCHAR.CHAR_PASSWORD, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(String password) {
        byte[] passwordBytes = password.getBytes();
        int length = passwordBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xED;
        data[1] = (byte) 0x01;
        data[2] = (byte) 0x01;
        data[3] = (byte) length;
        System.arraycopy(passwordBytes, 0, data, 4, length);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
