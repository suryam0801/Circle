package circleapp.circlepackage.circle.Login;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;

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
    private DatabaseReference broadcastsDB, circlesDB, commentsDB, usersDB, tagsDB, locationsDB;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Set<String> locationList;
    ProgressDialog progressDialog;
    SharedPreferences pref;
    String Name, contact, userId;
    EditText name;
    Button register;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    String avatar;
    RuntimePermissionHelper runtimePermissionHelper;
    RelativeLayout setProfile;
    int photo;


    //location services elements
    private FusedLocationProviderClient client;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private String ward, district, temp;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        locationsDB = database.getReference("Locations");
        register = findViewById(R.id.registerButton);
        Button profilepicButton = findViewById(R.id.profilePicSetterImage);
        progressDialog = new ProgressDialog(GatherUserDetails.this);
        progressDialog.setTitle("Registering User....");
        avatar = "";
        photo = 0;
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

        readLocationDB();

        runtimePermissionHelper = new RuntimePermissionHelper(GatherUserDetails.this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        //listener for button to add the profilepic
        avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar1);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar2);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar8);
                HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        //listener for button to add the profilepic
        setProfile.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(GatherUserDetails.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GatherUserDetails.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
            if(photo==0)
                selectImage();
        });

        // Listener for Register button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().equals("") || name.getText().toString().isEmpty()) {

                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Name = name.getText().toString();
                    Name = Name.replaceAll("\\s+", " ");
                    contact = pref.getString("key_name5", null);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    //The function to register the Users with their appropriate details
                    progressDialog.show();
                    UserReg();

                }
            }
        });
    }

    public void readLocationDB() {
        locationsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    locationList = (Set<String>) ((HashMap<String, Boolean>) dataSnapshot.getValue()).keySet();
                } else {
                    locationList = Collections.<String>emptySet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(GatherUserDetails.this);
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
        if(runtimePermissionHelper.isPermissionAvailable(READ_EXTERNAL_STORAGE)){
            builder.show();
        }
    }

    //Check whether the permission is granted or not for uploading the profile pic
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(photo==1)
                takePhoto();
            else
                selectImage();
        } else {
            Toast.makeText(GatherUserDetails.this,
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
            downloadUri = filePath;
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = downloadUri;
        }
            //check the path for the image
            //if the image path is notnull the uploading process will start

            if (filePath != null) {
                ContentResolver resolver = getContentResolver();
                HelperMethods.compressImage(resolver, filePath);
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
                        Glide.with(GatherUserDetails.this).load(filePath).into(profilePic);
                        filePath = null;
                        for (int i = 0; i < 8; i++) {
                            avatarBgList[i].setVisibility(View.INVISIBLE);
                        }

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
        Log.d(TAG, "User reg called");
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
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                            //Adding the user to collection
                            if (!locationList.contains(district))
                                createInitialCircles();

                            addUser();
                            Log.d(TAG, "User Registered success fully added");
                            Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GatherUserDetails.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //to signout the current firebase user
                            firebaseAuth.signOut();
                            //delete the user details
                            firebaseAuth.getCurrentUser().delete();
                        }
                    });

        } else {
            Toast.makeText(GatherUserDetails.this, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    //function that adds the user to the firestore
    private void addUser() {

        usersDB = database.getReference("Users");

        HelperMethods.addDistrict(district);
        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();

        //checking the dowloadUri to store the profile pic
        //if the downloadUri id null then 'default' value is stored
        if (downloadUri != null) {
            //creaeting the user object
            Log.d(TAG, "DownloadURI ::" + downloadUri);
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null", true);
            user = new User(Name, contact, downloadUri.toString(), userId, 0, 0, 0, token_id, ward,
                    district, null, null, null, null);
        } else if (!avatar.equals("")) {
            HashMap<String, Boolean> interestTag = new HashMap<>();
            interestTag.put("null", true);
            Log.d(TAG, "Avatar :: " + avatar);
            user = new User(Name, contact, avatar, userId, 0, 0, 0, token_id, ward, district,
                    null, null, null, null);
        } else {
            user = new User(Name, contact, "default", userId, 0, 0, 0,
                    token_id, ward, district, null, null, null, null);
        }
        //storing user as a json in file locally
        String string = new Gson().toJson(user);
        SessionStorage.saveUser(GatherUserDetails.this, user);
        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(task -> {
            Log.d(TAG, "User data success fully added realtime db");

            Log.d(TAG, "User data success fully added");
            progressDialog.cancel();
            Intent i = new Intent(GatherUserDetails.this, ExploreTabbedActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            Log.d(TAG, "Intent lines are executed...");
            SendNotification.sendnotification("new_user","adminCircle","Meet the developers of Circle",firebaseAuth.getCurrentUser().getUid());
//            sendnotify();
            db.collection("Users")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed to create user", Toast.LENGTH_LONG).show();
                        }
                    });
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

    private void createInitialCircles() {
/*
        //admin circle
        String adminCircleId, adminNormalBroadcastId, adminPollBroadcastId;
        adminCircleId = HelperMethods.createCircle("Meet The Developers", "Get started by joining this circle to connect with the creators and get a crashcourse on how to use The Circle App.",
                "Automatic", "The Circle Team", "Admin World", 2, 0, "The Circle App");

        adminNormalBroadcastId = HelperMethods.createMessageBroadcast("Hi guys, Welcome to Circle", "Use this app to form circles " +
                "to find people around you that enjoy doing the same things as you. Organise events, make announcements and get " +
                "opinions. All on a single platform!", "Admin", 1, 0, adminCircleId);

        HashMap<String, Integer> adminPollOptions = new HashMap<>(); //creating poll options
        adminPollOptions.put("This app is amazing!", 0);
        adminPollOptions.put("I'd like to see some changes", 0);
        adminPollOptions.put("meh :D", 0);
        adminPollBroadcastId = HelperMethods.createPollBroadcast("Use polls like this to quickly get your friends’ opinion about something!", "Admin",
                2, adminPollOptions, null, 0, adminCircleId);
*/

        //quarantine circle
        String quarantineCircleId, quarantineNormalBroadcastId, quarantinePollBroadcastId;
        quarantineCircleId = HelperMethods.createCircle("Quarantine Talks " + district, "Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle",
                "Automatic", "Vijay Ram", district, 2, 0, "Community Discussion");

        quarantineNormalBroadcastId = HelperMethods.createMessageBroadcast("Welcome All! Stay Safe!","Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we" +
                        " can help eachother stay safe and clarify any doubts in these uncertain times :)", "Mekkala Nair", 1,
                0,quarantineCircleId);

        HashMap<String, Integer> quarantinePollOptions = new HashMap<>(); //creating poll options
        quarantinePollOptions.put("Lets find out at 8 PM", 0);
        quarantinePollOptions.put("Never :(", 0);
        quarantinePollOptions.put("Soon? Please be soon!", 0);
        quarantinePollBroadcastId = HelperMethods.createPollBroadcast("How much longer do you guys think our PM will extend lockdown?", "Jacob Abraham",
                2, quarantinePollOptions,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/ProfilePics%2F4903a0a2-fc75-4c33-b417-98e45a8f6332?alt=media&token=aa71896f-90a9-4e2c-8322-5e04fac8ba56"
                , 0, quarantineCircleId);

        //students circle
        String studentsCircleId, studentsNormalBroadcastId, studentsPollBroadcastId;
        studentsCircleId = HelperMethods.createCircle(district + " Students Hangout!", "Lets use this circle to unite all students in " + district + ". Voice your problems, " +
                "questions, or anything you need support with. You will never walk alone!", "Automatic", "Srinithi",
                district, 0, 0, "Students & Clubs");

        studentsNormalBroadcastId = HelperMethods.createMessageBroadcast("Let's show the unity and power of students!!!", "Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. " +
                "You have the support of all the students from " + district + " here!", "Srinithi", 1, 0, studentsCircleId);

        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);

        studentsPollBroadcastId = HelperMethods.createPollBroadcast("Do you guys think we will have exams?", "Vijai VJR", 1,
                pollOptionsStudentsCircle,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/ProfilePics%2Fe60bebee-7141-47a0-a502-bf018a8fe31c?alt=media&token=be032bf6-511c-4757-8451-8b7c852f3cdb",
                0, studentsCircleId);
    }

}