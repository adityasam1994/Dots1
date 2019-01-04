package com.example.aditya.dots1;

public class usersignup {

    String fname;
    String lname;
    String ph;
    String address;
    double longi;
    double lati;

    public usersignup(String fname, String lname, String ph, String address, double lati, double longi) {
        this.fname = fname;
        this.lname = lname;
        this.ph = ph;
        this.address = address;
        this.lati=lati;
        this.longi=longi;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }
}

