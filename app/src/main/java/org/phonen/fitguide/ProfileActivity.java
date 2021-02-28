package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.PublicKey;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    TextView textView12;
    ScrollView scrollView4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        navBarSettings();
        textView12 = findViewById(R.id.textView12);
        scrollView4 = findViewById(R.id.scrollView4);
        textView12.setText("LOS RETOS QUE HAS COMPLETADO ESTA SEMANA");
        //scrollView4.addView(textView12);
        TextView tv = new TextView(this);
        tv.setText("Your string");

        scrollView4.addView(tv);
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

}