package org.phonen.fitguide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoggedOutActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_out);
        mAuth = FirebaseAuth.getInstance();

    }
    private void updateUI(FirebaseUser currentUser)
    {
        if(currentUser!=null)
        {
            startActivity(new Intent(getApplicationContext(), FeedActivity.class));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(mAuth.getCurrentUser());
    }

    public void Login(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    public void Register(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }
}