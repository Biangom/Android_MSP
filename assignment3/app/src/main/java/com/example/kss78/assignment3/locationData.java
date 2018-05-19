package com.example.kss78.assignment3;

/**
 * Created by kss78 on 2018-05-19.
 */

public class locationData {

    String ssid;
    String bssid;
    int level;
    boolean isLocated;

    public locationData() {}

    public locationData(String ssid, String bssid, int level, boolean isLocated) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.level = level;
        this.isLocated = isLocated;
    }


}
