package com.moko.mkgw4.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MokoDevice implements Parcelable {

    public int id;
    public String name;
    public String mac;
    public String mqttInfo;
    public int lwtEnable;
    public String lwtTopic;
    public String topicPublish;
    public String topicSubscribe;
    public boolean isOnline;
    public int deviceType;
    public int wifiRssi;
    public int networkType;
    public boolean isSelected;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.mac);
        dest.writeString(this.mqttInfo);
        dest.writeInt(this.lwtEnable);
        dest.writeString(this.lwtTopic);
        dest.writeString(this.topicPublish);
        dest.writeString(this.topicSubscribe);
        dest.writeByte(this.isOnline ? (byte) 1 : (byte) 0);
        dest.writeInt(this.deviceType);
        dest.writeInt(this.wifiRssi);
        dest.writeInt(this.networkType);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readInt();
        this.name = source.readString();
        this.mac = source.readString();
        this.mqttInfo = source.readString();
        this.lwtEnable = source.readInt();
        this.lwtTopic = source.readString();
        this.topicPublish = source.readString();
        this.topicSubscribe = source.readString();
        this.isOnline = source.readByte() != 0;
        this.deviceType = source.readInt();
        this.wifiRssi = source.readInt();
        this.networkType = source.readInt();
        this.isSelected = source.readByte() != 0;
    }

    public MokoDevice() {
    }

    protected MokoDevice(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.mac = in.readString();
        this.mqttInfo = in.readString();
        this.lwtEnable = in.readInt();
        this.lwtTopic = in.readString();
        this.topicPublish = in.readString();
        this.topicSubscribe = in.readString();
        this.isOnline = in.readByte() != 0;
        this.deviceType = in.readInt();
        this.wifiRssi = in.readInt();
        this.networkType = in.readInt();
        this.isSelected = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MokoDevice> CREATOR = new Parcelable.Creator<MokoDevice>() {
        @Override
        public MokoDevice createFromParcel(Parcel source) {
            return new MokoDevice(source);
        }

        @Override
        public MokoDevice[] newArray(int size) {
            return new MokoDevice[size];
        }
    };
}
