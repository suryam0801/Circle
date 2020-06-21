package circleapp.circlepackage.circle.EditProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles;
    private Button editProfPic, finalizeChanges, logout;
    private ImageButton back;
    private Uri filePath;
    private Dialog editUserNamedialogue,editUserProfiledialogue;
    private StorageReference storageReference;
    private Uri downloadUri;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    String TAG = EditProfile.class.getSimpleName();
    ImageButton editName;
    User user;
    ProgressDialog progressDialog;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7;
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg;
    private CircleImageView profilePic;
    String avatar;
    private  int propic;
    int myImageList;

//    int[] myImageList = new int[]{R.drawable.avatar1, R.drawable.avatar3, R.drawable.avatar4,
//            R.drawable.avatar2, R.drawable.avatar5};

    private FirebaseDatabase database;
    private DatabaseReference tags, userDB;
    private FirebaseAuth currentUser;

    private Boolean finalizeChange= false;

    //UI elements for location tag selector popup and interest tag selector popup
    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setFinishOnTouchOutside(false);


        user = SessionStorage.getUser(EditProfile.this);

        progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setCancelable(false);

        userName = findViewById(R.id.viewProfile_name);
        userNumber = findViewById(R.id.viewProfile_email);
        createdCircles = findViewById(R.id.viewProfileCreatedCirclesCount);
        workingCircles = findViewById(R.id.viewProfileActiveCirclesCount);
        editProfPic = findViewById(R.id.profile_view_profilePicSetterImage);
        profileImageView = findViewById(R.id.profile_view_profile_image);
        finalizeChanges = findViewById(R.id.profile_finalize_changes);
        editName = findViewById(R.id.editName);
        logout = findViewById(R.id.profile_logout);
        back = findViewById(R.id.bck_view_edit_profile);
        currentUser = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users");
        analyticsLogEvents = new AnalyticsLogEvents();

        userName.setText(user.getName());
        userNumber.setText(user.getContact());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");


        if (user.getProfileImageLink().length() > 10) {
            Glide.with(EditProfile.this)
                    .load(user.getProfileImageLink())
                    .into(profileImageView);
        } else {
            propic = Integer.parseInt(user.getProfileImageLink());
            Glide.with(EditProfile.this)
                    .load(propic)
                    .into(profileImageView);
        }

//        Random r = new Random();
//        int count = r.nextInt((4 - 0) + 1);
//        Glide.with(EditProfile.this)
//                .load(user.getProfileImageLink())
//                .placeholder(ContextCompat.getDrawable(EditProfile.this, myImageList[count]))
//                .into(profileImageView);

            editName.setOnClickListener(view -> {
                edituserNamedialogue();
//                Toast.makeText(EditProfile.this,"Please Enter Your Name...",Toast.LENGTH_SHORT).show();
        });

        editProfPic.setOnClickListener(view -> {
            editprofile(user.getProfileImageLink());
        });

        finalizeChanges.setOnClickListener(view -> {
            if (downloadUri != null)
                user.setProfileImageLink(downloadUri.toString());

            userDB.child(user.getUserId()).setValue(user);
            SessionStorage.saveUser(EditProfile.this, user);
            finalizeChange = true;

            startActivity(new Intent(EditProfile.this, ExploreTabbedActivity.class));
            finish();
        });

        logout.setOnClickListener(view -> {
            analyticsLogEvents.logEvents(EditProfile.this, "change_dp", "logout","edit_profile");
            currentUser.signOut();
            startActivity(new Intent(EditProfile.this, EntryPage.class));
            finish();
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(EditProfile.this, ExploreTabbedActivity.class));
            finish();
        });

    }

    private void editprofile(String uri) {
        editUserProfiledialogue = new Dialog(EditProfile.this);
        editUserProfiledialogue.setContentView(R.layout.user_profile_edit_dialogue);

        avatar1 = editUserProfiledialogue.findViewById(R.id.avatar1);
        avatar2 = editUserProfiledialogue.findViewById(R.id.avatar2);
        avatar3 = editUserProfiledialogue.findViewById(R.id.avatar3);
        avatar4 =editUserProfiledialogue.findViewById(R.id.avatar4);
        avatar5 = editUserProfiledialogue.findViewById(R.id.avatar5);
        avatar6 = editUserProfiledialogue.findViewById(R.id.avatar6);
        avatar7 = editUserProfiledialogue.findViewById(R.id.avatar7);
        avatar1_bg = editUserProfiledialogue.findViewById(R.id.avatar1_State);
        avatar2_bg = editUserProfiledialogue.findViewById(R.id.avatar2_State);
        avatar3_bg = editUserProfiledialogue.findViewById(R.id.avatar3_State);
        avatar4_bg = editUserProfiledialogue.findViewById(R.id.avatar4_State);
        avatar5_bg = editUserProfiledialogue.findViewById(R.id.avatar5_State);
        avatar6_bg = editUserProfiledialogue.findViewById(R.id.avatar6_State);
        avatar7_bg = editUserProfiledialogue.findViewById(R.id.avatar7_State);
        profilePic = editUserProfiledialogue.findViewById(R.id.profile_image);
        Button profilepicButton = editUserProfiledialogue.findViewById(R.id.profilePicSetterImage);
        Button profileuploadButton = editUserProfiledialogue.findViewById(R.id.edit_profile_Button);
        Glide.with(EditProfile.this).load(uri).into(profilePic);
        profilepicButton.setOnClickListener( view ->{
            analyticsLogEvents.logEvents(EditProfile.this, "change_dp_start", "profile_pic","edit_profile");
            if (ContextCompat.checkSelfPermission(EditProfile.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfile.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                finalizeChange = false;
                avatar1_bg.setVisibility(View.GONE);
                avatar2_bg.setVisibility(View.GONE);
                avatar3_bg.setVisibility(View.GONE);
                avatar4_bg.setVisibility(View.GONE);
                avatar5_bg.setVisibility(View.GONE);
                avatar6_bg.setVisibility(View.GONE);
                avatar7_bg.setVisibility(View.GONE);
                avatar = "";
                selectFile();
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


        profileuploadButton.setOnClickListener(view ->{
            if ( avatar != "" || downloadUri != null)
            {
                progressDialog.setTitle("Uploading Profile....");
                progressDialog.show();
                if (downloadUri != null)
                {
                    Log.d(TAG,"DownloadURI ::"+downloadUri);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri)
                            .build();
                    currentUser.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            userDB.child(currentUser.getCurrentUser().getUid()).child("profileImageLink").setValue(downloadUri).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Glide.with(EditProfile.this).load(downloadUri.toString()).into(profileImageView);
                                    progressDialog.dismiss();
                                    editUserProfiledialogue.dismiss();
                                }
                            });
                        }
                    });


                }
                else
                    {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(avatar))
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                userDB.child(currentUser.getCurrentUser().getUid()).child("profileImageLink").setValue(avatar).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Glide.with(EditProfile.this)
                                                .load(Integer.parseInt(avatar))
                                                        .into(profileImageView);
                                        progressDialog.dismiss();
                                        editUserProfiledialogue.dismiss();
                                    }
                                });
                            }
                        });
                    }

            }
            else
                {
                    Toast.makeText(getApplicationContext(), "Select a Profile Picture to Continue....", Toast.LENGTH_SHORT).show();
                }

        });
        editUserProfiledialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserProfiledialogue.show();

    }

    private void edituserNamedialogue() {
        editUserNamedialogue = new Dialog(EditProfile.this);
        editUserNamedialogue.setContentView(R.layout.user_name_edit_dialogue); //set dialog view
        final Button edit_name_finalize = editUserNamedialogue.findViewById(R.id.edit_name_Button);
        final EditText edit_name = editUserNamedialogue.findViewById(R.id.edit_name);

        edit_name_finalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString().trim();
                if (!TextUtils.isEmpty(name))
                {
                    progressDialog.setTitle("Updating Name....");
                    progressDialog.show();
                    String userId = currentUser.getInstance().getCurrentUser().getUid();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    currentUser.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            userName.setText(currentUser.getCurrentUser().getDisplayName());
                            progressDialog.dismiss();
                            editUserNamedialogue.dismiss();
                            userDB.child(userId).child("name").setValue(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"User Name updated:: "+name);
//                                    editUserNamedialogue.cancel();
                                }
                            });
                        }
                    });

                }
                else
                    {
                        Toast.makeText(EditProfile.this,"Please Enter Your Name...",Toast.LENGTH_SHORT).show();
                    }
            }
        });

        editUserNamedialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserNamedialogue.show();


    }

    public void selectFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EditProfile.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                selectFile();
            } else {
                Toast.makeText(EditProfile.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
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
                final ProgressDialog progressDialog = new ProgressDialog(EditProfile.this);
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
                        finalizeChanges.setVisibility(View.VISIBLE);
                        //and displaying a success toast
                        downloadUri = uri;
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates);
                        Glide.with(EditProfile.this).load(downloadUri.toString()).into(profileImageView);

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
        super.onBackPressed();
        Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
        startActivity(intent);
        finish();
    }
}
