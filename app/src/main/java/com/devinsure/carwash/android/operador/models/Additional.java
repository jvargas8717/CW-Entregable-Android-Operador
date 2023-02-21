package com.devinsure.carwash.android.operador.models;

import android.os.Parcel;
import android.os.Parcelable;


public class Additional implements Parcelable {

    String idAdditional;
    String name;
    String key;
    String price;

    public Additional() {
    }

    public Additional(String idAdditional, String name, String key, String price) {
        this.idAdditional = idAdditional;
        this.name = name;
        this.key = key;
        this.price = price;
    }

    protected Additional(Parcel in) {
        idAdditional = in.readString();
        name = in.readString();
        key = in.readString();
        price = in.readString();
    }

    public static final Creator<Additional> CREATOR = new Creator<Additional>() {
        @Override
        public Additional createFromParcel(Parcel in) {
            return new Additional(in);
        }

        @Override
        public Additional[] newArray(int size) {
            return new Additional[size];
        }
    };

    public String getIdAdditional() {
        return idAdditional;
    }

    public void setIdAdditional(String idAdditional) {
        this.idAdditional = idAdditional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " " + price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(idAdditional);
        parcel.writeString(name);
        parcel.writeString(key);
        parcel.writeString(price);
    }
}
