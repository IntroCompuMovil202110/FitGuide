package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddFriendActivity extends AppCompatActivity {
    //View
    BottomNavigationView bottomNavigationView;
    TextInputEditText usrNameField;
    //Google
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    //Data
    private User currentUser;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        usrNameField = findViewById(R.id.usrNameEditText);
        this.mAuth = FirebaseAuth.getInstance();
        this.mDB = FirebaseDatabase.getInstance();
        mDB.getReference(Constants.USERS_PATH + mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("SENDR", error.toString());
                    }
                });
        navBarSettings();
    }
    public void navBarSettings(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
        });
    }
    public void sendFriendRequest(View view) {
        //Check if username exists
        //Check if it is already friend
        //Check if it already has an invitation
        if (validFields()){
            String friendUsrName = usrNameField.getText().toString();
            mDB.getReference(Constants.USERS_PATH).orderByChild("userName")
                    .equalTo(friendUsrName).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String friendUid = "";
                        for(DataSnapshot ds : snapshot.getChildren()){
                            friendUid = ds.getKey();
                        }
                        checkIfIsFriendAndSend(friendUsrName, friendUid);
                    }else{
                        Toast.makeText(getApplicationContext(),
                                friendUsrName + " no existe, trate con otro username...",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("SENDR", error.toString());
                }
            });
        }else{
            Toast.makeText(this, "Ingrese el username de la persona que quiere agregar...", Toast.LENGTH_LONG).show();
        }
    }

    private void checkIfIsFriendAndSend(String friendUsrName, String friendUid) {
        mDB.getReference(Constants.FRIENDS_LIST + mAuth.getCurrentUser().getUid() + "/" + friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    checkIfHasrequestAndSend(friendUsrName, friendUid);
                }else {
                    Toast.makeText(getApplicationContext(),
                            friendUsrName + " ya es tu amigo!",
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("SENDR", error.toString());
            }
        });
    }

    private void checkIfHasrequestAndSend(String friendUsrName, String friendUid) {
        mDB.getReference(Constants.FRIENDS_REQUEST + friendUid + "/" +  mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            createFriendRequest(friendUid);
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Ya has enviado solicitud de amistad a " +friendUsrName + "!",
                                    Toast.LENGTH_LONG).show();
                            usrNameField.setText("");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("SENDR", error.toString());
                    }
                });
    }

    private void createFriendRequest(String friendUid) {
        mDB.getReference(Constants.FRIENDS_REQUEST +
                friendUid +
                "/" +
                mAuth.getCurrentUser().getUid()).setValue(
                        formatter.format(new Date())
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),
                        "Solicitud enviada!", Toast.LENGTH_LONG).show();
                usrNameField.setText("");
            }
        });
    }

    private boolean validFields() {
        return !TextUtils.isEmpty(this.usrNameField.getText().toString())
                && !currentUser.getUserName().equals(this.usrNameField.getText().toString());
    }

    public void loadContactsActivity(View view) {
        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
    }
}