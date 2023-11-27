package com.moko.ps101m;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elvishew.xlog.XLog;
import com.moko.ps101m.entity.ExcelBean;
import com.moko.ps101m.entity.NetworkSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author: jun.liu
 * @date: 2023/11/1 15:45
 * @des:
 */
public class ExcelHelper {
    private WritableWorkbook wwb;

    /**
     * 创建数据表
     *
     * @param path 文件路径
     * @throws Exception 抛出异常
     */
    public File createExcel(String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            boolean a = file.delete();
            XLog.i(a);
        }
        wwb = Workbook.createWorkbook(file);//创建表
        //单个设备 表名没要求 直接命名成sheet1
        createSheet();
        //写入文件
        wwb.write();
        wwb.close();
        return file;
    }

    /**
     * 创建数据表
     *
     * @throws Exception 抛出异常
     */
    private void createSheet() throws Exception {
        WritableSheet ws1 = wwb.createSheet("sheet1", 0);//表名 页数
        Label lbl1 = new Label(0, 0, "Config_Item");
        Label lbl2 = new Label(1, 0, "Config_value");
        Label lbl3 = new Label(2, 0, "Remark");

        ws1.addCell(lbl1);
        ws1.addCell(lbl2);
        ws1.addCell(lbl3);
    }

    /**
     * 向数据表写入数据
     *
     * @param ls   插入的数据
     * @param file 文件
     * @throws Exception 抛出异常
     */
    public void writeToExcel(List<Map<String, String>> ls, File file) throws Exception {
        Workbook oldWwb = Workbook.getWorkbook(file);
        wwb = Workbook.createWorkbook(file, oldWwb);
        //单表的数据写入
        writeToSheet(wwb, ls);
        // 向内存中写入文件中,只能刷一次
        wwb.write();
        wwb.close();
    }

    /**
     * 向数据表写入数据
     *
     * @param wwb WritableWorkbook对象
     * @param ls  数据
     * @throws Exception 抛出异常
     */
    private void writeToSheet(WritableWorkbook wwb, List<Map<String, String>> ls) throws Exception {
        WritableSheet ws1 = wwb.getSheet(0);
        for (int i = 0; i < ls.size(); i++) {
            int row = ws1.getRows();
            Label lab1 = new Label(0, row, ls.get(i).get("item"));
            Label lab2 = new Label(1, row, ls.get(i).get("value"));
            Label lab3 = new Label(2, row, ls.get(i).get("remark"));
            ws1.addCell(lab1);
            ws1.addCell(lab2);
            ws1.addCell(lab3);
        }
    }

    public List<Map<String, String>> handleExcelData(@NonNull NetworkSettings bean) {
        List<ExcelBean> dataSource = new ArrayList<>();
        dataSource.add(new ExcelBean("Host", String.format("value:%s", bean.host), "1-64 characters"));
        dataSource.add(new ExcelBean("Port", String.format("value:%s", bean.port), "Range: 1-65535"));
        dataSource.add(new ExcelBean("Client id", String.format("value:%s", bean.clientId), "1-64 characters"));
        dataSource.add(new ExcelBean("Subscribe Topic", String.format("value:%s", bean.subscribe), "1-128 characters"));
        dataSource.add(new ExcelBean("Publish Topic", String.format("value:%s", bean.publish), "1-128 characters"));
        dataSource.add(new ExcelBean("Clean Session", String.format("value:%s", bean.cleanSession ? "1" : "0"), "Range: 0/1 0:NO 1:YES"));
        dataSource.add(new ExcelBean("Qos", String.format(Locale.getDefault(), "value:%d", bean.qos), "Range: 0/1/2 0:qos0 1:qos1 2:qos2"));
        dataSource.add(new ExcelBean("Keep Alive", String.format(Locale.getDefault(), "value:%d", bean.keepAlive), "Range: 10-120, unit: second"));
        dataSource.add(new ExcelBean("MQTT Username", !TextUtils.isEmpty(bean.userName) ? String.format("value:%s", bean.userName) : "", "0-256 characters"));
        dataSource.add(new ExcelBean("MQTT Password", !TextUtils.isEmpty(bean.password) ? String.format("value:%s", bean.password) : "", "0-256 characters"));
        dataSource.add(new ExcelBean("SSL/TLS", bean.connectMode > 0 ? "value:1" : String.format(Locale.getDefault(), "value:%d", bean.connectMode), "Range: 0/1 0:Disable SSL (TCP mode) 1:Enable SSL"));
        dataSource.add(new ExcelBean("Certificate type", bean.connectMode > 0 ? String.format(Locale.getDefault(), "value:%d", bean.connectMode) : "value:1", "Valid when SSL is enabled, range: 1/2/3 1: CA certificate file 2: CA certificate file 3: Self signed certificates"));
        dataSource.add(new ExcelBean("LWT", bean.lwtEnable ? "value:1" : "value:0", "Range: 0/1 0:Disable 1:Enable"));
        dataSource.add(new ExcelBean("LWT Retain", bean.lwtRetain ? "value:1" : "value:0", "Range: 0/1 0:NO 1:YES"));
        dataSource.add(new ExcelBean("LWT Qos", String.format(Locale.getDefault(), "value:%d", bean.lwtQos), "Range: 0/1/2 0:qos0 1:qos1 2:qos2"));
        dataSource.add(new ExcelBean("LWT Topic", String.format("value:%s", bean.lwtTopic), "1-128 characters"));
        dataSource.add(new ExcelBean("LWT Payload", String.format("value:%s", bean.lwtPayload), "1-128 characters"));
        dataSource.add(new ExcelBean("APN", !TextUtils.isEmpty(bean.apn) ? String.format("value:%s", bean.apn) : "", "0-100 Characters"));
        dataSource.add(new ExcelBean("Network Priority", String.format("value:%s", bean.networkFormat), "Range: 0/1/2/3"));

        List<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < dataSource.size(); i++) {
            Map<String, String> map = new HashMap<>();
            ExcelBean excelBean = dataSource.get(i);
            map.put("item", excelBean.item);
            map.put("value", excelBean.value);
            map.put("remark", excelBean.remark);
            data.add(map);
        }
        return data;
    }

    public @Nullable NetworkSettings parseImportFile(@NonNull File file) {
        try {
            Workbook workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            int rows = sheet.getRows();
            int columns = sheet.getColumns();
            NetworkSettings bean = new NetworkSettings();
            // 从第二行开始
            for (int i = 1; i < rows; i++) {
                Cell[] cells = sheet.getRow(i);
                if (cells.length != columns) continue;
                String strValue = !TextUtils.isEmpty(cells[1].getContents()) ? cells[1].getContents().replaceAll("value:", "") : "";
                if (i == 1) {
                    bean.host = strValue;
                } else if (i == 2) {
                    bean.port = strValue;
                } else if (i == 3) {
                    bean.clientId = strValue;
                } else if (i == 4) {
                    bean.subscribe = strValue;
                } else if (i == 5) {
                    bean.publish = strValue;
                } else if (i == 6) {
                    bean.cleanSession = "1".equals(strValue);
                } else if (i == 7) {
                    bean.qos = Integer.parseInt(strValue);
                } else if (i == 8) {
                    bean.keepAlive = Integer.parseInt(strValue);
                } else if (i == 9) {
                    bean.userName = strValue;
                } else if (i == 10) {
                    bean.password = strValue;
                } else if (i == 11) {
                    bean.connectMode = Integer.parseInt(strValue);
                } else if (i == 12) {
                    if (bean.connectMode > 0) {
                        bean.connectMode = Integer.parseInt(strValue);
                    }
                } else if (i == 13) {
                    bean.lwtEnable = "1".equals(strValue);
                } else if (i == 14) {
                    bean.lwtRetain = "1".equals(strValue);
                } else if (i == 15) {
                    bean.lwtQos = Integer.parseInt(strValue);
                } else if (i == 16) {
                    bean.lwtTopic = strValue;
                } else if (i == 17) {
                    bean.lwtPayload = strValue;
                } else if (i == 18) {
                    bean.apn = strValue;
                } else if (i == 19) {
                    bean.networkFormat = Integer.parseInt(strValue);
                }
            }
            workbook.close();
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
