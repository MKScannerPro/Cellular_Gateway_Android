package com.moko.mkgw4.net.entity;


import java.io.Serializable;

public class SyncDevice implements Serializable {

    public String macName;
    public String mac;
    public String lastWill;
    public String publishTopic;
    public String subscribeTopic;
    // MKGW-mini 01 10
    // MK107 20
    // MK110 Plus 01 40
    // MKGW-min3 20-D 11
    // MK107D Pro-35D 30
    // MK110 Plus 02 50
    // MK110 Plus 03 60
    // MKGW3 70
    // MKGW7 85
    public String model;
}
