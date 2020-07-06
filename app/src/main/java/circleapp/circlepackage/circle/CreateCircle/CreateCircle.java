package circleapp.circlepackage.circle.CreateCircle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

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
    private Uri filePath, downloadUri;
    private LinearLayout circleVisibilityDisplay, circleAcceptanceDisplay;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    RuntimePermissionHelper runtimePermissionHelper;
    int photo;
    private String backgroundImageLink;

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
            if (ContextCompat.checkSelfPermission(CreateCircle.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateCircle.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
            if (photo == 0)
                selectImage();
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
        if (downloadUri != null)
            backgroundImageLink = downloadUri.toString();
        else
            backgroundImageLink = "default";

        //updating circles
        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, visibilty, creatorUserID, creatorName,
                category, backgroundImageLink, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(), 0, 0);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateCircle.this);
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
        photo = 0;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
        }
        //check the path for the image
        //if the image path is notnull the uploading process will start
        if (filePath != null) {
            ContentResolver resolver = getContentResolver();
            HelperMethods.compressImage(resolver, filePath);

            //Creating an  custom dialog to show the uploading status
            final ProgressDialog progressDialog = new ProgressDialog(CreateCircle.this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //generating random id to store the backgroundpic
            String id = UUID.randomUUID().toString();
            final StorageReference profileRef = FirebaseWriteHelper.getStorageReference("BackgroundPics/" + id);

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
                    backgroundText.setVisibility(View.GONE);
                    logoHelp.setVisibility(View.GONE);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();
                    FirebaseWriteHelper.updateUserProfile(profileUpdates);
                    Log.d(TAG, "Profile URL: " + downloadUri.toString());
                    Glide.with(CreateCircle.this).load(filePath).into(backgroundPic);
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
        Intent about_intent = new Intent(CreateCircle.this, ExploreTabbedActivity.class);
        startActivity(about_intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}