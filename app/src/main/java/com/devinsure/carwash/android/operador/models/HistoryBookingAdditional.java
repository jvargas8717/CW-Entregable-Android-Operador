package com.devinsure.carwash.android.operador.models;

public class HistoryBookingAdditional {

    String idHistoryBooking;
    String idAdditional;
    String idHistoryBookingAdditional;
    double price;

    public HistoryBookingAdditional() {
    }

    public HistoryBookingAdditional(String idHistoryBooking, String idAdditional, double price) {
        this.idHistoryBooking = idHistoryBooking;
        this.idAdditional = idAdditional;
        this.price = price;
    }

    public String getIdHistoryBooking() {
        return idHistoryBooking;
    }

    public void setIdHistoryBooking(String idHistoryBooking) {
        this.idHistoryBooking = idHistoryBooking;
    }

    public String getIdAdditional() {
        return idAdditional;
    }

    public void setIdAdditional(String idAdditional) {
        this.idAdditional = idAdditional;
    }

    public String getIdHistoryBookingAdditional() {
        return idHistoryBookingAdditional;
    }

    public void setIdHistoryBookingAdditional(String idHistoryBookingAdditional) {
        this.idHistoryBookingAdditional = idHistoryBookingAdditional;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
