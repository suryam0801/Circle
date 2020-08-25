package circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.tooltip.Tooltip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Helpers.SessionStorage;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.Utils.PollExportUtil;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.BroadcastsViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastCreation.CreateNormalBroadcastDialog;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastCreation.CreatePhotoBroadcastDialog;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastCreation.CreatePollBroadcastDialog;
import circleapp.circleapppackage.circle.ui.CircleWall.CircleInformation;
import circleapp.circleapppackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageImageDisplay;
import circleapp.circleapppackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;
import circleapp.circleapppackage.circle.ui.PersonelDisplay.PersonelDisplay;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class CircleWall extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234;
    private Uri downloadLink;
    private LinearLayout emptyDisplay;
    private Circle circle;
    private List<String> allCircleMembers;
    private HashMap<String, Subscriber> listOfMembers;
    private List<Subscriber> listOfCirclePersonel;
    private ImageButton back, moreOptions;
    private User user;

    //create broadcast popup ui elements
    private TextView circleBannerName;
    private Dialog confirmationDialog, reportAbuseDialog;
    private ImageButton viewApplicants;
    private RelativeLayout parentLayout, fabLayout;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton poll, newPost, imagePost;
    private String broadcastid;
    int broadcastPos;
    private ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog, pdfGenerateProgressBar;
    private GlobalVariables globalVariables = new GlobalVariables();
    private CreateNormalBroadcastDialog normalBroadcastDialog;
    private CreatePhotoBroadcastDialog photoBroadcastDialog;
    private CreatePollBroadcastDialog pollBroadcastDialog;
    private TextView getStartedPoll, getStartedBroadcast, getStartedPhoto, getStartedText, blackGetStartedBroadcast, blackGetStartedPhoto, blackGetStartedPoll;
    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);

            setUserObserver();
            setCircleObserver();
            HelperMethodsBL.updateDeviceTokenInPersonel(circle.getId(),user);
            setImageUploadObserver();
            initUIElements();
            initializeRecyclerView();
            setParentBgImage();
            initBtnListeners();
            setBroadcastObserver();
    }

    private void initUIElements(){
        confirmationDialog = new Dialog(CircleWall.this);
        reportAbuseDialog = new Dialog(CircleWall.this);
        normalBroadcastDialog = new CreateNormalBroadcastDialog();
        photoBroadcastDialog = new CreatePhotoBroadcastDialog();
        pollBroadcastDialog = new CreatePollBroadcastDialog();


        broadcastid = getIntent().getStringExtra("broadcastId");
        broadcastPos = getIntent().getIntExtra("broadcastPos", 0);
        imageUploadProgressDialog = new ProgressDialog(this);
        if (getIntent().getBooleanExtra("fromCreateCircle", false) == true) {
            addMembersToCirclePersonel();
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
        getStartedText = findViewById(R.id.circle_wall_get_started_text);
        pdfGenerateProgressBar = new ProgressDialog(this);
        blackGetStartedBroadcast = findViewById(R.id.circle_wall_black_get_started_broadcast);
        blackGetStartedPhoto = findViewById(R.id.circle_wall_black_get_started_image);
        blackGetStartedPoll = findViewById(R.id.circle_wall_black_get_started_poll);
        floatingActionMenu = findViewById(R.id.menu);
        fabLayout = findViewById(R.id.floating_btn_layout);
        moreOptions = findViewById(R.id.circle_wall_more_options);
        parentLayout = findViewById(R.id.circle_wall_parent_layout);
        viewApplicants = findViewById(R.id.applicants_display_creator);
        recyclerView = findViewById(R.id.broadcastViewRecyclerView);
        allCircleMembers = new ArrayList<>();
        circleBannerName.setText(circle.getName());

        if(circle.getMembersList()!=null)
            if(circle.getMembersList().containsKey(user.getUserId()))
                if(circle.getMembersList().get(user.getUserId()).equals("admin"))
                    fabLayout.setVisibility(View.VISIBLE);
        if (circle.getApplicantsList() != null && circle.getMembersList().get(user.getUserId()).equals("admin")) {
            new Tooltip.Builder(viewApplicants)
                    .setText("You have pending applicants")
                    .setTextColor(Color.BLACK)
                    .setBackgroundColor(Color.WHITE)
                    .setGravity(Gravity.BOTTOM)
                    .setCornerRadius(20f)
                    .setDismissOnClick(true)
                    .show();
        }
        if (circle.getNoOfBroadcasts() == 0){
            emptyDisplay.setVisibility(View.VISIBLE);
            if (SessionStorage.getCircleWallBgImage(CircleWall.this) != null){
                 if(SessionStorage.getCircleWallBgImage(CircleWall.this).equals("bg10")){
                    getStartedText.setTextColor(getResources().getColor(R.color.black));
                    blackGetStartedBroadcast.setVisibility(View.VISIBLE);
                    blackGetStartedPhoto.setVisibility(View.VISIBLE);
                    blackGetStartedPoll.setVisibility(View.VISIBLE);
                    getStartedPoll.setVisibility(View.GONE);
                    getStartedBroadcast.setVisibility(View.GONE);
                    getStartedPhoto.setVisibility(View.GONE);
                }
            }
        }
        setCircleMembersObserver();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initBtnListeners(){
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
        if(circle.getMembersList()!=null){
            if(circle.getMembersList().containsKey(user.getUserId()))
                if (circle.getMembersList().get(user.getUserId()).equals("admin"))
                    viewApplicants.setVisibility(View.VISIBLE);
        }

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

        blackGetStartedPhoto.setOnClickListener(view -> photoBroadcastDialog.showCreatePhotoBroadcastDialog(CircleWall.this));
        blackGetStartedPoll.setOnClickListener(view -> pollBroadcastDialog.showCreatePollBroadcastDialog(CircleWall.this));
        blackGetStartedBroadcast.setOnClickListener(view -> normalBroadcastDialog.showCreateNormalBroadcastDialog(CircleWall.this));

        circleBannerName.setOnClickListener(v->{
            globalVariables.setCircleWallPersonel(new ArrayList<>());
            Intent intent = new Intent(CircleWall.this,CircleInformation.class);
            intent.putExtra("circle_wall_nav",true);
            startActivity(intent);
            finish();
        });
    }

    private void setBroadcastObserver(){
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

    private void setCircleObserver(){
        circle = globalVariables.getCurrentCircle();
        MyCirclesViewModel tempViewModel = ViewModelProviders.of(CircleWall.this).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) CircleWall.this, dataSnapshot -> {
            Circle circleTemp = dataSnapshot.getValue(Circle.class);
            if (circleTemp != null&&circleTemp.getMembersList()!=null) {
                circle = circleTemp;
                if (circle.getMembersList().containsKey(user.getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                }
            }
        });
    }

    private void setUserObserver(){
        user = globalVariables.getCurrentUser();
        UserViewModel tempViewModel = ViewModelProviders.of(CircleWall.this).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(user.getUserId());
        tempLiveData.observe((LifecycleOwner) CircleWall.this, dataSnapshot -> {
            User temp = dataSnapshot.getValue(User.class);
            if (temp != null) {
                user = temp;
                globalVariables.saveCurrentUser(user);
            }
        });
    }

    private void setImageUploadObserver() {
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath).observe(this, progress -> {
            // update UI
            if(progress==null);

            else if(progress[1].equals("-1")){
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Error uploading. Please try again", Toast.LENGTH_SHORT).show();
            }

            else if(!progress[1].equals("100")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100")){
                downloadLink = Uri.parse(progress[0]);
                globalVariables.setTempdownloadLink(downloadLink);
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
        HelperMethodsBL.initializeNewCommentsAlertTimestamp(broadcast);

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
        if (circle.getMembersList().get(user.getUserId()).equals("admin")){
            popup.getMenu().findItem(R.id.deleteCircleMenuBar).setVisible(true);
            popup.getMenu().findItem(R.id.report_abuseMenubar).setVisible(false);
        }
        else{
            popup.getMenu().findItem(R.id.report_abuseMenubar).setVisible(true);
            popup.getMenu().findItem(R.id.exitCircleMenuBar).setVisible(true);
        }
        if (circle.getMembersList().get(user.getUserId()).equals("admin"))
            popup.getMenu().findItem(R.id.exportPollsMenuBar).setVisible(true);
        popup.getMenu().findItem(R.id.share_qr_code_menu_bar).setVisible(true);

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Change wallpaper":
                    finishAfterTransition();
                    startActivity(new Intent(CircleWall.this, CircleWallBackgroundPicker.class));
                    globalVariables.setCircleWallPersonel(new ArrayList<>());
                    break;
                case "Invite a friend":
                    InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
                    break;
                case "Report Abuse":
                    HelperMethodsUI.showReportAbusePopup(reportAbuseDialog, CircleWall.this, circle.getId(), "", "", circle.getCreatorID(), user.getUserId());
                    break;
                case "Export Poll Data":
                    Permissions.check(this, WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            pdfGenerateProgressBar.setTitle("Generating PDF...");
                            pdfGenerateProgressBar.show();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    exportPollsToFile();
                                }
                            };
                            AsyncTask.execute(runnable);
                        }
                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions){
                            // permission denied, block the feature.
                        }
                    });
                    break;
                case "Exit circle":
                    HelperMethodsUI.showExitDialog(CircleWall.this,circle,user);
                    globalVariables.setCircleWallPersonel(new ArrayList<>());
                    break;
                case "Delete circle":
                    HelperMethodsUI.showDeleteDialog(CircleWall.this,circle,user);
                    globalVariables.setCircleWallPersonel(new ArrayList<>());
                    break;
                case "Circle Information":
                    globalVariables.setCircleWallPersonel(new ArrayList<>());
                    Intent intent = new Intent(CircleWall.this,CircleInformation.class);
                    intent.putExtra("circle_wall_nav",true);
                    startActivity(intent);
                    finish();
                    break;
                case "Share QR Code":
                    Permissions.check(this, WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
                        @Override
                        public void onGranted() {
                            globalVariables.setCircleWallPersonel(new ArrayList<>());
                            runQRGenerator();
                        }
                        @Override
                        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                            // permission denied, block the feature.
                        }
                    });
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
            if(!path.exists())
                path.mkdir();
            File file = new File(path, "/" + "All Poll Results "+circle.getName()+".xls");
            PollExportUtil pollExportUtil = new PollExportUtil();
            pollExportUtil.writeAllPollsToExcelFile(this, file, pollBroadcasts, allCircleMembers, listOfMembers, circle.getName(), pdfGenerateProgressBar);
            File file1 = new File(path,"/" + "All Poll Results "+circle.getName()+".pdf");
            shareFile(file1);
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
        listOfCirclePersonel = new ArrayList<>();
        CirclePersonnelViewModel circlePersonnelViewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);
        LiveData<String[]> liveData = circlePersonnelViewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");
        liveData.observe(this, returnArray -> {
            Subscriber member = new Gson().fromJson(returnArray[0], Subscriber.class);
            if(member!=null){
                listOfCirclePersonel.add(member);
                globalVariables.setCircleWallPersonel(listOfCirclePersonel);
                allCircleMembers.add(member.getId());
                listOfMembers.put(member.getId(), member);
            }
        });
    }

    private void runQRGenerator(){
        String qrHash = "https://worfo.app.link/8JMEs34W96/?"+circle.getId();
        String qrUri = "";
        Bitmap bitmap = Bitmap.createBitmap(200,200,Bitmap.Config.RGB_565);;
        BitMatrix bitMatrix;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            bitMatrix = qrCodeWriter.encode(qrHash, BarcodeFormat.QR_CODE,200,200);
            for(int x = 0; x<200; x++){
                for (int y=0; y<200; y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)?Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        //write to local
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if(!path.exists())
            path.mkdir();
        File file = new File(path, "/" + "QRCode "+circle.getName()+".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);// bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            qrUri=file.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(CircleWall.this, FullPageImageDisplay.class);
        intent.putExtra("uri", qrUri);
        intent.putExtra("indexOfBroadcast", 0);
        intent.putExtra("QRCode",true);
        CircleWall.this.startActivity(intent);
        CircleWall.this.finish();
    }

    private void addMembersToCirclePersonel() {
        if(globalVariables.getUsersList()!=null){
            for(String userId: globalVariables.getUsersList()) {
                UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) this).get(UserViewModel.class);
                LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(userId);
                tempLiveData.observe((LifecycleOwner) this, dataSnapshot -> {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Subscriber subscriber = new Subscriber(user, System.currentTimeMillis());
                        HelperMethodsBL.updateCirclePersonel(subscriber, circle.getId());
                    }
                });
            }
            globalVariables.setUsersList(null);
        }
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
        adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle, user);
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
        globalVariables.setCircleWallPersonel(new ArrayList<>());
        Intent intent = new Intent(CircleWall.this, ExploreTabbedActivity.class);
        intent.putExtra("fromcirclewall",true);
        globalVariables.saveCurrentCircle(null);
        startActivity(intent);
        finish();
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