package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.phonen.fitguide.services.RequestsListenerService;
import org.phonen.fitguide.utils.References;
import org.phonen.fitguide.model.User;

public class ProfileActivity extends AppCompatActivity {
    //Google
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    //Data
    private String CHANNEL_ID_REQ = "RequestChannel";


    User user;
    BottomNavigationView bottomNavigationView;
    TextView textVPoint;
    TextView textLevel;
    TextView textViewN;
    TextView textVRank;
    ScrollView scrollView4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        navBarSettings();
        scrollView4 = findViewById(R.id.scrollView4);
        //firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //-----------------------
        textViewN = findViewById(R.id.textNameV);
        textLevel = findViewById(R.id.levelVText);
        textVPoint =  findViewById(R.id.textVPoints);
        textVRank = findViewById(R.id.textVRank);
        String uId =mAuth.getUid();
        Log.i("DEBUG uID",uId);
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(References.PATH_USERS).child(uId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                user = task.getResult().getValue(User.class);
                textViewN.setText(user.getName().toUpperCase());
                textLevel.setText(String.valueOf(user.getLevel()));
                textVPoint.setText(String.valueOf(user.getPoints()));
                textVRank.setText("NIVEL:     "+user.getRank());
            }
        });
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Friends request notifications
            CharSequence nameReq ="Friends requests channel";
            String descriptionReq = "Channel used to notify new incoming friend requests";
            int importanceReq = NotificationManager.IMPORTANCE_DEFAULT;
            //IMPORTANCE_MAX MUESTRA LA NOTIFICACIÓN ANIMADA
            NotificationChannel channelReq = new NotificationChannel(CHANNEL_ID_REQ, nameReq, importanceReq);
            channelReq.setDescription(descriptionReq);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelReq);
        }
    }

    private void initNotificationService() {
        Intent intentReq = new Intent(ProfileActivity.this, RequestsListenerService.class);
        RequestsListenerService.enqueueWork(ProfileActivity.this, intentReq);
    }
    public void navBarSettings(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
                    startActivity(new Intent(getApplicationContext(), ChallengesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.feedActivity:
                    startActivity(new Intent(getApplicationContext(), FeedActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.profileActivity:
                    return true;
                case R.id.startActivity:
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    public void EditarPerfil(View view) {
        startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        overridePendingTransition(0, 0);
    }

    public void ListaAmigos(View view) {
        startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));
        overridePendingTransition(0, 0);
    }

    public void logOut(View view)
    {
        mAuth.signOut();
        Intent intent = new Intent (getApplicationContext(),LoggedOutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    protected void onStart(){
        super.onStart();
        this.initNotificationService();
    }


}