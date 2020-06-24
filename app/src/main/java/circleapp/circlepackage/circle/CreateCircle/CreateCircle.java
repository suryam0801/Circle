package circleapp.circlepackage.circle.CreateCircle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.ObjectModels.Circle;
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
    private TextView typePrompt, logoHelp, backgroundText;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton acceptanceButton;
    private Button typeInfoButton;
    private User user;
    private TextView categoryName;
    private RelativeLayout addLogo;
    private CircleImageView backgroundPic;
    private Uri filePath, downloadUri;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    RuntimePermissionHelper runtimePermissionHelper;
    int photo;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase database;
    private DatabaseReference tags, circleDB, userDB;
    private FirebaseAuth currentUser;
    private String backgroundImageLink;

    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_create_circle);

        analyticsLogEvents = new AnalyticsLogEvents();
        user = SessionStorage.getUser(CreateCircle.this);
        photo = 0;
        backgroundImageLink = "";
        firebaseAuth = FirebaseAuth.getInstance();
        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        typeInfoButton = findViewById(R.id.circle_type_info_button);
        typePrompt = findViewById(R.id.circle_type_tip_prompt);
        circleNameEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        circleDescriptionEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        backgroundPic = findViewById(R.id.background_image);
        addLogo = findViewById(R.id.backgroundPreview);
        categoryName = findViewById(R.id.category_name);
        categoryName.setText(getIntent().getStringExtra("category_name"));
        //to set invisible on adding image
        logoHelp = findViewById(R.id.logo_help);
        backgroundText = findViewById(R.id.backgroundText);
        runtimePermissionHelper = new RuntimePermissionHelper(CreateCircle.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        analyticsLogEvents.logEvents(CreateCircle.this, "create_circle", "new_circle","on_button_click");

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users").child(user.getUserId());

        btn_createCircle.setOnClickListener(view -> {
            String cName = circleNameEntry.getText().toString().trim();
            String cDescription = circleDescriptionEntry.getText().toString().trim();
                if (!cName.isEmpty() && !cDescription.isEmpty()) {
                    analyticsLogEvents.logEvents(CreateCircle.this, "created_circle", "new_circle_created","on_button_click");
                    createCircle(cName, cDescription);
                } else {
                    Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
                }
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(CreateCircle.this, ExploreTabbedActivity.class));
            finish();
        });

        typeInfoButton.setOnClickListener(view -> typePrompt.setVisibility(View.VISIBLE));
        addLogo.setOnClickListener(v->{
            photo = 2;
            if (ContextCompat.checkSelfPermission(CreateCircle.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateCircle.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
            selectImage();
        });
    }

    public void createCircle(String cName, String cDescription) {

        User user = SessionStorage.getUser(CreateCircle.this);
        circleDB = database.getReference("Circles");
        currentUser = FirebaseAuth.getInstance();
        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);

        String category = getIntent().getStringExtra("category_name");

        String myCircleID = circleDB.push().getKey();
        String creatorUserID = currentUser.getCurrentUser().getUid();
        String acceptanceType = acceptanceButton.getText().toString();
        if(acceptanceType.equals("Public"))
            acceptanceType = "Automatic";
        else if(acceptanceType.equals("Private"))
            acceptanceType = "Review";

        String creatorName = currentUser.getCurrentUser().getDisplayName();

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);
        if(downloadUri!=null)
            backgroundImageLink = downloadUri.toString();
        else
            backgroundImageLink = "default";

        //updating circles
        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, creatorUserID, creatorName,
                category,backgroundImageLink, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(),0, 0);

        circleDB.child(myCircleID).setValue(circle);

        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        SessionStorage.saveUser(CreateCircle.this, user);
        SessionStorage.saveCircle(CreateCircle.this, circle);
        userDB.child("createdCircles").setValue(currentCreatedNo);

        //navigate back to explore. new circle will be available in workbench
        Intent intent = new Intent(CreateCircle.this, CircleWall.class);
        intent.putExtra("fromCircleWall", true);
        startActivity(intent);
        finish();
    }
        public void selectFile(){

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        }
        public void takePhoto(){
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            downloadUri = HelperMethods.getImageUri();
            m_intent.putExtra(MediaStore.EXTRA_OUTPUT, downloadUri);
            startActivityForResult(m_intent, REQUEST_IMAGE_CAPTURE);
        }
        private void selectImage() {
            final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateCircle.this);
            builder.setTitle("Add Photo!");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Take Photo"))
                    {
                        photo = 1;
                        if (!runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)){
                            runtimePermissionHelper.askPermission(READ_EXTERNAL_STORAGE);
                        }
                        if (runtimePermissionHelper.isPermissionAvailable(CAMERA)&&runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)){
                            takePhoto();
                        }
                        else{
                            runtimePermissionHelper.askPermission(CAMERA);
                        }
                    }
                    else if (options[item].equals("Choose from Gallery"))
                    {
                        photo = 0;
                        if (runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {
                            selectFile();
                        } else {
                            runtimePermissionHelper.requestPermissionsIfDenied(READ_EXTERNAL_STORAGE);
                        }

                    }
                    else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }
    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(photo==0){
                selectFile();
            }
            else if(photo==1){
                if(runtimePermissionHelper.isPermissionAvailable(CAMERA))
                    takePhoto();
            }
        } else {
            Toast.makeText(CreateCircle.this,
                    "Permission Denied",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            //check the path for the image
            //if the image path is notnull the uploading process will start
            ContentResolver resolver = getContentResolver();
            HelperMethods.compressImage(resolver, filePath);
            if (filePath != null) {

                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(CreateCircle.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the backgroundpic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("BackgroundPics/" + id);

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
                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        Glide.with(CreateCircle.this).load(downloadUri.toString()).into(backgroundPic);

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
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
            //check the path for the image
            //if the image path is notnull the uploading process will start
            ContentResolver resolver = getContentResolver();
            HelperMethods.compressImage(resolver, filePath);
            if (filePath != null) {


                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(CreateCircle.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("BackgroundPics/" + id);

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
                        Log.d("test1",""+downloadUri);
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        Glide.with(CreateCircle.this).load(downloadUri.toString()).into(backgroundPic);
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
    }
    @Override
    public void onBackPressed() {
        Intent about_intent = new Intent(CreateCircle.this, ExploreTabbedActivity.class);
        analyticsLogEvents.logEvents(CreateCircle.this, "create_circle", "new_circle_bounce","on_back_press");
        startActivity(about_intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}