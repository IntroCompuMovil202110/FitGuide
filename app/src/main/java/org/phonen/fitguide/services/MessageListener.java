package org.phonen.fitguide.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.phonen.fitguide.ChatActivity;
import org.phonen.fitguide.FeedActivity;
import org.phonen.fitguide.R;
import org.phonen.fitguide.UserChatActivity;
import org.phonen.fitguide.model.Message;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageListener extends JobIntentService {
    private static final int JOB_ID = 15;

    //auth
    FirebaseAuth mAuth;
    //database
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference usersref;
    DatabaseReference ultimoMensajeRef;
    ValueEventListener vel;

    Map<String, Long> chats;
    String name;
    String lastName;
    String username;
    String key;

    Message msg;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, MessageListener.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myRef = database.getReference(Constants.CHAT_PATH + "/" + mAuth.getUid());
        usersref = database.getReference(Constants.USERS_PATH);
        chats = new HashMap<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot single : snapshot.getChildren()) {
                    String id = single.getKey();
                    Long childs = single.getChildrenCount();
                    chats.put(id, childs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        vel = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Pair<String, Long>> cambios = new ArrayList<>();
                for (DataSnapshot single : snapshot.getChildren()) {
                    Pair<String, Long> cambio = new Pair<>(single.getKey(), single.getChildrenCount());
                    cambios.add(cambio);
                }
                for (int i = 0; i < cambios.size(); i++) {
                    if(cambios.get(i).second!=null && chats.get(cambios.get(i).first)!=null)
                    {
                        if (cambios.get(i).second > chats.get(cambios.get(i).first)) {
                            ultimoMensajeRef = database.getReference(Constants.CHAT_PATH + mAuth.getUid() + "/" + cambios.get(i).first);
                            ultimoMensajeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int k = 0;
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (k == snapshot.getChildrenCount() - 1) {
                                            msg = dataSnapshot.getValue(Message.class);
                                        }
                                        k++;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //referencia usuarios
                            usersref.child(cambios.get(i).first).get().addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    User us = task.getResult().getValue(User.class);
                                    name = us.getName();
                                    lastName = us.getLastName();
                                    username = us.getUserName();
                                    if (!msg.getEmisor().equals(mAuth.getUid())) {
                                        buildAndShowNotification(name, lastName, key, username);
                                    }
                                }
                            });
                            chats.put(cambios.get(i).first, cambios.get(i).second);
                            key = cambios.get(i).first;
                        }
                    }
                    else{
                        ultimoMensajeRef = database.getReference(Constants.CHAT_PATH + mAuth.getUid() + "/" + cambios.get(i).first);
                        ultimoMensajeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int k = 0;
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    if (k == snapshot.getChildrenCount() - 1) {
                                        msg = dataSnapshot.getValue(Message.class);
                                    }
                                    k++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        usersref.child(cambios.get(i).first).get().addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                User us = task.getResult().getValue(User.class);
                                name = us.getName();
                                lastName = us.getLastName();
                                username = us.getUserName();
                                if (!msg.getEmisor().equals(mAuth.getUid())) {
                                    buildAndShowNotification(name, lastName, key, username);
                                }
                            }
                        });
                        chats.put(cambios.get(i).first, cambios.get(i).second);
                        key = cambios.get(i).first;

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildAndShowNotification(String name, String lastName, String key, String username) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, FeedActivity.CHANNEL_ID);
        nBuilder.setSmallIcon(R.drawable.w2b);
        nBuilder.setContentTitle(name + " " + lastName + " envió un mensaje");
        nBuilder.setContentText(msg.getMessage());
        nBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent intent = new Intent(this, UserChatActivity.class);
        Bundle extras = new Bundle();
        extras.putString("name", name);
        extras.putString("username", username);
        extras.putString("lastName", lastName);
        extras.putString("nickName", username);
        extras.putString("uId", key);
        intent.putExtras(extras);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(pendingIntent);
        nBuilder.setAutoCancel(true);

        int notificationId = 001;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, nBuilder.build());
    }
}
