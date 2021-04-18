package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.phonen.fitguide.utils.PermissionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunningMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_ID = 0;
    private static final String pLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    private FusedLocationProviderClient locationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static final int SETTINGS_GPS = 10;
    private List<LatLng> routeCoords;
    private LatLng prevLocation;
    private double currentDistance;
    private static final double VALID_DISTANCE_THRESHOLD = 5;
    private TextView distanceIndicator;
    private DecimalFormat df = new DecimalFormat("###.###");
    // Sensors
    private SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        distanceIndicator = findViewById(R.id.labelKM);
        currentDistance = 0;
        PermissionManager.requestPermission(
                this,
                pLocation,
                "Se necesita el permiso para realizar seguimiento de su actividad física.",
                LOCATION_PERMISSION_ID);
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        this.initView();
        routeCoords = new ArrayList<>();
    }

    public void endActivity(View view) {
        startActivity(new Intent(getApplicationContext(), FinishActivity.class));
    }

    private void initLightSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //Null: Si el dispositivo no tiene ese sensor.
        lightSEL = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mMap != null) {
                    //Nivel de luz: 2000
                    if (event.values[0] < 10000) {
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.darkmap));
                    } else {
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.lightmap));

                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void initView() {
        if (PermissionManager.checkPermission(this, pLocation)) {
            initLightSensor();
            checkSettingsLocation();
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (prevLocation == null) {
                            drawInitialPoint(newLocation);
                            prevLocation = newLocation;
                        } else {
                            updateRoute(prevLocation, newLocation);
                        }
                    }
                }
            };
        }
    }
    private void drawInitialPoint(LatLng newLocation) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
    }

    private void updateRoute(LatLng prevLocation, LatLng newLocation) {
        if (validDistance(prevLocation, newLocation)){
            prevLocation = newLocation;
            Log.i("MAPS DEBUG: ", "Valid distance: " + newLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
            routeCoords.add(newLocation);
            //drawLine();
        }else{
            Log.i("MAPS DEBUG: ", "Not valid distance: " + newLocation);
        }

    }

    private boolean validDistance(LatLng p, LatLng n){
        double d2;
        double RADIUS_OF_EARTH_KM = 6371; // metres
        double latDistance = Math.toRadians(n.latitude - p.latitude);
        double lngDistance = Math.toRadians(n.longitude - p.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(n.latitude)) * Math.cos(Math.toRadians(p.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        d2=  Math.round(result*100.0)/100.0;
        double distance;
        Location pLoc = new Location("Punto inicial");
        pLoc.setLatitude(p.latitude);
        pLoc.setLongitude(p.longitude);
        Location nLoc = new Location("Punto final");
        nLoc.setLatitude(n.latitude);
        nLoc.setLongitude(n.longitude);
        distance = pLoc.distanceTo(nLoc);
        Log.i("MAPS DEBUG: ", "Distance: " + distance + " nostros: " + d2);
        currentDistance += distance;
        if (distance > VALID_DISTANCE_THRESHOLD){
            updateDistanceIndicator(currentDistance);
            return true;
        } else{
            return false;
        }
    }

    private void updateDistanceIndicator(double currentDistance) {

        distanceIndicator.setText(df.format(currentDistance/1000) + " KM");
    }

    private void startLocationUpdates() {
        if (mLocationCallback != null && mLocationRequest != null && PermissionManager.checkPermission(this, pLocation)) {
            locationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        if (mLocationCallback != null && mLocationRequest != null)
            locationClient.removeLocationUpdates(mLocationCallback);
    }

    private void checkSettingsLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addAllLocationRequests(Collections.singleton(mLocationRequest));
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(RunningMapsActivity.this,
                                    SETTINGS_GPS);
                        } catch (IntentSender.SendIntentException sendEx) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.lightmap));

        // Add a marker in Sydney and move the camera
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_ID) {
            this.initView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightSEL, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightSEL);
        stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_GPS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this,
                            "Sin acceso a localización, hardware deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}