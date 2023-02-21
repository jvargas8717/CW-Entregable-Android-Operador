package com.devinsure.carwash.android.operador.providers;

import android.content.Context;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.retrofit.IGoogleAPI;
import com.devinsure.carwash.android.operador.retrofit.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;

public class GoogleApiProvider {

    private Context context;

    public GoogleApiProvider(Context context){
        this.context = context;
    }

    public Call<String> getDirections(LatLng originLatLng, LatLng destinationLatLng){
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&" +
                "origin=" + originLatLng.latitude + "," + originLatLng.longitude + "&" +
                "destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&" +
                "key=" + context.getResources().getString(R.string.google_maps_key);

        return RetrofitClient.getClient(baseUrl).create(IGoogleAPI.class).getDirections(baseUrl + query);
    }
}
