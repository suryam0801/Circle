package circleapp.circlepackage.circle.EditProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
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
import android.widget.RelativeLayout;
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
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles;
    private Button editProfPic, logout, finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    private Dialog editUserNamedialogue, editUserProfiledialogue;
    private StorageReference storageReference;
    private Uri downloadUri;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    String TAG = EditProfile.class.getSimpleName();
    ImageButton editName;
    User user;
    ProgressDialog progressDialog;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    private CircleImageView profilePic;
    RelativeLayout setProfile;
    String avatar;
    RuntimePermissionHelper runtimePermissionHelper;
    int photo;

    private FirebaseDatabase database;
    private DatabaseReference tags, userDB;
    private FirebaseAuth currentUser;

    private Boolean finalizeChange = false;

    //UI elements for location tag selector popup and interest tag selector popup
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setFinishOnTouchOutside(false);


        user = SessionStorage.getUser(EditProfile.this);

        progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setCancelable(false);
        runtimePermissionHelper = new RuntimePermissionHelper(EditProfile.this);

        userName = findViewById(R.id.viewProfile_name);
        userNumber = findViewById(R.id.viewProfile_email);
        createdCircles = findViewById(R.id.viewProfileCreatedCirclesCount);
        workingCircles = findViewById(R.id.viewProfileActiveCirclesCount);
        editProfPic = findViewById(R.id.profile_view_profilePicSetterImage);
        profileImageView = findViewById(R.id.profile_view_profile_image);
        editName = findViewById(R.id.editName);
        logout = findViewById(R.id.profile_logout);
        back = findViewById(R.id.bck_view_edit_profile);
        finalizeChanges = findViewById(R.id.profile_finalize_changes);

        currentUser = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users");
        userName.setText(user.getName());
        userNumber.setText(user.getContact());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");
        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];

        HelperMethods.setUserProfileImage(user, this, profileImageView);


        editProfPic.setOnClickListener(view -> {
            editprofile(user.getProfileImageLink());
        });
        editName.setOnClickListener(v->{
            edituserNamedialogue();
        });

        logout.setOnClickListener(view -> {
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

        avatarList[0] = avatar1 = editUserProfiledialogue.findViewById(R.id.avatar1);
        avatarList[1] = avatar2 = editUserProfiledialogue.findViewById(R.id.avatar2);
        avatarList[2] = avatar3 = editUserProfiledialogue.findViewById(R.id.avatar3);
        avatarList[3] = avatar4 = editUserProfiledialogue.findViewById(R.id.avatar4);
        avatarList[4] = avatar5 = editUserProfiledialogue.findViewById(R.id.avatar5);
        avatarList[5] = avatar6 = editUserProfiledialogue.findViewById(R.id.avatar6);
        avatarList[6] = avatar7 = editUserProfiledialogue.findViewById(R.id.avatar7);
        avatarList[7] = avatar8 = editUserProfiledialogue.findViewById(R.id.avatar8);
        avatarBgList[0] = avatar1_bg = editUserProfiledialogue.findViewById(R.id.avatar1_State);
        avatarBgList[1] = avatar2_bg = editUserProfiledialogue.findViewById(R.id.avatar2_State);
        avatarBgList[2] = avatar3_bg = editUserProfiledialogue.findViewById(R.id.avatar3_State);
        avatarBgList[3] = avatar4_bg = editUserProfiledialogue.findViewById(R.id.avatar4_State);
        avatarBgList[4] = avatar5_bg = editUserProfiledialogue.findViewById(R.id.avatar5_State);
        avatarBgList[5] = avatar6_bg = editUserProfiledialogue.findViewById(R.id.avatar6_State);
        avatarBgList[6] = avatar7_bg = editUserProfiledialogue.findViewById(R.id.avatar7_State);
        avatarBgList[7] = avatar8_bg = editUserProfiledialogue.findViewById(R.id.avatar8_State);
        profilePic = editUserProfiledialogue.findViewById(R.id.profile_image);
        setProfile = editUserProfiledialogue.findViewById(R.id.imagePreview);
        photo = 0;
        Button profilepicButton = editUserProfiledialogue.findViewById(R.id.profilePicSetterImage);
        Button profileuploadButton = editUserProfiledialogue.findViewById(R.id.edit_profile_Button);
        Glide.with(EditProfile.this).load(uri).into(profilePic);
        profilepicButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditProfile.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfile.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                finalizeChange = false;
                for (int i = 0; i < 8; i++) {
                    avatarBgList[i].setVisibility(View.GONE);
                }
                avatar = "";
                /*selectFile();*/
                if (photo == 0)
                    selectImage();
                editUserProfiledialogue.dismiss();
            }
        });
        finalizeChanges.setOnClickListener(view -> {
            if (downloadUri != null)
                user.setProfileImageLink(downloadUri.toString());

            userDB.child(user.getUserId()).setValue(user);
            SessionStorage.saveUser(EditProfile.this, user);
            finalizeChange = true;
            finalizeChanges.setVisibility(View.GONE);

            /*startActivity(new Intent(EditProfile.this, ExploreTabbedActivity.class));
            finish();*/
        });
        //listener for button to add the profilepic
        avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar1);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar2);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
                downloadUri = null;
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar8);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
                downloadUri = null;
            }
        });


        profileuploadButton.setOnClickListener(view -> {
            if (avatar != "" || downloadUri != null) {
                progressDialog.setTitle("Uploading Profile....");
                progressDialog.show();
                if (downloadUri != null) {
                    Log.d(TAG, "DownloadURI ::" + downloadUri);
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
                                    HelperMethods.GlideSetProfilePic(EditProfile.this, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
                                    progressDialog.dismiss();
                                    editUserProfiledialogue.dismiss();
                                }
                            });
                        }
                    });


                } else {
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
                                    user.setProfileImageLink(avatar);
                                    SessionStorage.saveUser(EditProfile.this, user);
                                    finalizeChange = true;
                                    editUserProfiledialogue.dismiss();
                                }
                            });
                        }
                    });
                }

            } else {
                editUserProfiledialogue.dismiss();
                //Toast.makeText(getApplicationContext(), "Select a Profile Picture to Continue....", Toast.LENGTH_SHORT).show();
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
        edit_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        edit_name_finalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString().replaceAll("\\s+", " ");
                ;
                if (!TextUtils.isEmpty(name)) {
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
                                    Log.d(TAG, "User Name updated:: " + name);
//                                    editUserNamedialogue.cancel();
                                }
                            });
                        }
                    });

                } else {
                    Toast.makeText(EditProfile.this, "Please Enter Your Name...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editUserNamedialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserNamedialogue.show();


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
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
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
            Toast.makeText(EditProfile.this,
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
                        //and displaying a success toast
                        finalizeChanges.setVisibility(View.VISIBLE);
                        downloadUri = uri;
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates);
                        Glide.with(EditProfile.this).load(filePath).into(profileImageView);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
        startActivity(intent);
        finish();
    }
}
