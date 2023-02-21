package com.devinsure.carwash.android.operador.retrofit;

import com.devinsure.carwash.android.operador.models.FCMBody;
import com.devinsure.carwash.android.operador.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/*FCM FIREBASE CLOUD MESSAGING*/
public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAmIb6DfU:APA91bGWS8pTqs4bNhWWPLgJofEs0ayyjOpEyTfd0TR-9h2iOHBiQpGNOmKoZ0NmeoamuarPgW36Vf5kf-o54ejinEmVQkHVKw2Jk_rShmQdjoORXjKl6ON6KuBCRH6GY-BPulp1Nd61"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
