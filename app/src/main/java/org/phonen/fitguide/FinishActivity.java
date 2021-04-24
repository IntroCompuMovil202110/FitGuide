package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class FinishActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_ID = 1;
    private static final String CAMERA_NAME = Manifest.permission.CAMERA;

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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}