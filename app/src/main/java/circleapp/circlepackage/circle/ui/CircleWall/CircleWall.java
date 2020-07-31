package circleapp.circlepackage.circle.ui.CircleWall;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.tooltip.Tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.ui.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.BroadcastsViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;

import static android.Manifest.permission.CAMERA;

public class CircleWall extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234;
    public Uri downloadLink;

    private LinearLayout emptyDisplay;

    private Circle circle;

    private List<String> pollAnswerOptionsList = new ArrayList<>();
    public boolean pollExists = false;
    public boolean imageExists = false;

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
    private RelativeLayout pollUploadButtonView, parentLayout;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost, imagePost;
    String broadcastid;
    int broadcastPos;
    public ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;
    private GlobalVariables globalVariables = new GlobalVariables();
    private CreateNormalBroadcastDialog normalBroadcastDialog;
    private CreatePhotoBroadcastDialog photoBroadcastDialog;
    private CreatePollBroadcastDialog pollBroadcastDialog;

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
        user = globalVariables.getCurrentUser();
        circle = globalVariables.getCurrentCircle();
        normalBroadcastDialog = new CreateNormalBroadcastDialog();
        photoBroadcastDialog = new CreatePhotoBroadcastDialog();
        pollBroadcastDialog = new CreatePollBroadcastDialog();
        MyCirclesViewModel tempViewModel = ViewModelProviders.of(CircleWall.this).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) CircleWall.this, dataSnapshot -> {
            circle = dataSnapshot.getValue(Circle.class);
            if (circle != null&&circle.getMembersList()!=null) {
                Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                if (circle.getMembersList().containsKey(user.getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                }
            }
        });

        broadcastid = getIntent().getStringExtra("broadcastId");
        broadcastPos = getIntent().getIntExtra("broadcastPos", 0);

        imageUploadProgressDialog = new ProgressDialog(this);
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath).observe(this, progress -> {
            Log.d("progressvalue",""+progress);
            // update UI
            if(progress==null);

            else if(progress[1].equals("-1")){
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Error uploading. Please try again", Toast.LENGTH_SHORT).show();
            }

            else if(!progress[1].equals("100.0")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100.0")){
                downloadLink = Uri.parse(progress[0]);
                globalVariables.setTempdownloadLink(downloadLink);
                Log.d("boolean", String.valueOf(pollExists));
                if (pollBroadcastDialog.pollExists) {
                    pollBroadcastDialog.pollUploadButtonView.setVisibility(View.GONE);
                    pollBroadcastDialog.pollAddPhoto.setVisibility(View.VISIBLE);

                } else {
                    photoBroadcastDialog.photoUploadButtonView.setVisibility(View.GONE);
                    photoBroadcastDialog.addPhoto.setVisibility(View.VISIBLE);
                }
                if (pollBroadcastDialog.pollExists)
                    Glide.with(CircleWall.this).load(filePath).fitCenter().into(pollBroadcastDialog.pollAddPhoto);
                else
                    Glide.with(CircleWall.this).load(filePath).fitCenter().into(photoBroadcastDialog.addPhoto);
                imageUploadProgressDialog.dismiss();
            }
        });


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
//            showCreatePollBroadcastDialog();
            pollBroadcastDialog.showCreatePollBroadcastDialog(CircleWall.this);
            floatingActionMenu.close(true);
        });
        newPost.setOnClickListener(view -> {
            normalBroadcastDialog.showCreateNormalBroadcastDialog(CircleWall.this);
            floatingActionMenu.close(true);
        });
        imagePost.setOnClickListener(view -> {
//            showCreatePhotoBroadcastDialog();
            photoBroadcastDialog.showCreatePhotoBroadcastDialog(CircleWall.this);
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

        getStartedPhoto.setOnClickListener(view -> photoBroadcastDialog.showCreatePhotoBroadcastDialog(CircleWall.this));
        getStartedPoll.setOnClickListener(view -> pollBroadcastDialog.showCreatePollBroadcastDialog(CircleWall.this));
        getStartedBroadcast.setOnClickListener(view -> normalBroadcastDialog.showCreateNormalBroadcastDialog(CircleWall.this));

        BroadcastsViewModel viewModel = ViewModelProviders.of(this).get(BroadcastsViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsBroadcastLiveData(circle.getId());

        liveData.observe(this, returnArray -> {
            Broadcast broadcast = new Gson().fromJson(returnArray[0], Broadcast.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    if(broadcast.isAdminVisibility())
                        addBroadcast(broadcast);
                    break;
                case "changed":
                    if(broadcast.isAdminVisibility()==true)
                        changeBroadcast(broadcast);
                    break;
                case "removed":
                    removeBroadcast(broadcast);
                    break;
            }
        });
    }

    private void addBroadcast(Broadcast broadcast) {
        boolean exists = HelperMethodsUI.listContainsBroadcast(broadcastList, broadcast);
        if (!exists) {
            broadcastList.add(0, broadcast); //to store timestamp values descendingly
            adapter.notifyItemInserted(0);
            recyclerView.setAdapter(adapter);
        }

        recyclerView.scrollToPosition(broadcastPos);
        HelperMethodsBL.initializeNewCommentsAlertTimestamp(broadcast, user);

        //coming back from image display
        int indexOfReturnFromFullImage = getIntent().getIntExtra("indexOfBroadcast", 0);
        if (indexOfReturnFromFullImage != 0)
            recyclerView.scrollToPosition(indexOfReturnFromFullImage);

        emptyDisplay.setVisibility(View.GONE);
    }

    private void changeBroadcast(Broadcast broadcast) {
        int position = HelperMethodsUI.returnIndexOfBroadcast(broadcastList, broadcast);
        broadcastList.set(position, broadcast);
        adapter.notifyItemChanged(position);

    }

    private void removeBroadcast(Broadcast broadcast) {
        int position = HelperMethodsUI.returnIndexOfBroadcast(broadcastList, broadcast);
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
                    HelperMethodsUI.showReportAbusePopup(reportAbuseDialog, CircleWall.this, circle.getId(), "", "", circle.getCreatorID(), user.getUserId());
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
            HelperMethodsBL.exitCircle(circle, user);
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
            HelperMethodsBL.deleteCircle(circle, user);
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }
    private void showCreatePhotoBroadcastDialog() {
        createPhotoBroadcastPopup = new Dialog(CircleWall.this);
        createPhotoBroadcastPopup.setContentView(R.layout.photo_broadcast_create_popup); //set dialog view
        createPhotoBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPhotoBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadLink = null;

        setTitlePhoto = createPhotoBroadcastPopup.findViewById(R.id.photoTitleEditText);
        setTitlePhoto.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        addPhoto = createPhotoBroadcastPopup.findViewById(R.id.photo_display_photo_add_broadcast);
//        photoUploadButtonView = createPhotoBroadcastPopup.findViewById(R.id.photo_add_photo_view);
        addPhotoText = createPhotoBroadcastPopup.findViewById(R.id.photo_upload_photo);

        btnUploadPhotoBroadcast = createPhotoBroadcastPopup.findViewById(R.id.upload_photo_broadcast_btn);
        cancelPhotoButton = createPhotoBroadcastPopup.findViewById(R.id.create_photo_broadcast_cancel_btn);

        cancelPhotoButton.setOnClickListener(view -> createPhotoBroadcastPopup.dismiss());

        photoBroadcastDialog.photoUploadButtonView.setOnClickListener(v -> {
            Permissions.check(this/*context*/, CAMERA, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    pickImageIntent();
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });
        btnUploadPhotoBroadcast.setOnClickListener(view -> {
            if (downloadLink != null && !setTitlePhoto.getText().toString().isEmpty()) {
                imageExists = true;
                createPhotoBroadcast();
            } else
                Toast.makeText(getApplicationContext(), "Fill out all fields", Toast.LENGTH_SHORT).show();

        });
        createPhotoBroadcastPopup.show();
    }

    private void showCreatePollBroadcastDialog() {
        createPollBroadcastPopup = new Dialog(CircleWall.this);
        createPollBroadcastPopup.setContentView(R.layout.poll_broadcast_create_popup); //set dialog view
        createPollBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPollBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        downloadLink = null;

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
            Permissions.check(this/*context*/, CAMERA, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    pickImageIntent();
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
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
                if (downloadLink != null)
                    imageExists = true;
                createPollBroadcast();
            }
        });
        createPollBroadcastPopup.show();
    }

    private void pickImageIntent(){
        ImagePicker imagePicker = new ImagePicker(getApplication());
        Intent chooseImageIntent = imagePicker.getPickImageIntent();
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }
    private void createPhotoBroadcast() {
        BroadcastsRepository broadcastsRepository = new BroadcastsRepository();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();
        Broadcast photoBroadcast = new Broadcast();
        if (imageExists) {
            photoBroadcast = new Broadcast(broadcastId, setTitlePhoto.getText().toString(), null, downloadLink.toString(), currentUserName, circle.getMembersList(), currentUserId, false, true,
                    System.currentTimeMillis(), null, user.getProfileImageLink(), 0, 0,true);
        }


        SendNotification.sendBCinfo(this, photoBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), setTitlePhoto.getText().toString());
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        globalVariables.saveCurrentCircle(circle);

        updateUserCount(circle);
        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle.getId(), photoBroadcast, newCount);
        pollExists = false;
        imageExists = false;
        createPhotoBroadcastPopup.dismiss();
    }

    private void createPollBroadcast() {
        BroadcastsRepository broadcastsRepository = new BroadcastsRepository();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        String pollQuestion = setPollQuestionET.getText().toString();
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();

        //creating poll options hashmap
        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadLink.toString(), currentUserName, circle.getMembersList(), currentUserId, true, true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
            } else
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, circle.getMembersList(), currentUserId, true, false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
        }
        //updating number of broadcasts in circle
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        globalVariables.saveCurrentCircle(circle);
        SendNotification.sendBCinfo(this, pollBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), pollQuestion);
        updateUserCount(circle);

        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle.getId(), pollBroadcast, newCount);
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

    private void uploadPicture(){
        imageUploadModel.imageUpload(filePath);
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath = imagePicker.getImageUri(bitmap);
                if(filePath !=null){
                    uploadPicture();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
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
                HelperMethodsUI.showShareCirclePopup(circle, CircleWall.this);
                break;
            case "copyLink":
                HelperMethodsUI.copyLinkToClipBoard(circle, CircleWall.this);
                break;
        }
    }

    public void updateUserCount(Circle c) {
        User temp = globalVariables.getCurrentUser();
        Circle temp_Circle = globalVariables.getCurrentCircle();
        if (temp.getNotificationsAlert() != null) {
            HashMap<String, Integer> newNotifs = new HashMap<>(temp.getNotificationsAlert());
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            temp.setNotificationsAlert(newNotifs);
            UserRepository userRepository = new UserRepository();
            userRepository.updateUserCount(temp.getUserId(), c.getId(), temp_Circle.getNoOfBroadcasts());
        } else {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), c.getNoOfBroadcasts());
            temp.setNotificationsAlert(newNotifs);
        }
        globalVariables.saveCurrentUser(temp);
    }
}