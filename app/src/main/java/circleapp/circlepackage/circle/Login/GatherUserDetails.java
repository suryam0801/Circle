package circleapp.circlepackage.circle.Login;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Notification.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;

public class GatherUserDetails extends AppCompatActivity implements View.OnKeyListener {

    private String TAG = GatherUserDetails.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private Uri filePath;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Uri downloadUri;
    private CircleImageView profilePic;
    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB, circlesDB, commentsDB, usersDB;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    SharedPreferences pref;
    String Name, contact, userId;
    EditText name;
    TextView resetprofpic;
    Button  register;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7;
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg;
    AnalyticsLogEvents analyticsLogEvents;
    String avatar;


    //location services elements
    private FusedLocationProviderClient client;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private String ward, district;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_user_details);
        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        //Getting the instance and references
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        name = findViewById(R.id.name);
        register = findViewById(R.id.registerButton);
        resetprofpic = findViewById(R.id.resetTV);
        Button profilepicButton = findViewById(R.id.profilePicSetterImage);
        progressDialog = new ProgressDialog(GatherUserDetails.this);
        progressDialog.setTitle("Registering User....");
        avatar = "";
        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);
        avatar5 = findViewById(R.id.avatar5);
        avatar6 = findViewById(R.id.avatar6);
        avatar7 = findViewById(R.id.avatar7);
        avatar1_bg = findViewById(R.id.avatar1_State);
        avatar2_bg = findViewById(R.id.avatar2_State);
        avatar3_bg = findViewById(R.id.avatar3_State);
        avatar4_bg = findViewById(R.id.avatar4_State);
        avatar5_bg = findViewById(R.id.avatar5_State);
        avatar6_bg = findViewById(R.id.avatar6_State);
        avatar7_bg = findViewById(R.id.avatar7_State);
        profilePic = findViewById(R.id.profile_image);
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");
        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(GatherUserDetails.this);
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
                downloadUri =null;
                    avatar1.setPressed(true);
                int visibility = avatar1_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar1_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar1.setPressed(false);
                }
                else
                {
                    avatar1_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar2.setPressed(false);
                    avatar3.setPressed(false);
                    avatar4.setPressed(false);
                    avatar5.setPressed(false);
                    avatar6.setPressed(false);
                    avatar7.setPressed(false);
                }
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                    avatar = String.valueOf(R.drawable.avatar2);
                downloadUri =null;
                    avatar2.setPressed(true);
                int visibility = avatar1_bg.getVisibility();
                int visibility2  = avatar2_bg.getVisibility();
                if(visibility2 == View.VISIBLE  )
                {
                    avatar2_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar2.setPressed(false);
                }
                else
                {
                    avatar2_bg.setVisibility(View.VISIBLE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar1.setPressed(false);
                    avatar3.setPressed(false);
                    avatar4.setPressed(false);
                    avatar5.setPressed(false);
                    avatar6.setPressed(false);
                    avatar7.setPressed(false);

                }
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                downloadUri =null;
                avatar3.setPressed(true);
                int visibility = avatar3_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar3_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar3.setPressed(false);
                }
                else
                {
                    avatar3_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar2.setPressed(false);
                    avatar1.setPressed(false);
                    avatar4.setPressed(false);
                    avatar5.setPressed(false);
                    avatar6.setPressed(false);
                    avatar7.setPressed(false);
                }
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                downloadUri =null;
                avatar4.setPressed(true);
                int visibility = avatar4_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar4_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar4.setPressed(false);
                }
                else
                {
                    avatar4_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar2.setPressed(false);
                    avatar3.setPressed(false);
                    avatar1.setPressed(false);
                    avatar5.setPressed(false);
                    avatar6.setPressed(false);
                    avatar7.setPressed(false);
                }
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                downloadUri =null;
                avatar5.setPressed(true);
                int visibility = avatar5_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar5_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar5.setPressed(false);
                }
                else
                {
                    avatar5_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar2.setPressed(false);
                    avatar3.setPressed(false);
                    avatar4.setPressed(false);
                    avatar1.setPressed(false);
                    avatar6.setPressed(false);
                    avatar7.setPressed(false);
                }
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                downloadUri =null;
                avatar6.setPressed(true);
                int visibility = avatar6_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar6_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar6.setPressed(false);
                }
                else
                {
                    avatar6_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar7_bg.setVisibility(View.GONE);
                    avatar2.setPressed(false);
                    avatar3.setPressed(false);
                    avatar4.setPressed(false);
                    avatar5.setPressed(false);
                    avatar1.setPressed(false);
                    avatar7.setPressed(false);
                }
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                avatar7.setPressed(true);
                downloadUri =null;
                int visibility = avatar7_bg.getVisibility();
                if(visibility == View.VISIBLE)
                {
                    avatar7_bg.setVisibility(View.GONE);
                    avatar = "";
                    avatar7.setPressed(false);
                }
                else
                {
                    avatar7_bg.setVisibility(View.VISIBLE);
                    avatar2_bg.setVisibility(View.GONE);
                    avatar1_bg.setVisibility(View.GONE);
                    avatar3_bg.setVisibility(View.GONE);
                    avatar4_bg.setVisibility(View.GONE);
                    avatar5_bg.setVisibility(View.GONE);
                    avatar6_bg.setVisibility(View.GONE);
                    avatar1.setPressed(false);
                    avatar2.setPressed(false);
                    avatar3.setPressed(false);
                    avatar4.setPressed(false);
                    avatar5.setPressed(false);
                    avatar6.setPressed(false);
                }
            }
        });
        //listener for button to add the profilepic
        profilepicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)) {

                    selectFile();
                } else {
                    analyticsLogEvents.logEvents(GatherUserDetails.this, "storage_off","asked_permission", "gather_user_details");
                    runtimePermissionHelper.requestPermissionsIfDenied(READ_EXTERNAL_STORAGE);
                }

            }
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

    public void selectFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFile();
                analyticsLogEvents.logEvents(GatherUserDetails.this,"storage_granted","permission_granted","gather_user_details");
        } else {
                Toast.makeText(GatherUserDetails.this,
                        "Storage Permission Denied",
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
//            db.collection("Users")
//                    .document(userId)
//                    .set(user)
//                    .addOnSuccessListener(aVoid -> {
////                        progressDialog.cancel();
//
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(getApplicationContext(), "Failed to create user", Toast.LENGTH_LONG).show();
//                        }
//                    });
//            finish();
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
    private void createInitialCirlces() {
        analyticsLogEvents.logEvents(GatherUserDetails.this,"default_circles_added","new_location","gather_user_details");
        broadcastsDB = database.getReference("Broadcasts");
        circlesDB = database.getReference("Circles");
        commentsDB = database.getReference("BroadcastComments");

        //admin circle
        HashMap<String, Boolean> circleIntTags = new HashMap<>();
        circleIntTags.put("sample", true);
        Circle adminCircle = new Circle("adminCircle", "Meet the developers of Circle",
                "Get started by joining this circle to connect with the creators and get a crashcourse on how to use The Circle App.",
                "automatic", "CreatorAdmin", "The Circle Team", circleIntTags,
                null, null, "test", null, System.currentTimeMillis(), 2, 0);

        HashMap<String, Integer> pollOptions = new HashMap<>(); //creating poll options
        pollOptions.put("It's going to rain tomorrow", 0);
        pollOptions.put("No way, its dry as a dog biscuit", 0);
        Poll adminPoll = new Poll("Use polls like this to quickly get your friends’ opinion about something!", pollOptions, null);
        Broadcast commentBroadcast = new Broadcast("commentBroadcast", "You can have a discussion about your posts down in the " +
                "comments below. Click on Go to discussion to see the secret message. :)", null, "Jacob",
                "AdminId", false, (System.currentTimeMillis() - 1), null, "default", 0, 0);
        Broadcast pollBroadcast = new Broadcast("pollBroadcast", null, null, "Abrar", "AdminId", true,
                System.currentTimeMillis(), adminPoll, "default", 0, 0);
        Broadcast introBroadcast = new Broadcast("introBroadcast", "Welcome to Circle! Your friendly neighborhood app. Form circles " +
                "to find people around you that enjoy doing the same things as you. Organise events, make announcements and get " +
                "opinions - all on a single platform.", null, "Surya", "AdminId", false,
                (System.currentTimeMillis() + 1), null, "default", 0, 0);

        Comment comment = new Comment("Srinithi", "The answer to life is not 42. It's the bonds you build " +
                "around your circle.",
                "adminCommentId", null, System.currentTimeMillis());

        circlesDB.child("adminCircle").setValue(adminCircle);
        broadcastsDB.child("adminCircle").child("introBroadcast").setValue(introBroadcast);
        broadcastsDB.child("adminCircle").child("pollBroadcast").setValue(pollBroadcast);
        broadcastsDB.child("adminCircle").child("commentBroadcast").setValue(commentBroadcast);
        commentsDB.child("adminCircle").child("commentBroadcast").child("adminCommentId").setValue(comment);

        //running circle
        String runningCircleID = UUID.randomUUID().toString();
        String runningBroadcastID = UUID.randomUUID().toString();
        String runningBroadcasPollID = UUID.randomUUID().toString();
        String runningCommentID = UUID.randomUUID().toString();
        Circle runningCircle = new Circle(runningCircleID, "Morning Runner's " + district,
                "Hi guys, i would love to form a morning running group for anybody in " + district + ". Please join if you would like to be part of this friendly runner's circle",
                "automatic", "CreatorAdmin", "Vijay Ram", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 2, 0);
        HashMap<String, Integer> pollOptionsRunningCircle = new HashMap<>(); //creating poll options
        pollOptionsRunningCircle.put("Sure!", 0);
        pollOptionsRunningCircle.put("Thats too early :(", 0);
        Poll runningPoll = new Poll("Hey guys! Can we go running every friday early in the morning?", pollOptionsRunningCircle, null);
        Broadcast runnersBroadcastMessage = new Broadcast(runningBroadcastID, "Hi all! This is a group to find mates to go on daily runs with. Runners of all levels welcome!", null, "Vijay Ram", "AdminId", false,
                System.currentTimeMillis(), null, "default", 0, 0);
        Broadcast runnersBroadcastPoll = new Broadcast(runningBroadcasPollID, null, null, "Vijay Ram", "AdminId", true,
                System.currentTimeMillis(), runningPoll, "default", 0, 0);
        Comment runnerComment = new Comment("Madhu mitha", "Hey where do you guys go running?",
                runningCommentID, null, System.currentTimeMillis());
        circlesDB.child(runningCircleID).setValue(runningCircle);
        broadcastsDB.child(runningCircleID).child(runningBroadcastID).setValue(runnersBroadcastMessage);
        broadcastsDB.child(runningCircleID).child(runningBroadcasPollID).setValue(runnersBroadcastPoll);
        commentsDB.child(runningCircleID).child(runningBroadcastID).child(runningCommentID).setValue(runnerComment);

        //students circle
        String studentsCircleID = UUID.randomUUID().toString();
        String studentsBroadcastID = UUID.randomUUID().toString();
        String studentsBroadcastPollID = UUID.randomUUID().toString();
        String studentsCommentID = UUID.randomUUID().toString();
        String studentsCommentIDResponse = UUID.randomUUID().toString();
        Circle cookingCircle = new Circle(studentsCircleID, district + " Students Hangout!",
                "Lets use this circle to unite all students in " + district + ". Voice your problems, questions, or anything you need support with. You will never walk alone!",
                "automatic", "CreatorAdmin", "Malavika Kumar", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 2, 0);
        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);
        Poll cookingPoll = new Poll("Are you guys still preparing for exams?", pollOptionsStudentsCircle, null);
        Broadcast studentBroadcast = new Broadcast(studentsBroadcastID, "Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. You have the support of all the students from " + district + " here!", null, "Mekkala Nair", "AdminId", false,
                System.currentTimeMillis(), null, "default", 0, 0);
        Broadcast studentBroadcastPoll = new Broadcast(studentsBroadcastPollID, null, null, "Mekkala Nair", "AdminId", true,
                System.currentTimeMillis(), cookingPoll, "default", 0, 0);
        Comment studentComment = new Comment("Arijit Samuel", "Can i post promotions for my college events here?",
                studentsCommentID, null, (System.currentTimeMillis() - (1800 * 1000)));
        Comment studentCommentResponse = new Comment("Mekkala Nair", "Yeah that's not a problem!",
                studentsCommentIDResponse, null, System.currentTimeMillis());
        circlesDB.child(studentsCircleID).setValue(cookingCircle);
        broadcastsDB.child(studentsCircleID).child(studentsBroadcastID).setValue(studentBroadcast);
        broadcastsDB.child(studentsCircleID).child(studentsBroadcastPollID).setValue(studentBroadcastPoll);
        commentsDB.child(studentsCircleID).child(studentsBroadcastID).child(studentsCommentID).setValue(studentComment);
        commentsDB.child(studentsCircleID).child(studentsBroadcastID).child(studentsCommentIDResponse).setValue(studentCommentResponse);

        //quarantine circle
        String quarantineCircleID = UUID.randomUUID().toString();
        String quarantineBroadcastID = UUID.randomUUID().toString();
        String quarantineBroadcastPollID = UUID.randomUUID().toString();
        String quarantineCommentID = UUID.randomUUID().toString();
        Circle quarantineCircle = new Circle(quarantineCircleID,  "Quarantine Talks " + district,
                "Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle",
                "automatic", "CreatorAdmin", "Surya Manivannan", circleIntTags,
                null, null, district, ward, System.currentTimeMillis(), 2, 0);
        HashMap<String, Integer> pollOptionsQuarantineCircle = new HashMap<>(); //creating poll options
        pollOptionsQuarantineCircle.put("1 month", 0);
        pollOptionsQuarantineCircle.put("2 months", 0);
        pollOptionsQuarantineCircle.put("3 months", 0);
        pollOptionsQuarantineCircle.put("haven't even started", 0);
        Poll quarantinePoll = new Poll("How long have you been in quarantine?", pollOptionsQuarantineCircle, null);
        Broadcast quarantineBroadcast = new Broadcast(quarantineBroadcastID, "Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we can help eachother stay safe and clarify any doubts in these uncertain times :)", null, "Mekkala Nair", "AdminId", false,
                (System.currentTimeMillis() - (1800 * 1000)), null, "default", 0, 0);
        Broadcast quarantineBroadcastPoll = new Broadcast(quarantineBroadcastPollID, null, null, "Mekkala Nair", "AdminId", true,
                System.currentTimeMillis(), quarantinePoll, "default", 0, 0);
        Comment quarantineComment = new Comment("Nithin M", "Where are you guys buying your essentials?",
                quarantineCommentID, null, (System.currentTimeMillis()));
        circlesDB.child(quarantineCircleID).setValue(quarantineCircle);
        broadcastsDB.child(quarantineCircleID).child(quarantineBroadcastID).setValue(quarantineBroadcast);
        broadcastsDB.child(quarantineCircleID).child(quarantineBroadcastPollID).setValue(quarantineBroadcastPoll);
        commentsDB.child(quarantineCircleID).child(quarantineBroadcastID).child(quarantineCommentID).setValue(quarantineComment);
    }

}

