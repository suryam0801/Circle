package circleapp.circleapppackage.circle.ui.CircleWall.FullPageView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Helpers.SessionStorage;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circleapppackage.circle.ui.CircleWall.CircleInformation;
import circleapp.circleapppackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circleapppackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circleapppackage.circle.ui.PersonelDisplay.PersonelDisplay;

public class FullPageBroadcastCardView extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener{

    private Uri filePath, downloadLink;
    private User user;
    private Circle circle;
    private List<Broadcast> broadcastList;
    private int initialBroadcastPosition;
    private TextView banner;
    private ImageButton back;
    private LinearLayout parentLayout;
    private ImageButton moreOptions, viewApplicants;
    private RecyclerView recyclerView;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog reportAbuseDialog;
    private ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;
    private static final int PICK_IMAGE_ID = 234;
    private RecyclerView.Adapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_broadcast_card_view);
        user = globalVariables.getCurrentUser();

        setUIElements();
        setParentBgImage();
        setCircleObserver();
        initObserverForUser();
        setImageUploadObserver();
        //Go back to home
        back.setOnClickListener(view -> {
            onBackPressed();
        });
        //Only for creator
        viewApplicants.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(getApplicationContext(), PersonelDisplay.class));
        });
        //Drop down menu
        moreOptions.setOnClickListener(view -> {
            setPopupMenu();
        });
        //Snapping the recyclerview
        SnapHelper snapHelper = new PagerSnapHelper();
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FullPageBroadcastCardAdapter(this, broadcastList, circle, initialBroadcastPosition);
        recyclerView.setAdapter(adapter);

        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.scrollToPosition(initialBroadcastPosition);
    }
    private void setUIElements(){
        imageUploadProgressDialog = new ProgressDialog(this);
        reportAbuseDialog = new Dialog(this);
        recyclerView = findViewById(R.id.full_page_broadcast_card_recycler_view);
        banner = findViewById(R.id.full_page_broadcast_banner_name);
        back = findViewById(R.id.bck_fullpage_broadcast);
        parentLayout = findViewById(R.id.full_page_broadcast_parent_layout);
        moreOptions = findViewById(R.id.full_page_broadcast_more_options);
        viewApplicants = findViewById(R.id.full_page_broadcast_applicants_display_creator);

        broadcastList = globalVariables.getCurrentBroadcastList();
        circle = globalVariables.getCurrentCircle();
        initialBroadcastPosition = getIntent().getIntExtra("broadcastPosition", 0);
        //set applicants button visible
        if (circle.getCreatorID().equals(globalVariables.getCurrentUser().getUserId()))
            viewApplicants.setVisibility(View.VISIBLE);
        banner.setText(circle.getName());

    }

    private void initObserverForUser(){
        UserViewModel tempViewModel = ViewModelProviders.of(FullPageBroadcastCardView.this).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(user.getUserId());
        tempLiveData.observe((LifecycleOwner) FullPageBroadcastCardView.this, dataSnapshot -> {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                globalVariables.saveCurrentUser(user);
            }
        });
    }

    private void setCircleObserver(){
        circle = globalVariables.getCurrentCircle();
        MyCirclesViewModel tempViewModel = ViewModelProviders.of(FullPageBroadcastCardView.this).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) FullPageBroadcastCardView.this, dataSnapshot -> {
            Circle circleTemp = dataSnapshot.getValue(Circle.class);
            if (circleTemp != null&&circleTemp.getMembersList()!=null) {
                circle = circleTemp;
                globalVariables.saveCurrentCircle(circle);
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
                adapter.notifyDataSetChanged();
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100")){
                downloadLink = Uri.parse(progress[0]);
                if(!downloadLink.toString().contains("content://media")){
                    globalVariables.setCommentDownloadLink(downloadLink);
                    adapter.notifyDataSetChanged();
                }
                imageUploadProgressDialog.dismiss();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setPopupMenu(){
        PopupMenu popup = new PopupMenu(this, moreOptions);
        popup.getMenuInflater()
                .inflate(R.menu.circle_wall_menu, popup.getMenu());
        if (circle.getCreatorID().equals(globalVariables.getCurrentUser().getUserId()))
            popup.getMenu().findItem(R.id.deleteCircleMenuBar).setVisible(true);
        else
            popup.getMenu().findItem(R.id.exitCircleMenuBar).setVisible(true);
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Change wallpaper":
                    finishAfterTransition();
                    startActivity(new Intent(this, CircleWallBackgroundPicker.class));
                    break;
                case "Invite a friend":
                    InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
                    break;
                case "Report Abuse":
                    HelperMethodsUI.showReportAbusePopup(reportAbuseDialog, this, circle.getId(), "", "", circle.getCreatorID(), globalVariables.getCurrentUser().getUserId());
                    break;
                case "Exit circle":
                    HelperMethodsUI.showExitDialog(this, circle, globalVariables.getCurrentUser());
                    break;
                case "Delete circle":
                    HelperMethodsUI.showDeleteDialog(this, circle, globalVariables.getCurrentUser());
                    break;
                case "Circle Information":
                    startActivity(new Intent(this, CircleInformation.class));
                    break;
            }
            return true;
        });
        popup.show();
    }
    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "shareLink":
                HelperMethodsUI.showShareCirclePopup(circle, this);
                break;
            case "copyLink":
                HelperMethodsUI.copyLinkToClipBoard(circle, this);
                break;
        }
    }


    public void setParentBgImage() {
        String bg = SessionStorage.getCircleWallBgImage(this);
        if (bg != null) {
            switch (bg) {
                case "bg1":
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_1)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg2":
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_2)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg3":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);Glide.with(this.getApplicationContext())
                        .load(R.drawable.circle_wall_background_3)
                        .into(new CustomTarget<Drawable>() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                parentLayout.setBackground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    break;
                case "bg4":
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_4)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg5":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_5)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg6":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_6)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg7":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_7)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg8":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_8)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg9":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    Glide.with(this.getApplicationContext())
                            .load(R.drawable.circle_wall_background_9)
                            .into(new CustomTarget<Drawable>() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    parentLayout.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    break;
                case "bg10":
                    banner.setTextColor(Color.BLACK);
                    parentLayout.setBackgroundColor(Color.WHITE);
                    break;
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(getApplicationContext(), CircleWall.class));
    }
}
