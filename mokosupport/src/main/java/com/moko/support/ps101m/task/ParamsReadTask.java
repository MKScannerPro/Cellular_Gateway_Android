package com.moko.support.ps101m.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.ps101m.entity.OrderCHAR;
import com.moko.support.ps101m.entity.ParamsKeyEnum;

public class ParamsReadTask extends OrderTask {
    public byte[] data;

    public ParamsReadTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        createGetConfigData(key.getParamsKey());
    }

    private void createGetConfigData(int configKey) {
        response.responseValue = data = new byte[]{
                (byte) 0xED,
                (byte) 0x00,
                (byte) configKey,
                (byte) 0x00
        };
    }

    public void getFilterName() {
        response.responseValue = data = new byte[]{
                (byte) 0xEE,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey(),
                (byte) 0x00
        };
    }
}
