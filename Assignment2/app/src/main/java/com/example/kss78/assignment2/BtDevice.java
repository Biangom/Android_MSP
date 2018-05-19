package com.example.kss78.assignment2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kss78 on 2018-04-23.
 */

// 블루투스 디바이스를 저장할 클래스, dialog로 넘겨주기 위해 parcelable을 implement하였다.
public class BtDevice implements Parcelable {
    String btName;  // 블루투스 디바이스 이름
    String userName;    // 블루투스 유저 이름
    boolean isEncountering; // Encounter라면 true, 그것이 아니면 false
    int count;          // 실제로 discover를 해서 검색되는 횟수를 저장함

    public BtDevice(String btName, String userName, boolean isEncountering, int count) {
        this.btName = btName;
        this.userName = userName;
        this.isEncountering = isEncountering;
        this.count = count;
    }

    public BtDevice() {
    }

    protected BtDevice(Parcel in) {
        btName = in.readString();
        userName = in.readString();
        isEncountering = in.readByte() != 0;
        count = in.readInt();
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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
        parcel.writeInt(count);
    }
}
