package circleapp.circlepackage.circle.ui.EditProfile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.UserRegistration.NewUserRegistration;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles;
    private Button editProfPic, logout, finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    private FirebaseUser firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Dialog editUserNamedialogue, editUserProfiledialogue;
    private StorageReference storageReference;
    private Uri downloadLink;
    private static final int PICK_IMAGE_ID = 234;
    String TAG = EditProfile.class.getSimpleName();
    ImageButton editName;
    User user;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    private CircleImageView profilePic;
    RelativeLayout setProfile;
    String avatar;
    public ImageUpload imageUploadModel;
    public EditProfileViewModel editProfileViewModel;
    public  EditUserProfileImage editUserProfileImage;
    public EdituserName edituserName;

    private Boolean finalizeChange = false;

    //UI elements for location tag selector popup and interest tag selector popup
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setFinishOnTouchOutside(false);

        InitUIElements();
        defUIValues();
        editUserProfileImage = new EditUserProfileImage();
        edituserName  = new EdituserName();
        editProfileViewModel = ViewModelProviders.of(this).get(EditProfileViewModel.class);
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath).observe(this, progress -> {
            Log.d("progressvalue",""+progress);
            // update UI
            if(progress==null);

            else if(!progress[1].equals("100.0")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100.0")){
                Glide.with(this).load(filePath).into(profileImageView);
                downloadLink = Uri.parse(progress[0]);
                for (int i = 0; i < 8; i++) {
                    avatarBgList[i].setVisibility(View.GONE);
                }
                finalizeChanges.setVisibility(View.VISIBLE);
                imageUploadProgressDialog.dismiss();
            }
        });
        editProfPic.setOnClickListener(view -> {
            editUserProfileImage.editprofile(user.getProfileImageLink(), EditProfile.this,finalizeChanges,user,downloadLink,profileImageView);
//            editprofile(user.getProfileImageLink());
        });
        editName.setOnClickListener(v -> {
            edituserName.edituserNamedialogue(EditProfile.this,user,userName);
//            edituserNamedialogue();
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
    private void InitUIElements(){
        userNameProgressDialogue = new ProgressDialog(EditProfile.this);
        userNameProgressDialogue.setCancelable(false);
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
        userNameProgressDialogue = new ProgressDialog(this);
        imageUploadProgressDialog = new ProgressDialog(this);

        storageReference = FirebaseStorage.getInstance().getReference();
    }
    private void defUIValues(){
        user = SessionStorage.getUser(EditProfile.this);
        userName.setText(FirebaseWriteHelper.getUser().getDisplayName());
        userNumber.setText(FirebaseWriteHelper.getUser().getPhoneNumber());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");
        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];
        HelperMethods.setUserProfileImage(user, this, profileImageView);
    }
    private void uploadUserProfilePic(){
        imageUploadModel.imageUpload(filePath);
    }
    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                filePath= ImagePicker.getImageUri(getApplicationContext(),bitmap);
                if(filePath !=null){
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
