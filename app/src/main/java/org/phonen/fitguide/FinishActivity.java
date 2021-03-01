package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FinishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);
    }

    public void shareFeed(View view) {
        startActivity(new Intent(getApplicationContext(), FeedActivity.class));
    }

    public void sharePhoto(View view) {
        startActivity(new Intent(getApplicationContext(), EndShareActivity.class));
    }

    public void finishActivity(View view) {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
    }
}