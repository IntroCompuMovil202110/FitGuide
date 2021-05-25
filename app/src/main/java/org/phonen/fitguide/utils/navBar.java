package org.phonen.fitguide.utils;

import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.phonen.fitguide.ChallengesActivity;
import org.phonen.fitguide.FeedActivity;
import org.phonen.fitguide.ProfileActivity;
import org.phonen.fitguide.R;
import org.phonen.fitguide.StartActivity;

public class navBar {
    public Intent navBarSettings(BottomNavigationView bottomNavigationView, Context context){
        final Intent[] intent = {new Intent()};
        bottomNavigationView.setSelectedItemId(R.id.profileActivity);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.challengesActivity:
                    intent[0] = new Intent(context, ChallengesActivity.class);
                    return true;
                case R.id.feedActivity:
                    intent[0] = new Intent(context, FeedActivity.class);
                    return true;
                case R.id.profileActivity:
                    intent[0] = new Intent(context, ProfileActivity.class);
                    return true;
                case R.id.startActivity:
                    intent[0] = new Intent(context, StartActivity.class);
                    return true;
            }
            return false;
        });
        return intent[0];
    }
}
