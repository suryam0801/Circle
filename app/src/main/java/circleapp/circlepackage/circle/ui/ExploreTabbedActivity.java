package circleapp.circlepackage.circle.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import circleapp.circlepackage.circle.ViewModels.ParticularCircleViewModel;
import circleapp.circlepackage.circle.ViewModels.UserViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.CircleWall;
import circleapp.circlepackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.EditProfile.EditProfile;

import circleapp.circlepackage.circle.ui.CreateCircle.CreateCircleCategoryPicker;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ui.Explore.ExploreFragment;
import circleapp.circlepackage.circle.ui.Feedback.FeedbackFragment;
import circleapp.circlepackage.circle.ui.MyCircles.WorkbenchFragment;
import circleapp.circlepackage.circle.ui.Notifications.NotificationFragment;
import de.hdodenhof.circleimageview.CircleImageView;


public class ExploreTabbedActivity extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private ImageView profPicHolder;
    private TextView location;
    private User user;
    private String  intentUri;
    private Dialog linkCircleDialog, circleJoinSuccessDialog;
    private String url;
    private TextView locationDisplay;
    private BottomNavigationView bottomNav;
    private Boolean popupCalled = false;
    private View decorView;
    boolean shownPopup;
    private FloatingActionButton btnAddCircle;
    private GlobalVariables globalVariables = new GlobalVariables();
    //Popup circle elements
    private TextView tv_circleName, tv_creatorName, tv_circleDesc;
    private Button join;
    private ImageView bannerImage;
    private CircleImageView profPic;
    private boolean alreadyMember, alreadyApplicant;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_tabbed);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initUIElements();
        initObserverForUser();
        setCirclePosition();

        profPicHolder.setOnClickListener(v -> {
            finishAfterTransition();
            startActivity(new Intent(ExploreTabbedActivity.this, EditProfile.class));
        });

        btnAddCircle.setOnClickListener(v -> {
            finishAfterTransition();
            startActivity(new Intent(this, CreateCircleCategoryPicker.class));
        });
        //set view pager adapter
        bottomNav.setOnNavigationItemSelectedListener(navListener);
    }
    private void setCirclePosition(){
        if (getIntent().getBooleanExtra("fromFilters", false)) {
            bottomNav.setSelectedItemId(R.id.explore_bottom_nav_item);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        } else if (getIntent().getIntExtra("exploreIndex", -1) != -1) {
            bottomNav.setSelectedItemId(R.id.explore_bottom_nav_item);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WorkbenchFragment()).commit();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initUIElements(){
        location = findViewById(R.id.explore_district_name_display);
        user = globalVariables.getCurrentUser();
        location.setText(user.getDistrict());
        intentUri = getIntent().getStringExtra("imagelink");
        shownPopup = false;

        //to hide the status and nav bar
        decorView = getWindow().getDecorView();

        //recieve ur request on opening
        if (intentUri != null) {
            Log.d("intenturl",intentUri);
            url = intentUri;
            processUrl(url);
        }


        profPicHolder = findViewById(R.id.explore_profilePicture);
        HelperMethodsUI.increaseTouchArea(profPicHolder);
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
            int index = Integer.parseInt(String.valueOf(user.getProfileImageLink().charAt(user.getProfileImageLink().length()-1)));
            index = index-1;
            Log.d("index", index+"");
            TypedArray avatarResourcePos = this.getResources().obtainTypedArray(R.array.AvatarValues);
            int profilePic = avatarResourcePos.getResourceId(index, 0);
            Glide.with(ExploreTabbedActivity.this)
                    .load(ContextCompat.getDrawable(ExploreTabbedActivity.this, profilePic))
                    .into(profPicHolder);
        }
    }
    private void initObserverForUser(){
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.getUserObject(this).observe((LifecycleOwner)this, userObject -> {
            if(userObject != null)
            {
                user = userObject;
            }
        });
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
        initPopupElements(popupCircle);

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
    }
    private void initPopupElements(Circle popupCircle){
        linkCircleDialog = new Dialog(ExploreTabbedActivity.this);
        linkCircleDialog.setContentView(R.layout.circle_card_display_view); //set dialog view

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
            case "General":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_general)).centerCrop().into(bannerImage);
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
        if(popupCircle.getAcceptanceType()!=null){
            if (popupCircle.getAcceptanceType().equalsIgnoreCase("review"))
                join.setText("Apply");
        }

        alreadyMember = HelperMethodsUI.isMemberOfCircle(popupCircle, user.getUserId());
        alreadyApplicant = HelperMethodsUI.ifUserApplied(popupCircle, user.getUserId());

        if (alreadyApplicant)
            join.setText("Already Applied");

        if (alreadyMember)
            join.setText("Already Member");
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
                globalVariables.saveCurrentCircle(circle);
                shownPopup = false;
                startActivity(new Intent(ExploreTabbedActivity.this, CircleWall.class));
            }
            circleJoinSuccessDialog.cancel();
        });

        Subscriber subscriber = new Subscriber(user, System.currentTimeMillis());

        HelperMethodsBL.sendUserApplicationToCreator(user, subscriber, circle);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void processUrl(String url) {
        String circleID = HelperMethodsBL.getCircleIdFromShareURL(url);
        ParticularCircleViewModel particularCircleViewModel = new ParticularCircleViewModel();

        MutableLiveData<Circle> liveData = particularCircleViewModel.getParticularCircle(this, circleID);

        liveData.observe(this, circleObject -> {
            Circle circle = circleObject;
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
        Circle circle = globalVariables.getCurrentCircle();
        switch (text) {
            case "shareLink":
                HelperMethodsUI.showShareCirclePopup(circle, this);
                break;
            case "copyLink":
                HelperMethodsUI.copyLinkToClipBoard(circle, this);
                break;
        }
    }
    @Override
    public void onBackPressed(){
        //close app
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}