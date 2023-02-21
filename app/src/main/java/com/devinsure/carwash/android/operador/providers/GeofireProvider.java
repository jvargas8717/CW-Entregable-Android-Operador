package com.devinsure.carwash.android.operador.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;

    /*Constructor que inicializa la variable mDatabase*/
    public GeofireProvider(String reference){
        mDatabase = FirebaseDatabase.getInstance().getReference().child(reference);
        mGeofire = new GeoFire(mDatabase);
    }

    /*Guarda una localización*/
    public void saveLocation(String idDriver, LatLng latLng){
        mGeofire.setLocation(idDriver, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    /*Método para remover la localización de un conductor por medio del id en cuanto se desconecta*/
    public void removeLocation(String idDriver){
        mGeofire.removeLocation(idDriver);
    }

    /*Obtiene las localizaciones de los conductores activos*/
    public GeoQuery getActiveDrivers(LatLng latLng, double radius){
        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference getDriverLocation(String idDriver){
        return mDatabase.child(idDriver).child("l");
    }

    public DatabaseReference getDriver(String idDriver){
        return mDatabase.child(idDriver);
    }

    public DatabaseReference isDriverWorking(String idDriver){
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }

    public Task<Void> deleteDriverWorking(String idDriver){
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver).removeValue();
    }
}
