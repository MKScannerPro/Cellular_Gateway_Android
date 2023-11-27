package com.moko.ps101m.entity;

/**
 * @author: jun.liu
 * @date: 2023/11/1 15:53
 * @des:
 */
public class ExcelBean {
    public String item;
    public String value;
    public String remark;

    public ExcelBean(String item, String value, String remark) {
        this.item = item;
        this.value = value;
        this.remark = remark;
    }

    public ExcelBean(){

    }
}
