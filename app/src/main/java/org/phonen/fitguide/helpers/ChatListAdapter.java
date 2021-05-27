package org.phonen.fitguide.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.phonen.fitguide.FollowActivity;
import org.phonen.fitguide.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.phonen.fitguide.UserChatActivity;
import org.phonen.fitguide.model.Position;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.model.UserModel;
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends ArrayAdapter<UserModel> {

    private ArrayList<UserModel> userList;
    private Context mContext;
    private int resource_layout;

    FirebaseDatabase database;
    DatabaseReference posRef;
    Position position;
    Boolean isMoving;
    // UserModel user;

    public ChatListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UserModel> objects) {
        super(context, resource, objects);
        userList = objects;
        mContext = context;
        resource_layout = resource;

        position = new Position();
    }

    public class ViewHolder {
        ImageButton locationButton;
        ImageButton messageButton;
        TextView nombre;
        TextView nickName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        isMoving=false;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resource_layout, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.locationButton = (ImageButton) view.findViewById(R.id.runningButton);
        viewHolder.messageButton = (ImageButton) view.findViewById(R.id.messageButton);
        viewHolder.nombre = (TextView) view.findViewById(R.id.nameandlast);
        viewHolder.nickName = view.findViewById(R.id.nickname);
        UserModel user = userList.get(position);

        database = FirebaseDatabase.getInstance();
        posRef = database.getReference(Constants.POSITION_PATH + user.getIdU() + "/moving");
        posRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(boolean.class) == null){
                    isMoving=false;
                    viewHolder.locationButton.setImageResource(R.drawable.ic_baseline_directions_run_24);
                } else if(!snapshot.getValue(boolean.class)){
                    isMoving =false;
                    viewHolder.locationButton.setImageResource(R.drawable.ic_baseline_directions_run_24);
                }else if(snapshot.getValue(boolean.class)){
                    isMoving =true;
                    viewHolder.locationButton.setImageResource(R.drawable.green_man_running);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewHolder.nombre.setText(user.getName() + " " + user.getLastName());
        viewHolder.nickName.setText(user.getUserName());

        viewHolder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle extras = new Bundle();
                extras.putString("name", user.getName());
                Log.i("username", user.getName());
                extras.putString("lastName", user.getLastName());
                extras.putString("nickName", user.getUserName());
                extras.putString("uId", user.getIdU());
                intent.putExtras(extras);
                mContext.startActivity(intent);
            }
        });

        viewHolder.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(isMoving){
                    Intent intent = new Intent(v.getContext(), FollowActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle extras = new Bundle();
                    extras.putString("uId", user.getIdU());
                    extras.putString("name",user.getName());
                    intent.putExtras(extras);
                    mContext.startActivity(intent);
                }else{
                    Toast.makeText(mContext, user.getName() + " no está haciendo actividad física en el momento", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}