package com.devinsure.carwash.android.operador.providers;

import com.devinsure.carwash.android.operador.models.HistoryBookingAdditional;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HistoryBookingAdditionalProvider {

    DatabaseReference mDatabase;

    public HistoryBookingAdditionalProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistoryBookingAdditional");
    }

    public Task<Void> create(HistoryBookingAdditional historyBookingAdditional){
        String idPush = mDatabase.push().getKey();
        historyBookingAdditional.setIdHistoryBookingAdditional(idPush);
        return mDatabase.child(historyBookingAdditional.getIdHistoryBookingAdditional()).setValue(historyBookingAdditional);
    }

    public DatabaseReference getClientBookingAdditional() {
        return mDatabase.child("HistoryBookingAdditional");
    }

    public DatabaseReference getClientBookingAdditionals() {
        return mDatabase;
    }

}
