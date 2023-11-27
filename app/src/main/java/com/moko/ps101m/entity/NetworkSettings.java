package com.moko.ps101m.entity;

/**
 * @author: jun.liu
 * @date: 2023/10/25 16:03
 * @des:
 */
public class NetworkSettings {
    public String host;
    public String port;
    public String clientId;
    public String subscribe;
    public String publish;
    public boolean cleanSession = true;
    public int qos = 1;
    public int keepAlive;
    public String apn;
    public int networkFormat = 3;
    public String userName;
    public String password;
    public int connectMode;
    public boolean lwtEnable = true;
    public boolean lwtRetain;
    public int lwtQos = 1;
    public String lwtTopic;
    public String lwtPayload;
}
