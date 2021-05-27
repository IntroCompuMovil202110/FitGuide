package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.helpers.ChatListAdapter;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.model.UserModel;
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity  {


    BottomNavigationView bottomNavigationView;
    //auth
    private FirebaseAuth mAuth;
    //private StorageReference storage;
    private  ArrayList<String > friends;
    //view
    ListView users;
    private ArrayList<UserModel> mlista = new ArrayList<>();
    ChatListAdapter mAdapter;


    //firebase database
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference friendRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        navBarSettings();

        //database
        database = FirebaseDatabase.getInstance();
        //auth
        mAuth = FirebaseAuth.getInstance();
        //storage
        //inflate
        users  =findViewById(android.R.id.list);
        mlista = new ArrayList<UserModel>();
        friends = new ArrayList<>();
        mAdapter = new ChatListAdapter(getApplicationContext(),R.layout.chat_list,mlista);

        users.setAdapter(mAdapter);
        subscribeToChanges();

    }

    private void subscribeToChanges()
    {
        FirebaseUser currentuser = mAuth.getCurrentUser();
        myRef = database.getReference(Constants.USERS_PATH);

        friendRef = database.getReference(Constants.FRIENDS_LIST+mAuth.getUid());

        friendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mlista.clear();
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                for (DataSnapshot single: snapshot.getChildren())
                {
                    String idu = (String)single.getKey();
                    myRef = database.getReference(Constants.USERS_PATH);
                    myRef.child(idu).get().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            User user = task.getResult().getValue(User.class);
                            UserModel um = new UserModel(user.getName(), user.getLastName(), user.getUserName(), idu);
                            mlista.add(um);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.chatActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
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
                    return true;
            }
            return false;
        });
    }
}