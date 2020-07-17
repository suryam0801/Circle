package circleapp.circlepackage.circle.Explore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.CreateCircle.CreateCircle;
import circleapp.circlepackage.circle.CreateCircle.CreateCircleCategoryPicker;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Login.OtpActivity;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;
import ru.dimorinny.showcasecard.position.Center;
import ru.dimorinny.showcasecard.step.ShowCaseStep;
import ru.dimorinny.showcasecard.step.ShowCaseStepDisplayer;


public class ExploreTabbedActivity extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private ImageView profPicHolder;
    TextView location;
    private User user;
    private Uri intentUri;
    private Dialog linkCircleDialog, circleJoinSuccessDialog;
    private String url;
    private TextView locationDisplay;
    private BottomNavigationView bottomNav;
    Boolean popupCalled = false;
    View decorView;
    boolean shownPopup;
    private FloatingActionButton btnAddCircle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tabbed);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        location = findViewById(R.id.explore_district_name_display);
        user = SessionStorage.getUser(this);
        FirebaseRetrievalViewModel tempViewModel = ViewModelProviders.of(ExploreTabbedActivity.this).get(FirebaseRetrievalViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(user.getUserId());
        tempLiveData.observe((LifecycleOwner) ExploreTabbedActivity.this, dataSnapshot -> {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                String string = new Gson().toJson(user);
                SessionStorage.saveUser(ExploreTabbedActivity.this, user);
            }
        });
        location.setText(user.getDistrict());
        intentUri = getIntent().getData();
        shownPopup = false;

        //to hide the status and nav bar
        decorView = getWindow().getDecorView();

        //recieve ur request on opening
        if (intentUri != null) {
            url = getIntent().getData().toString();
            processUrl(url);
        }


        profPicHolder = findViewById(R.id.explore_profilePicture);
        HelperMethods.increaseTouchArea(profPicHolder);
        locationDisplay = findViewById(R.id.explore_district_name_display);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnAddCircle = findViewById(R.id.add_circle_button);

        locationDisplay.setText(user.getDistrict());

        if (user.getProfileImageLink().length() > 10) { //checking if its uploaded image
            Glide.with(ExploreTabbedActivity.this)
                    .load(user.getProfileImageLink())
                    .into(profPicHolder);
        } else if (user.getProfileImageLink().equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(ExploreTabbedActivity.this)
                    .load(ContextCompat.getDrawable(ExploreTabbedActivity.this, profilePic))
                    .into(profPicHolder);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(user.getProfileImageLink());
            Glide.with(ExploreTabbedActivity.this)
                    .load(ContextCompat.getDrawable(ExploreTabbedActivity.this, profilePic))
                    .into(profPicHolder);
        }

        profPicHolder.setOnClickListener(v -> {
            finishAfterTransition();
            startActivity(new Intent(ExploreTabbedActivity.this, EditProfile.class));
        });

        btnAddCircle.setOnClickListener(v -> {
            finishAfterTransition();
            startActivity(new Intent(this, CreateCircleCategoryPicker.class));
        });


        if (getIntent().getBooleanExtra("fromFilters", false)) {
            bottomNav.setSelectedItemId(R.id.explore_bottom_nav_item);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        } else if (getIntent().getIntExtra("exploreIndex", -1) != -1) {
            SessionStorage.tempIndexStore(this, getIntent().getIntExtra("exploreIndex", -1));
            bottomNav.setSelectedItemId(R.id.explore_bottom_nav_item);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WorkbenchFragment()).commit();
        }


        //set view pager adapter
        bottomNav.setOnNavigationItemSelectedListener(navListener);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.workbench_bottom_nav_item:
                selectedFragment = new WorkbenchFragment();
                break;
            case R.id.explore_bottom_nav_item:
                selectedFragment = new ExploreFragment();
                break;
            case R.id.create_circle_nav_bar:
                selectedFragment = new WorkbenchFragment();
                break;
            case R.id.notifications_bottom_nav_item:
                selectedFragment = new NotificationFragment();
                break;

            case R.id.search_bottom_nav_item:
                selectedFragment = new FeedbackFragment();
                break;

        }
        assert selectedFragment != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).commit();
        return true;
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showLinkPopup(Circle popupCircle) {
        linkCircleDialog = new Dialog(ExploreTabbedActivity.this);
        linkCircleDialog.setContentView(R.layout.circle_card_display_view); //set dialog view

        TextView tv_circleName, tv_creatorName, tv_circleDesc;
        Button join;
        ImageView bannerImage;
        CircleImageView profPic;

        tv_circleName = linkCircleDialog.findViewById(R.id.circle_name);
        tv_creatorName = linkCircleDialog.findViewById(R.id.circle_creatorName);
        tv_circleDesc = linkCircleDialog.findViewById(R.id.circle_desc);
        join = linkCircleDialog.findViewById(R.id.circle_card_join);
        bannerImage = linkCircleDialog.findViewById(R.id.circle_banner_image);
        profPic = linkCircleDialog.findViewById(R.id.explore_circle_logo);

        switch (popupCircle.getCategory()) {
            case "Events":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_events)).centerCrop().into(bannerImage);
                break;
            case "Apartments & Communities":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_apartment_and_communities)).centerCrop().into(bannerImage);
                break;
            case "Sports":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_sports)).centerCrop().into(bannerImage);
                break;
            case "Friends & Family":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_friends_and_family)).centerCrop().into(bannerImage);
                break;
            case "Food & Entertainment":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_food_and_entertainment)).centerCrop().into(bannerImage);
                break;
            case "Science & Tech":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_science_and_tech_background)).centerCrop().into(bannerImage);
                break;
            case "Gaming":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_gaming)).centerCrop().into(bannerImage);
                break;
            case "Health & Fitness":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_health_and_fitness)).centerCrop().into(bannerImage);
                break;
            case "Students & Clubs":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_students_and_clubs)).centerCrop().into(bannerImage);
                break;
            case "The Circle App":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.admin_circle_banner)).centerCrop().into(bannerImage);
                break;
            default:
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_custom_circle)).centerCrop().into(bannerImage);
                break;
        }

        if (!popupCircle.getBackgroundImageLink().equals("default"))
            Glide.with(this).load(popupCircle.getBackgroundImageLink()).into(profPic);
        else {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
            Glide.with(this)
                    .load(ContextCompat.getDrawable(this, profilePic))
                    .into(profPic);
        }

        tv_circleName.setText(popupCircle.getName());
        tv_creatorName.setText(popupCircle.getCreatorName());
        tv_circleDesc.setText(popupCircle.getDescription());

        if (popupCircle.getAcceptanceType().equalsIgnoreCase("review"))
            join.setText("Apply");

        final boolean alreadyMember = HelperMethods.isMemberOfCircle(popupCircle, user.getUserId());
        final boolean alreadyApplicant = HelperMethods.ifUserApplied(popupCircle, user.getUserId());

        if (alreadyApplicant)
            join.setText("Already Applied");

        if (alreadyMember)
            join.setText("Already Member");

        join.setOnClickListener(view -> {
            if (!alreadyMember && !alreadyApplicant) {
                linkCircleDialog.dismiss();
                applyOrJoin(popupCircle);
            } else {
                linkCircleDialog.dismiss();
            }
        });

        //clearing intent data so popup doesnt show each time
        linkCircleDialog.setOnDismissListener(dialogInterface -> {
            getIntent().setData(null);
        });


        linkCircleDialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.popup_height)); //width,height
        linkCircleDialog.getWindow().setGravity(Gravity.CENTER);
        linkCircleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        linkCircleDialog.show();
        shownPopup = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void applyOrJoin(final Circle circle) {
        circleJoinSuccessDialog = new Dialog(ExploreTabbedActivity.this);
        circleJoinSuccessDialog.setContentView(R.layout.apply_popup_layout);
        circleJoinSuccessDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button closeDialogButton = circleJoinSuccessDialog.findViewById(R.id.completedDialogeDoneButton);
        TextView title = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationTitle);
        TextView description = circleJoinSuccessDialog.findViewById(R.id.applyConfirmationDescription);
        circleJoinSuccessDialog.show();

        //case for review is preset in XML
        if (circle.getAcceptanceType().equalsIgnoreCase("automatic")) {
            linkCircleDialog.dismiss();
            title.setText("Successfully Joined!");
            description.setText("Congratulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Click 'Done' to be redirected to this circle's wall.");
        }

        closeDialogButton.setOnClickListener(view -> {
            if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                finishAfterTransition();
                SessionStorage.saveCircle(ExploreTabbedActivity.this, circle);
                shownPopup = false;
                startActivity(new Intent(ExploreTabbedActivity.this, CircleWall.class));
            }
            circleJoinSuccessDialog.cancel();
        });

        Subscriber subscriber = new Subscriber(user.getUserId(), user.getName(),
                user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

        FirebaseWriteHelper.applyOrJoin(this, circle, user, subscriber);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void processUrl(String url) {
        String circleID = HelperMethods.getCircleIdFromShareURL(url);

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsCircleValueCirlceLiveData(circleID);

        liveData.observe(this, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (!popupCalled) {
                if(circle!=null){
                    showLinkPopup(circle);
                    popupCalled = true;
                }
                else
                    Toast.makeText(ExploreTabbedActivity.this, "That Circle does not exist anymore",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //bottom sheet dialog onclick (only called when nagivating from create circle)
    @Override
    public void onButtonClicked(String text) {
        Circle circle = SessionStorage.getCircle(this);
        switch (text) {
            case "shareLink":
                HelperMethods.showShareCirclePopup(circle, this);
                break;
            case "copyLink":
                HelperMethods.copyLinkToClipBoard(circle, this);
                break;
        }
    }
}
