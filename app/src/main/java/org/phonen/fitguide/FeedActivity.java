package org.phonen.fitguide;

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
import org.phonen.fitguide.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private FirebaseUser user;
    private DatabaseReference ref;
    private ListView list;
    private List<String> friendsUids;
    private List<Post> postsList;
    private Map<String, Boolean> postIndexed;
    private FeedAdapter feedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        navBarSettings();

        friendsUids = new ArrayList<>();
        postsList = new ArrayList<>();
        postIndexed = new HashMap<>();
        list = findViewById(R.id.feed_list_view);

        this.getFeedForUser();
    }

    private void getFeedForUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("friends/" + user.getUid());
        ref.get().addOnSuccessListener( v -> {
            for(DataSnapshot ds : v.getChildren()){
                friendsUids.add(ds.getKey());
            }

            for(String user: friendsUids){
                DatabaseReference posts = FirebaseDatabase.getInstance().getReference(Constants.POSTS_PATH + user);
                posts.get().addOnSuccessListener( a -> {
                   Log.i("Arrived", a.toString());
                   if(a.getValue() != null){
                       for(DataSnapshot ds: a.getChildren()){
                           if(postIndexed.get(ds.getKey() == null)){
                               postIndexed.put(ds.getKey(), true);
                               postsList.add(ds.getValue(Post.class));
                           }
                       }
                   }
                });
            }
            //feedAdapter = new FeedAdapter(this, postsList, )


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
}