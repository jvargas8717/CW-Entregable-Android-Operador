package com.devinsure.carwash.android.operador.providers;

import com.devinsure.carwash.android.operador.models.DriverFound;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class DriversFoundProvider {

    DatabaseReference mDatabase;

    public DriversFoundProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DriversFound");
    }

    public Task<Void> create(DriverFound driverFound){
        return mDatabase.child(driverFound.getIdDriver()).setValue(driverFound);
    }
    //SI UN CONDUCTOR ESTA RECIBIENDOP LA NOTIFICACIÓN
    public Query getDriverFoundByIdDriver(String idDriver){
        return mDatabase.orderByChild("idDriver").equalTo(idDriver);
    }

    public Task<Void> delete(String idDriver){
        return mDatabase.child(idDriver).removeValue();
    }
}
