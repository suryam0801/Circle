package circleapp.circlepackage.circle.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circlepackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.ui.CreateCircle.CreateCircleCategoryPicker;
import circleapp.circlepackage.circle.ui.EditProfile.EditProfile;
import circleapp.circlepackage.circle.ui.Explore.ExploreFragment;
import circleapp.circlepackage.circle.ui.Feedback.FeedbackFragment;
import circleapp.circlepackage.circle.ui.MyCircles.WorkbenchFragment;
import circleapp.circlepackage.circle.ui.Notifications.NotificationFragment;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;


public class ExploreTabbedActivity extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private CircleImageView profPicHolder;
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
    private Button join, cancel;
    private CircleImageView profPic;
    private ImageButton scanQrCodeBtn;
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
            url = intentUri;
            processUrl(url);
        }

        scanQrCodeBtn = findViewById(R.id.scan_qr_code_image_btn);
        profPicHolder = findViewById(R.id.explore_profilePicture);
        HelperMethodsUI.increaseTouchArea(profPicHolder);
        locationDisplay = findViewById(R.id.explore_district_name_display);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnAddCircle = findViewById(R.id.add_circle_button);

        locationDisplay.setText(user.getDistrict());
        HelperMethodsUI.setUserProfileImage(user.getProfileImageLink(),this, profPicHolder);
        scanQrCodeBtn.setOnClickListener(v->{
            Permissions.check(this, new String[]{CAMERA},null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    IntentIntegrator intentIntegrator = new IntentIntegrator(ExploreTabbedActivity.this);
                    intentIntegrator.setOrientationLocked(false);
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    intentIntegrator.initiateScan();
                }
            });
        });
    }
    private void initObserverForUser(){
        UserViewModel tempViewModel = ViewModelProviders.of(ExploreTabbedActivity.this).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(user.getUserId());
        tempLiveData.observe((LifecycleOwner) ExploreTabbedActivity.this, dataSnapshot -> {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                globalVariables.saveCurrentUser(user);
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

        cancel.setOnClickListener(view->{
            getIntent().setData(null);
            linkCircleDialog.dismiss();
        });

        //clearing intent data so popup doesnt show each time
        linkCircleDialog.setOnDismissListener(dialogInterface -> {
            getIntent().setData(null);
        });
    }
    private void initPopupElements(Circle popupCircle){
        linkCircleDialog = new Dialog(ExploreTabbedActivity.this);
        linkCircleDialog.setContentView(R.layout.join_circle_popup); //set dialog view

        tv_circleName = linkCircleDialog.findViewById(R.id.popup_circle_name);
        tv_creatorName = linkCircleDialog.findViewById(R.id.popup_circle_creatorName);
        tv_circleDesc = linkCircleDialog.findViewById(R.id.popup_circle_desc);
        join = linkCircleDialog.findViewById(R.id.join_card_join);
        cancel = linkCircleDialog.findViewById(R.id.join_card_cancel);
        profPic = linkCircleDialog.findViewById(R.id.popup_circle_logo);
        HelperMethodsUI.createDefaultCircleIcon(popupCircle,this,profPic);

        tv_circleName.setText(popupCircle.getName());
        tv_creatorName.setText("By "+popupCircle.getCreatorName());
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
            description.setText("Congratulations! You are now a member of " + circle.getName() + ". Click 'Done' to go to this circle.");
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

        MyCirclesViewModel viewModel = ViewModelProviders.of(this).get(MyCirclesViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsCircleSingleValueLiveData(circleID);

        liveData.observe(this, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (!popupCalled) {
                if(circle!=null){
                    showLinkPopup(circle);
                    popupCalled = true;
                }
                else
                    Toast.makeText(ExploreTabbedActivity.this, "That Circle was deleted",Toast.LENGTH_SHORT).show();
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}