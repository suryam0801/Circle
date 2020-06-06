package circleapp.circlepackage.circle.Explore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Notification.NotificationActivity;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class ExploreTabbedActivity extends AppCompatActivity {


    private ImageView profPic, notificationBell;

    private User user;

    int[] myImageList = new int[]{R.drawable.person_blonde_head, R.drawable.person_job, R.drawable.person_singing,
            R.drawable.person_teacher, R.drawable.person_woman_dancing};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tabbed);

        profPic = findViewById(R.id.explore_profilePicture);
        notificationBell = findViewById(R.id.main_activity_notifications_bell);


        user = SessionStorage.getUser(ExploreTabbedActivity.this);

        Random r = new Random();
        int count = r.nextInt((4 - 0) + 1);
        Glide.with(ExploreTabbedActivity.this)
                .load(user.getProfileImageLink())
                .placeholder(ContextCompat.getDrawable(ExploreTabbedActivity.this, myImageList[count]))
                .into(profPic);


        notificationBell.setOnClickListener(v -> {
            startActivity(new Intent(ExploreTabbedActivity.this, NotificationActivity.class));
            finish();
        });

        profPic.setOnClickListener(v -> {
            startActivity(new Intent(ExploreTabbedActivity.this, EditProfile.class));
            finish();
        });

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
    }
}
