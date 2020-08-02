package circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Utils.PollExportUtil;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastCreation.CreateNormalBroadcastDialog;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastCreation.CreatePhotoBroadcastDialog;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastCreation.CreatePollBroadcastDialog;
import circleapp.circlepackage.circle.ui.CircleWall.CircleInformation;
import circleapp.circlepackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.ui.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.BroadcastsViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CircleWall extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234;
    private Uri downloadLink;
    private LinearLayout emptyDisplay;
    private Circle circle;
    private List<String> allCircleMembers;
    private HashMap<String, Subscriber> listOfMembers;
    private boolean pollExists = false;
    private ImageButton back, moreOptions;
    private User user;

    //create broadcast popup ui elements
    private TextView circleBannerName;
    private Dialog confirmationDialog, reportAbuseDialog;
    private ImageButton viewApplicants;
    private RelativeLayout parentLayout;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton poll, newPost, imagePost;
    private String broadcastid;
    int broadcastPos;
    private ImageUpload imageUploadModel;
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
        ImageUploadModel();


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
        allCircleMembers = new ArrayList<>();

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

        setCircleMembersObserver();
    }

    private void ImageUploadModel() {
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
                case "Export Poll Data":
                    Permissions.check(this/*context*/, WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            exportPollsToFile();
                        }
                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            // permission denied, block the feature.
                        }
                    });
                    break;
                case "Exit circle":
                    HelperMethodsUI.showExitDialog(CircleWall.this,circle,user);
                    break;
                case "Delete circle":
                    HelperMethodsUI.showDeleteDialog(CircleWall.this,circle,user);
                    break;
                case "Circle Information":
                    startActivity(new Intent(CircleWall.this, CircleInformation.class));
                    break;
            }
            return true;
        });
        popup.show();
    }

    private void exportPollsToFile(){
        List<Broadcast> pollBroadcasts = new ArrayList<>();
        for(Broadcast broadcast: broadcastList){
            if(broadcast.isPollExists()){
                pollBroadcasts.add(broadcast);
            }
        }
        if(pollBroadcasts!=null){
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "/" + "All Poll Results "+circle.getName()+".xls");
            PollExportUtil pollExportUtil = new PollExportUtil();
            Log.d("BroadcastQuestion", allCircleMembers.size()+"");
            pollExportUtil.writeAllPollsToExcelFile(file, pollBroadcasts, allCircleMembers, listOfMembers);
            shareFile(file);
        }
        else {
            Toast.makeText(this, "No Polls exist in this Circle", Toast.LENGTH_SHORT).show();
        }
    }
    private void shareFile(File myFilePath){
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        if(myFilePath.exists()) {
            intentShareFile.setType("application/xls");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(myFilePath));

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Circle Poll Results");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Here are the Circle Poll Results:");

            startActivity(Intent.createChooser(intentShareFile, "Circle Poll Results"));
        }
    }

    private void setCircleMembersObserver(){
        listOfMembers = new HashMap<>();
        CirclePersonnelViewModel circlePersonnelViewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
        LiveData<String[]> liveData = circlePersonnelViewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");
        liveData.observe(this, returnArray -> {
            Subscriber member = new Gson().fromJson(returnArray[0], Subscriber.class);
            if(member!=null){
                allCircleMembers.add(member.getId());
                listOfMembers.put(member.getId(), member);
            }
        });
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