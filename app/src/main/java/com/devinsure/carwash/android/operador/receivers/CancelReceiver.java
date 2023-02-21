package com.devinsure.carwash.android.operador.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.ClientBookingProvider;
import com.devinsure.carwash.android.operador.providers.DriversFoundProvider;

public class CancelReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private DriversFoundProvider mDriversFoundProvider;
    private AuthProvider mAuthProvider;

    @Override
    public void onReceive(Context context, Intent intent) {

        String idClient = intent.getStringExtra("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mDriversFoundProvider = new DriversFoundProvider();
        mAuthProvider = new AuthProvider();

        // mClientBookingProvider.updateStatus(idClient, "cancel");
        mDriversFoundProvider.delete(mAuthProvider.getId());

        //cancela la notificación enviada
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
