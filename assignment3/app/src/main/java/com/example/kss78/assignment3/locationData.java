package com.example.kss78.assignment3;

/**
 * Created by kss78 on 2018-05-19.
 */

// 위치 데이터를 정의한다.
public class locationData {

    String ssid; // 해당 ap의 이름이다.
    String bssid; // 해당 mac주소의 이름이다.
    int level; // 해당 dBm의 평균 값이다.
    boolean isLocated; // 조건에 따라서 해당 ap 근처에 있으면 true이다.

    public locationData() {}

    public locationData(String ssid, String bssid, int level, boolean isLocated) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.level = level;
        this.isLocated = isLocated;
    }
}
