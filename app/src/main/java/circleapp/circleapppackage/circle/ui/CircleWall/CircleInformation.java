package circleapp.circleapppackage.circle.ui.CircleWall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;
import circleapp.circleapppackage.circle.ui.PersonelDisplay.MemberListAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class CircleInformation extends AppCompatActivity {


    private Uri filePath, downloadLink;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    public ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;

    private ImageView banner;
    private CircleImageView logo;
    private TextView creatorName, circleName, circleDescription;
    private RecyclerView membersDisplay;
    private RecyclerView.Adapter adapter;
    private List<Subscriber> memberList = new ArrayList<>();
    private Circle circle;
    private LinearLayout noPermissionToViewMembers, noMembersDisplay;
    private User user;
    private ImageButton backBtn, editCircleNameBtn, editCircleDescBtn, editCircleLogo;
    private int indexValue;
    private LiveData<String[]> liveData;
    private Dialog editCircleNameDialog, editCircleDescDialog;
    private GlobalVariables globalVariables = new GlobalVariables();
    private boolean isCircleWall;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_information);

        isCircleWall = getIntent().getBooleanExtra("circle_wall_nav",false);

        initUIElements();
        setObserverForImageUpload();
        if(isCircleWall)
            ifActionFromCircleWall();

        //back button
        backBtn.setOnClickListener(view -> {//go back to circle wall
            if (indexValue == -1) {
                finishAfterTransition();
                startActivity(new Intent(CircleInformation.this, CircleWall.class));
            } else {//go back to postion of click in explore
                finishAfterTransition();
                Intent intent = new Intent(CircleInformation.this, ExploreTabbedActivity.class);
                intent.putExtra("exploreIndex", indexValue);
                startActivity(intent);
            }
        });

    }
    @Override
    public void onPause() {
        if(liveData!=null)
        liveData.removeObservers(this);
        super.onPause();
    }

    private void sendImageUploadIntent() {
        ImagePicker imagePicker = new ImagePicker(getApplication());
        Intent chooseImageIntent = imagePicker.getPickImageIntent();
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setObserverForImageUpload() {
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath, false).observe(this, progress -> {
            // update UI
            if (progress == null) ;

            else if (progress[1].equals("-1")) {
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Error uploading. Please try again", Toast.LENGTH_SHORT).show();
            } else if (!progress[1].equals("100")) {
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            } else if (progress[1].equals("100")) {
                downloadLink = Uri.parse(progress[0]);
                Glide.with(this).load(filePath).into(logo);
                if(downloadLink!=null)
                    HelperMethodsBL.updateCircleLogo(circle, downloadLink.toString());
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Circle Logo Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ResourceType")
    private void initUIElements(){
        circle = globalVariables.getCurrentCircle();
        user = globalVariables.getCurrentUser();

        indexValue = getIntent().getIntExtra("exploreIndex", -1);
        banner = findViewById(R.id.circle_info_circle_banner);
        logo = findViewById(R.id.circle_info_circle_logo);
        creatorName = findViewById(R.id.circle_info_creator_name);
        circleName = findViewById(R.id.circle_info_circle_name);
        circleDescription = findViewById(R.id.circle_info_circle_description);
        membersDisplay = findViewById(R.id.circle_info_members_display);
        noPermissionToViewMembers = findViewById(R.id.circle_info_members_not_available);
        noMembersDisplay = findViewById(R.id.circle_info_empty_membersList);
        backBtn = findViewById(R.id.bck_circle_information);
        editCircleDescBtn = findViewById(R.id.edit_circle_desc_btn);
        editCircleNameBtn = findViewById(R.id.edit_circle_name_btn);
        editCircleLogo = findViewById(R.id.edit_circle_profile_pic);
        imageUploadProgressDialog = new ProgressDialog(this);

        creatorName.setText(circle.getCreatorName());
        circleName.setText(globalVariables.getCurrentCircle().getName());
        circleDescription.setText(globalVariables.getCurrentCircle().getDescription());
        HelperMethodsUI.createDefaultCircleIcon(circle,this,logo);
        //setting circle banner
        setBannerBackground(circle.getCategory());

        //setting members display
        if (circle.getMembersList() != null && circle.getMembersList().keySet().contains(user.getUserId())) {
            noPermissionToViewMembers.setVisibility(View.GONE);
            loadMembersList();
        } else if (circle.getAcceptanceType().equalsIgnoreCase("review")) {
            noMembersDisplay.setVisibility(View.GONE);
            noPermissionToViewMembers.setVisibility(View.VISIBLE);
        } else {
            noMembersDisplay.setVisibility(View.VISIBLE);
        }

    }

    private void ifActionFromCircleWall(){
        //Check if user is admin
        if(circle.getMembersList().get(user.getUserId()).equals("admin")){
            editCircleNameBtn.setVisibility(View.VISIBLE);
            editCircleDescBtn.setVisibility(View.VISIBLE);
            editCircleLogo.setVisibility(View.VISIBLE);
            editCircleDescDialog =new Dialog(this);
            editCircleNameDialog = new Dialog(this);

            editCircleNameDialog.setContentView(R.layout.edit_circle_name_dialog);
            editCircleDescDialog.setContentView(R.layout.edit_circle_desc_dialog);

            editCircleNameBtn.setOnClickListener(v->{
                showEditCircleNameDialog();
            });

            editCircleDescBtn.setOnClickListener(v->{
                showEditCircleDescDialog();
            });

            editCircleLogo.setOnClickListener(v->{
                Permissions.check(this, new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, null, null, new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        sendImageUploadIntent();
                    }
                });
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void uploadCircleLogo() {
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath, false).observe((LifecycleOwner) this, progress -> {
            // update UI
            if (progress == null) ;

            else if (!progress[1].equals("100")) {
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            } else if (progress[1].equals("100")) {
                downloadLink = Uri.parse(progress[0]);
                Glide.with(this).load(filePath).into(logo);
                if(downloadLink!=null)
                    HelperMethodsBL.updateCircleLogo(circle, downloadLink.toString());
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Circle Logo Updated!", Toast.LENGTH_SHORT).show();
            }
        });
        imageUploadModel.imageUpload(filePath, false);
    }

    private void showEditCircleNameDialog(){
        final EditText changeName = editCircleNameDialog.findViewById(R.id.edit_circle_name_edittext);
        final Button change = editCircleNameDialog.findViewById(R.id.edit_circle_name_Button);
        change.setOnClickListener(v->{
            if(!changeName.getText().toString().isEmpty()) {
                HelperMethodsBL.updateCircleName(circle, changeName.getText().toString());
                circleName.setText(changeName.getText().toString());
                Toast.makeText(this,"Changed Name Successfully",Toast.LENGTH_SHORT).show();
                editCircleNameDialog.dismiss();
            }
            else {
                Toast.makeText(this,"No change",Toast.LENGTH_SHORT).show();
                editCircleNameDialog.dismiss();
            }
        });
        editCircleNameDialog.show();
    }

    private void showEditCircleDescDialog(){
        final EditText changeDesc = editCircleDescDialog.findViewById(R.id.edit_circle_desc);
        final Button change = editCircleDescDialog.findViewById(R.id.edit_circle_desc_Button);
        change.setOnClickListener(v->{
            if(!changeDesc.getText().toString().isEmpty()) {
                HelperMethodsBL.updateCircleDescription(circle, changeDesc.getText().toString());
                circleDescription.setText(changeDesc.getText().toString());
                Toast.makeText(this,"Changed Description Successfully",Toast.LENGTH_SHORT).show();
                editCircleDescDialog.dismiss();
            }
            else {
                Toast.makeText(this,"No change",Toast.LENGTH_SHORT).show();
                editCircleDescDialog.dismiss();
            }
        });
        editCircleDescDialog.show();
    }

    private void loadMembersList(){
         //initialize membersList
        membersDisplay.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        membersDisplay.setLayoutManager(layoutManager);
        adapter = new MemberListAdapter(this, memberList, false);
        membersDisplay.setAdapter(adapter);

        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);

        liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(this, returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            memberList.add(subscriber);
            adapter.notifyDataSetChanged();
        });
    }

    private void setBannerBackground(String circleCategory) {
        switch (circleCategory) {
            case "Events":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_events)).centerCrop().into(banner);
                break;
            case "Apartments & Communities":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_apartment_and_communities)).centerCrop().into(banner);
                break;
            case "Sports":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_sports)).centerCrop().into(banner);
                break;
            case "Friends & Family":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_friends_and_family)).centerCrop().into(banner);
                break;
            case "Food & Entertainment":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_food_and_entertainment)).centerCrop().into(banner);
                break;
            case "Science & Tech":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_science_and_tech_background)).centerCrop().into(banner);
                break;
            case "Gaming":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_gaming)).centerCrop().into(banner);
                break;
            case "Health & Fitness":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_health_and_fitness)).centerCrop().into(banner);
                break;
            case "Students & Clubs":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_students_and_clubs)).centerCrop().into(banner);
                break;
            case "The Circle App":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.admin_circle_banner)).centerCrop().into(banner);
                break;
            case "General":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_general)).centerCrop().into(banner);
                break;
            default:
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_custom_circle)).centerCrop().into(banner);
                break;
        }
    }

    //code for upload the image
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath = imagePicker.getImageUri(bitmap);
                if (filePath != null) {
                    uploadCircleLogo();
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
        int indexValue = getIntent().getIntExtra("exploreIndex", -1);
        if (indexValue == -1) {
            finishAfterTransition();
            startActivity(new Intent(CircleInformation.this, CircleWall.class));
        } else {
            finishAfterTransition();
            Intent intent = new Intent(CircleInformation.this, ExploreTabbedActivity.class);
            intent.putExtra("exploreIndex", indexValue);
            startActivity(intent);
        }
    }
}