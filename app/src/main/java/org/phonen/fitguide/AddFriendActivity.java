package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
                    startActivity(new Intent(getApplicationContext(), ChatActivity.class));
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
                    .equalTo(friendUsrName).limitToFirst(1).get().addOnSuccessListener(snapshot -> {
                    if(snapshot.exists()){
                        String friendUid = "";
                        for(DataSnapshot ds : snapshot.getChildren()){
                            friendUid = ds.getKey();
                        }
                        checkIfIsFriendAndSend(friendUsrName, friendUid);
                    }else{
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Ups!")
                                .setMessage("No encontramos a " + usrNameField.getText().toString())
                                .setPositiveButton("OK",null)
                                .show();
                    }
            });
        }else{
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Ups!")
                    .setMessage("Debes ingresar un nombre de usuario válido.")
                    .setPositiveButton("OK",null)
                    .show();
        }
    }

    private void checkIfIsFriendAndSend(String friendUsrName, String friendUid) {
        mDB.getReference(Constants.FRIENDS_LIST + mAuth.getCurrentUser().getUid() + "/" + friendUid).get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()){
                    checkIfHasrequestAndSend(friendUsrName, friendUid);
                }else {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Ups!")
                            .setMessage(friendUsrName + " ya es tu amigo!")
                            .setPositiveButton("OK",null)
                            .show();
                    usrNameField.setText("");
                }
        });
    }

    private void checkIfHasrequestAndSend(String friendUsrName, String friendUid) {
        mDB.getReference(Constants.FRIENDS_REQUEST + friendUid + "/" +  mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(snapshot -> {
                        if (!snapshot.exists()){
                            createFriendRequest(friendUid);
                        }else{
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle("Ups!")
                                    .setMessage("Ya has enviado solicitud de amistad a " +friendUsrName + "!")
                                    .setPositiveButton("OK",null)
                                    .show();
                            usrNameField.setText("");
                        }
                });
    }

    private void createFriendRequest(String friendUid) {
        mDB.getReference(Constants.FRIENDS_REQUEST +
                friendUid +
                "/" +
                mAuth.getCurrentUser().getUid()).setValue(
                        formatter.format(new Date())
        ).addOnSuccessListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Correcto!")
                    .setMessage("Solicitud enviada!")
                    .setPositiveButton("OK",null)
                    .show();
        });
    }

    private boolean validFields() {
        return !TextUtils.isEmpty(this.usrNameField.getText().toString())
                && !currentUser.getUserName().equals(this.usrNameField.getText().toString());
    }
}