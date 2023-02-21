package com.devinsure.carwash.android.operador.providers;

import com.devinsure.carwash.android.operador.models.Additional;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdditionalProvider {

    DatabaseReference mDatabase;

    public AdditionalProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Additionals");
    }

    public Task<Void> create(Additional additional){
        String idPush = mDatabase.push().getKey();
        additional.setIdAdditional(idPush);
        return mDatabase.child(additional.getIdAdditional()).setValue(additional);
    }

    public DatabaseReference getAdditional() {
        return mDatabase.child("Additionals");
    }

    public DatabaseReference getAdditionals() {
        return mDatabase;
    }
}
