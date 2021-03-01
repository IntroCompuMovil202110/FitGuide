package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LoggedOutActivity extends AppCompatActivity {
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);


    }


    public void Login(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    public void Register(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }
}