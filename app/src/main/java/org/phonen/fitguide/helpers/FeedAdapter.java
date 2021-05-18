package org.phonen.fitguide.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.phonen.fitguide.R;
import org.phonen.fitguide.model.Post;
import org.phonen.fitguide.model.User;

import java.util.List;

public class FeedAdapter extends ArrayAdapter<Post> {

    List<User> users;
    List<String> uids;

    public FeedAdapter(@NonNull Context context, List<Post> posts, List<User> users, List<String> uids) {
        super(context, 0, posts);
        this.users = users;
        this.uids = uids;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Post post = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item, parent, false);
        }

        TextView name = convertView.findViewById(R.id.feed_name);
        TextView username = convertView.findViewById(R.id.feed_username);
        TextView time = convertView.findViewById(R.id.feed_time);
        TextView description = convertView.findViewById(R.id.feed_text);

        int index = uids.indexOf(post.getUserUID());
        if(index != -1){
            User user = users.get(index);
            name.setText(user.getName() + user.getLastName());
            username.setText(user.getLevel());
            // time.setText(post.getType());
            switch (post.getType()){
                case 0:
                    description.setText("Ha completado un nuevo ejercicio! Ha quemado " + post.getDescription() + " calorias!");
                    break;
            }
        }

        return convertView;
    }
}
