package circleapp.circlepackage.circle.Login;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.ImagePicker;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.LocationsViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class GatherUserDetails extends AppCompatActivity implements View.OnKeyListener {

    private String TAG = GatherUserDetails.class.getSimpleName();

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private Uri downloadUri;
    private CircleImageView profilePic;
    private FirebaseDatabase database;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean locationExists;
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
    private String ward, district;
    private LoginUserObject loginUserObject;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_user_details);
        //Getting the instance and references
        database = FirebaseDatabase.getInstance();
        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];
        name = findViewById(R.id.name);
        register = findViewById(R.id.registerButton);
        Button profilepicButton = findViewById(R.id.profilePicSetterImage);
        progressDialog = new ProgressDialog(GatherUserDetails.this);
        progressDialog.setTitle("Registering User....");
        avatar = "";
        photo = 0;
        locationExists = false;
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

        setLoginUserObject();

        readLocationDB();

        runtimePermissionHelper = new RuntimePermissionHelper(GatherUserDetails.this);
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        setAvatarOnclickListeners();
        //listener for button to add the profilepic
        setProfile.setOnClickListener(v -> {
                    if (!runtimePermissionHelper.isPermissionAvailable(CAMERA)) {
                        runtimePermissionHelper.requestCameraPermissionsIfDenied(CAMERA);
                    } else {
                        Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
                        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                    }
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

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    //The function to register the Users with their appropriate details
                    progressDialog.show();
                    UserReg();

                }
            }
        });
    }
    private void setLoginUserObject(){
        loginUserObject = SessionStorage.getLoginUserObject(this);
        ward = loginUserObject.getWard();
        district = loginUserObject.getDistrict();
        contact = loginUserObject.getCompletePhoneNumber();
    }
    private void setAvatarOnclickListeners(){
        avatar1.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar1);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar2.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar2);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar3.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar3);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar4.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar4);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar5.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar5);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar6.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar6);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar7.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar7);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
            downloadUri = null;
        });
        avatar8.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar8);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
            downloadUri = null;
        });
    }

    public void readLocationDB() {
        LocationsViewModel viewModel = ViewModelProviders.of(GatherUserDetails.this).get(LocationsViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsLocationsSingleValueLiveData(district);
        liveData.observe(GatherUserDetails.this, dataSnapshot -> {
            if (dataSnapshot.exists()) {
                locationExists=true;
            } else {
                return;
            }
        });
    }

    private void uploadImage(){
        if (filePath != null) {
            ContentResolver resolver = getContentResolver();
            HelperMethods.compressImage(resolver, filePath);
            //Creating an  custom dialog to show the uploading status
            final ProgressDialog progressDialog = new ProgressDialog(GatherUserDetails.this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //generating random id to store the profliepic
            String id = UUID.randomUUID().toString();
            final StorageReference profileRef = FirebaseWriteHelper.getStorageReference("ProfilePics/" + id);

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
                                throw Objects.requireNonNull(task.getException());
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
                    FirebaseWriteHelper.getUser().updateProfile(profileUpdates);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
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
        //super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                downloadUri = ImagePicker.getImageUri(getApplicationContext(),bitmap);
                if(downloadUri!=null){
                    filePath = downloadUri;
                    uploadImage();
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
        FirebaseWriteHelper.signOutAuth();

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void UserReg() {
        Log.d(TAG, "User reg called");
        //Ensure the textboxes are not empty
        if (!TextUtils.isEmpty(Name)) {
            //getting the current user id
            userId = FirebaseWriteHelper.getUser().getUid();

            //Merging the fname and lname to set the displayname to the user for easy access
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(Name)
                    .build();

            //update the user display name
            FirebaseWriteHelper.getAuthToken().getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                            //Adding the user to collection
                            if (!locationExists){
                                FirebaseWriteHelper.addDistrict(district);
                                createInitialCircles();
                            }

                            addUser();
                            Log.d(TAG, "User Registered success fully added");
                            Toast.makeText(GatherUserDetails.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(GatherUserDetails.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //to signout the current firebase user
                            FirebaseWriteHelper.getAuthToken().signOut();
                            //delete the user details
                            FirebaseWriteHelper.getUser().delete();
                        }
                    });

        } else {
            Toast.makeText(GatherUserDetails.this, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    //function that adds the user to the firestore
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addUser() {

        DatabaseReference usersDB = database.getReference("Users");
        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();
        Log.d("token",token_id);

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
        SessionStorage.saveUser(GatherUserDetails.this, user);
        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(task -> {
            sendIntentsToExploreTabbed();
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
            finishAfterTransition();
        });
    }
    private void sendIntentsToExploreTabbed(){
        progressDialog.cancel();
        Intent i = new Intent(GatherUserDetails.this, ExploreTabbedActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
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
        quarantineCircleId = FirebaseWriteHelper.createDefaultCircle("Quarantine Talks " + district, "Figure out how quarantine life is for the rest of " + district + " and ask any questions or help out your neighbors using this circle",
                "Automatic", "Vijay Ram", district, 2, 0, "Community Discussion");

        quarantineNormalBroadcastId = FirebaseWriteHelper.createMessageBroadcast("Welcome All! Stay Safe!","Hey guys lets use this app to connect with our neighborhood in these times of isolation. I hope we" +
                        " can help eachother stay safe and clarify any doubts in these uncertain times :)", "Mekkala Nair", 1,
                0,quarantineCircleId);

        HashMap<String, Integer> quarantinePollOptions = new HashMap<>(); //creating poll options
        quarantinePollOptions.put("Lets find out at 8 PM", 0);
        quarantinePollOptions.put("Never :(", 0);
        quarantinePollOptions.put("Soon? Please be soon!", 0);
        quarantinePollBroadcastId = FirebaseWriteHelper.createPollBroadcast("How much longer do you guys think our PM will extend lockdown?", "Jacob Abraham",
                2, quarantinePollOptions,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/modi-us-2126610f-1481508682.jpg?alt=media&token=5ff4230c-945f-4918-9c21-bff5f90c75e9"
                , 0, quarantineCircleId);

        //students circle
        String studentsCircleId, studentsNormalBroadcastId, studentsPollBroadcastId;
        studentsCircleId = FirebaseWriteHelper.createDefaultCircle(district + " Students Hangout!", "Lets use this circle to unite all students in " + district + ". Voice your problems, " +
                        "questions, or anything you need support with. You will never walk alone!", "Automatic", "Srinithi",
                district, 0, 0, "Students & Clubs");

        studentsNormalBroadcastId = FirebaseWriteHelper.createMessageBroadcast("Let's show the unity and power of students!!!", "Welcome guys! Be respectful and have a good time. This circle will be our safe place from parents, college, school, and tests. " +
                "You have the support of all the students from " + district + " here!", "Srinithi", 1, 0, studentsCircleId);

        HashMap<String, Integer> pollOptionsStudentsCircle = new HashMap<>(); //creating poll options
        pollOptionsStudentsCircle.put("no! it will get cancelled!", 0);
        pollOptionsStudentsCircle.put("im preparing :(", 0);
        pollOptionsStudentsCircle.put("screw it! lets go with the flow", 0);

        studentsPollBroadcastId = FirebaseWriteHelper.createPollBroadcast("Do you guys think we will have exams?", "Vijai VJR", 1,
                pollOptionsStudentsCircle,"https://firebasestorage.googleapis.com/v0/b/circle-d8cc7.appspot.com/o/k9rd8iesn6ygrioen9cv.jpg?alt=media&token=220677ac-6e5f-473e-a28d-ae5c034e83e1",
                0, studentsCircleId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseWriteHelper.getAuthToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseWriteHelper.getAuthToken();
    }
}