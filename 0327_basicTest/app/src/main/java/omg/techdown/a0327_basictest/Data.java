package omg.techdown.a0327_basictest;

import android.os.Parcel;
import android.os.Parcelable;

// Parcelable을 implements한 이유는 인텐트에서 ArrayList를 전달하기 위함
public class Data implements Parcelable {
    String name;    // 지역 이름
    double lat;     // 위도
    double lon;     // 경도
    float rad;      // 반경

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
        lat = in.readDouble();
        lon = in.readDouble();
        rad = in.readFloat();
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

    public double getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getRad() {
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
        parcel.writeDouble(this.lat);
        parcel.writeDouble(this.lon);
        parcel.writeFloat(this.rad);
    }
}
