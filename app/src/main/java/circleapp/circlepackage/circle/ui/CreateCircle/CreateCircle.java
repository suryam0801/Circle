package circleapp.circlepackage.circle.ui.CreateCircle;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.suke.widget.SwitchButton;

import java.util.HashMap;

import circleapp.circlepackage.circle.DataLayer.CircleRepository;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.ViewModels.CreateCircle.WriteNewCircle;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circlepackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class CreateCircle extends AppCompatActivity {

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView visibilityPrompt, visibiltyHeading, acceptanceHeading, acceptancePrompt;
    private Button btn_createCircle;
    private ImageButton back;
    private SwitchButton visibilitySwitchButton, acceptanceSwitchButton;
    private User user;
    private TextView categoryName;
    private RelativeLayout addLogo;
    private CircleImageView backgroundPic;
    private Uri filePath, downloadLink;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private String backgroundImageLink="", acceptanceType, visibilityType, cName, cDescription;
    public ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;
    private Circle circle;
    private Subscriber creatorSubscriber;
    private GlobalVariables globalVariables = new GlobalVariables();
    private WriteNewCircle writeNewCircle = new WriteNewCircle();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_create_circle);
        setUIElements();
        setObserverForImageUpload();
        setButtonListeners();
    }
    private void setUIElements(){
        //Initialize all UI elements in the CreateCircle activity
        user = globalVariables.getCurrentUser();
        visibiltyHeading = findViewById(R.id.circle_visibility_heading_text);
        acceptanceHeading = findViewById(R.id.acceptance_heading);
        acceptancePrompt = findViewById(R.id.acceptance_textview);
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        visibilitySwitchButton = findViewById(R.id.visibilitySwitch);
        acceptanceSwitchButton = findViewById(R.id.joiningSwitch);
        visibilitySwitchButton.setChecked(true);
        acceptanceSwitchButton.setChecked(true);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        visibilityPrompt = findViewById(R.id.visibility_prompt_create_circle);
        circleNameEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        circleDescriptionEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        backgroundPic = findViewById(R.id.background_image);
        addLogo = findViewById(R.id.backgroundPreview);
        categoryName = findViewById(R.id.category_name);
        categoryName.setText(getIntent().getStringExtra("category_name"));
        //to set invisible on adding image
        imageUploadProgressDialog = new ProgressDialog(this);
        acceptanceType="Automatic";
        visibilityType="Everybody";
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setObserverForImageUpload(){
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
                backgroundPic.setScaleX((float) 1.0);
                backgroundPic.setScaleY((float) 1.0);
                backgroundPic.setBackground(getDrawable(R.drawable.circle_wall_background_white));
                Glide.with(this).load(filePath).into(backgroundPic);
                imageUploadProgressDialog.dismiss();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setButtonListeners(){
        btn_createCircle.setOnClickListener(view -> {
            cName = circleNameEntry.getText().toString().trim();
            cDescription = circleDescriptionEntry.getText().toString().trim();
            checkIfFormIsFilled();
        });

        back.setOnClickListener(view -> {
            sendToHome();
        });

        addLogo.setOnClickListener(v -> {
            Permissions.check(this, new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    sendImageUploadIntent();
                }
            });
        });
        visibilitySwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked){
                    visibilityType="Everybody";
                    visibilityPrompt.setText("Anyone in " + user.getDistrict() + " can view this Circle");
                    visibiltyHeading.setText("Public");
                }
                else{
                    visibilityType="OnlyShare";
                    visibilityPrompt.setText("Only people with Invite Link can view this Circle");
                    visibiltyHeading.setText("Private");
                }
                Log.d("CreateCircleVisibility", visibilityType);
            }
        });
        acceptanceSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked){
                    acceptanceType="Automatic";
                    acceptanceHeading.setText("Members join freely");
                    acceptancePrompt.setText("People can join this Circle without applying");
                }
                else{
                    acceptanceType="Review";
                    acceptanceHeading.setText("Members have to apply");
                    acceptancePrompt.setText("You have to accept member applications for them to join");
                }
                Log.d("CreateCircleAcceptance", acceptanceType);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkIfFormIsFilled(){
        if (!cName.isEmpty() && !cDescription.isEmpty()) {
            createCircle();
        } else {
            Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void createCircle() {

        setLocalCircleObject();
        Log.d("circledetails", circle.getAcceptanceType()+circle.getVisibility());
        writeNewCircle.writeCircleToDb(circle, user, creatorSubscriber);
        //navigate back to explore. new circle will be available in workbench
        goToCreatedCircle();
    }

    private void setLocalCircleObject(){
        CircleRepository circleRepository = new CircleRepository();
        user = globalVariables.getCurrentUser();
        String category = getIntent().getStringExtra("category_name");
        String myCircleID = circleRepository.getCircleId();
        String creatorUserID = user.getUserId();
        String creatorName = user.getName();

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);
        if (downloadLink != null)
            backgroundImageLink = downloadLink.toString();
        else
            backgroundImageLink = "default";

        circle = new Circle(myCircleID, cName, cDescription, acceptanceType, visibilityType, creatorUserID, creatorName,
                category, backgroundImageLink, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(), 0, 0,true);

        creatorSubscriber = new Subscriber(user, System.currentTimeMillis());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToCreatedCircle(){
        //navigate back to explore. new circle will be available in workbench
        SharedPreferences prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);
        if (prefs.getBoolean("firstWall", true)) {
            finishAfterTransition();
            Intent intent = new Intent(this, CircleWallBackgroundPicker.class);
            intent.putExtra("fromCreateCircle", true);
            startActivity(intent);
            prefs.edit().putBoolean("firstWall", false).commit();
        } else {
            finishAfterTransition();
            Intent intent = new Intent(this, CircleWall.class);
            intent.putExtra("fromCreateCircle", true);
            startActivity(intent);
        }
    }
    private void sendImageUploadIntent(){
        ImagePicker imagePicker = new ImagePicker(getApplication());
        Intent chooseImageIntent = imagePicker.getPickImageIntent();
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void uploadCircleLogo(){
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath).observe(this, progress -> {
            Log.d("progressvalue",""+progress);
            // update UI
            if(progress==null);

            else if(!progress[1].equals("100.0")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100.0")){
                downloadLink = Uri.parse(progress[0]);
                backgroundPic.setScaleX((float) 1.0);
                backgroundPic.setScaleY((float) 1.0);
                backgroundPic.setBackground(getDrawable(R.drawable.circle_wall_background_white));
                Glide.with(this).load(filePath).into(backgroundPic);
                imageUploadProgressDialog.dismiss();
            }
        });
        imageUploadModel.imageUpload(filePath);
    }

    //code for upload the image
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath = imagePicker.getImageUri(bitmap);
                if(filePath !=null){
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
        finishAfterTransition();
        Intent about_intent = new Intent(CreateCircle.this, ExploreTabbedActivity.class);
        startActivity(about_intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendToHome(){
        finishAfterTransition();
        startActivity(new Intent(CreateCircle.this, ExploreTabbedActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}