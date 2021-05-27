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
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.util.List;

public class FriendsListAdapter extends ArrayAdapter<String> {
    private final String currentUserUid;
    private final FirebaseDatabase mDB;
    private final TextView emptyText;

    public FriendsListAdapter(@NonNull Context context, List<String> friendsUids, String currentUid, TextView emptyText) {
        super(context, 0, friendsUids);
        this.currentUserUid = currentUid;
        this.mDB = FirebaseDatabase.getInstance();
        this.emptyText = emptyText;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_item, parent, false);
        }
        String uid = getItem(position);
        TextView name = convertView.findViewById(R.id.friendName);
        TextView username = convertView.findViewById(R.id.friendUser);
        ImageButton btn = convertView.findViewById(R.id.removeFriendBtn);
        btn.setTag(position);
        btn.setOnClickListener(view -> {
            this.removeFriend(view);
        });
        //Recover friend's data.
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference(Constants.USERS_PATH + uid);
        dbRef.get().addOnSuccessListener(v -> {
            User user = v.getValue(User.class);
            name.setText(user.getName());
            username.setText(user.getUserName());
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (this.getCount() < 1)
            emptyText.setText("No tienes amigos...");
    }

    private void removeFriend(View view) {
        //Delete friend in both user's and friend's list.
        int pos = (int) view.getTag();
        mDB.getReference(Constants.FRIENDS_LIST +
                getItem(pos) +
                "/" +
                currentUserUid)
                .setValue(null).addOnSuccessListener(v -> {
            mDB.getReference(Constants.FRIENDS_LIST +
                    currentUserUid +
                    "/" +
                    getItem(pos))
                    .setValue(null).addOnSuccessListener(v2 -> {
                remove(getItem(pos));
            });
        });
    }



}
