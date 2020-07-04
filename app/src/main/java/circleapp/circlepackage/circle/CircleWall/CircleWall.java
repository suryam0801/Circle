package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
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
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.tooltip.Tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SendNotification;
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

    RuntimePermissionHelper runtimePermissionHelper;
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
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost, imagePost;
    String broadcastid;
    int broadcastPos;
    int photo;

    private TextView getStartedPoll, getStartedBroadcast, getStartedPhoto;

    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        runtimePermissionHelper = new RuntimePermissionHelper(CircleWall.this);

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
        recyclerView = findViewById(R.id.broadcastViewRecyclerView);

        initializeRecyclerView();

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
            finishAfterTransition();
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        });

        poll.setOnClickListener(view -> {
            showCreatePollBroadcastDialog();
            floatingActionMenu.close(true);
        });
        newPost.setOnClickListener(view -> {
            showCreateNormalBroadcastDialog();
            floatingActionMenu.close(true);
        });
        imagePost.setOnClickListener(view -> {
            showCreatePhotoBroadcastDialog();
            floatingActionMenu.close(true);
        });

        //set applicants button visible
        if (circle.getCreatorID().equals(user.getUserId()))
            viewApplicants.setVisibility(View.VISIBLE);

        viewApplicants.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(CircleWall.this, PersonelDisplay.class));
        });

        moreOptions.setOnClickListener(view -> {
            makeMenuPopup();
        });

        getStartedPhoto.setOnClickListener(view -> showCreatePhotoBroadcastDialog());
        getStartedPoll.setOnClickListener(view -> showCreatePollBroadcastDialog());
        getStartedBroadcast.setOnClickListener(view -> showCreateNormalBroadcastDialog());

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsBroadcastLiveData(circle.getId());

        liveData.observe(this, returnArray -> {
            Broadcast broadcast = new Gson().fromJson(returnArray[0], Broadcast.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    addBroadcast(broadcast);
                    break;
                case "changed":
                    changeBroadcast(broadcast);
                    break;
                case "removed":
                    removeBroadcast(broadcast);
                    break;
            }
        });
    }

    private void addBroadcast(Broadcast broadcast) {
        boolean exists = HelperMethods.listContainsBroadcast(broadcastList, broadcast);
        if (!exists) {
            broadcastList.add(0, broadcast); //to store timestamp values descendingly
            adapter.notifyItemInserted(0);
            recyclerView.setAdapter(adapter);
        }

        recyclerView.scrollToPosition(broadcastPos);
        initializeNewCommentsAlertTimestamp(broadcast);

        //coming back from image display
        int indexOfReturnFromFullImage = getIntent().getIntExtra("indexOfBroadcast", 0);
        if (indexOfReturnFromFullImage != 0)
            recyclerView.scrollToPosition(indexOfReturnFromFullImage);

        emptyDisplay.setVisibility(View.GONE);
    }

    private void changeBroadcast(Broadcast broadcast) {
        int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
        broadcastList.set(position, broadcast);
        adapter.notifyItemChanged(position);

    }

    private void removeBroadcast(Broadcast broadcast) {
        int position = HelperMethods.returnIndexOfBroadcast(broadcastList, broadcast);
        broadcastList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                    finishAfterTransition();
                    startActivity(new Intent(CircleWall.this, CircleWallBackgroundPicker.class));
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

    private void initializeRecyclerView() {

        //initialize recylcerview
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showExitDialog() {
        confirmationDialog.setContentView(R.layout.exit_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.remove_user_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.remove_user_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            finishAfterTransition();
            FirebaseWriteHelper.exitCircle(this, circle, user);
            confirmationDialog.dismiss();
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showDeleteDialog() {

        confirmationDialog.setContentView(R.layout.delete_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.delete_circle_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.delete_circle_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            finishAfterTransition();
            FirebaseWriteHelper.deleteCircle(this, circle, user);
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void showCreateNormalBroadcastDialog() {
        photo = 0;
        createNormalBroadcastPopup = new Dialog(CircleWall.this);
        createNormalBroadcastPopup.setContentView(R.layout.normal_broadcast_create_popup); //set dialog view
        createNormalBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createNormalBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadUri = null;

        broadcastHeader = createNormalBroadcastPopup.findViewById(R.id.broadcast_header);
        setTitleET = createNormalBroadcastPopup.findViewById(R.id.broadcastTitleEditText);
        setTitleET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setMessageET = createNormalBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        setMessageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        btnUploadNormalBroadcast = createNormalBroadcastPopup.findViewById(R.id.upload_normal_broadcast_btn);
        cancelNormalButton = createNormalBroadcastPopup.findViewById(R.id.create_normal_broadcast_cancel_btn);

        cancelNormalButton.setOnClickListener(view -> createNormalBroadcastPopup.dismiss());
        btnUploadNormalBroadcast.setOnClickListener(view -> {
            if (setTitleET.getText().toString().isEmpty() || setMessageET.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
            else
                createNormalBroadcast();
        });
        createNormalBroadcastPopup.show();
    }

    private void showCreatePhotoBroadcastDialog() {
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
            if (downloadUri != null && !setTitlePhoto.getText().toString().isEmpty()) {
                imageExists = true;
                createPhotoBroadcast();
            } else
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();

        });
        createPhotoBroadcastPopup.show();
    }

    private void showCreatePollBroadcastDialog() {
        photo = 0;
        createPollBroadcastPopup = new Dialog(CircleWall.this);
        createPollBroadcastPopup.setContentView(R.layout.poll_broadcast_create_popup); //set dialog view
        createPollBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPollBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadUri = null;

        setPollQuestionET = createPollBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollQuestionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setPollOptionET = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        setPollOptionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
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
            if (pollAnswerOptionsList.isEmpty() || setPollQuestionET.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();
            else {
                if (downloadUri != null)
                    imageExists = true;
                createPollBroadcast();
            }
        });
        createPollBroadcastPopup.show();
    }

    private void createNormalBroadcast() {
        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        Broadcast normalBroadcast;
        normalBroadcast = new Broadcast(broadcastId, setTitleET.getText().toString(), setMessageET.getText().toString(), null,
                currentUserName, null, currentUserId, false, false, System.currentTimeMillis(), null,
                user.getProfileImageLink(), 0, 0);
        SendNotification.sendBCinfo(user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), setTitleET.getText().toString());
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);
        updateUserCount(circle);

        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(CircleWall.this, circle.getId(), normalBroadcast, newCount);
        pollExists = false;
        imageExists = false;

        createNormalBroadcastPopup.dismiss();
    }

    private void createPhotoBroadcast() {
        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        Broadcast photoBroadcast = new Broadcast();

        if (imageExists) {
            photoBroadcast = new Broadcast(broadcastId, setTitlePhoto.getText().toString(), null, downloadUri.toString(), currentUserName, null, currentUserId, false, true,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), 0, 0);
        }

        SendNotification.sendBCinfo(user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), setTitlePhoto.getText().toString());
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);

        updateUserCount(circle);
        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(CircleWall.this, circle.getId(), photoBroadcast, newCount);
        pollExists = false;
        imageExists = false;
        createPhotoBroadcastPopup.dismiss();
    }

    private void createPollBroadcast() {
        String currentCircleId = circle.getId();
        String broadcastId = FirebaseWriteHelper.getBroadcastId(currentCircleId);
        String pollQuestion = setPollQuestionET.getText().toString();
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();

        SendNotification.sendBCinfo(user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), pollQuestion);
        //creating poll options hashmap
        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadUri.toString(), currentUserName, null, currentUserId, true, true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0);
            } else
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, null, currentUserId, true, false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0);
        }
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        SessionStorage.saveCircle(CircleWall.this, circle);

        updateUserCount(circle);

        //updating broadcast in broadcast db
        FirebaseWriteHelper.writeBroadcast(CircleWall.this, circle.getId(), pollBroadcast, newCount);
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
        }
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
            final StorageReference profileRef = FirebaseWriteHelper.getStorageReference("BroadcastImage/" + id);
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
                    if (pollExists) {
                        pollUploadButtonView.setVisibility(View.GONE);
                        pollAddPhoto.setVisibility(View.VISIBLE);

                    } else {
                        photoUploadButtonView.setVisibility(View.GONE);
                        addPhoto.setVisibility(View.VISIBLE);
                    }
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();
                    FirebaseWriteHelper.updateUserProfile(profileUpdates);
                    Log.d(TAG, "Profile URL: " + downloadUri.toString());
                    if (pollExists)
                        Glide.with(CircleWall.this).load(filePath).fitCenter().into(pollAddPhoto);
                    else
                        Glide.with(CircleWall.this).load(filePath).fitCenter().into(addPhoto);
                    filePath = null;
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        Intent intent = new Intent(CircleWall.this, ExploreTabbedActivity.class);
        startActivity(intent);
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
            FirebaseWriteHelper.updateUserNewTimeStampComments(user.getUserId(), b.getId(), b.getLatestCommentTimestamp());
        } else if (user.getNewTimeStampsComments() != null && !user.getNewTimeStampsComments().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            commentTimeStampTemp = new HashMap<>(user.getNewTimeStampsComments());
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser(CircleWall.this, user);
            FirebaseWriteHelper.updateUserNewTimeStampComments(user.getUserId(), b.getId(), b.getLatestCommentTimestamp());
        }
    }

    public void updateUserCount(Circle c) {
        if (user.getNotificationsAlert() != null) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
            SessionStorage.saveUser(this, user);
            FirebaseWriteHelper.updateUserCount(user.getUserId(), c.getId(), circle.getNoOfBroadcasts());
        } else {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
            SessionStorage.saveUser(this, user);
        }
    }
}
