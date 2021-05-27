package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.helpers.FriendsListAdapter;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {
    //Google
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    //View
    BottomNavigationView bottomNavigationView;
    ListView friendsListView;
    FriendsListAdapter adapter;
    TextView emptyText;
    //Data
    private List<String> friendsUids;
    private String currentUserUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        navBarSettings();
        friendsListView = findViewById(R.id.friendsList);
        this.emptyText = findViewById(R.id.emptyText);
        this.friendsUids = new ArrayList<>();
        this.mAuth = FirebaseAuth.getInstance();
        this.mDB = FirebaseDatabase.getInstance();
        this.initializeFriendsList();
    }

    private void initializeFriendsList() {
        currentUserUid = mAuth.getCurrentUser().getUid();
        mDB.getReference(Constants.FRIENDS_LIST +
                currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() < 1){
                    emptyText.setText("No tienes amigos...");
                }else {
                    for(DataSnapshot ds: snapshot.getChildren()){
                        friendsUids.add(ds.getKey());
                    }
                    loadAdapter();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FRIENDS DEBUG",error.toString());
            }
        });
    }


    private void loadAdapter() {
        this.adapter = new FriendsListAdapter(this, this.friendsUids, currentUserUid, emptyText);
        this.friendsListView.setAdapter(this.adapter);
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

    public void AddFriend(View view) {
        startActivity(new Intent(getApplicationContext(), AddFriendActivity.class));
    }

    public void FriendRequests(View view) {
        startActivity(new Intent(getApplicationContext(), FriendRequestActivity.class));
    }
}