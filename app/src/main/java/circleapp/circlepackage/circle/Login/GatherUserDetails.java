package circleapp.circlepackage.circle.Login;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Notification.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;

public class GatherUserDetails extends AppCompatActivity implements View.OnKeyListener {

    private String TAG = GatherUserDetails.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private Uri downloadUri;
    private CircleImageView profilePic;
    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB, circlesDB, commentsDB, usersDB, tagsDB;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    SharedPreferences pref;
    String Name, contact, userId;
    EditText name;
    TextView resetprofpic;
    Button  register;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7,avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    AnalyticsLogEvents analyticsLogEvents;
    String avatar;
    RuntimePermissionHelper runtimePermissionHelper;
    RelativeLayout setProfile;
    int photo;

    //location services elements
    private FusedLocationProviderClient client;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private String ward, district,temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_user_details);
        //Getting the instance and references
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];
        storageReference = FirebaseStorage.getInstance().getReference();
        name = findViewById(R.id.name);
        register = findViewById(R.id.registerButton);
        resetprofpic = findViewById(R.id.resetTV);
        Button profilepicButton = findViewById(R.id.profilePicSetterImage);
        progressDialog = new ProgressDialog(GatherUserDetails.this);
        progressDialog.setTitle("Registering User....");
        photo = 0;
        avatar = "";
        avatarList[0] = avatar1 = findViewById(R.id.avatar1);
        avatarList[1] = avatar2 = findViewById(R.id.avatar2);
        avatarList[2] = avatar3 = findViewById(R.id.avatar3);
        avatarList[3] = avatar4 = findViewById(R.id.avatar4);
        avatarList[4] = avatar5 = findViewById(R.id.avatar5);
        avatarList[5] = avatar6 = findViewById(R.id.avatar6);
        avatarList[6] = avatar7 = findViewById(R.id.avatar7);
        avatarList[7] = avatar8 = findViewById(R.id.avatar8);
        avatarBgList[0] = avatar1_bg = findViewById(R.id.avatar1_State);
        avatarBgList[1] = avatar2_bg = findViewById(R.id.avatar2_State);
        avatarBgList[2] = avatar3_bg = findViewById(R.id.avatar3_State);
        avatarBgList[3] = avatar4_bg = findViewById(R.id.avatar4_State);
        avatarBgList[4] = avatar5_bg = findViewById(R.id.avatar5_State);
        avatarBgList[5] = avatar6_bg = findViewById(R.id.avatar6_State);
        avatarBgList[6] = avatar7_bg = findViewById(R.id.avatar7_State);
        avatarBgList[7] = avatar8_bg = findViewById(R.id.avatar8_State);
        profilePic = findViewById(R.id.profile_image);
        setProfile = findViewById(R.id.imagePreview);
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        runtimePermissionHelper = new RuntimePermissionHelper(GatherUserDetails.this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        analyticsLogEvents = new AnalyticsLogEvents();

        resetprofpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        //listener for button to add the profilepic
        avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar1);
                setProfilePicMethod(avatar,avatar1_bg,avatar1);
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                    avatar = String.valueOf(R.drawable.avatar2);
                setProfilePicMethod(avatar,avatar2_bg,avatar2);
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                setProfilePicMethod(avatar,avatar3_bg,avatar3);
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                setProfilePicMethod(avatar,avatar4_bg,avatar4);
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                setProfilePicMethod(avatar,avatar5_bg,avatar5);
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                setProfilePicMethod(avatar,avatar6_bg,avatar6);
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                setProfilePicMethod(avatar,avatar7_bg,avatar7);
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar8);
                setProfilePicMethod(avatar,avatar8_bg,avatar8);
            }
        });
        //listener for button to add the profilepic
        setProfile.setOnClickListener(v -> {
                selectImage();
        });

        // Listener for Register button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( avatar != "" || downloadUri != null){

                    if(name.getText().equals("") || name.getText().toString().isEmpty()){

                        Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        analyticsLogEvents.logEvents(GatherUserDetails.this, "entered_name", "name_success","gather_user_details");
                        Name = name.getText().toString();
                        contact = pref.getString("key_name5", null);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        //The function to register the Users with their appropriate details
                        progressDialog.show();
                        analyticsLogEvents.logEvents(GatherUserDetails.this,"registration_success","user_registered","gather_user_details");
                        UserReg();
                        analyticsLogEvents.logEvents(GatherUserDetails.this,ward.trim(),district.trim(),"gather_user_details");

                    }
                }
                else
                    {
                        Toast.makeText(getApplicationContext(), "Select a Profile Picture to Continue....", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }
    public void setProfilePicMethod(String avatar, ImageView avatarBg, ImageButton avatarButton){
        HelperMethods.GlideSetProfilePic(GatherUserDetails.this,avatar, profilePic);
        downloadUri =null;
        avatarButton.setPressed(true);
        int visibility = avatarBg.getVisibility();
        if(visibility == View.VISIBLE)
        {
            HelperMethods.GlideSetProfilePic(GatherUserDetails.this,String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
            avatarBg.setVisibility(View.GONE);
            avatar = "";
            avatarButton.setPressed(false);
        }
        else
        {
            for(int i=0; i<8;i++){
                if(avatarList[i]!=avatarButton){
                    avatarBgList[i].setVisibility(View.GONE);
                    avatarList[i].setPressed(false);
                }
                else
                    avatarBgList[i].setVisibility(View.VISIBLE);
            }
        }
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
        downloadUri = getImageUri();
        m_intent.putExtra(MediaStore.EXTRA_OUTPUT, downloadUri);
        startActivityForResult(m_intent, REQUEST_IMAGE_CAPTURE);
    }
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(GatherUserDetails.this);
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
                        analyticsLogEvents.logEvents(GatherUserDetails.this, "storage_off","asked_permission", "gather_user_details");
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
    private Uri getImageUri(){
        Uri m_imgUri = null;
        File m_file;
        try {
            SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String m_curentDateandTime = m_sdf.format(new Date());
            String m_imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + m_curentDateandTime + ".jpg";
            m_file = new File(m_imagePath);
            m_imgUri = Uri.fromFile(m_file);
        } catch (Exception p_e) {
        }
        return m_imgUri;
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
                analyticsLogEvents.logEvents(GatherUserDetails.this,"storage_granted","permission_granted","gather_user_details");
        } else {
                Toast.makeText(GatherUserDetails.this,
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
            Log.d("test2",""+filePath);

            //check the path for the image
            //if the image path is notnull the uploading process will start
            if (filePath != null) {

                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(GatherUserDetails.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("ProfilePics/" + id);

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
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        Glide.with(GatherUserDetails.this).load(downloadUri.toString()).into(profilePic);

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                analyticsLogEvents.logEvents(GatherUserDetails.this,"pic_upload_fail","device_error","gather_user_details");

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
            Log.d("test2",""+filePath);
            //check the path for the image
            //if the image path is notnull the uploading process will start
            if (filePath != null) {


                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(GatherUserDetails.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("ProfilePics/" + id);

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
                        Log.d("test1",""+downloadUri);
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                        Log.d(TAG, "Profile URL: " + downloadUri.toString());
                        Glide.with(GatherUserDetails.this).load(downloadUri.toString()).into(profilePic);

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                analyticsLogEvents.logEvents(GatherUserDetails.this,"pic_capture_fail","device_error","gather_user_details");

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        firebaseAuth.signOut();

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        EditText myEditText = (EditText) v;

        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

        }
        return false; // pass on to other listeners.

    }
    public void UserReg() {
        Log.d(TAG,"User reg called");
        //Ensure the textboxes are not empty
        if (!TextUtils.isEmpty(Name)) {
            //getting the current user id
            userId = firebaseAuth.getInstance().getCurrentUser().getUid();

            //Merging the fname and lname to set the displayname to the user for easy access
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(Name)
                    .build();

            //update the user display name
            firebaseAuth.getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                                //Adding the user to collection
                                addUser();
                                Log.d(TAG,"User Registered success fully added");
                                Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(GatherUserDetails.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                //to signout the current firebase user
                                firebaseAuth.signOut();
                                //delete the user details
                                firebaseAuth.getCurrentUser().delete();
                            }
                        }
                    });

        } else {
            Toast.makeText(GatherUserDetails.this, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    //function that adds the user to the firestore
    private void addUser() {

        usersDB = database.getReference("Users");

        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();


        //checking the dowloadUri to store the profile pic
        //if the downloadUri id null then 'default' value is stored
        if (downloadUri != null) {
            //creaeting the user object
            Log.d(TAG,"DownloadURI ::"+downloadUri);
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null",true);
            user = new User(Name, contact, downloadUri.toString(),interestTag, userId, 0, 0, 0, token_id, ward, district, null, null, 0);
        } else
            {
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null",true);
            Log.d(TAG,"Avatar :: "+avatar);
            user = new User(Name, contact, avatar, interestTag, userId, 0, 0, 0, token_id, ward, district, null, null, 0);
        }
        //storing user as a json in file locally
        String string = new Gson().toJson(user);
        SessionStorage.saveUser(GatherUserDetails.this, user);
        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(task -> {
            Log.d(TAG,"User data success fully added realtime db");

            Log.d(TAG,"User data success fully added");
            progressDialog.cancel();
            Intent i = new Intent(GatherUserDetails.this, ExploreTabbedActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY );
            startActivity(i);
            Log.d(TAG,"Intent lines are executed...");
            SendNotification.sendnotification("new_user","adminCircle","Meet the developers of Circle",firebaseAuth.getCurrentUser().getUid());
            sendnotify();
            finish();
        });
    }

    private void sendnotify() {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Circle")
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.circle_logo)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentText("Welcome to the Circle " + firebaseAuth.getCurrentUser().getDisplayName() +
                                " You can find the people with same Interest in your Locality");


        Intent notificationIntent = new Intent(this, ExploreTabbedActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
    /*private void createInitialCirlces() {
        analyticsLogEvents.logEvents(GatherUserDetails.this,"default_circles_added","new_location","gather_user_details");

        //admin circle
        String circleId = HelperMethods.createCircle("Meet the developers of Circle","Get started by joining this circle to connect with the creators and get a crashcourse on how to use The Circle App.","automatic","The Circle Team", district, 2, 0);
        HashMap<String, Integer> pollOptions = new HashMap<>(); //creating poll options
        pollOptions.put("It's going to rain tomorrow", 0);
        pollOptions.put("No way, its dry as a dog biscuit", 0);
        String broadcastId1 = HelperMethods.createPollBroadcast("Use polls like this to quickly get your friends’ opinion about something!","Abrar",-1,pollOptions,0,circleId);

        String broadcastId2 = HelperMethods.createBroadcast("You can have a discussion about your posts down in the " +
                "comments below. Click on Go to discussion to see the secret message. :)","Jacob", -1, 0, circleId);
        String broadcastId3 = HelperMethods.createBroadcast("Welcome to Circle! Your friendly neighborhood app. Form circles " +
                "to find people around you that enjoy doing the same things as you. Organise events, make announcements and get " +
                "opinions - all on a single platform.","Surya", 1,0,circleId);

        HelperMethods.createComment("Srinithi", "The answer to life is not 42. It's the bonds you build " +
                "around your circle.",0,circleId,broadcastId2);

        //running circle
        String circleId2 = HelperMethods.createCircle("Morning Runner's "+district,"Hi guys, i would love to form a morning running group for anybody in " + district + ". Please join if you would like to be part of this friendly runner's circle","automatic","Vijay Ram", district, 2, 0);
        HashMap<String, Integer> pollOptionsRunningCircle = new HashMap<>(); //creating poll options
        pollOptionsRunningCircle.put("Sure!", 0);
        pollOptionsRunningCircle.put("Thats too early :(", 0);
        broadcastId1 = HelperMethods.createBroadcast("Hi all! This is a group to find mates to go on daily runs with. Runners of all levels welcome!", "Vijay Ram",0,0,circleId2);
        broadcastId2 = HelperMethods.createPollBroadcast("Hey guys! Can we go running every friday early in the morning?","Vijay Ram",0,pollOptionsRunningCircle,0, circleId2);
        HelperMethods.createComment("Madhu mitha", "Hey where do you guys go running?",0,circleId2,broadcastId2);

        //students circle
        String circleId3 = HelperMethods.createCircle(district + " Students Hangout!","Lets use this circle to unite all students in " + district + ". Voice your problems, questions, or anything you need support with. You will never walk alone!","automatic","Malavika Kumar",district,2,0);
        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);
        broadcastId1 = HelperMethods.createBroadcast("Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. You have the support of all the students from " + district + " here!","Mekkala Nair",0,0,circleId3);
        broadcastId2 = HelperMethods.createPollBroadcast("Are you guys still preparing for exams?","Mekkala Nair", 0, pollOptionsStudentsCircle, 0,circleId3);
        HelperMethods.createComment("Arijit Samuel","Can i post promotions for my college events here?",-(1800*1000),circleId3,broadcastId1);
        HelperMethods.createComment("Mekkala Nair", "Yeah that's not a problem!",0,circleId3,broadcastId1);

        //quarantine circle
        String circleId4 = HelperMethods.createCircle("Quarantine Talks " + district,"Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle","automatic","Surya Manivannan", district, 2,0);
        HashMap<String, Integer> pollOptionsQuarantineCircle = new HashMap<>(); //creating poll options
        pollOptionsQuarantineCircle.put("1 month", 0);
        pollOptionsQuarantineCircle.put("2 months", 0);
        pollOptionsQuarantineCircle.put("3 months", 0);
        pollOptionsQuarantineCircle.put("haven't even started", 0);
        broadcastId1 = HelperMethods.createBroadcast("Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we can help eachother stay safe and clarify any doubts in these uncertain times :)","Mekkala Nair",-(1800*1000),0,circleId4);
        broadcastId2 = HelperMethods.createPollBroadcast("How long have you been in quarantine?","Mekkala Nair", 0, pollOptionsQuarantineCircle, 0 ,circleId4);
        HelperMethods.createComment("Nithin M", "Where are you guys buying your essentials?",0,circleId4,broadcastId1);
    }*/
}