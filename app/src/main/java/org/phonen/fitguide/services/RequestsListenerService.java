package org.phonen.fitguide.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.phonen.fitguide.FriendRequestActivity;
import org.phonen.fitguide.R;
import org.phonen.fitguide.utils.Constants;

public class RequestsListenerService extends JobIntentService {
    private static final int JOB_ID = 1;
    public static String CHANNEL_ID = "RequestChannel";
    int notificationId = 0;
    //Google
    private FirebaseDatabase mDB;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private ChildEventListener listener;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, RequestsListenerService.class, JOB_ID, intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent){
        this.mDB = FirebaseDatabase.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.usersRef = this.mDB.getReference(Constants.FRIENDS_REQUEST + mAuth.getCurrentUser().getUid());
        listener = this.usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (mAuth.getCurrentUser() != null)
                    buildAndShowNotification();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildAndShowNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.contacts_icon);
        mBuilder.setContentTitle("Recibiste una nueva solicitud de amistad!");
        mBuilder.setContentText("Selecciona para abrir solicitudes");
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        //Acción asociada a la notificación
        Intent intent = new Intent(this, FriendRequestActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true); //Remueve la notificación cuando se toca
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        // notificationId es un entero unico definido para cada notificacion que se lanza
        notificationManager.notify(notificationId, mBuilder.build());
        notificationId ++;
    }
}