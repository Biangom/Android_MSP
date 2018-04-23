package com.example.kss78.assignment2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kss78 on 2018-04-23.
 */

public class BtDevice implements Parcelable {
    String btName;
    String userName;
    boolean isEncountering;

    protected BtDevice(Parcel in) {
        btName = in.readString();
        userName = in.readString();
        isEncountering = in.readByte() != 0;
    }

    public static final Creator<BtDevice> CREATOR = new Creator<BtDevice>() {
        @Override
        public BtDevice createFromParcel(Parcel in) {
            return new BtDevice(in);
        }

        @Override
        public BtDevice[] newArray(int size) {
            return new BtDevice[size];
        }
    };

    public BtDevice(String btName, String userName, boolean isEncountering) {
        this.btName = btName;
        this.userName = userName;
        this.isEncountering = isEncountering;
    }

    public String getBtName() {
        return btName;
    }

    public void setBtName(String btName) {
        this.btName = btName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isEncountering() {
        return isEncountering;
    }

    public void setEncountering(boolean encountering) {
        isEncountering = encountering;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(btName);
        parcel.writeString(userName);
        parcel.writeByte((byte) (isEncountering ? 1 : 0));
    }
}
