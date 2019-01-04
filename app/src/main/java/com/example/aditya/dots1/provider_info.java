package com.example.aditya.dots1;

public class provider_info {

    String eservice,eavilable,eage,eaddress,ecomment;
    double lati,longi;

    public provider_info(String eservice, String eavilable, String eage, String eaddress, String ecomment, double lati, double longi) {
        this.eservice = eservice;
        this.eavilable = eavilable;
        this.eage = eage;
        this.eaddress = eaddress;
        this.ecomment = ecomment;
        this.lati = lati;
        this.longi = longi;
    }

    public String getEservice() {
        return eservice;
    }

    public void setEservice(String eservice) {
        this.eservice = eservice;
    }

    public String getEavilable() {
        return eavilable;
    }

    public void setEavilable(String eavilable) {
        this.eavilable = eavilable;
    }

    public String getEage() {
        return eage;
    }

    public void setEage(String eage) {
        this.eage = eage;
    }

    public String getEaddress() {
        return eaddress;
    }

    public void setEaddress(String eaddress) {
        this.eaddress = eaddress;
    }

    public String getEcomment() {
        return ecomment;
    }

    public void setEcomment(String ecomment) {
        this.ecomment = ecomment;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }
}
