package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.phonen.fitguide.model.Position;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;
import org.phonen.fitguide.utils.LocationManager;

public class FollowActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Geocoder geocoder;
    TextView name;
    TextView nickname;
    User friend;
    String friendId;
    Position pos;
    String friendName;

    //location
    private Marker marker;

    //Firebase database
    FirebaseDatabase database;
    DatabaseReference friendRef;
    DatabaseReference locationRef;
    FirebaseStorage strg_instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        name = findViewById(R.id.nameuserFollow);
        nickname = findViewById(R.id.nicknameFollow);

        friend = new User();
        pos = new Position();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        friendId = extras.getString("uId");
        friendName = extras.getString("name");
        database = FirebaseDatabase.getInstance();
        friendRef = database.getReference(Constants.USERS_PATH + friendId);
        locationRef = database.getReference(Constants.POSITION_PATH + friendId);

        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friend = snapshot.getValue(User.class);
                name.setText(friend.getName());
                nickname.setText(friend.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        locationRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.i("Firebase", "Error getting data", task.getException());
                } else {
                    pos = task.getResult().getValue(Position.class);
                }
            }
        });

        geocoder = new Geocoder(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        subscribeToChange();


    }

    private void subscribeToChange(){
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pos = snapshot.getValue(Position.class);
                if(pos.isMoving()){
                    if(marker != null){
                        marker.remove();
                    }
                    LatLng latLng = new LatLng(pos.getLatitude(), pos.getLongitude());
                    if(latLng.latitude != 0.0 && latLng.longitude != 0.0){
                        Log.i("LATLNG", String.valueOf(latLng.longitude));
                        marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicacion de " + friendName
                        + ":" + LocationManager.geocoderSearch(latLng, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((latLng), 13));
                    }
                }else{
                    locationRef.removeEventListener(this);
                    new MaterialAlertDialogBuilder(FollowActivity.this)
                            .setTitle("Ups!")
                            .setMessage(friend.getName()+ " se ha desconectado, no se harán más actualizaciones!")
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent (getApplicationContext(),ChatActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            })
                            .show();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        LatLng latilong = new LatLng(pos.getLatitude(), pos.getLongitude());
        if(latilong.latitude != 0.0 && latilong.longitude != 0.0){
            Log.i("LATLNG", String.valueOf(latilong.longitude));
            marker = mMap.addMarker(new MarkerOptions().position(latilong).title("Ubicación de :" + friendName
                    + " " + LocationManager.geocoderSearch(latilong, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((latilong), 13));
        }

        // Add a marker in Sydney and move the camera
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }
}