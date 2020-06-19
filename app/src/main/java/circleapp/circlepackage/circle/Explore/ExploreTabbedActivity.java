package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Notification.NotificationActivity;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class ExploreTabbedActivity extends AppCompatActivity {


    private ImageView profPic, notificationBell;
    private User user;
    int myImageList ;
    private Uri intentUri;
    private FirebaseDatabase database;
    private DatabaseReference circlesDB, usersDB;
    private Circle popupCircle;
    private Dialog linkCircleDialog, circleJoinSuccessDialog;
    private boolean link_flag = false;
    private String url;
    private TabLayout tabLayout;
    private TabItem exploreTab, workbenchTab;
    Intent shareIntent;
    Boolean circleExists = false;
    AnalyticsLogEvents analyticsLogEvents;
    int propic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tabbed);
        analyticsLogEvents = new AnalyticsLogEvents();
        intentUri = getIntent().getData();
        //recieve ur request on opening
        if (intentUri != null) {
            analyticsLogEvents.logEvents(ExploreTabbedActivity.this, "invite_link", "used_link", "read_external_link");
            url = getIntent().getData().toString();
            processUrl(url);
        }

        profPic = findViewById(R.id.explore_profilePicture);
        notificationBell = findViewById(R.id.main_activity_notifications_bell);
        tabLayout = findViewById(R.id.main_tab_layout);
        exploreTab = findViewById(R.id.main_explore_tab);
        workbenchTab = findViewById(R.id.main_workbench_tab);

        user = SessionStorage.getUser(ExploreTabbedActivity.this);

        if (user.getProfileImageLink().matches("^[a-zA-Z]*$"))
        {
            Glide.with(ExploreTabbedActivity.this)
                    .load(user.getProfileImageLink())
                    .into(profPic);
        }
        else
            {
                propic = Integer.parseInt(user.getProfileImageLink());
                myImageList = propic;
                Glide.with(ExploreTabbedActivity.this)
                        .load(propic)
                        .placeholder(ContextCompat.getDrawable(ExploreTabbedActivity.this, myImageList))
                        .into(profPic);
            }
        Random r = new Random();
        int count = r.nextInt((4 - 0) + 1);

        if (propic != 0)
        {
        }
        else
            {
            }


        notificationBell.setOnClickListener(v -> {
            startActivity(new Intent(ExploreTabbedActivity.this, NotificationActivity.class));
            finish();
        });

        profPic.setOnClickListener(v -> {

            startActivity(new Intent(ExploreTabbedActivity.this, EditProfile.class));
            finish();
        });

        //setting the tab adapter
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    private void showLinkPopup() {
        linkCircleDialog = new Dialog(ExploreTabbedActivity.this);
        linkCircleDialog.setContentView(R.layout.circle_card_display_view); //set dialog view

        TextView tv_circleName, tv_creatorName, tv_circleDesc;
        ChipGroup circleDisplayTags;
        Button join;
        ImageButton share;
        LinearLayout container;

        container = linkCircleDialog.findViewById(R.id.container);
        tv_circleName = linkCircleDialog.findViewById(R.id.circle_name);
        tv_creatorName = linkCircleDialog.findViewById(R.id.circle_creatorName);
        tv_circleDesc = linkCircleDialog.findViewById(R.id.circle_desc);
        circleDisplayTags = linkCircleDialog.findViewById(R.id.circle_display_tags);
        join = linkCircleDialog.findViewById(R.id.circle_card_join);
        share = linkCircleDialog.findViewById(R.id.circle_card_share);

        GradientDrawable wbItemBackground = new GradientDrawable();
        wbItemBackground.setShape(GradientDrawable.RECTANGLE);
        wbItemBackground.setCornerRadius(30.0f);
        wbItemBackground.setColor(Color.parseColor("#FE42AE"));

        container.setBackground(wbItemBackground);
        tv_circleName.setText(popupCircle.getName());
        tv_creatorName.setText(popupCircle.getCreatorName());
        tv_circleDesc.setText(popupCircle.getDescription());

        if (popupCircle.getAcceptanceType().equalsIgnoreCase("review")) {
            join.setText("Apply");
        }


        for (String tag : popupCircle.getInterestTags().keySet())
            setPopupInterestTag(tag, circleDisplayTags, "#D42D8D");

        boolean alreadyMember = false;
        boolean alreadyApplicant = false;
        if (popupCircle.getMembersList() != null && popupCircle.getMembersList().containsKey(user.getUserId())) {
            join.setText("Already Member");
            alreadyMember = true;
        }
        if (popupCircle.getApplicantsList() != null && popupCircle.getApplicantsList().containsKey(user.getUserId())) {
            join.setText("Already Applied");
            alreadyApplicant = true;
        }


        boolean finalAlreadyMember = alreadyMember;
        boolean finalAlreadyApplicant = alreadyApplicant;
        join.setOnClickListener(view -> {
            if (finalAlreadyMember == false && finalAlreadyApplicant == false) {
                linkCircleDialog.cancel();
                getIntent().setData(null);
                applyOrJoin(popupCircle);
            }
        });

        share.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(ExploreTabbedActivity.this, "invite_workbench", "circle_invite_member", "on_button_click");
            showShareCirclePopup(popupCircle);
        });

        linkCircleDialog.setOnDismissListener(dialogInterface -> {
            getIntent().setData(null);
        });

        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);


        linkCircleDialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, height);
        linkCircleDialog.getWindow().setGravity(Gravity.CENTER);
        linkCircleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        linkCircleDialog.show();
    }

    public void setPopupInterestTag(final String name, ChipGroup chipGroupLocation, String chipColor) {
        final Chip chip = new Chip(ExploreTabbedActivity.this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setRippleColor(ColorStateList.valueOf(Color.WHITE));
        chip.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3,
                        getResources().getDisplayMetrics()
                ),
                paddingDp, paddingDp, paddingDp);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.parseColor(chipColor),
                Color.parseColor(chipColor),
                Color.parseColor(chipColor),
                Color.parseColor(chipColor)
        };

        ColorStateList myList = new ColorStateList(states, colors);

        chip.setChipBackgroundColor(myList);
        chip.setChipCornerRadius(60);
        chip.setChipMinHeight(100);
        chip.setTextColor(Color.WHITE);
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);

        chipGroupLocation.addView(chip);
    }

    private void showShareCirclePopup(Circle c) {
        try {
            shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage = "\nCome join my circle: " + c.getName() + "\n\n";
            //https://play.google.com/store/apps/details?id=
            shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + c.getId();
            Log.d("Share", shareMessage);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }

    private void applyOrJoin(final Circle circle) {
        circleJoinSuccessDialog = new Dialog(ExploreTabbedActivity.this);
        circleJoinSuccessDialog.setContentView(R.layout.apply_popup_layout);
        Button closeDialogButton = circleJoinSuccessDialog.findViewById(R.id.completedDialogeDoneButton);
        TextView title = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationTitle);
        TextView description = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationDescription);

        if (circle.getAcceptanceType().equalsIgnoreCase("automatic")) {
            title.setText("Successfully Joined!");
            description.setText("Congradulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Click 'Done' to be redirected to this circle's wall.");
        }

        closeDialogButton.setOnClickListener(view -> {
            if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                SessionStorage.saveCircle(ExploreTabbedActivity.this, circle);
                startActivity(new Intent(ExploreTabbedActivity.this, CircleWall.class));
            }
            circleJoinSuccessDialog.cancel();
        });

        circleJoinSuccessDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        usersDB = database.getReference().child("Users").child(user.getUserId());

        Subscriber subscriber = new Subscriber(user.getUserId(), user.getName(),
                user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

        if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            //adding userID to applicants list
            circlesDB.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            circleJoinSuccessDialog.show();

        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
            int nowActive = user.getActiveCircles() + 1;
            usersDB.child("activeCircles").setValue((nowActive));
            circleJoinSuccessDialog.show();
        }

    }
    public void processUrl(String url){
        String lines[] = url.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            Log.d("URL", lines[i]);
        }
        url = url.replace("https://worfo.app.link/8JMEs34W96/?", "");
        String circleID = url;
        Log.d("TAG", "URI:" + circleID);
        database = FirebaseDatabase.getInstance();
        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true);

        circlesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Circle circle = snapshot.getValue(Circle.class);
                    if (circle.getId().equals(circleID)) {
                        circleExists = true;
                        popupCircle = circle;
                        if (getIntent().getData() != null) {
                            showLinkPopup();
                        }

                    }
                }
                if (circleExists == false) {
                    analyticsLogEvents.logEvents(ExploreTabbedActivity.this, "_expired_invite_link", "incorrect_link", "read_external_link");
                    Toast.makeText(ExploreTabbedActivity.this, "The circle shared does not exist anymore", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
