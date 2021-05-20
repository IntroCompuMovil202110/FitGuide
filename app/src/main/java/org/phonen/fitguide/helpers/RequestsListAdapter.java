package org.phonen.fitguide.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.phonen.fitguide.R;
import org.phonen.fitguide.model.Post;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;
import org.phonen.fitguide.utils.PostUploader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestsListAdapter extends ArrayAdapter<String> {

    private String currentUserUid;
    private FirebaseDatabase mDB;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private List<String> reqNames;
    private String currentUserName;

    public RequestsListAdapter(@NonNull Context context, List<String> friendsUids, String currentUid) {
        super(context, 0, friendsUids);
        this.currentUserUid = currentUid;
        this.mDB = FirebaseDatabase.getInstance();
        reqNames = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.request_item, parent, false);
            String uid = getItem(position);
            TextView name = convertView.findViewById(R.id.reqName);
            TextView username = convertView.findViewById(R.id.reqUsrname);
            ImageButton btnAccept = convertView.findViewById(R.id.acceptRequestBtn);
            ImageButton btnDecline = convertView.findViewById(R.id.declineRequestBtn);
            btnAccept.setTag(position);
            btnDecline.setTag(position);
            btnAccept.setOnClickListener(view -> {
                this.acceptRequest(view);
            });
            btnDecline.setOnClickListener(view -> {
                this.declineRequest(view);
            });
            //Recover data
            DatabaseReference dbRef = FirebaseDatabase.getInstance()
                    .getReference(Constants.USERS_PATH + uid);
            dbRef.get().addOnSuccessListener(v-> {
                User user =  v.getValue(User.class);
                reqNames.add(user.getName());
                name.setText(user.getName());
                username.setText(user.getUserName());
            });
            DatabaseReference dbRef2 = FirebaseDatabase.getInstance()
                    .getReference(Constants.USERS_PATH + uid).child("name");
            dbRef2.get().addOnSuccessListener(v -> {
               currentUserName = v.getValue(String.class);
            });
        }
        return convertView;
    }

    private void acceptRequest(View view){
        //Delete request element in user's list.
        //Create friend element in both user's and friend's list.
        //CreatePost
        int pos = (int)view.getTag();
        mDB.getReference(Constants.FRIENDS_REQUEST +
                currentUserUid +
                "/" +
                getItem(pos))
                .setValue(null).addOnSuccessListener(v -> {
            mDB.getReference(Constants.FRIENDS_LIST +
                    currentUserUid +
                    "/" +
                    getItem(pos))
                    .setValue(formatter.format(new Date())).addOnSuccessListener(v2 -> {
                mDB.getReference(Constants.FRIENDS_LIST +
                        getItem(pos) +
                        "/" +
                        currentUserUid)
                        .setValue(formatter.format(new Date())).addOnSuccessListener(v3 -> {
                            createNewFriendPost(pos);
                });
            });
        });
    }

    private void createNewFriendPost(int pos) {
        Post postA = PostUploader.createNewFriendPost(currentUserUid, currentUserName, reqNames.get(pos));
        Post postB = PostUploader.createNewFriendPost(getItem(pos), reqNames.get(pos), currentUserName);
        PostUploader.uploadPost(postA, null, FirebaseDatabase.getInstance(), null);
        PostUploader.uploadPost(postB, null, FirebaseDatabase.getInstance(), null);
        remove(getItem(pos));
    }

    private void declineRequest(View view){
        //Delete request element in user's list.
        int pos = (int)view.getTag();
        mDB.getReference(Constants.FRIENDS_REQUEST +
            currentUserUid +
            "/" +
            getItem(pos))
            .setValue(null).addOnSuccessListener(v -> {
                remove(getItem(pos));
        });
    }

}