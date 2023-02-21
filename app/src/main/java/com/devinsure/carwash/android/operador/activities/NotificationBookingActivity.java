package com.devinsure.carwash.android.operador.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.ClientBookingProvider;
import com.devinsure.carwash.android.operador.providers.DriversFoundProvider;
import com.devinsure.carwash.android.operador.providers.GeofireProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class NotificationBookingActivity extends AppCompatActivity {

    //aqui
    //private TextView mTextViewDestination;
    private TextView mTextViewOrigin;
    private TextView mTextViewMin;
    private TextView mTextViewDistance;
    private TextView mTextViewCounter;
    private Button mButtonAccept;
    private Button mButtonCancel;

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private DriversFoundProvider mDriversFoundProvider;

    private String mExtraIdClient;
    private String mExtraOrigin;
    //aqui
    //private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;

    private int mCounter = 25;
    private Handler mHandler;

    private MediaPlayer mMediaPlayer;

    private ValueEventListener mListener;

    //Counter
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter = mCounter - 1;
            mTextViewCounter.setText(String.valueOf(mCounter));

            if (mCounter > 0) {
                initTimer();
            } else {
                cancelBooking();
            }
        }
    };

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mTextViewOrigin = findViewById(R.id.textViewNotificationOrigin);
        //aqui
        //mTextViewDestination = findViewById(R.id.textViewNotificationDestination);

        mTextViewMin = findViewById(R.id.textViewMin);
        mTextViewDistance = findViewById(R.id.textViewNotificacionDistance);
        mTextViewCounter = findViewById(R.id.textViewCounter);

        mButtonAccept = findViewById(R.id.btnAcceptBooking);
        mButtonCancel = findViewById(R.id.btnCancelBooking);

        //se llenan variables a partir de parametros enviados desde el intent
        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigin = getIntent().getStringExtra("origin");
        //aqui
        //mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");

        //llenar textview a partir de los extra's
        mTextViewOrigin.setText(mExtraOrigin);
        //aqui
        //mTextViewDestination.setText(mExtraDestination);
        mTextViewMin.setText(mExtraMin);
        mTextViewDistance.setText(mExtraDistance);

        mMediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mMediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mDriversFoundProvider = new DriversFoundProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();

        checkIfClientCancelBooking();

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });
    }

    private void checkIfClientCancelBooking() {
        //addValueEventListener es un método que permite extraer información en tiempo real
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    goToMapDriverActivity();
                }
                //SIGNIFICA QUE EL CLIENTBOOKING SI EXISTE
                else if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")) {

                    String idDriver = snapshot.child("idDriver").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    if ((status.equals("accept") || status.equals("cancel")) && !idDriver.equals(mAuthProvider.getId())) {
                        //permite eliminar la notificación
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(2);
                        goToMapDriverActivity();
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToMapDriverActivity() {

        Toast.makeText(NotificationBookingActivity.this, "El cliente ya no está disponible", Toast.LENGTH_LONG).show();

        //validación para retirar los callbacks del mhandler
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
        startActivity(intent);
        finish();

    }

    private void cancelBooking() {

        //validación para retirar los callbacks del mhandler
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        // mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");
        mDriversFoundProvider.delete(mAuthProvider.getId());

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void acceptBooking() {

        //validación para retirar los callbacks del mhandler
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        //obtiene el id del usuario loggeado
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        mClientBookingProvider = new ClientBookingProvider();

        //Permite eliminar la notificación
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        checkIfClientBookingWasAccept(mExtraIdClient, NotificationBookingActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    // evento en el ciclo de vida de una aplicación que se ejecuta cuando la aplicación ha sido minimizada
    @Override
    protected void onStop() {
        super.onStop();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.release();
            }
        }
    }

    // evento en el ciclo de vida de una aplicación que se ejecuta cuando la actividad ha sido creada
    @Override
    protected void onResume() {
        super.onResume();

        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }

        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }

    private void checkIfClientBookingWasAccept(final String idClient, final Context context) {
        mClientBookingProvider.getClientBooking(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    if (snapshot.hasChild("idDriver") && snapshot.hasChild("status")) {
                        String status = snapshot.child("status").getValue().toString();
                        String idDriver = snapshot.child("idDriver").getValue().toString();

                        if (status.equals("create") && idDriver.equals("")) {
                            mClientBookingProvider.updateStatusAndIdDriver(idClient, "accept", mAuthProvider.getId());

                            Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.setAction(Intent.ACTION_RUN);
                            intent1.putExtra("idClient", idClient);
                            context.startActivity(intent1);
                        } else {
                            gotoMapDriverActivity(context);
                        }
                    } else {
                        gotoMapDriverActivity(context);
                    }
                } else {
                    gotoMapDriverActivity(context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void gotoMapDriverActivity(Context context) {
        Toast.makeText(context, "Otro colaborador ya aceptó el lavado", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(context, MapDriverActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);
    }
}
