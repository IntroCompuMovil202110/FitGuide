package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.Reference;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.Utils.References;
import org.phonen.fitguide.model.User;

import java.security.PublicKey;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

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
        myRef.child(References.PATH_USERS).child(uId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
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
            }
        });

       /* myRef = database.getReference(References.PATH_USERS+uId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if(user!= null)
                {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/
    }

    public void navBarSettings(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
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
}