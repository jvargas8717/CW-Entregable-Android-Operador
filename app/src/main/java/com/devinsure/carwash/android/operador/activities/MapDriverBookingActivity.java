package com.devinsure.carwash.android.operador.activities;

import static com.devinsure.carwash.android.operador.R.id.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.devinsure.carwash.android.operador.R;
import com.devinsure.carwash.android.operador.models.Additional;
import com.devinsure.carwash.android.operador.models.FCMBody;
import com.devinsure.carwash.android.operador.models.FCMResponse;
import com.devinsure.carwash.android.operador.models.Info;
import com.devinsure.carwash.android.operador.providers.AuthProvider;
import com.devinsure.carwash.android.operador.providers.ClientBookingAdditionalProvider;
import com.devinsure.carwash.android.operador.providers.ClientBookingProvider;
import com.devinsure.carwash.android.operador.providers.ClientProvider;
import com.devinsure.carwash.android.operador.providers.GeofireProvider;
import com.devinsure.carwash.android.operador.providers.GoogleApiProvider;
import com.devinsure.carwash.android.operador.providers.InfoProvider;
import com.devinsure.carwash.android.operador.providers.NotificationProvider;
import com.devinsure.carwash.android.operador.providers.TokenProvider;
import com.devinsure.carwash.android.operador.services.ForegroundService;
import com.devinsure.carwash.android.operador.utils.CarMoveAnim;
import com.devinsure.carwash.android.operador.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private GeofireProvider mGeofireProvider;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private InfoProvider mInfoProvider;
    private ClientBookingAdditionalProvider mClientBookingAdditionalProvider;

    private Info mInfo;

    //NOTIFICACIONES
    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;

    private LocationRequest mLocationRequest = new LocationRequest();
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;

    private LatLng mCurrentLatLng;

    //inicio: variables para contener los datos de la solicitud en la pantalla del conductor
    private TextView mTextViewOriginClientBooking;
    //AQUI DESTINO
    //private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewClientBooking;
    //private TextView mTextViewEmailClientBooking;

    private TextView mTextViewPlates;
    private TextView mTextViewAdditionals;

    private ImageView mImageViewBooking;

    //fin

    private String mExtraClientId;

    //inicio variables para poder trazar rutas
    private LatLng mOriginLatLng;
    //AQUI DESTINO
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    //fin

    private boolean mIsFirstTime = true;

    private boolean mIsCloseToClient = false;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;

    private TextView mTextViewTime;

    private double mDistanceInMeters = 1;
    private int mMinutes = 0;
    private int mSeconds = 0;
    private Boolean mSecondsIsOver = false;
    private Boolean mRideStart = false;

    Handler mHandler = new Handler();
    Location mPreviusLocation = new Location("");

    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;

    boolean mIsFinishBooking = false;

    private String mAdicionales = "ADICIONALES: Sin Adicionales";
    private String mIdClientBooking = "";

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;

            if (!mSecondsIsOver) {
                mTextViewTime.setText(mSeconds + " Seg");
            } else {
                mTextViewTime.setText(mMinutes + " Min " + mSeconds);
            }

            if (mSeconds == 59) {
                mSeconds = 0;
                mSecondsIsOver = true;
                mMinutes++;
            }

            mHandler.postDelayed(runnable, 1000);
        }
    };

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

            if (mRideStart) {
                mDistanceInMeters = mDistanceInMeters + mPreviusLocation.distanceTo(location);
            }

            mPreviusLocation = location;

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

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!mIsStartLocation) {
                        mMap.clear();

                        mMarker = mMap.addMarker(new MarkerOptions().position(
                                new LatLng(location.getLatitude(), location.getLongitude())
                                ).title("Tu posición")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car_min))
                        );

                        //OBTENER LA LOCALIZACIÓN DEL USUARIO EN TIEMPO REAL
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().
                                        target(new LatLng(location.getLatitude(), location.getLongitude())).
                                        zoom(15f).
                                        build()));

                        updateLocation();

                        //saber si es la primera vez que se busca la ubicación
                        if (mIsFirstTime) {
                            mIsFirstTime = false;
                            getClientBooking();
                        }

                        mIsStartLocation = true;


                        if (ActivityCompat.checkSelfPermission(MapDriverBookingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapDriverBookingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListenerGPS);

                        stopLocation();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mInfoProvider = new InfoProvider();
        mClientBookingAdditionalProvider = new ClientBookingAdditionalProvider();

        //NOTIFICACIONES
        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mMapFragment.getMapAsync(this);

        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        //AQUI DESTINO
        //mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        mTextViewClientBooking = findViewById(R.id.textViewClientBooking);
        //mTextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        mButtonStartBooking = findViewById(R.id.btnStartBooking);
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking);
        mTextViewTime = findViewById(R.id.textViewTime);
        mTextViewPlates = findViewById(R.id.textViewPlates);
        mTextViewAdditionals = findViewById(R.id.textViewAdditionals);

        mImageViewBooking = findViewById(R.id.imageViewClientBooking);

        //BOTON SE INICIA COMO DESHABILITADO HASTA QUE CONDUCTOR ESTE CERCA DEL CLIENTE
        //mButtonStartBooking.setEnabled(false);

        mExtraClientId = getIntent().getStringExtra("idClient");

        mGoogleApiProvider = new GoogleApiProvider(MapDriverBookingActivity.this);

        //OBTIENE NODOS CON TARIFAS BASES
        //getInfo();

        getClient();

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mIsCloseToClient) {
                    startBooking();
                } else {
                    Toast.makeText(MapDriverBookingActivity.this, "Debes estar más cerca a la ubicación del lavado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });
    }

    private void removeLocation() {
        if (locationListenerGPS != null) {
            mLocationManager.removeUpdates(locationListenerGPS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeLocation();
        stopLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!mIsFinishBooking) {
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
    }

    private void stopLocation() {
        if (mLocationCallback != null && mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }

    }

    private void startService() {
        stopLocation();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        ContextCompat.startForegroundService(MapDriverBookingActivity.this, serviceIntent);
    }

    private void stopService() {
        startLocation();
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private void calculateRide() {

        if (mMinutes == 0) {
            mMinutes = 1;
        }

        double priceMin = mMinutes * mInfo.getMin();
        double priceKm = (mDistanceInMeters / 1000) * mInfo.getKm();

        Log.d("VALORES", "Min total: " + mMinutes);
        Log.d("VALORES", "KM total: " + (mDistanceInMeters / 1000));

        double total = priceMin + priceKm;

        mClientBookingProvider.updatePrice(mExtraClientId, total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mClientBookingProvider.updateStatus(mExtraClientId, "finish");

                Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClientActivity.class);
                intent.putExtra("idClient", mExtraClientId);
                intent.putExtra("price", total);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getInfo() {
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mInfo = snapshot.getValue(Info.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mIsFinishBooking = true;

                //ELIMINA TODOS LOS DATOS GUARDADOS EN SHARED PREFERENCES
                mEditor.clear().commit();

                sendNotification("Lavado Finalizado");
                removeLocation();
                stopLocation();

                /*if (mFusedLocation != null) {
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }*/

                //AL FINALIZAR EL VIAJE EL CONDUCTOR DEBE REMOVER DE DRIVERS_WORKING Y DEJAR DE ACTUALIZAR SU UBICACIÓN EN TIEMPO REAL
                mGeofireProvider.removeLocation(mAuthProvider.getId());

                if (mHandler != null) {
                    mHandler.removeCallbacks(runnable);
                }

                //METODO PARA CALCULAR EL VIAJE
                //calculateRide();

                mClientBookingProvider.updateStatus(mExtraClientId, "finish");
                Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClientActivity.class);
                intent.putExtra("idClient", mExtraClientId);
                intent.putExtra("price", 0);
                startActivity(intent);
                finish();
            }
        });
    }

    private void startBooking() {

        mEditor.putString("status", "start");
        mEditor.putString("idClient", mExtraClientId);
        mEditor.apply();

        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);

        //LIMPIA EL MAPA DESPUES DE ACEPTAR EL VIAJE
        mMap.clear();

        //AQUI DESTINO
        //mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_destination)));

        if (mCurrentLatLng != null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(
                    new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude)
                    ).title("Tu posición")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car_min))
            );
        }

        //AQUI DIBUJA EL ORIGEN DEL CLIENTE HACIA AL DESTINO EN CASO DE SER VIAJE
        /*drawRoute(mDestinationLatLng);
        sendNotification("Viaje Iniciado");*/

        sendNotification("Lavado Iniciado");

        mRideStart = true;
        mHandler.postDelayed(runnable, 1000);
    }

    /** posible solución: *METODO QUE CALCULA LA DISTANCIA ENTRE DOS POSICIONES EN EL MAPA | PARA AUTORIZAR EL PODER INICIAR EL VIAJE*/
    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");

        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);

        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);

        distance = clientLocation.distanceTo(driverLocation);

        return distance;
    }

    //Método para obtener la información del clientBooking
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    //AQUI DESTINO
                    /*String destination = snapshot.child("destination").getValue().toString();
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());*/

                    if (snapshot.hasChild("idClientBooking")){
                        mIdClientBooking = snapshot.child("idClientBooking").getValue().toString();
                    }

                    String origin = snapshot.child("origin").getValue().toString();
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());

                    String plates = snapshot.child("plates").getValue().toString();
                    mTextViewPlates.setText("PLACAS: " + plates);

                    //inicializar las variables de longitud y latitud del origen y destino
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mTextViewOriginClientBooking.setText("LAVAR EN : " + origin);

                    //AQUI DESTINO
                    //mDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    //mTextViewDestinationClientBooking.setText("DESTINO : " + destination);

                    mPref = getApplicationContext().getSharedPreferences("RideStatus", MODE_PRIVATE);
                    mEditor = mPref.edit();
                    //OBTENER EL ULTIMO ESTADO ALMACENADO EN EL SHARED PREFERENCE
                    String status = mPref.getString("status", "");

                    getAdditionals();

                    if (status.equals("start")) {
                        startBooking();
                    } else {
                        //ESTE VALOR SE ALMACENA CUANDO EL CONDUCTOR INICIA EL VIAJE POR PRIMERA VEZ
                        mEditor.putString("status", "ride");
                        mEditor.putString("idClient", mExtraClientId);
                        mEditor.apply();

                        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Lavar aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_origin)));
                        //AQUI DIBUJA LA RUTA DE LA POSICION DEL LAVADOR HACIA EL ORIGEN O LUGAR DE LAVADO DEL CLIENTE
                        drawRoute(mOriginLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAdditionals() {

        ArrayList<Additional> mAdditionalList = new ArrayList<>();

        mClientBookingAdditionalProvider.getClientBookingByIdClientBooking(mIdClientBooking).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    mAdicionales = "";

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        String idAdditional = ds.child("idAdditional").getValue().toString();
                        String name = ds.child("name").getValue().toString();
                        String price = ds.child("price").getValue().toString();

                        mAdditionalList.add(new Additional(idAdditional, name, "", price));
                    }

                    mAdicionales = mAdditionalList.stream().map(Additional::getName)
                            .collect(Collectors.joining(","));

                    mTextViewAdditionals.setText("ADICIONALES: " + mAdicionales);
                }
                else {
                    mTextViewAdditionals.setText(mAdicionales);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String image = "";

                    if (snapshot.hasChild("image")) {
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(MapDriverBookingActivity.this).load(image).into(mImageViewBooking);
                    }

                    mTextViewClientBooking.setText("CLIENTE: " + name);
                    //mTextViewEmailClientBooking.setText("EMAIL: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            //GUARDA LA POSICIÓN DEL CONDUCTOR
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);

            if (mOriginLatLng != null && mCurrentLatLng != null) {


                if (!mIsCloseToClient) {
                    //MIDE LA DISTANCIA ENTRE LA POSICIÓN DEL CLIENTE Y LA DEL CONDUCTOR, RETORNA EN METROS
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng);

                    //Pruebas de Adrián
                    if (distance <= 15000) {
                        // mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca a la posición de solicitud de lavado", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
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

                        mMap.setMyLocationEnabled(true);

                    } else {
                        showAlertDialogNOGPS();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    //METODO PARA TRAZAR LA RUTA DEL LAVADOR HACIA EL ORIGEN O LUGAR DE LAVADO DEL CLIENTE
    private void drawRoute(LatLng latLng) {
        //traza la rruta entre la ubicación del conductor y el origen del cliente
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                    mTextViewTime.setText(durationText);
                    //mTextViewDistance.setText(distanceText);

                } catch (Exception e) {

                    Log.d("Error", "Error encontrado: " + e.getMessage());

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
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
                return;
            }

            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);

        } else {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create()
                .show();
    }

    private void disconnect() {

        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        } else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {

                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                } else {
                    showAlertDialogNOGPS();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {

                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }

    /*ESTE METODO SE MANDA A LLAMAR DESPUES DE HABER ENCONTRADO UN CONDUCTOR*/
    private void sendNotification(final String status) {
        //ESTE METODO SIRVE PARA OBTENER DATOS DE LA BD UNA SOLA VEZ
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Valida si existe un token parqa el conductor
                if (snapshot.exists()) {

                    // datasnapshot contiene la información del nodo
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "ESTADO DE TU SERVICIO DE LAVADO");
                    map.put("body",
                            "Tu estado del lavado es: " + status);

                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);

                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() != 1) {
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error: " + t.getMessage());
                        }
                    });
                } else {

                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificación por que el lavador no tiene un token de sesión", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}