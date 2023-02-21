package com.devinsure.carwash.android.operador.providers;

import com.devinsure.carwash.android.operador.models.ClientBookingAdditional;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ClientBookingAdditionalProvider {

    DatabaseReference mDatabase;

    public ClientBookingAdditionalProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBookingAdditional");
    }

    public Task<Void> create(ClientBookingAdditional clientBookingAdditional){
        /*String idPush = mDatabase.push().getKey();
        clientBookingAdditional.setIdClientBookingAdditional(idPush);*/
        return mDatabase.child(clientBookingAdditional.getIdClientBooking()).setValue(clientBookingAdditional);
    }

    public DatabaseReference getClientBookingAdditional() {
        return mDatabase.child("ClientBookingAdditional");
    }

    public DatabaseReference getClientBookingAdditionals() {
        return mDatabase;
    }

    public Query getClientBookingByIdClientBooking(String mIdClientBooking) {
        return mDatabase.orderByChild("idClientBooking").equalTo(mIdClientBooking);
    }
}