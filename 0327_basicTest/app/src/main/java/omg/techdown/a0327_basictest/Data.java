package omg.techdown.a0327_basictest;

import android.os.Parcel;
import android.os.Parcelable;

public class Data implements Parcelable {
    String name;
    float lat;
    float lon;
    int rad;

    public Data() {
    }



    public Data(String name, float lat, float lon, int rad) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.rad = rad;
    }


    protected Data(Parcel in) {
        name = in.readString();
        lat = in.readFloat();
        lon = in.readFloat();
        rad = in.readInt();
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public int getRad() {
        return rad;
    }

    public void setRad(int rad) {
        this.rad = rad;
    }

    // ArrayList를 다른 Activity로 넘겨주기 위한 설정 ( implements Parcelable 때문에)
    @Override
    public int describeContents() {
        return 0;
    }

    // ArrayList를 다른 Activity로 넘겨주기 위한 설정 ( implements Parcelable 때문에)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeFloat(this.lat);
        parcel.writeFloat(this.lon);
        parcel.writeInt(this.rad);

    }
}
