package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EditProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //navBarSettings();
    }

    private void navBarSettings() {
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
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
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

    public void editProfile(View view)
    {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        overridePendingTransition(0, 0);
    }
}