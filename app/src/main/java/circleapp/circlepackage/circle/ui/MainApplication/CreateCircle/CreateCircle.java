package circleapp.circlepackage.circle.ui.MainApplication.CreateCircle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.suke.widget.SwitchButton;

import java.util.HashMap;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Utils.UserSessionHelper;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static circleapp.circlepackage.circle.ViewModels.CreateCircle.WriteNewCircle.writeCircleToDb;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView visibilityPrompt, logoHelp, backgroundText;
    private Button btn_createCircle;
    private ImageButton back;
    private SwitchButton visibilitySwitchButton, acceptanceSwitchButton;
    private User user;
    private TextView categoryName;
    private RelativeLayout addLogo;
    private CircleImageView backgroundPic;
    private Uri filePath, downloadLink;
    private LinearLayout circleVisibilityDisplay, circleAcceptanceDisplay;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private String backgroundImageLink="", acceptanceType, visibilityType, cName, cDescription;
    public ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;
    private Circle circle;
    private Subscriber creatorSubscriber;
    private UserSessionHelper userSessionHelper = new UserSessionHelper();

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
        circleVisibilityDisplay = findViewById(R.id.circle_visibility_display);
        circleAcceptanceDisplay = findViewById(R.id.acceptanceDisplayView);
        categoryName = findViewById(R.id.category_name);
        categoryName.setText(getIntent().getStringExtra("category_name"));
        //to set invisible on adding image
        logoHelp = findViewById(R.id.logo_help);
        backgroundText = findViewById(R.id.backgroundText);
        imageUploadProgressDialog = new ProgressDialog(this);
        visibilityPrompt.setText("Anyone in " + user.getDistrict() + " can see this Circle");
    }

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
                Glide.with(this).load(filePath).into(backgroundPic);
                backgroundText.setVisibility(View.GONE);
                logoHelp.setVisibility(View.GONE);
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
            Permissions.check(this, CAMERA, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    sendImageUploadIntent();
                }
            });
        });
        visibilitySwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked)
                    visibilityType="Everybody";
                else
                    visibilityType="OnlyShare";
                Log.d("CreateCircleVisibility", visibilityType);
            }
        });
        acceptanceSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked)
                    acceptanceType="Automatic";
                else
                    acceptanceType="Review";
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
        writeCircleToDb(this,circle, user, creatorSubscriber);
        //navigate back to explore. new circle will be available in workbench
        goToCreatedCircle();
    }

    private void setLocalCircleObject(){
        user = userSessionHelper.getUserFromSession(this);
        String category = getIntent().getStringExtra("category_name");
        String myCircleID = FirebaseWriteHelper.getCircleId();
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
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }
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
                Glide.with(this).load(filePath).into(backgroundPic);
                backgroundText.setVisibility(View.GONE);
                logoHelp.setVisibility(View.GONE);
                imageUploadProgressDialog.dismiss();
            }
        });
        imageUploadModel.imageUpload(filePath);
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                filePath = ImagePicker.getImageUri(getApplicationContext(),bitmap);
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