
package org.phonen.fitguide;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.phonen.fitguide.utils.ImageGenerator;
import org.phonen.fitguide.utils.PermissionManager;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunningMapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    //MapRelated
    private GoogleMap mMap;
    private Marker marker;
    private Bitmap snapshot;
    private FusedLocationProviderClient locationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static final int SETTINGS_GPS = 10;
    private List<LatLng> routeCoords;
    private LatLng prevLocation;
    private LatLng initialLocation;
    private static final double VALID_DISTANCE_THRESHOLD = 1;
    //Permissions
    private static final int LOCATION_PERMISSION_ID = 0;
    private static final String pLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    // Sensors
    private SensorManager sensorManagerLight;
    private SensorManager sensorManagerPressure;
    private SensorManager sensorManagerTemperature;
    Sensor lightSensor;
    Sensor pressureSensor;
    Sensor tempSensor;
    private double currentTemp = -999;
    private double currentPressure = -999;
    private static final double SEA_LEVEL_PRESSURE = 1013.25;
    private static final double PRESSURE_EXPONENT = 1 / 5.257;
    private static final double TEMP_ADDITION_CONSTANT = 273.15;
    private static final double DIVITION_CONSTANT = 0.0065;
    private static final double ALTITUT_THRESHOLD_A = 1000;
    private static final double ALTITUT_THRESHOLD_B = 2000;
    //Layout
    private final DecimalFormat df = new DecimalFormat("##.###");
    private TextView distanceIndicator;
    private TextView bpmIndicator;
    private ImageView pepitoRunning;
    private Chronometer chronometer;
    //Data
    private double EXERCISE_CALORIES_CONSTANT_MET;
    private double currentDistance;
    private double totalTime;
    private String oxigeno;
    private double burnedCalories;
    private double weight;
    private double currentAltitut;
    private int exerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        distanceIndicator = findViewById(R.id.labelKM);
        bpmIndicator = findViewById(R.id.labelBPM);
        chronometer = findViewById(R.id.chronometer);
        pepitoRunning = findViewById(R.id.pepitoRunning);
        currentDistance = 0;
        routeCoords = new ArrayList<>();
        this.getIntentData(getIntent().getBundleExtra("bundle"));

        initLightSensor();
        initPressureSensor();
        initTempSensor();

        PermissionManager.requestPermission(
                this,
                pLocation,
                "Se necesita el permiso para realizar seguimiento de su actividad física.",
                LOCATION_PERMISSION_ID);
        this.initView();
    }

    private void getIntentData(Bundle bundle) {
        this.exerType = bundle.getInt("Type", 0);
        switch (this.exerType) {
            case 0: //caminar
                this.EXERCISE_CALORIES_CONSTANT_MET = 9.8;
                break;
            case 1: // correr
                this.EXERCISE_CALORIES_CONSTANT_MET = 23;
                break;
            case 2: // bicicleta
                this.EXERCISE_CALORIES_CONSTANT_MET = 14;
                break;
            default: //Por defecto se asume que es caminar
                this.EXERCISE_CALORIES_CONSTANT_MET = 8.8;
                break;
        }
        this.weight = bundle.getDouble("weight", 60);
        Log.i("INTENT DEBUG: ", "Received exercise weight: " + this.weight);
        Log.i("INTENT DEBUG: ", "Selected MED: " + this.EXERCISE_CALORIES_CONSTANT_MET);
    }


    public void endActivity(View view) {
        this.endDataGathering();
        this.sendIntent();
    }

    public void endDataGathering() {
        this.chronometer.stop();
        this.totalTime = (SystemClock.elapsedRealtime() - this.chronometer.getBase()) / 1000;
        this.oxigeno = this.getOxigeno();
        this.burnedCalories = this.calcCalories();
    }

    private double calcCalories() {
        double kkalmin = this.EXERCISE_CALORIES_CONSTANT_MET * 3.5 * this.weight / 200;
        return kkalmin * this.totalTime / 60;
    }

    private String getOxigeno() {
        if (this.currentAltitut < ALTITUT_THRESHOLD_A) {
            return "ALTO";
        } else if (this.currentAltitut < ALTITUT_THRESHOLD_B) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }

    public void sendIntent() {
        SnapshotReadyCallback callBack = bitmap -> {
            byte[] bytes = ImageGenerator.bytesFromBitmap(bitmap);
            Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
            intent.putExtra("time", totalTime);
            intent.putExtra("distance", currentDistance);
            intent.putExtra("oxigeno", oxigeno);
            intent.putExtra("calories", burnedCalories);
            intent.putExtra("BMP", bytes);
            intent.putExtra("width", bitmap.getWidth());
            intent.putExtra("height", bitmap.getHeight());
            intent.putExtra("temperature", currentTemp);
            intent.putExtra("pressure", currentPressure);
            intent.putExtra("exerType", exerType );
            startActivity(intent);
        };
        if (mMap != null){
            this.moveCameraToMarkers(initialLocation, prevLocation);
            mMap.snapshot(callBack);
        }else {
            Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
            startActivity(intent);
        }
    }

    private void moveCameraToMarkers(LatLng a, LatLng b) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(a);
        builder.include(b);
        LatLngBounds bounds = builder.build();
        int padding = 300;
        if (a == b) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(a, 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    private void initLightSensor() {
        sensorManagerLight = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManagerLight.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    private void initPressureSensor() {
        sensorManagerPressure = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManagerPressure.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    private void initTempSensor() {
        sensorManagerTemperature = (SensorManager) getSystemService(SENSOR_SERVICE);
        tempSensor = sensorManagerTemperature.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    }

    private void updateAltituteAlert() {
        if (this.currentPressure != -999 && this.currentPressure != -999) {
            //Hyosometric formula
            double altitut = (
                    Math.pow((SEA_LEVEL_PRESSURE / this.currentPressure), PRESSURE_EXPONENT) - 1)
                    * (this.currentTemp + TEMP_ADDITION_CONSTANT) / DIVITION_CONSTANT;
            if (altitut < ALTITUT_THRESHOLD_A) {
                this.bpmIndicator.setText("ALTA");
                this.bpmIndicator.setTextColor(getColor(R.color.main_green));
            } else if (altitut < ALTITUT_THRESHOLD_B) {
                this.bpmIndicator.setText("MEDIO");
                this.bpmIndicator.setTextColor(getColor(R.color.mid_alert));
            } else {
                this.bpmIndicator.setText("BAJA");
                this.bpmIndicator.setTextColor(getColor(R.color.low_alert));
            }
            this.currentAltitut = altitut;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (mMap != null) {
                if (event.values[0] < 10000) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.darkmap));
                } else {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.lightmap));
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            Log.i("DEBUG: ", "NONITOOOOOOO");
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            this.currentPressure = event.values[0];
            updateAltituteAlert();
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            this.currentTemp = event.values[0];
            updateAltituteAlert();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void initView() {
        if (PermissionManager.checkPermission(this, pLocation)) {
            locationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationRequest = createLocationRequest();
            checkSettingsLocation();
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (prevLocation == null) {
                            currentDistance = 0;
                            //startTime = Calendar.getInstance().getTime();
                            startChronometer();
                            drawInitialPoint(newLocation);
                            prevLocation = new LatLng(newLocation.latitude, newLocation.longitude);
                            initialLocation = new LatLng(newLocation.latitude, newLocation.longitude);
                        } else {
                            updateRoute(prevLocation, newLocation);
                        }
                    }
                }
            };
        }
    }


    private void startChronometer() {
        this.chronometer.setBase(SystemClock.elapsedRealtime());
        this.chronometer.start();
    }


    private void drawInitialPoint(LatLng newLocation) {
        this.marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).position(newLocation).title("Punto de partida"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
    }

    private void updateRoute(LatLng prLocation, LatLng newLocation) {
        if (validDistance(prLocation, newLocation)) {
            updateDistanceIndicator(currentDistance);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
            routeCoords.add(newLocation);
            drawLine(prLocation, newLocation);
            this.prevLocation = new LatLng(newLocation.latitude, newLocation.longitude);
        }
    }

    private void drawLine(LatLng origin, LatLng end) {
        if (this.marker != null)
            this.marker.remove();
        this.marker = mMap.addMarker(new MarkerOptions()
                .icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )
                .position(end).title("Punto de partida"));
        mMap.addPolyline(new PolylineOptions().color(Color.argb(255, 96, 224, 190)).add(origin, end));
    }

    private double getDistance(LatLng l1, LatLng l2) {
        double RADIUS_OF_EARTH_KM = 6371;
        double latDistance = Math.toRadians(l2.latitude - l1.latitude);
        double lngDistance = Math.toRadians(l2.longitude - l1.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(l2.latitude)) * Math.cos(Math.toRadians(l1.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIUS_OF_EARTH_KM * c * 1000;
    }

    private boolean validDistance(LatLng p, LatLng n) {
        double distance;
        distance = this.getDistance(p, n);
        //Log.i("MAPS DEBUG: ", "Distance: " + distance);
        currentDistance += distance;
        // Log.i("MAPS DEBUG: ", "Total distance: " + currentDistance);
        return distance > VALID_DISTANCE_THRESHOLD;
    }

    private void updateDistanceIndicator(double currentDistance) {

        distanceIndicator.setText(df.format(currentDistance / 1000) + " KM");
    }

    private void startLocationUpdates() {
        if (mLocationCallback != null && mLocationRequest != null && PermissionManager.checkPermission(this, pLocation))
            locationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void stopLocationUpdates() {
        if (mLocationCallback != null && mLocationRequest != null && PermissionManager.checkPermission(this, pLocation))
            locationClient.removeLocationUpdates(mLocationCallback);
    }

    private void checkSettingsLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addAllLocationRequests(Collections.singleton(mLocationRequest));
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, e -> {
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
        });
        task.addOnSuccessListener(this, locationSettingsResponse -> startLocationUpdates());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RunningMapsActivity.this, R.raw.lightmap));
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
        if (lightSensor != null && sensorManagerLight != null)
            sensorManagerLight.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (pressureSensor != null && sensorManagerPressure != null)
            sensorManagerPressure.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (tempSensor != null && sensorManagerTemperature != null)
            sensorManagerTemperature.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensor != null && sensorManagerLight != null)
            sensorManagerLight.unregisterListener(this);
        if (pressureSensor != null && sensorManagerPressure != null)
            sensorManagerPressure.unregisterListener(this);
        if (tempSensor != null && sensorManagerTemperature != null)
            sensorManagerTemperature.unregisterListener(this);
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