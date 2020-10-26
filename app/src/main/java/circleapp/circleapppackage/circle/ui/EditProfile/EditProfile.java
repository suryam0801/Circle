package circleapp.circleapppackage.circle.ui.EditProfile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;

import circleapp.circleapppackage.circle.DataLayer.FBRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circleapppackage.circle.ui.ExploreTabbedActivity;
import circleapp.circleapppackage.circle.ui.Login.EntryPage.EntryPage;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName;
    private TextView userNumber;
    private TextView createdCircles;
    private TextView workingCircles;
    private Button editProfPic, logout, finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    private Uri downloadLink;
    private AlertDialog.Builder confirmation;
    private static final int PICK_IMAGE_ID = 234;
    private ImageButton editName;
    private User user;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    private ImageUpload imageUploadModel;
    private EditProfileImage editUserProfileImage;
    private EdituserName edituserName;
    private GlobalVariables globalVariables = new GlobalVariables();

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
        editUserProfileImage = new EditProfileImage();
        edituserName  = new EdituserName();
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath, false).observe(this, progress -> {
            // update UI
            if(progress==null);

            else if(!progress[1].equals("100")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100")){
                Glide.with(this).load(filePath).into(profileImageView);
                downloadLink = Uri.parse(progress[0]);
                finalizeChanges.setVisibility(View.VISIBLE);
                user.setProfileImageLink(downloadLink.toString());
                globalVariables.saveCurrentUser(user);
                imageUploadProgressDialog.dismiss();
            }
        });
        editProfPic.setOnClickListener(view -> {
            editUserProfileImage.editProfile(EditProfile.this,profileImageView,finalizeChanges);
        });
        editName.setOnClickListener(v -> {
            edituserName.edituserNamedialogue(EditProfile.this,userName);
//            edituserNamedialogue();
        });
        logout.setOnClickListener(view -> {
            FBRepository fbRepository = new FBRepository();
            fbRepository.signOutAuth();
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
        int activeCircles;
        if(user.getActiveCircles()==null)
            activeCircles = 0;
        else {
            activeCircles = user.getActiveCircles().size();
        }
        workingCircles.setText(activeCircles + "");
        HelperMethodsUI.setUserProfileImage(user.getProfileImageLink(), this, profileImageView);
    }
    private void uploadUserProfilePic(){
        imageUploadModel.imageUpload(filePath, false);
    }
    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath= imagePicker.getImageUri(bitmap);
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

        int tempVisibility = finalizeChanges.getVisibility();
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
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finishAfterTransition();
                        Intent intent = new Intent(EditProfile.this, ExploreTabbedActivity.class);
                        startActivity(intent);
                    }
                });
    }
}