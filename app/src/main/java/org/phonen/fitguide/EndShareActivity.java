package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EndShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_share);
    }

    public void endSocialShare(View view) {
        startActivity(new Intent(getApplicationContext(),FeedActivity.class));
    }
}