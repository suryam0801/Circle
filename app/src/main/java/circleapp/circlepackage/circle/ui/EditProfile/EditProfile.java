package circleapp.circlepackage.circle.ui.EditProfile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    public CircleImageView profileImageView;
    public TextView userName;
    private TextView userNumber;
    private TextView createdCircles;
    private TextView workingCircles;
    public Button editProfPic, logout, finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    private Uri downloadLink;
    AlertDialog.Builder confirmation;
    private static final int PICK_IMAGE_ID = 234;
    private ImageButton editName;
    private User user;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    public ImageUpload imageUploadModel;
    public EditProfileViewModel editProfileViewModel;
    public  EditProfileImage editUserProfileImage;
    public EdituserName edituserName;
    private GlobalVariables globalVariables = new GlobalVariables();


    //UI elements for location tag selector popup and interest tag selector popup
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setFinishOnTouchOutside(false);

        InitUIElements();
        defUIValues();
        editUserProfileImage = new EditProfileImage();
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
                finalizeChanges.setVisibility(View.VISIBLE);
                user.setProfileImageLink(downloadLink.toString());
                globalVariables.saveCurrentUser(user);
                imageUploadProgressDialog.dismiss();
            }
        });
        editProfPic.setOnClickListener(view -> {
            editUserProfileImage.editProfile(EditProfile.this);
        });
        editName.setOnClickListener(v -> {
            edituserName.edituserNamedialogue(EditProfile.this);
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
    }
    private void defUIValues(){
        user = globalVariables.getCurrentUser();
        userName.setText(globalVariables.getAuthenticationToken().getCurrentUser().getDisplayName());
        userNumber.setText(globalVariables.getAuthenticationToken().getCurrentUser().getPhoneNumber());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");
        HelperMethodsUI.setUserProfileImage(user, this, profileImageView);
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
        if (finalizeChanges.getVisibility() == View.VISIBLE){
            alertDialog();
            AlertDialog alertDialog = confirmation.create();
            alertDialog.setTitle("Alert");
            alertDialog.show();
        }
        else {
            finishAfterTransition();
            Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
            startActivity(intent);
        }
    }
    public void alertDialog(){
        confirmation = new AlertDialog.Builder(this);
        confirmation.setMessage("Finalize the Changes!!!")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editUserProfileImage.FinalizeChangesBtn();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
