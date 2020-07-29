package circleapp.circlepackage.circle.ui.EditProfile;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class EditProfileImage extends AppCompatActivity {
    Dialog editUserProfiledialogue;
    private ProgressDialog imageUploadProgressDialog;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    CircleImageView profilePic;
    RelativeLayout setProfile;
    Boolean finalizeChange = false;
    private static final int PICK_IMAGE_ID = 234;
    String avatar;
    Button profilepicButton, profileuploadButton;
    Uri downloadLink;
    User user;
    EditProfile EditProfileClassTemp;
    public EditProfileViewModel editProfileViewModel;
    Uri filePath;
    GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;

    public void editProfile(EditProfile EditProfileClass) {
        this.EditProfileClassTemp = EditProfileClass;
        InitUI();
        InitAvatars();
        profileuploadButton.setOnClickListener(view -> {
            ProfileUploadButton();
        });
        profilepicButton.setOnClickListener(view -> {
            ImagePickerIntent();
        });
        EditProfileClass.finalizeChanges.setOnClickListener(view -> {
            FinalizeChangesBtn();
        });
        avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar1";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar2";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar3";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar4";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar5";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar6";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar7";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = "avatar8";
                HelperMethodsUI.setProfilePicMethod(EditProfileClass, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        editUserProfiledialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserProfiledialogue.show();
    }
    private void InitUI() {
        editUserProfiledialogue = new Dialog(EditProfileClassTemp);
        editUserProfiledialogue.setContentView(R.layout.user_profile_edit_dialogue);
        imageUploadProgressDialog = new ProgressDialog(EditProfileClassTemp);
//        user = SessionStorage.getUser(EditProfileClass);
        user = globalVariables.getCurrentUser();
        profilePic = editUserProfiledialogue.findViewById(R.id.profile_image);
        setProfile = editUserProfiledialogue.findViewById(R.id.imagePreview);
        profilepicButton = editUserProfiledialogue.findViewById(R.id.profilePicSetterImage);
        profileuploadButton = editUserProfiledialogue.findViewById(R.id.edit_profile_Button);
        Glide.with(EditProfileClassTemp).load(globalVariables.getAuthenticationToken().getCurrentUser().getPhotoUrl()).into(profilePic);
        editProfileViewModel = ViewModelProviders.of(EditProfileClassTemp).get(EditProfileViewModel.class);

    }

    public void ProfileUploadButton() {

        if (avatar != "") {
            imageUploadProgressDialog.setTitle("Uploading Profile....");
            imageUploadProgressDialog.show();
            Log.d("TAG2", "DownloadURI ::" + avatar);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(avatar))
                    .build();

            user.setProfileImageLink(avatar);
            globalVariables.saveCurrentUser(user);
            editProfileViewModel.editprofileimage(profileUpdates, user,EditProfileClassTemp).observe(EditProfileClassTemp, state -> {
                if (state) {
                    user.setProfileImageLink(avatar);
                    int index = Integer.parseInt(String.valueOf(avatar.charAt(avatar.length()-1)));
                    index = index-1;
                    TypedArray avatarResourcePos = EditProfileClassTemp.getResources().obtainTypedArray(R.array.AvatarValues);
                    Glide.with(EditProfileClassTemp)
                            .load(avatarResourcePos.getResourceId(index, 0))
                            .into(EditProfileClassTemp.profileImageView);
                    Log.d("TAG2", "DownloadURI bv::" + globalVariables.getAuthenticationToken().getCurrentUser().getPhotoUrl());
                    finalizeChange = true;
                    imageUploadProgressDialog.dismiss();
                    editUserProfiledialogue.dismiss();
                }
            });

        } else {
            editUserProfiledialogue.dismiss();
            //Toast.makeText(getApplicationContext(), "Select a Profile Picture to Continue....", Toast.LENGTH_SHORT).show();
        }
    }

    private void FinalizeChangesBtn() {
//        String TempUrl = SessionStorage.getUser(EditProfileClassTemp).getProfileImageLink();
        String TempUrl = globalVariables.getCurrentUser().getProfileImageLink();
        Log.d("TAG", "DownloadURI ::" + TempUrl);
        imageUploadProgressDialog.setTitle("Uploading Profile....");
        imageUploadProgressDialog.show();
        if (TempUrl != null) {
            Log.d("TAG", "DownloadURI ::" + TempUrl);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(TempUrl))
                    .build();
            user.setProfileImageLink(TempUrl);
            editProfileViewModel.editprofileimage(profileUpdates, user, EditProfileClassTemp).observe(EditProfileClassTemp, state -> {
                if (state) {
                    Log.d("TAG", "DownloadURI ::" + globalVariables.getAuthenticationToken().getCurrentUser().getPhotoUrl());
                    globalVariables.saveCurrentUser(user);
                    Glide.with(EditProfileClassTemp).load(TempUrl).into(EditProfileClassTemp.profileImageView);
                    HelperMethodsUI.GlideSetProfilePic(EditProfileClassTemp, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
                    imageUploadProgressDialog.dismiss();
                    editUserProfiledialogue.dismiss();
                }
            });
        }
        finalizeChange = true;
        EditProfileClassTemp.finalizeChanges.setVisibility(View.GONE);
    }
    private void ImagePickerIntent() {
        Permissions.check(EditProfileClassTemp, new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, null, null, new PermissionHandler() {

            @Override
            public void onGranted() {
                finalizeChange = false;
                for (int i = 0; i < 8; i++) {
                    avatarBgList[i].setVisibility(View.GONE);
                }
                avatar = "";
                editUserProfiledialogue.dismiss();
                ImagePicker imagePicker = new ImagePicker(EditProfileClassTemp.getApplication());
                Intent chooseImageIntent = imagePicker.getPickImageIntent();
                EditProfileClassTemp.startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });
    }

    private void InitAvatars() {
        avatar = "";
        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];
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
    }
}
