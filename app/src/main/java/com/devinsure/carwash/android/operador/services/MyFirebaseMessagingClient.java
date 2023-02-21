package com.devinsure.carwash.android.operador.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.activities.NotificationBookingActivity;
import com.devinsure.carwash.android.operador.channel.NotificationHelper;
import com.devinsure.carwash.android.operador.receivers.AcceptReceiver;
import com.devinsure.carwash.android.operador.receivers.CancelReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        RemoteMessage.Notification notification = message.getNotification();
        //mapa de valores
        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");

        if (title != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                if (title.contains("SOLICITUD DE SERVICIO")){
                    String idClient = data.get("idClient");
                    String origin = data.get("origin");
                    //aqui destino en notificación
                    //String destination = data.get("destination");
                    String min = data.get("min");
                    String distance = data.get("distance");
                    showNotificationApiOreoActions(title, body, idClient);
                    // showNotificationActivity(idClient, origin, destination, min, distance);
                    showNotificationActivity(idClient, origin, "", min, distance);
                }
                else if (title.contains("LAVADO CANCELADO")){
                    //PERMITE ELIMINAR LA NOTIFICACIÓN DE SOLICITUD DE SERVICIO
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //EL ID ES IMPORTANTE, DEBE SER EL MISMO QUE ESTABLECIMOS CUANDO MOSTRAMOS LAS NOTIFICACIONES
                    //ELIMINANDO LA SOLICITUD DE VIAJE POR NOTIFICACIÓN
                    manager.cancel(2);
                    //showNotificationApiOreo(title, body);
                }
                else {
                    showNotificationApiOreo(title, body);
                }
            }
            else {
                if (title.contains("SOLICITUD DE SERVICIO")){
                    String idClient = data.get("idClient");
                    String origin = data.get("origin");
                    //aqui destino
                    //String destination = data.get("destination");
                    String min = data.get("min");
                    String distance = data.get("distance");
                    showNotificationActions(title, body, idClient);
                    //aqui
                    //showNotificationActivity(idClient, origin, destination, min, distance);
                    showNotificationActivity(idClient, origin, "", min, distance);
                }
                else if (title.contains("LAVADO CANCELADO")){
                    //PERMITE ELIMINAR LA NOTIFICACIÓN DE SOLICITUD DE SERVICIO
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    //EL ID ES IMPORTANTE, DEBE SER EL MISMO QUE ESTABLECIMOS CUANDO MOSTRAMOS LAS NOTIFICACIONES
                    manager.cancel(2);
                    //showNotification(title, body);
                }
                else {
                    showNotification(title, body);
                }
            }
        }
    }

    //aqui destino en notificación
    /*
    private void showNotificationActivity(String idClient, String origin, String destination, String min, String distance) {
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        if (!isScreenOn){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE,
                    "AppName:MyLock"
            );

            wakeLock.acquire(10000);
        }

        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("idClient", idClient);
        intent.putExtra("origin", origin);
        intent.putExtra("destination", destination);
        intent.putExtra("min", min);
        intent.putExtra("distance", distance);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    * */

    private void showNotificationActivity(String idClient, String origin, String destination, String min, String distance) {
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();

        if (!isScreenOn){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE,
                    "AppName:MyLock"
            );

            wakeLock.acquire(10000);
        }

        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("idClient", idClient);
        intent.putExtra("origin", origin);
        //aqui
        // intent.putExtra("destination", destination);
        intent.putExtra("min", min);
        intent.putExtra("distance", distance);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showNotification(String title, String body) {
        // PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApi(title, body, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    private void showNotificationActions(String title, String body, String idClient) {

        // Acciones para el boton aceptar
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        // Acciones para el boton cancelar
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldApiActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreo(String title, String body) {
        //PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(title, body, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationApiOreoActions(String title, String body, String idClient) {

        //ACCIONES PARA EL BOTON DE ACEPTAR
        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("idClient", idClient);
        //05-11-2022
        //PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent acceptPendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            acceptPendingIntent = PendingIntent.getActivity(
                    this,
                    NOTIFICATION_CODE, acceptIntent,
                    PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            acceptPendingIntent = PendingIntent.getActivity(
                    this,
                    NOTIFICATION_CODE, acceptIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.Action acceptAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build();

        //ACCIONES PARA EL BOTON DE CANCELAR
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient", idClient);
        //05-11-2022
        //PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelPendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            cancelPendingIntent = PendingIntent.getActivity(
                    this,
                    NOTIFICATION_CODE, cancelIntent,
                    PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            cancelPendingIntent = PendingIntent.getActivity(
                    this,
                    NOTIFICATION_CODE, cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();

        // PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotificationActions(title, body, sound, acceptAction, cancelAction);
        notificationHelper.getManager().notify(2, builder.build());
    }
}
