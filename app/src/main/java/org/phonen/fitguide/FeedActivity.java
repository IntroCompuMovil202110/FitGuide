package org.phonen.fitguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.phonen.fitguide.helpers.FeedAdapter;
import org.phonen.fitguide.model.Post;
import org.phonen.fitguide.model.User;
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    private final String STATE_LIST = "State Adapter List";
    private final String _LIST = "State Adapter List";


    BottomNavigationView bottomNavigationView;
    private FirebaseUser user;
    private DatabaseReference ref;
    private ListView list;
    private List<String> friendsUids;
    private List<Post> postsList;
    private Map<String, Boolean> postIndexed;
    private Map<String, User> friendsMap;
    private FeedAdapter feedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        navBarSettings();

        friendsUids = new ArrayList<>();
        postsList = new ArrayList<>();
        postIndexed = new HashMap<>();
        friendsMap = new HashMap<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        list = findViewById(R.id.feed_list_view);
        feedAdapter = new FeedAdapter(this, postsList, friendsMap, user.getUid());

        list.setAdapter(feedAdapter);

        this.getFeedForUser();
    }

    private void getFeedForUser() {
        ref = FirebaseDatabase.getInstance().getReference("friends/" + user.getUid());
        ref.get().addOnSuccessListener(v -> {
            // Get all user friends
            for (DataSnapshot ds : v.getChildren())
                friendsUids.add(ds.getKey());
            friendsUids.add(user.getUid());

            //Iterate over friends
            for (String user : friendsUids) {
                DatabaseReference posts = FirebaseDatabase.getInstance().getReference(Constants.POSTS_PATH + user);
                //Iterate over posts
                posts.get().addOnSuccessListener(a -> {
                    // If the user does have post
                    Log.i("USERS", "Inside");
                    if (a.getValue() != null) {
                        FirebaseDatabase.getInstance().getReference(Constants.USERS_PATH + a.getKey())
                                .get()
                                .addOnSuccessListener(b -> {
                                    Log.i("POSTS", "Inside");
                                    friendsMap.put(a.getKey(), b.getValue(User.class));
                                    for (DataSnapshot ds : a.getChildren()) {
                                        if (postIndexed.get(ds.getKey()) == null) {
                                            postIndexed.put(ds.getKey(), true);
                                            feedAdapter.add(ds.getValue(Post.class));
                                            feedAdapter.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                                        }
                                    }
                                });
                    }
                });
            }


        });


    }

    public void navBarSettings() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.feedActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
                    startActivity(new Intent(getApplicationContext(), ChallengesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.feedActivity:
                    return true;
                case R.id.profileActivity:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.startActivity:
                    startActivity(new Intent(getApplicationContext(), StartActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}