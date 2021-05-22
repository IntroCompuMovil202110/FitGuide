package org.phonen.fitguide.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.phonen.fitguide.R;
import org.phonen.fitguide.model.Post;
import org.phonen.fitguide.model.User;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedAdapter extends ArrayAdapter<Post> {

    private Map<String, User> users;
    private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    private Map<String, Bitmap> cachedImages;
    private String uid;

    public FeedAdapter(@NonNull Context context, List<Post> posts, Map<String, User> users, String userUID) {
        super(context, 0, posts);
        this.users = users;
        this.uid = userUID;
        this.cachedImages = new HashMap<>();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Post post = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_item, parent, false);
        }


        TextView name = convertView.findViewById(R.id.feed_name);
        TextView username = convertView.findViewById(R.id.feed_username);
        TextView time = convertView.findViewById(R.id.feed_time);
        TextView description = convertView.findViewById(R.id.feed_text);
        ImageView image = convertView.findViewById(R.id.feed_image_post);
        ImageView logo = convertView.findViewById(R.id.feed_logo_icon);


        User user = users.get(post.getUserUID());
        name.setText(user.getName() + " " + user.getLastName());
        username.setText(user.getUserName());
        time.setText(format.format(post.getDate()));
        description.setText(post.getDescription());
        if(!post.getImagePath().isEmpty()){
            if(cachedImages.get(post.getImagePath()) == null){
                StorageReference imageRef = FirebaseStorage.getInstance().getReference();
                imageRef.child(post.getImagePath())
                        .getBytes(5 * 1024 * 1024).addOnSuccessListener(v -> {
                    Bitmap bm = BitmapFactory.decodeByteArray(v, 0, v.length);
                    image.setImageBitmap(bm);
                    cachedImages.put(post.getImagePath(), bm);
                });
            }else {
                Log.i("CACHE HIT", "Already cached image, using this instead");
                image.setImageBitmap(cachedImages.get(post.getImagePath()));
            }
        }

        LinearLayout layoutTop = convertView.findViewById(R.id.feed_top_card);
        if(post.getUserUID().equals(uid)){
            layoutTop.setBackground(convertView.getResources().getDrawable(R.drawable.feed_own_user, getContext().getTheme()));
        } else {
            layoutTop.setBackground(convertView.getResources().getDrawable(R.drawable.border_feed_message, getContext().getTheme()));
        }
        switch (post.getType()) {
            case 0:
                logo.setImageDrawable(convertView.getResources().getDrawable(R.drawable.accept_icon, getContext().getTheme()));
                break;
            case 1:
                logo.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_baseline_assignment_turned_in_24, getContext().getTheme()));
                break;
            case 2:
                logo.setImageDrawable(convertView.getResources().getDrawable(R.drawable.add_friend_icon, getContext().getTheme()));
                break;
        }

        return convertView;
    }
}
