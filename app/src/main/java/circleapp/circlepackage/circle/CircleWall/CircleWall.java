package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tooltip.Tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class CircleWall extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB, commentsDB, circlesPersonelDB, circlesDB, usersDB;
    private FirebaseAuth currentUser;
    RuntimePermissionHelper runtimePermissionHelper;
    private StorageReference storageReference;
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private Uri downloadUri;


    private String TAG = CircleWall.class.getSimpleName();

    private LinearLayout emptyDisplay;

    private Circle circle;

    private List<String> pollAnswerOptionsList = new ArrayList<>();
    private boolean pollExists = false, imageExists = false;

    private ImageButton back, moreOptions;
    private User user;

    //create broadcast popup ui elements
    private EditText setTitleET, setMessageET, setPollQuestionET, setPollOptionET, setTitlePhoto;
    private LinearLayout pollOptionsDisplay, pollImageUploadInitiation;
    private TextView circleBannerName, broadcastHeader, addPhotoText, pollAddPhotoText;
    private Button btnAddPollOption, btnUploadNormalBroadcast, cancelNormalButton, btnUploadPollBroadcast, cancelPollButton, btnUploadPhotoBroadcast, cancelPhotoButton;
    private Dialog createNormalBroadcastPopup, createPhotoBroadcastPopup, createPollBroadcastPopup, confirmationDialog, reportAbuseDialog;
    private ImageView addPhoto, pollAddPhoto;
    private ImageButton viewApplicants;
    private RelativeLayout photoUploadButtonView, pollUploadButtonView, parentLayout;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost, imagePost;
    String broadcastid;
    int broadcastPos;
    int photo;

    private TextView getStartedPoll, getStartedBroadcast, getStartedPhoto;

    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();
    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);
        confirmationDialog = new Dialog(CircleWall.this);
        reportAbuseDialog = new Dialog(CircleWall.this);
        user = SessionStorage.getUser(CircleWall.this);
        circle = SessionStorage.getCircle(CircleWall.this);

        broadcastid = getIntent().getStringExtra("broadcastId");
        broadcastPos = getIntent().getIntExtra("broadcastPos", 0);

        database = FirebaseDatabase.getInstance();
        runtimePermissionHelper = new RuntimePermissionHelper(CircleWall.this);
        broadcastsDB = database.getReference("Broadcasts");
        circlesPersonelDB = database.getReference("CirclePersonel");
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference("Users").child(user.getUserId());
        commentsDB = database.getReference("BroadcastComments");
        broadcastsDB.keepSynced(true);
        currentUser = FirebaseAuth.getInstance();
        analyticsLogEvents = new AnalyticsLogEvents();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (getIntent().getBooleanExtra("fromCreateCircle", false) == true) {
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
        }

        circleBannerName = findViewById(R.id.circleBannerName);
        back = findViewById(R.id.bck_Circlewall);
        emptyDisplay = findViewById(R.id.circle_wall_empty_display);
        poll = findViewById(R.id.poll_creation_FAB);
        newPost = findViewById(R.id.message_creation_FAB);
        imagePost = findViewById(R.id.image_creation_FAB);
        getStartedPoll = findViewById(R.id.circle_wall_get_started_poll);
        getStartedBroadcast = findViewById(R.id.circle_wall_get_started_broadcast);
        getStartedPhoto = findViewById(R.id.circle_wall_get_started_image);
        floatingActionMenu = findViewById(R.id.menu);
        moreOptions = findViewById(R.id.circle_wall_more_options);
        parentLayout = findViewById(R.id.circle_wall_parent_layout);
        viewApplicants = findViewById(R.id.applicants_display_creator);

        photo = 0;
        setParentBgImage();
        circleBannerName.setText(circle.getName());

        if (circle.getApplicantsList() != null) {
            new Tooltip.Builder(viewApplicants)
                    .setText("You have pending applicants")
                    .setTextColor(Color.BLACK)
                    .setBackgroundColor(Color.WHITE)
                    .setGravity(Gravity.BOTTOM)
                    .setCornerRadius(20f)
                    .setDismissOnClick(true)
                    .show();
        }


        if (circle.getNoOfBroadcasts() == 0)
            emptyDisplay.setVisibility(View.VISIBLE);

        back.setOnClickListener(view -> {
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            finish();
        });

        poll.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "new_poll", "pressed_button", "circle_wall");
            showCreatePollBroadcastDialog();
            floatingActionMenu.close(true);
        });
        newPost.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "add_message", "pressed_button", "circle_wall");
            showCreateNormalBroadcastDialog();
            floatingActionMenu.close(true);
        });
        imagePost.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "new_photo", "pressed_button", "circle_wall");
            showCreatePhotoBroadcastDialog();
            floatingActionMenu.close(true);
        });

        //set applicants button visible
        if (circle.getCreatorID().equals(user.getUserId()))
            viewApplicants.setVisibility(View.VISIBLE);

        viewApplicants.setOnClickListener(view -> {
            startActivity(new Intent(CircleWall.this, PersonelDisplay.class));
            finish();
        });

        moreOptions.setOnClickListener(view -> {

            makeMenuPopup();

        });

        getStartedPhoto.setOnClickListener(view -> showCreatePhotoBroadcastDialog());
        getStartedPoll.setOnClickListener(view -> showCreatePollBroadcastDialog());
        getStartedBroadcast.setOnClickListener(view -> showCreateNormalBroadcastDialog());

        loadCircleBroadcasts();
    }

    public void makeMenuPopup() {
        PopupMenu popup = new PopupMenu(this, moreOptions);
        popup.getMenuInflater()
                .inflate(R.menu.circle_wall_menu, popup.getMenu());
        if (circle.getCreatorID().equals(user.getUserId()))
            popup.getMenu().findItem(R.id.deleteCircleMenuBar).setVisible(true);
        else
            popup.getMenu().findItem(R.id.exitCircleMenuBar).setVisible(true);

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Change wallpaper":
                    startActivity(new Intent(CircleWall.this, CircleWallBackgroundPicker.class));
                    finish();
                    break;
                case "Invite a friend":
                    InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
                    break;
                case "Report Abuse":
                    HelperMethods.showReportAbusePopup(reportAbuseDialog, CircleWall.this, circle.getId(), "", "", circle.getCreatorID(), user.getUserId());
                    break;
                case "Exit circle":
                    showExitDialog();
                    break;
                case "Delete circle":
                    showDeleteDialog();
                    break;
                case "Circle Information":
                    startActivity(new Intent(CircleWall.this, CircleInformation.class));
                    break;
            }
            return true;
        });
        popup.show();
    }

    public void setParentBgImage() {
        String bg = SessionStorage.getCircleWallBgImage(CircleWall.this);
        if (bg != null) {
            switch (bg) {
                case "bg1":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_1));
                    break;
                case "bg2":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_2));
                    break;
                case "bg3":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_3));
                    break;
                case "bg4":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_4));
                    break;
                case "bg5":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_5));
                    break;
                case "bg6":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_6));
                    break;
                case "bg7":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_7));
                    break;
                case "bg8":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_8));
                    break;
                case "bg9":
                    circleBannerName.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_9));
                    break;
                case "bg10":
                    circleBannerName.setTextColor(Color.BLACK);
                    parentLayout.setBackgroundColor(Color.WHITE);
                    break;
            }
        }
    }

    private void loadCircleBroadcasts() {

        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.broadcastViewRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        broadcastsDB.child(circle.getId()).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                broadcastList.add(0, broadcast); //to store timestamp values descendingly
                adapter.notifyItemInserted(0);
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(broadcastPos);
                initializeNewCommentsAlertTimestamp(broadcast);
                initializeNewReadComments(broadcast);

                //coming back from image display
                int indexOfReturnFromFullImage = getIntent().getIntExtra("indexOfBroadcast", 0);
                if (indexOfReturnFromFullImage != 0)
                    recyclerView.scrollToPosition(indexOfReturnFromFullImage);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
                Log.d("wefkj", position + "");
                broadcastList.set(position, broadcast);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
                broadcastList.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void showExitDialog() {
        confirmationDialog.setContentView(R.layout.exit_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.remove_user_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.remove_user_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            exitCircle();
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    public void showDeleteDialog() {

        confirmationDialog.setContentView(R.layout.delete_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.delete_circle_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.delete_circle_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            deleteCircle();
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void deleteCircle() {
        circlesPersonelDB.child(circle.getId()).removeValue();
        circlesDB.child(circle.getId()).removeValue();
        //reducing created circle count
        int currentCreatedCount = user.getCreatedCircles() - 1;
        user.setCreatedCircles(currentCreatedCount);
        usersDB.child("createdCircles").setValue(currentCreatedCount);
        broadcastsDB.child(circle.getId()).removeValue();
        commentsDB.child(circle.getId()).removeValue();
        SessionStorage.saveUser(CircleWall.this, user);
        analyticsLogEvents.logEvents(CircleWall.this, "circle_delete", "delete_button", "circle_wall");
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        finish();
    }

    private void exitCircle() {
        circlesPersonelDB.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
        //reducing active circle count
        int currentActiveCount = user.getActiveCircles() - 1;
        user.setActiveCircles(currentActiveCount);
        usersDB.child("activeCircles").setValue(currentActiveCount);
        SessionStorage.saveUser(CircleWall.this, user);
        analyticsLogEvents.logEvents(CircleWall.this, "circle_exit", "exit_button", "circle_wall");
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        finish();
    }
    private void showCreateNormalBroadcastDialog(){
        photo = 0;
        createNormalBroadcastPopup = new Dialog(CircleWall.this);
        createNormalBroadcastPopup.setContentView(R.layout.normal_broadcast_create_popup); //set dialog view
        createNormalBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createNormalBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadUri = null;

        broadcastHeader = createNormalBroadcastPopup.findViewById(R.id.broadcast_header);
        setTitleET = createNormalBroadcastPopup.findViewById(R.id.broadcastTitleEditText);
        setTitleET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setMessageET = createNormalBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        setMessageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        btnUploadNormalBroadcast = createNormalBroadcastPopup.findViewById(R.id.upload_normal_broadcast_btn);
        cancelNormalButton = createNormalBroadcastPopup.findViewById(R.id.create_normal_broadcast_cancel_btn);

        cancelNormalButton.setOnClickListener(view -> createNormalBroadcastPopup.dismiss());
        btnUploadNormalBroadcast.setOnClickListener(view -> {
            if (setTitleET.getText().toString().isEmpty()||setMessageET.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
            else
                createNormalBroadcast();
        });
        createNormalBroadcastPopup.show();
    }
    private void showCreatePhotoBroadcastDialog(){
        photo = 0;
        createPhotoBroadcastPopup = new Dialog(CircleWall.this);
        createPhotoBroadcastPopup.setContentView(R.layout.photo_broadcast_create_popup); //set dialog view
        createPhotoBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPhotoBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadUri = null;

        setTitlePhoto = createPhotoBroadcastPopup.findViewById(R.id.photoTitleEditText);
        setTitlePhoto.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        addPhoto = createPhotoBroadcastPopup.findViewById(R.id.photo_display_photo_add_broadcast);
        photoUploadButtonView = createPhotoBroadcastPopup.findViewById(R.id.photo_add_photo_view);
        addPhotoText = createPhotoBroadcastPopup.findViewById(R.id.photo_upload_photo);

        btnUploadPhotoBroadcast = createPhotoBroadcastPopup.findViewById(R.id.upload_photo_broadcast_btn);
        cancelPhotoButton = createPhotoBroadcastPopup.findViewById(R.id.create_photo_broadcast_cancel_btn);

        cancelPhotoButton.setOnClickListener(view -> createPhotoBroadcastPopup.dismiss());

        photoUploadButtonView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CircleWall.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CircleWall.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
            if (photo == 0)
                selectImage();
        });
        btnUploadPhotoBroadcast.setOnClickListener(view -> {
            if (downloadUri != null&&!setTitlePhoto.getText().toString().isEmpty()) {
                imageExists = true;
                createPhotoBroadcast();
            }
            else
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();

        });
        createPhotoBroadcastPopup.show();
    }
    private void showCreatePollBroadcastDialog(){
        photo = 0;
        createPollBroadcastPopup = new Dialog(CircleWall.this);
        createPollBroadcastPopup.setContentView(R.layout.poll_broadcast_create_popup); //set dialog view
        createPollBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPollBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadUri = null;

        setPollQuestionET = createPollBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollQuestionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        setPollOptionET = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        setPollOptionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        pollOptionsDisplay = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_display);
        btnAddPollOption = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_add_btn);
        pollAddPhoto = createPollBroadcastPopup.findViewById(R.id.poll_display_photo_add_broadcast);
        pollUploadButtonView = createPollBroadcastPopup.findViewById(R.id.poll_add_photo_view);
        pollAddPhotoText = createPollBroadcastPopup.findViewById(R.id.poll_upload_photo);
        pollImageUploadInitiation = createPollBroadcastPopup.findViewById(R.id.poll_image_upload_initiate_layout);
        pollExists = true;


        btnUploadPollBroadcast = createPollBroadcastPopup.findViewById(R.id.upload_poll_broadcast_btn);
        cancelPollButton = createPollBroadcastPopup.findViewById(R.id.create_poll_broadcast_cancel_btn);

        cancelPollButton.setOnClickListener(view -> createPollBroadcastPopup.dismiss());

        pollImageUploadInitiation.setOnClickListener(view -> {
            pollImageUploadInitiation.setVisibility(View.GONE);
            pollUploadButtonView.setVisibility(View.VISIBLE);
        });

        pollUploadButtonView.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(CircleWall.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CircleWall.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
            if (photo == 0)
                selectImage();
        });

        btnAddPollOption.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(CircleWall.this, "add_poll", "pressed_button", "circle_wall");

            String option = setPollOptionET.getText().toString();

            if ((option.contains(".") || option.contains("$") || option.contains("#") || option.contains("[") || option.contains("]") || option.isEmpty())) {
                //checking for invalid characters
                Toast.makeText(getApplicationContext(), "Option cannot use special characters or be empty", Toast.LENGTH_SHORT).show();
            } else {
                if (!option.isEmpty() && !setPollQuestionET.getText().toString().isEmpty()) {

                    final TextView tv = generatePollOptionTV(option);

                    tv.setOnClickListener(view1 -> {
                        pollOptionsDisplay.removeView(tv);
                        pollAnswerOptionsList.remove(tv.getText());
                    });

                    pollAnswerOptionsList.add(option);
                    pollOptionsDisplay.addView(tv);
                    setPollOptionET.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUploadPollBroadcast.setOnClickListener(view -> {
            if(pollAnswerOptionsList.isEmpty()||setPollQuestionET.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
            else {
                if (downloadUri != null)
                    imageExists = true;
                createPollBroadcast();
            }
        });
        createPollBroadcastPopup.show();
    }

    private void createNormalBroadcast(){
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();
        Broadcast normalBroadcast;

        SendNotification.sendBCinfo(broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink());
        normalBroadcast = new Broadcast(broadcastId, setTitleET.getText().toString(), setMessageET.getText().toString(), null,
                currentUserName, currentUserId, false, false, System.currentTimeMillis(), null,
                user.getProfileImageLink(), 0, 0);
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);

        updateUserCount(circle);

        //updating broadcast in broadcast db
        broadcastsDB.child(circle.getId()).child(broadcastId).setValue(normalBroadcast);
        pollExists = false;
        imageExists = false;

        createNormalBroadcastPopup.dismiss();
    }
    private void createPhotoBroadcast(){
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();
        Broadcast photoBroadcast = new Broadcast();

        SendNotification.sendBCinfo(broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink());
        if (imageExists) {
            photoBroadcast = new Broadcast(broadcastId, setTitlePhoto.getText().toString(), null, downloadUri.toString(), currentUserName, currentUserId, false, true,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), 0, 0);
        }
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);

        updateUserCount(circle);
        //updating broadcast in broadcast db
        broadcastsDB.child(circle.getId()).child(broadcastId).setValue(photoBroadcast);
        pollExists = false;
        imageExists = false;
        createPhotoBroadcastPopup.dismiss();
    }
    private void createPollBroadcast() {
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String pollQuestion = setPollQuestionET.getText().toString();
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();

        SendNotification.sendBCinfo(broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink());
        //creating poll options hashmap
        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadUri.toString(), currentUserName, currentUserId, true, true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0);
            } else
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, currentUserId, true, false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0);
        }
            //updating number of broadcasts in circle
            int newCount = circle.getNoOfBroadcasts() + 1;
            circle.setNoOfBroadcasts(newCount);
            circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
            SessionStorage.saveCircle(CircleWall.this, circle);

            updateUserCount(circle);

            //updating broadcast in broadcast db
            broadcastsDB.child(circle.getId()).child(broadcastId).setValue(pollBroadcast);
            pollExists = false;
            imageExists = false;
            pollAnswerOptionsList.clear();
            createPollBroadcastPopup.dismiss();
        }


    public TextView generatePollOptionTV(String option) {
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
        lparams.setMargins(0, 10, 20, 0);

        final TextView tv = new TextView(CircleWall.this);
        tv.setLayoutParams(lparams);
        tv.setText(option);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setBackground(getResources().getDrawable(R.drawable.poll_creation_item_option_background));
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_white_24dp, 0);
        tv.setPaddingRelative(40, 10, 40, 10);

        return tv;
    }

    public void selectFile() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    public void takePhoto() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        downloadUri = HelperMethods.getImageUri();
        m_intent.putExtra(MediaStore.EXTRA_OUTPUT, downloadUri);
        startActivityForResult(m_intent, REQUEST_IMAGE_CAPTURE);
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CircleWall.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    photo = 1;
                    if (!runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {
                        runtimePermissionHelper.askPermission(READ_EXTERNAL_STORAGE);
                    }
                    if (runtimePermissionHelper.isPermissionAvailable(CAMERA) && runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {
                        takePhoto();
                    } else {
                        runtimePermissionHelper.askPermission(CAMERA);
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    if (runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {
                        selectFile();
                    } else {
                        analyticsLogEvents.logEvents(CircleWall.this, "storage_off", "asked_permission", "gather_user_details");
                        runtimePermissionHelper.requestPermissionsIfDenied(READ_EXTERNAL_STORAGE);
                    }

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        if (runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {
            builder.show();
        }
    }

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (photo == 1)
                takePhoto();
            else
                selectImage();
            analyticsLogEvents.logEvents(CircleWall.this, "storage_granted", "permission_granted", "circle_wall");
        } else {
            photo = 0;
            Toast.makeText(CircleWall.this,
                    "Permission Denied",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photo = 0;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            //check the path for the image
            //if the image path is notnull the uploading process will start
            //ContentResolver resolver = getContentResolver();
            //HelperMethods.compressImage(resolver, filePath);

            if (filePath != null) {

                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(CircleWall.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("ProfilePics/" + id);

                //storing  the pic
                profileRef.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                })
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return profileRef.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        //and displaying a success toast
//                        Toast.makeText(getApplicationContext(), "Profile Pic Uploaded " + uri.toString(), Toast.LENGTH_LONG).show();
                        downloadUri = uri;
                        if(pollExists) {
                            pollUploadButtonView.setVisibility(View.GONE);
                            pollAddPhoto.setVisibility(View.VISIBLE);

                        }
                        else {
                            photoUploadButtonView.setVisibility(View.GONE);
                            addPhoto.setVisibility(View.VISIBLE);
                        }
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        if(pollExists)
                            Glide.with(CircleWall.this).load(filePath).into(pollAddPhoto);
                        else
                            Glide.with(CircleWall.this).load(filePath).into(addPhoto);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                analyticsLogEvents.logEvents(CircleWall.this, "pic_upload_fail", "device_error", "circle_wall");

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
            //check the path for the image
            //if the image path is notnull the uploading process will start
            //ContentResolver resolver = getContentResolver();
            //HelperMethods.compressImage(resolver, filePath);
            if (filePath != null) {


                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(CircleWall.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("ProfilePics/" + id);

                //storing  the pic
                profileRef.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                })
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return profileRef.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        //and displaying a success toast
//                        Toast.makeText(getApplicationContext(), "Profile Pic Uploaded " + uri.toString(), Toast.LENGTH_LONG).show();
                        downloadUri = uri;

                        if(pollExists) {
                            pollUploadButtonView.setVisibility(View.GONE);
                            pollAddPhoto.setVisibility(View.VISIBLE);

                        }
                        else {
                            photoUploadButtonView.setVisibility(View.GONE);
                            addPhoto.setVisibility(View.VISIBLE);
                        }

                        Log.d("test1", "" + downloadUri);
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        if(pollExists)
                            Glide.with(CircleWall.this).load(filePath).into(pollAddPhoto);
                        else
                            Glide.with(CircleWall.this).load(filePath).into(addPhoto);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                analyticsLogEvents.logEvents(CircleWall.this, "pic_capture_fail", "device_error", "circle_wall");

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CircleWall.this, ExploreTabbedActivity.class);
        startActivity(intent);
        finish();
    }

    //bottom sheet dialog onclick (only called when nagivating from create circle)
    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "shareLink":
                HelperMethods.showShareCirclePopup(circle, CircleWall.this);
                break;
            case "copyLink":
                HelperMethods.copyLinkToClipBoard(circle, CircleWall.this);
                break;
        }
    }

    public void initializeNewCommentsAlertTimestamp(Broadcast b) {
        HashMap<String, Long> commentTimeStampTemp;
        if (user.getNewTimeStampsComments() == null) {
            //first time viewing any comments
            commentTimeStampTemp = new HashMap<>();
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("newTimeStampsComments").child(b.getId()).setValue(b.getLatestCommentTimestamp());
        } else if (user.getNewTimeStampsComments() != null && !user.getNewTimeStampsComments().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            commentTimeStampTemp = new HashMap<>(user.getNewTimeStampsComments());
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("newTimeStampsComments").child(b.getId()).setValue(b.getLatestCommentTimestamp());
        }
    }

    public void initializeNewReadComments(Broadcast b){
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() == null) {
            //first time viewing any comments
            userNoReadComments = new HashMap<>();
            userNoReadComments.put(b.getId(), b.getNumberOfComments());
            user.setNoOfReadDiscussions(userNoReadComments);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("noOfReadDiscussions").child(b.getId()).setValue(b.getNumberOfComments());
        } else if (user.getNoOfReadDiscussions() != null && !user.getNoOfReadDiscussions().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.put(b.getId(), 0);
            user.setNoOfReadDiscussions(userNoReadComments);

            SessionStorage.saveUser(CircleWall.this, user);
            usersDB.child("noOfReadDiscussions").child(b.getId()).setValue(b.getNumberOfComments());
        }
    }

    public void updateUserCount(Circle c) {
        if (user.getNotificationsAlert() != null) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
            SessionStorage.saveUser(this, user);
            usersDB.child("notificationsAlert").child(c.getId()).setValue(circle.getNoOfBroadcasts());
        } else {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
            SessionStorage.saveUser(this, user);
            usersDB.child("notificationsAlert").child(c.getId()).setValue(c.getNoOfBroadcasts());
        }
    }
}
