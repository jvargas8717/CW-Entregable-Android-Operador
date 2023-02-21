package com.devinsure.carwash.android.operador.activities;

import static com.devinsure.carwash.android.operador.R.id.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.includes.MyToolbar;
import com.devinsure.carwash.android.operador.models.ClientBooking;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.ClientBookingProvider;
import com.devinsure.carwash.android.operador.providers.DriversFoundProvider;
import com.devinsure.carwash.android.operador.providers.GeofireProvider;
import com.devinsure.carwash.android.operador.providers.TokenProvider;
import com.devinsure.carwash.android.operador.services.ForegroundService;
import com.devinsure.carwash.android.operador.utils.CarMoveAnim;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MapDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private DriversFoundProvider mDriversFoundProvider;

    //09-01-2023
    private ClientBookingProvider mClientBookingProvider;

    private LocationRequest mLocationRequest = new LocationRequest();
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;

    private Button mButtonConect;
    private boolean mIsConnect = false;
    private boolean mExtraConnect = false;

    private LatLng mCurrentLatLng;

    private ValueEventListener mListener;

    //RECUPERACIÓN DE ESTADOS DE PANTALLA
    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;

    //FOREGROUNDSERVICES
    private GoogleApiClient mGoogleApiClient;
    private final int REQUEST_CHECK_SETTINGS = 0x1;

    private final String statusFinal = "";

    //ANIMACIÓN
    private boolean mIsStartLocation = false;
    LatLng mStartLatLng;
    LatLng mEndLatLng;
    LocationManager mLocationManager;

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            //METODO PARA PODER HACER LA ANIMACIÓN
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (mStartLatLng != null) {
                mEndLatLng = mStartLatLng;
            }

            mStartLatLng = new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude);

            if (mEndLatLng != null) {
                CarMoveAnim.carAnim(mMarker, mEndLatLng, mStartLatLng);
            }

            //OBTENER LA LOCALIZACIÓN DEL USUARIO EN TIEMPO REAL
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(18f)
                            .build()));

            updateLocation();

        }
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {

                if (getApplicationContext() != null) {

                    //VALIDACIÓN QUE SIGNIFICA QUE YA RECONOCIÓ LA UBICACIÓN POR PRIMERA VEZ
                    if (!mIsStartLocation) {

                        mMap.clear();

                        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mIsStartLocation = true;

                        //OBTENER LA LOCALIZACIÓN DEL USUARIO EN TIEMPO REAL
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(18f)
                                        .build()));

                        mMarker = mMap.addMarker(new MarkerOptions().position(
                                new LatLng(location.getLatitude(), location.getLongitude())
                                ).title("Tu posición")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car_min))
                        );

                        updateLocation();

                        if (ActivityCompat.checkSelfPermission(MapDriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapDriverActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                2000,
                                10,
                                locationListenerGPS);

                        stopLocation();

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        MyToolbar.show(this, "Socio Lavador", false);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mDriversFoundProvider = new DriversFoundProvider();
        mClientBookingProvider = new ClientBookingProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mMapFragment.getMapAsync(this);
        mExtraConnect = getIntent().getBooleanExtra("connect", false);

        mButtonConect = findViewById(R.id.btnConect);
        mButtonConect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mIsConnect) {
                    disconnect();
                } else {
                    startLocation();
                }
            }
        });

        mGoogleApiClient = getAPIClientInstance();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }


        mPref = getApplicationContext().getSharedPreferences("RideStatus", MODE_PRIVATE);
        String status = mPref.getString("status", "");
        String idClient = mPref.getString("idClient", "");
        //boolean existsService = false;

        try {
            if (idClient.length() > 0){
                mClientBookingProvider.getClientBookingByIdClient(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                            String statusDatabase = clientBooking.getStatus();

                            if (statusDatabase.equals("start") || statusDatabase.equals("ride")){
                                goToMapDriverActivity(idClient);
                            }
                            else if (statusDatabase.equals("finish")){
                                Intent intent = new Intent(MapDriverActivity.this, CalificationClientActivity.class);
                                intent.putExtra("idClient", idClient);
                                intent.putExtra("price", 0);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else {
                            mEditor = mPref.edit();
                            mEditor.clear().commit();

                            generateToken();
                            deleteDriverWorking();
                            deleteDriverFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                generateToken();
                deleteDriverWorking();
                deleteDriverFound();
            }

        } catch (Exception e) {
            mEditor = mPref.edit();
            mEditor.clear().commit();

            generateToken();
            deleteDriverWorking();
            deleteDriverFound();
        }


        /*if (status.equals("start") || status.equals("ride")) {
            goToMapDriverActivity(idClient);
        } else {
            generateToken();
            deleteDriverWorking();
            deleteDriverFound();
        }*/
    }

    private GoogleApiClient getAPIClientInstance() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        return googleApiClient;
    }

    private void requestGPSSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                Status status = locationSettingsResult.getStatus();

                if (status.getStatusCode() == LocationSettingsStatusCodes.SUCCESS) {
                    Toast.makeText(MapDriverActivity.this, "El GPS ya esta activado", Toast.LENGTH_SHORT).show();
                } else if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {

                    try {
                        status.startResolutionForResult(MapDriverActivity.this, REQUEST_CHECK_SETTINGS);

                        if (ActivityCompat.checkSelfPermission(MapDriverActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapDriverActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(false);

                    } catch (IntentSender.SendIntentException e) {
                        Toast.makeText(MapDriverActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                } else if (status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(MapDriverActivity.this, "La configuración del GPS tiene algún error o no está disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToMapDriverActivity(String idClient) {
        Intent intent = new Intent(MapDriverActivity.this, MapDriverBookingActivity.class);
        intent.putExtra("idClient", idClient);
        startActivity(intent);
    }

    private void deleteDriverFound() {
        mDriversFoundProvider.delete(mAuthProvider.getId());
    }

    private void deleteDriverWorking() {
        mGeofireProvider.deleteDriverWorking(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                isDriverWorking();

                if (mExtraConnect) {
                    startLocation();
                }
            }
        });
    }

    private void checkIfDriverIsActived() {
        mGeofireProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    startLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void stopLocation() {
        //VALIDACIÓN PARA DETENER LA LOCALIZACIÓN EN TIEMPO REAL
        if (mLocationCallback != null && mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        removeLocation();

        if (mListener != null) {
            if (mAuthProvider.existSession()) {
                mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
            }
        }
    }

    private void isDriverWorking() {
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    disconnect();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        checkIfDriverIsActived();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                        /*
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        * */

                        //mMap.setMyLocationEnabled(true);

                    } else {
                        //showAlertDialogNOGPS();
                        requestGPSSettings();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    private Boolean gpsActived() {
        boolean isActive = false;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }

        return isActive;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }


            // mMap.setMyLocationEnabled(true);

        } else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private void disconnect() {
        removeLocation();
        mButtonConect.setText("Conectarse");
        mIsConnect = false;
        mIsStartLocation = false;
        // mFusedLocation.removeLocationUpdates(mLocationCallback);

        if (mAuthProvider.existSession()) {
            mGeofireProvider.removeLocation(mAuthProvider.getId());
        }
    }

    private void startLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mButtonConect.setText("Desconectarse");
                    mIsConnect = true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                } else {
                    //showAlertDialogNOGPS();
                    requestGPSSettings();
                }

            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                //showAlertDialogNOGPS();
                requestGPSSettings();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        /*if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapDriverActivity.this, UpdateProfileDriverActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapDriverActivity.this, HistoryBookingDriverActivity.class);
            startActivity(intent);
        }*/
        if (item.getItemId() == R.id.action_principal_nav) {
            Intent intent = new Intent(MapDriverActivity.this, PrincipalNavActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }

    private void removeLocation(){
        if (locationListenerGPS != null){
            mLocationManager.removeUpdates(locationListenerGPS);
        }
    }
}