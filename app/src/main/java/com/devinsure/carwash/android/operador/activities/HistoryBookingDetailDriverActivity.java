package com.devinsure.carwash.android.operador.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.models.HistoryBooking;
import com.devinsure.carwash.android.operador.providers.ClientProvider;
import com.devinsure.carwash.android.operador.providers.HistoryBookingProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailDriverActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    //AQUI DESTINO
    //private TextView mTextViewDestination;
    private TextView mTextViewYourCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraId;

    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_driver);

        mTextViewName = findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail);
        //AQUI DESTINO
        //mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewYourCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail);

        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack = findViewById(R.id.circleImageBack);

        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mClientProvider = new ClientProvider();

        getHistoryBooking();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    //AQUI DESTINO
                    //mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewYourCalification.setText("Tu calificación: " + historyBooking.getCalificationDriver());

                    if (snapshot.hasChild("calificationClient")) {
                        mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());
                    }

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue().toString();
                                mTextViewName.setText(name.toUpperCase());


                                String image = "";

                                if (snapshot.hasChild("image")) {
                                    image = snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriverActivity.this).load(image).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}