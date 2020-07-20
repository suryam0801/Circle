package circleapp.circlepackage.circle.CreateCircle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.HashMap;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView visibilityPrompt, logoHelp, backgroundText;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup, visibilityGroup;
    private RadioButton acceptanceButton, visibilityButton;
    private User user;
    private TextView categoryName;
    private RelativeLayout addLogo;
    private CircleImageView backgroundPic;
    private Uri filePath, downloadLink;
    private LinearLayout circleVisibilityDisplay, circleAcceptanceDisplay;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    RuntimePermissionHelper runtimePermissionHelper;
    int photo;
    private String backgroundImageLink;
    public ImageUpload imageUploadModel;
    private ProgressDialog imageUploadProgressDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_create_circle);

        user = SessionStorage.getUser(CreateCircle.this);
        photo = 0;
        backgroundImageLink = "";
        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        visibilityGroup = findViewById(R.id.visibilityRadioGroup);
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
        runtimePermissionHelper = new RuntimePermissionHelper(CreateCircle.this);

        imageUploadProgressDialog = new ProgressDialog(this);
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

        visibilityPrompt.setText("Do you want everybody in " + user.getDistrict() + " to see your circle?");

        btn_createCircle.setOnClickListener(view -> {
            String cName = circleNameEntry.getText().toString().trim();
            String cDescription = circleDescriptionEntry.getText().toString().trim();
            if (!cName.isEmpty() && !cDescription.isEmpty()) {
                radioButtonCheck(cName, cDescription);
            } else {
                Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(CreateCircle.this, ExploreTabbedActivity.class));
        });

        addLogo.setOnClickListener(v -> {
            if (!runtimePermissionHelper.isPermissionAvailable(CAMERA)) {
                runtimePermissionHelper.requestCameraPermissionsIfDenied(CAMERA);
            }
            else {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void radioButtonCheck(String name, String description) {
        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);
        int visibilityId = visibilityGroup.getCheckedRadioButtonId();
        visibilityButton = findViewById(visibilityId);
        if (radioId != -1 && visibilityId != -1) {
            if (acceptanceButton.getText().equals("Anyone can join")) {
                String visibility = (String) visibilityButton.getText();
                if (visibility.equals("Yes"))
                    createCircle(name, description, "Public", "Everybody");
                else
                    createCircle(name, description, "Public", "OnlyShare");
            } else {
                String visibility = (String) visibilityButton.getText();
                if (visibility.equals("Yes"))
                    createCircle(name, description, "Private", "Everybody");
                else
                    createCircle(name, description, "Private", "OnlyShare");
            }
        } else {
            Animation animShake = AnimationUtils.loadAnimation(CreateCircle.this, R.anim.shake_animation);
            acceptanceGroup.startAnimation(animShake);
            HelperMethods.vibrate(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void createCircle(String cName, String cDescription, String acceptanceType, String visibilty) {

        User user = SessionStorage.getUser(CreateCircle.this);

        String category = getIntent().getStringExtra("category_name");

        String myCircleID = FirebaseWriteHelper.getCircleId();
        String creatorUserID = user.getUserId();

        if (acceptanceType.equals("Public"))
            acceptanceType = "Automatic";
        else if (acceptanceType.equals("Private"))
            acceptanceType = "Review";

        String creatorName = user.getName();

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);
        if (downloadLink != null)
            backgroundImageLink = downloadLink.toString();
        else
            backgroundImageLink = "default";

        //updating circles
        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, visibilty, creatorUserID, creatorName,
                category, backgroundImageLink, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(), 0, 0,true);

        Subscriber creatorSubscriber = new Subscriber(user.getUserId(), user.getName(),
                user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

        FirebaseWriteHelper.createUserMadeCircle(circle, creatorSubscriber, user.getUserId());

        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        FirebaseWriteHelper.updateUser(user, this);

        SessionStorage.saveCircle(CreateCircle.this, circle);

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

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
        } else {
            Toast.makeText(CreateCircle.this,
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

    @Override
    protected void onStart() {
        super.onStart();
    }
}