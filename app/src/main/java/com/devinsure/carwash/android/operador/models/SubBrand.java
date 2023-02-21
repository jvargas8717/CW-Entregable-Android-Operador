package com.devinsure.carwash.android.operador.models;

public class SubBrand {
    String idSubBrand;
    String name;
    String key;
    String idBrand;

    public SubBrand() {
    }

    public SubBrand(String idSubBrand, String name, String key, String idBrand) {
        this.idSubBrand = idSubBrand;
        this.name = name;
        this.key = key;
        this.idBrand = idBrand;
    }

    public String getIdSubBrand() {
        return idSubBrand;
    }

    public void setIdSubBrand(String idSubBrand) {
        this.idSubBrand = idSubBrand;
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

    public String getIdBrand() {
        return idBrand;
    }

    public void setIdBrand(String idBrand) {
        this.idBrand = idBrand;
    }
}
