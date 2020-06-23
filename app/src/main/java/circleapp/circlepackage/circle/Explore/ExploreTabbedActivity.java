package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import ru.dimorinny.showcasecard.position.Center;
import ru.dimorinny.showcasecard.step.ShowCaseStep;
import ru.dimorinny.showcasecard.step.ShowCaseStepDisplayer;


public class ExploreTabbedActivity extends AppCompatActivity {

    private ImageView profPicHolder;
    TextView location;
    private User user;
    private Uri intentUri;
    private FirebaseDatabase database;
    private DatabaseReference circlesDB, usersDB;
    private Circle popupCircle;
    private Dialog linkCircleDialog, circleJoinSuccessDialog;
    private String url;
    private TextView locationDisplay;
    Boolean circleExists = false;
    AnalyticsLogEvents analyticsLogEvents;
    View decorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tabbed);
        location  = findViewById(R.id.explore_district_name_display);
        User user = SessionStorage.getUser(this);
        location.setText(user.getDistrict());
        analyticsLogEvents = new AnalyticsLogEvents();
        intentUri = getIntent().getData();

        //to hide the status and nav bar
        decorView = getWindow().getDecorView();
        hideSystemUI();

        //recieve ur request on opening
        if (intentUri != null) {
            analyticsLogEvents.logEvents(ExploreTabbedActivity.this, "invite_link", "used_link", "read_external_link");
            url = getIntent().getData().toString();
            processUrl(url);
        }
        

        profPicHolder = findViewById(R.id.explore_profilePicture);
        HelperMethods.increaseTouchArea(profPicHolder);
        locationDisplay = findViewById(R.id.explore_district_name_display);


        new ShowCaseStepDisplayer.Builder(this)
                .addStep(new ShowCaseStep(profPicHolder, "Message at center"))
                .addStep(new ShowCaseStep(locationDisplay, "Message at View"))
                .build().start();


        user = SessionStorage.getUser(ExploreTabbedActivity.this);

        locationDisplay.setText(user.getDistrict());

        if (user.getProfileImageLink().length() > 10) { //checking if its uploaded image
            Glide.with(ExploreTabbedActivity.this)
                    .load(user.getProfileImageLink())
                    .into(profPicHolder);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(user.getProfileImageLink());
            Glide.with(ExploreTabbedActivity.this)
                    .load(ContextCompat.getDrawable(ExploreTabbedActivity.this, profilePic))
                    .into(profPicHolder);
        }



        profPicHolder.setOnClickListener(v -> {
            startActivity(new Intent(ExploreTabbedActivity.this, EditProfile.class));
            finish();
        });

        setViewPageAdapter();
    }

    private void setViewPageAdapter() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new WorkbenchFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =

            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.workbench_bottom_nav_item:
                            selectedFragment = new WorkbenchFragment();
                            break;
                        case R.id.explore_bottom_nav_item:
                            selectedFragment = new ExploreFragment();
                            break;

                        case R.id.notifications_bottom_nav_item:
                            selectedFragment = new NotificationFragment();
                            break;

                        case R.id.search_bottom_nav_item:
                            selectedFragment = new FeedbackFragment();
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                return true;
            };

    private void showLinkPopup() {
        linkCircleDialog = new Dialog(ExploreTabbedActivity.this);
        linkCircleDialog.setContentView(R.layout.circle_card_display_view); //set dialog view

        TextView tv_circleName, tv_creatorName, tv_circleDesc;
        Button join;
        ImageButton share;
        LinearLayout container;

        container = linkCircleDialog.findViewById(R.id.container);
        tv_circleName = linkCircleDialog.findViewById(R.id.circle_name);
        tv_creatorName = linkCircleDialog.findViewById(R.id.circle_creatorName);
        tv_circleDesc = linkCircleDialog.findViewById(R.id.circle_desc);
        join = linkCircleDialog.findViewById(R.id.circle_card_join);
        share = linkCircleDialog.findViewById(R.id.circle_card_share);

        GradientDrawable wbItemBackground = HelperMethods.gradientRectangleDrawableSetter(30);
        wbItemBackground.setColor(Color.parseColor("#FE42AE"));

        container.setBackground(wbItemBackground);
        tv_circleName.setText(popupCircle.getName());
        tv_creatorName.setText(popupCircle.getCreatorName());
        tv_circleDesc.setText(popupCircle.getDescription());

        if (popupCircle.getAcceptanceType().equalsIgnoreCase("review"))
            join.setText("Apply");

        final boolean alreadyMember = HelperMethods.isMemberOfCircle(popupCircle, user.getUserId());
        final boolean alreadyApplicant = HelperMethods.ifUserApplied(popupCircle, user.getUserId());

        join.setOnClickListener(view -> {
            if (!alreadyMember && !alreadyApplicant) {
                linkCircleDialog.dismiss();
                applyOrJoin(popupCircle);
            }
        });

        share.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(ExploreTabbedActivity.this, "invite_workbench", "circle_invite_member", "on_button_click");
            HelperMethods.showShareCirclePopup(popupCircle, ExploreTabbedActivity.this);
        });

        //clearing intent data so popup doesnt show each time
        linkCircleDialog.setOnDismissListener(dialogInterface -> {
            getIntent().setData(null);
        });


        linkCircleDialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.popup_height)); //width,height
        linkCircleDialog.getWindow().setGravity(Gravity.CENTER);
        linkCircleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        linkCircleDialog.show();
    }

    private void applyOrJoin(final Circle circle) {
        circleJoinSuccessDialog = new Dialog(ExploreTabbedActivity.this);
        circleJoinSuccessDialog.setContentView(R.layout.apply_popup_layout);
        circleJoinSuccessDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button closeDialogButton = circleJoinSuccessDialog.findViewById(R.id.completedDialogeDoneButton);
        TextView title = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationTitle);
        TextView description = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationDescription);

        //case for review is preset in XML
        if (circle.getAcceptanceType().equalsIgnoreCase("automatic")) {
            title.setText("Successfully Joined!");
            description.setText("Congradulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Click 'Done' to be redirected to this circle's wall.");
        }

        closeDialogButton.setOnClickListener(view -> {
            if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                SessionStorage.saveCircle(ExploreTabbedActivity.this, circle);
                startActivity(new Intent(ExploreTabbedActivity.this, CircleWall.class));
                finish();
            }
            circleJoinSuccessDialog.cancel();
        });


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

    public void processUrl(String url) {
        String circleID = HelperMethods.getCircleIdFromShareURL(url);

        database = FirebaseDatabase.getInstance();
        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true);

        circlesDB.addListenerForSingleValueEvent(new ValueEventListener() {
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
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
