package org.phonen.fitguide.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import org.phonen.fitguide.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.phonen.fitguide.UserChatActivity;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends ArrayAdapter<UserModel> {

    private ArrayList<UserModel> userList;
    private Context mContext;
    private int resource_layout;
    private StorageReference storageReference;
    FirebaseStorage storageInstance;

   // UserModel user;

    public ChatListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UserModel> objects) {
        super(context, resource, objects);
        userList = objects;
        mContext = context;
        resource_layout = resource;

    }

    public  class  ViewHolder
    {
        ImageButton locationButton;
        ImageButton messageButton;
        TextView nombre ;
        TextView nickName;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View view = convertView;
    if(view == null)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(resource_layout,parent,false);

    }
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.locationButton = (ImageButton) view.findViewById(R.id.runningButton);
        viewHolder.messageButton = (ImageButton) view.findViewById(R.id.messageButton);
        viewHolder.nombre=(TextView) view.findViewById(R.id.nameandlast);
        viewHolder.nickName = view.findViewById(R.id.nickname);
        storageInstance = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
      UserModel user = userList.get(position);

        viewHolder.nombre.setText(user.getName()+" "+user.getLastName());
        viewHolder.nickName.setText(user.getUserName());

        viewHolder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle extras = new Bundle();
                extras.putString("name",user.getName());
                Log.i("username",user.getName());
                extras.putString("lastName",user.getLastName());
                extras.putString("nickName",user.getUserName());
                extras.putString("uId",user.getIdU());
                intent.putExtras(extras);
                mContext.startActivity(intent);
            }
        });
        viewHolder.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}