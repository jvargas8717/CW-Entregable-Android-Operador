package com.devinsure.carwash.android.operador.providers;

import com.devinsure.carwash.android.operador.retrofit.IFCMApi;
import com.devinsure.carwash.android.operador.retrofit.RetrofitClient;
import com.devinsure.carwash.android.operador.models.FCMBody;
import com.devinsure.carwash.android.operador.models.FCMResponse;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider(){
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }

}
