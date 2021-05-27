package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.helpers.FriendsListAdapter;
import org.phonen.fitguide.helpers.RequestsListAdapter;
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {
    //View
    BottomNavigationView bottomNavigationView;
    ListView requestsListView;
    private RequestsListAdapter adapter;
    private TextView emptyText;
    //Data
    private List<String> requestsUids;
    private String currentUserUid;
    //Google
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        this.emptyText = findViewById(R.id.emptyTextReq);
        navBarSettings();
        this.requestsListView = findViewById(R.id.requestListView);
        this.requestsUids = new ArrayList<>();
        this.mAuth = FirebaseAuth.getInstance();
        this.mDB = FirebaseDatabase.getInstance();
        this.initializeRequestsList();
    }

    private void initializeRequestsList() {
        currentUserUid = mAuth.getCurrentUser().getUid();
        mDB.getReference(Constants.FRIENDS_REQUEST +
                currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    requestsUids.add(ds.getKey());
                }
                if (requestsUids.size() == 0){
                    emptyText.setText("No tienes solicitudes.");
                }
                loadAdapter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FRIENDS DEBUG",error.toString());
            }
        });
    }


    private void loadAdapter() {
        this.adapter = new RequestsListAdapter(this, this.requestsUids, currentUserUid, emptyText);
        this.requestsListView.setAdapter(this.adapter);
    }

    private void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.chatActivity:
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
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
        });
    }
}