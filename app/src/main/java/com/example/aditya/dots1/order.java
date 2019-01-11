package com.example.aditya.dots1;

public class order {

    String service,time,ecomment,eaddress,code,format,username,servicetype, cost;
    double latitude,longitude;

    public order(String service, String time, String ecomment, String eaddress, double latitude, double longitude, String code, String format, String username, String servicetype, String cost) {
        this.service = service;
        this.time = time;
        this.ecomment = ecomment;
        this.eaddress = eaddress;
        this.longitude=longitude;
        this.latitude=latitude;
        this.code=code;
        this.format=format;
        this.username=username;
        this.servicetype=servicetype;
        this.cost=cost;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEcomment() {
        return ecomment;
    }

    public void setEcomment(String ecomment) {
        this.ecomment = ecomment;
    }

    public String getEaddress() {
        return eaddress;
    }

    public void setEaddress(String eaddress) {
        this.eaddress = eaddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServicetype() {
        return servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
