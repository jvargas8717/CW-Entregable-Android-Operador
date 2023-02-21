package com.devinsure.carwash.android.operador.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.models.ClientBooking;
import com.devinsure.carwash.android.operador.models.HistoryBooking;
import com.devinsure.carwash.android.operador.providers.ClientBookingProvider;
import com.devinsure.carwash.android.operador.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    //AQUI DESTINO
    private TextView mTextViewDestination;
    private TextView mTextViewPrice;
    private RatingBar mRatingBar;
    private Button mButtonCalification;
    private String mExtraClientId;

    private ClientBookingProvider mClientBookingProvider;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private float mCalification = 0;

    private double mExtraPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalificationDriver);
        //AQUI DESTINO
        //mTextViewDestination = findViewById(R.id.textViewDestinationCalificationDriver);
        mTextViewPrice = findViewById(R.id.textViewPriceCalificationDriver);
        mRatingBar = findViewById(R.id.ratingBarCalificationDriver);
        mButtonCalification = findViewById(R.id.btnCalificationDriver);

        mClientBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();

        mExtraClientId = getIntent().getStringExtra("idClient");
        //mExtraPrice = getIntent().getDoubleExtra("price", 0);

        //mTextViewPrice.setText("$" + String.format("%.0f", mExtraPrice));

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {
                mCalification = calification;
            }
        });

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });

        getClientBooking();

    }

    private void getClientBooking(){
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    //AQUI DESTINO
                    //mTextViewDestination.setText(clientBooking.getDestination());
                    mTextViewPrice.setText("$" + String.format("%.0f", clientBooking.getTotal()));

                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng(),
                            clientBooking.getServicio(),
                            clientBooking.getAdicionales(),
                            clientBooking.getSubtotal(),
                            clientBooking.getIva(),
                            clientBooking.getTotal(),
                            clientBooking.getIdBrand(),
                            clientBooking.getIdSubBrand(),
                            clientBooking.getPlates()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificate() {

        if (mCalification > 0){

            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        mHistoryBookingProvider.updateCalificationClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationClientActivity.this, "La calificación se guardó correctamente", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                                intent.putExtra("connect", true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {

                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificationClientActivity.this, "La calificación se guardó correctamente", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                                intent.putExtra("connect", true);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else {
            Toast.makeText(this, "Debe ingresar la calificación", Toast.LENGTH_SHORT).show();
        }
    }
}