package circleapp.circlepackage.circle.ui.EditProfile;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class EditUserProfileImage {
     ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
     ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
     CircleImageView profilePic;
    RelativeLayout setProfile;
     Boolean finalizeChange = false;
    private static final int PICK_IMAGE_ID = 234;
    String avatar;
    Uri downloadLink;
    Dialog editUserNamedialogue, editUserProfiledialogue;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    public EditProfileViewModel editProfileViewModel;

    public void editprofile(String uri, Activity editProfile, Button finalizeChanges, User user, Uri downloadLink1, CircleImageView profileImageView) {
        editUserProfiledialogue = new Dialog(editProfile);
        editUserProfiledialogue.setContentView(R.layout.user_profile_edit_dialogue);
        userNameProgressDialogue = new ProgressDialog(editProfile);
        imageUploadProgressDialog = new ProgressDialog(editProfile);
        this.downloadLink = downloadLink1;
        editProfileViewModel = ViewModelProviders.of((FragmentActivity) editProfile).get(EditProfileViewModel.class);
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
        profilePic = editUserProfiledialogue.findViewById(R.id.profile_image);
        setProfile = editUserProfiledialogue.findViewById(R.id.imagePreview);
        Button profilepicButton = editUserProfiledialogue.findViewById(R.id.profilePicSetterImage);
        Button profileuploadButton = editUserProfiledialogue.findViewById(R.id.edit_profile_Button);
        Glide.with(editProfile).load(uri).into(profilePic);
        profilepicButton.setOnClickListener(view -> {
            Permissions.check(editProfile/*context*/, CAMERA, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    finalizeChange = false;
                    for (int i = 0; i < 8; i++) {
                        avatarBgList[i].setVisibility(View.GONE);
                    }
                    avatar = "";
                    editUserProfiledialogue.dismiss();
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(editProfile);
                    editProfile.startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });
        finalizeChanges.setOnClickListener(view -> {
            if (downloadLink != null)
                user.setProfileImageLink(downloadLink.toString());

            FirebaseWriteHelper.updateUser(user, editProfile);
            SessionStorage.saveUser(editProfile, user);
            finalizeChange = true;
            finalizeChanges.setVisibility(View.GONE);

            /*startActivity(new Intent(editProfile, ExploreTabbedActivity.class));
            finish();*/
        });
        //listener for button to add the profilepic
        avatar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar1);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar2);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar3);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar4);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar5);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar6);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar7);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
                downloadLink = null;
            }
        });
        avatar8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add code to unpress rest of the buttons
                avatar = String.valueOf(R.drawable.avatar8);
                HelperMethods.setProfilePicMethod(editProfile, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
                downloadLink = null;
            }
        });


        profileuploadButton.setOnClickListener(view -> {
            if (avatar != "" || downloadLink != null) {
                userNameProgressDialogue.setTitle("Uploading Profile....");
                userNameProgressDialogue.show();
                if (downloadLink != null) {
                    Log.d("TAG", "DownloadURI ::" + downloadLink);

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadLink)
                            .build();
                    if (FirebaseWriteHelper.getUser() == null){
                        userNameProgressDialogue.dismiss();
                        editUserProfiledialogue.dismiss();
                        Toast.makeText(editProfile, "Error try Again!!!!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        editProfileViewModel.editprofileimage(profileUpdates,user,editProfile).observe((LifecycleOwner) editProfile, state->{
                            if (state){
                                user.setProfileImageLink(downloadLink.toString());
                                Glide.with(editProfile).load(downloadLink.toString()).into(profileImageView);
                                HelperMethods.GlideSetProfilePic(editProfile, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
                                userNameProgressDialogue.dismiss();
                                editUserProfiledialogue.dismiss();
                            }
                        });
                    }

                } else {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(avatar))
                            .build();
                    if (FirebaseWriteHelper.getUser() == null){
                        userNameProgressDialogue.dismiss();
                        editUserProfiledialogue.dismiss();
                        Toast.makeText(editProfile, "Error try Again!!!!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        editProfileViewModel.editprofileimage(profileUpdates,user,editProfile).observe((LifecycleOwner) editProfile, state->{
                            if (state){
                                user.setProfileImageLink(avatar);
                                Glide.with(editProfile)
                                        .load(Integer.parseInt(avatar))
                                        .into(profileImageView);
                                userNameProgressDialogue.dismiss();
                                user.setProfileImageLink(avatar);
                                SessionStorage.saveUser(editProfile, user);
                                finalizeChange = true;
                                editUserProfiledialogue.dismiss();
                            }
                        });
                    }
                }

            } else {
                editUserProfiledialogue.dismiss();
                //Toast.makeText(getApplicationContext(), "Select a Profile Picture to Continue....", Toast.LENGTH_SHORT).show();
            }

        });
        editUserProfiledialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserProfiledialogue.show();

    }

}
