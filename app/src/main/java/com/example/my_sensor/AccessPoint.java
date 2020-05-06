package com.example.my_sensor;

public class AccessPoint {
    private String ssid;
    private String bssid;
    private String rssi;
    private String timestamp;
    private String distance;
    int x;
    int y;

    public AccessPoint(String ssid, String bssid, String rssi, String timestamp, String distance, int x, int y){
        this.ssid=ssid;
        this.bssid=bssid;
        this.rssi=rssi;
        this.timestamp=timestamp;
        this.distance=distance;
        this.x=x;
        this.y=y;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getRssi() {
        return rssi;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDistance() {
        return distance;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
