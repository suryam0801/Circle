package circleapp.circlepackage.circle.EditProfile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUploadSuccessListener;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class EditProfile extends AppCompatActivity implements ImageUploadSuccessListener {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles;
    private Button editProfPic, logout, finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    private Dialog editUserNamedialogue, editUserProfiledialogue;
    private StorageReference storageReference;
    private Uri downloadLink;
    private static final int PICK_IMAGE_ID = 234;
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

    private Boolean finalizeChange = false;

    //UI elements for location tag selector popup and interest tag selector popup
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        storageReference = FirebaseStorage.getInstance().getReference();

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
        editName.setOnClickListener(v -> {
            edituserNamedialogue();
        });

        logout.setOnClickListener(view -> {
            FirebaseWriteHelper.signOutAuth();
            startActivity(new Intent(EditProfile.this, EntryPage.class));
            finish();
        });

        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(EditProfile.this, ExploreTabbedActivity.class));
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
            if (!runtimePermissionHelper.isPermissionAvailable(CAMERA)) {
                runtimePermissionHelper.requestCameraPermissionsIfDenied(CAMERA);
            }
            else {
                finalizeChange = false;
                for (int i = 0; i < 8; i++) {
                    avatarBgList[i].setVisibility(View.GONE);
                }
                avatar = "";
                editUserProfiledialogue.dismiss();
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });
        finalizeChanges.setOnClickListener(view -> {
            if (downloadLink != null)
                user.setProfileImageLink(downloadLink.toString());

            FirebaseWriteHelper.updateUser(user, this);
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
                downloadLink = null;
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar2);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar8);
                HelperMethods.setProfilePicMethod(EditProfile.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
                downloadLink = null;
            }
        });


        profileuploadButton.setOnClickListener(view -> {
            if (avatar != "" || downloadLink != null) {
                progressDialog.setTitle("Uploading Profile....");
                progressDialog.show();
                if (downloadLink != null) {
                    Log.d(TAG, "DownloadURI ::" + downloadLink);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadLink)
                            .build();
                    FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setProfileImageLink(downloadLink.toString());
                            FirebaseWriteHelper.updateUser(user, EditProfile.this);
                            Glide.with(EditProfile.this).load(downloadLink.toString()).into(profileImageView);
                            HelperMethods.GlideSetProfilePic(EditProfile.this, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
                            progressDialog.dismiss();
                            editUserProfiledialogue.dismiss();
                        }
                    });


                } else {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(avatar))
                            .build();
                    FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setProfileImageLink(avatar);
                            FirebaseWriteHelper.updateUser(user, EditProfile.this);
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
                    String userId = FirebaseWriteHelper.getUserId();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            userName.setText(FirebaseWriteHelper.getUser().getDisplayName());
                            progressDialog.dismiss();
                            editUserNamedialogue.dismiss();
                            user.setName(name);
                            FirebaseWriteHelper.updateUser(user, EditProfile.this);
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

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(getApplicationContext());
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
        } else {
            Toast.makeText(EditProfile.this,
                    "Permission Denied",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void uploadUserProfilePic(){
        ImageUpload imageUpload = new ImageUpload();
        imageUpload.imageUpload(this, filePath);
        isImageUploadSuccess(downloadLink,filePath);
    }

    public void isImageUploadSuccess(Uri downloadUri, Uri localFilePath){
        Glide.with(this).load(localFilePath).into(profileImageView);
        downloadLink = downloadUri;
        filePath = null;
        finalizeChanges.setVisibility(View.VISIBLE);
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                downloadLink = ImagePicker.getImageUri(getApplicationContext(),bitmap);
                if(downloadLink !=null){
                    filePath = downloadLink;
                    uploadUserProfilePic();
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
        Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
        startActivity(intent);
    }
}
