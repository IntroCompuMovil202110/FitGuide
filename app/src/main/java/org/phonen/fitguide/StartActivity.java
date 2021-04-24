package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.Utils.References;
import org.phonen.fitguide.model.User;
import org.w3c.dom.Text;

public class StartActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    User user;

    TextView userHeader;
    TextView level;
    TextView weight;
    BottomNavigationView bottomNavigationView;

    Button walkButton;
    Button bikeButton;
    Button runButton;
    private boolean isWalkClicked = false;
    private boolean isBikeClicked = false;
    private boolean isRunClicked = false;

    int activityType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        navBarSettings();

        userHeader = findViewById(R.id.titleComeon);
        level = findViewById(R.id.labelLevel);
        weight = findViewById(R.id.labelWeight);
      walkButton = findViewById(R.id.buttonWalk);
        runButton = findViewById(R.id.buttonRun);
        bikeButton = findViewById(R.id.buttonBici);
        //firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        String uId =mAuth.getUid();
        myRef = database.getReference(References.PATH_USERS+uId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                userHeader.setText("¡VAMOS "+ user.getName().toUpperCase()+ "!");
                level.setText("NIVEL:"+user.getRank());
                weight.setText(weight.getText()+user.getWeight()+"KG");


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       
        walkButton.setOnClickListener(v -> {
            deselectButtons();
            this.isWalkClicked = true;
            this.activityType = 0;
            this.walkButton.setBackgroundColor(getColor(R.color.dark_green));
        });
        runButton.setOnClickListener(v -> {
            deselectButtons();
            this.isRunClicked = true;
            this.activityType = 1;
            this.runButton.setBackgroundColor(getColor(R.color.dark_green));
        });
        bikeButton.setOnClickListener(v -> {
            deselectButtons();
            this.isBikeClicked = true;
            this.activityType = 2;
            this.bikeButton.setBackgroundColor(getColor(R.color.dark_green));
        });
    }

    private void deselectButtons() {
        if (this.isWalkClicked){
            this.walkButton.setBackgroundColor(getColor(R.color.clear_color));
            this.isWalkClicked = false;
        }else if (this.isBikeClicked){
            this.bikeButton.setBackgroundColor(getColor(R.color.clear_color));
            this.isBikeClicked = false;
        }else if (this.isRunClicked){
            this.runButton.setBackgroundColor(getColor(R.color.clear_color));
            this.isRunClicked = false;
        }
    }


    public void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.startActivity);
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
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.startActivity:
                        return true;
                }
                return false;
            }
        });
    }

    public void startActivity(View view) {
        double weight = getIntent().getDoubleExtra("weight", 70);
        Intent intent = new Intent(getApplicationContext(), RunningMapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("weight", weight);
        bundle.putInt("Type", this.activityType);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }
}