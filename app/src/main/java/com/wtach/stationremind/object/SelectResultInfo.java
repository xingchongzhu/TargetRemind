package com.wtach.stationremind.object;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

public class SelectResultInfo implements Serializable {
    private String key;
    private String city;
    private String district;
    private double latitude;
    private double longitude;
    private String uid;
    private String address;
    public SelectResultInfo(){

    }

    public SelectResultInfo(String key, String city, String district, double latitude, double longitude, String uid, String address) {
        this.key = key;
        this.city = city;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;
        this.address = address;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUid() {
        return uid;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "SelectResultInfo{" +
                "key='" + key + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", uid='" + uid + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
