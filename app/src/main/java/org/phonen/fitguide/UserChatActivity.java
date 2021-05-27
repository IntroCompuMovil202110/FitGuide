package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.helpers.MessageAdapter;
import org.phonen.fitguide.model.Message;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class UserChatActivity extends AppCompatActivity {
    //components view
    TextView nameUser;
    TextView userName;
    EditText editText;
    ImageButton buttonSend;
    ListView messages;
    //components firebase
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference friendref;
    DatabaseReference principalref;
    String friendId;
    User principal;
    //extras
    private ArrayList<Message> mList = new ArrayList<>();
    MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //inflate
        setContentView(R.layout.activity_user_chat);
        nameUser = findViewById(R.id.nameuserChat);
        userName = findViewById(R.id.nicknameChat);
        buttonSend = findViewById(R.id.buttonSend);
        editText = findViewById(R.id.messageText);
        messages = findViewById(android.R.id.list);
        //firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //variables
        mList = new ArrayList<Message>();
        mAdapter = new MessageAdapter(getApplicationContext(), R.layout.card_view_receiver, R.layout.card_view_transmit, mList);
        messages.setAdapter(mAdapter);
        String name;
        String lastName;
        String userName;

        //obtencion de intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        name = extras.getString("name");
        lastName = extras.getString("lastName");
        userName = extras.getString("nickName");
        friendId = extras.getString("uId");
        //actualización datos
        myRef = database.getReference(Constants.CHAT_PATH + mAuth.getUid() + "/" + friendId);
        myRef.child(Constants.CHAT_PATH).child(mAuth.getUid()).child("/" + friendId);
        friendref = database.getReference(Constants.CHAT_PATH + friendId + "/" + mAuth.getUid());
        nameUser.setText(name + " " + lastName);
        this.userName.setText(userName);
        principalref = FirebaseDatabase.getInstance().getReference();
        principalref.child(Constants.USERS_PATH).child(mAuth.getUid()).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                principal = task.getResult().getValue(User.class);
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() > 0) {
                    mList.clear();
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                    String date = new SimpleDateFormat("yyyy/MM/dd ").format(Calendar.getInstance().getTime());
                    String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
                    String messageText = editText.getText().toString();
                    Message message = new Message();
                    message.setMessage(messageText);
                    message.setEmisor(mAuth.getUid());
                    message.setDate(date);
                    message.setTime(time);
                    message.setNombre(principal.getName());
                    myRef.push().setValue(message);
                    friendref.push().setValue(message);
                    editText.setText("");
                    //setScrollbar();
                }
            }
        });
        subscribeToChanges();
    }

    /*@Override
    protected void onRestart() {
        super.onRestart();
        mList = new ArrayList<Message>();
        mAdapter = new MessageAdapter(getApplicationContext(),R.layout.card_view_receiver,R.layout.card_view_transmit,mList);
        messages.setAdapter(mAdapter);
    }*/

    public void setScrollbar(){
        messages.smoothScrollToPosition(0, messages.getBottom());
    }

    private void subscribeToChanges() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mList.clear();
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                //setScrollbar();
                for (DataSnapshot single : snapshot.getChildren()) {
                    Message ms = single.getValue(Message.class);
                    mList.add(ms);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}