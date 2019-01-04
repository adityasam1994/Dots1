package com.example.aditya.dots1;

public class payment_save {

    String payment_id,create_time,state;

    public payment_save(String payment_id, String create_time, String state) {
        this.payment_id = payment_id;
        this.create_time = create_time;
        this.state = state;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
