package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChallengeDetailActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);
        navBarSettings();
    }

    public void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.challengesActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
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
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }
}