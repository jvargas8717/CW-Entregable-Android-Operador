package com.devinsure.carwash.android.operador.models;

public class Brand {
    String idBrand;
    String name;
    String key;

    public Brand() {
    }

    public Brand(String idBrand, String name, String key) {
        this.idBrand = idBrand;
        this.name = name;
        this.key = key;
    }

    public String getIdBrand() {
        return idBrand;
    }

    public void setIdBrand(String idBrand) {
        this.idBrand = idBrand;
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
}
