package com.devinsure.carwash.android.operador.models;

public class ClientBookingAdditional {

    //String idClientBookingAdditional;
    //String idClient;
    String idClientBooking;
    String idAdditional;
    double price;
    String name;

    public ClientBookingAdditional() {
    }

    public ClientBookingAdditional(String idClientBooking, String idAdditional, double price, String name) {
        this.idClientBooking = idClientBooking;
        this.idAdditional = idAdditional;
        this.price = price;
        this.name = name;
    }

    public String getIdClientBooking() {
        return idClientBooking;
    }

    public void setIdClientBooking(String idClientBooking) {
        this.idClientBooking = idClientBooking;
    }

    public String getIdAdditional() {
        return idAdditional;
    }

    public void setIdAdditional(String idAdditional) {
        this.idAdditional = idAdditional;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
