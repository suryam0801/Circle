package circleapp.circlepackage.circle.ui.Login.UserRegistration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.UserRegistration.NewUserRegistration;
import circleapp.circlepackage.circle.data.FBDatabaseReads.LocationsViewModel;
import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class GatherUserDetails extends AppCompatActivity implements View.OnKeyListener{

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private Uri downloadLink;
    private CircleImageView profilePic;
    private boolean locationExists;
    SharedPreferences pref;
    String Name, contact;
    EditText name;
    Button register;
    ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    String avatar, uid;
    RuntimePermissionHelper runtimePermissionHelper;
    RelativeLayout setProfile;
    private String ward, district;
    private LoginUserObject loginUserObject;
    private ImageUpload imageUploadModel;
    private NewUserRegistration newUserRegistration;
    private ProgressDialog imageUploadProgressDialog;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_user_details);
        //Getting the instance and references

        name = findViewById(R.id.name);
        register = findViewById(R.id.registerButton);
        Button profilepicButton = findViewById(R.id.profilePicSetterImage);
        avatar = "";
        locationExists = false;

        setAvatarViews();
        profilePic = findViewById(R.id.profile_image);
        setProfile = findViewById(R.id.imagePreview);

        imageUploadProgressDialog = new ProgressDialog(this);
        setImageUploadProgressObservable();
        setUserRegisteredObservable();

        setLoginUserObject();
        getLocationAlreadyExistsResult();

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
                register.setText("Logging You In...");
                if (name.getText().equals("") || name.getText().toString().isEmpty()) {

                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Name = name.getText().toString();
                    Name = Name.replaceAll("\\s+", " ");

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    //The function to register the Users with their appropriate details
                    String imageLink = getImageLinkAsString();
                    newUserRegistration.userRegister(uid, Name, district, ward, imageLink, avatar, contact, locationExists);
                }
            }
        });
    }

    private void setAvatarViews() {

        avatarList = new ImageButton[8];
        avatarBgList = new ImageView[8];

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
    }

    private void setLoginUserObject() {
        loginUserObject = SessionStorage.getLoginUserObject(this);
        ward = loginUserObject.getWard();
        district = loginUserObject.getDistrict();
        contact = loginUserObject.getCompletePhoneNumber();
        uid = loginUserObject.getUid();
    }

    public void getLocationAlreadyExistsResult() {
        LocationsViewModel viewModel = ViewModelProviders.of((FragmentActivity) this).get(LocationsViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsLocationsSingleValueLiveData(district);
        liveData.observe((LifecycleOwner) this, dataSnapshot -> {
            if (dataSnapshot.exists()) {
                locationExists = true;
            } else {
                locationExists = false;
            }
        });
    }


    private void setAvatarOnclickListeners() {
        avatar1.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar1);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar2.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar2);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar3.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar3);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar4.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar4);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar5.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar5);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar6.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar6);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar7.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar7);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar8.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = String.valueOf(R.drawable.avatar8);
            HelperMethods.setProfilePicMethod(GatherUserDetails.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
            downloadLink = null;
        });
    }

    private void setImageUploadProgressObservable(){
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
                Glide.with(this).load(filePath).into(profilePic);
                downloadLink = Uri.parse(progress[0]);
                for (int i = 0; i < 8; i++) {
                    avatarBgList[i].setVisibility(View.INVISIBLE);
                }
                imageUploadProgressDialog.dismiss();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUserRegisteredObservable(){
        String tempLink;
        if(downloadLink==null)
            tempLink = "";
        else
            tempLink = downloadLink.toString();
        newUserRegistration = ViewModelProviders.of(this).get(NewUserRegistration.class);
        newUserRegistration.userObjectUploadProgress(false, uid, Name, district, ward, tempLink,avatar,contact,locationExists).observe(this, isUserUploaded -> {
            Log.d("userreg()value",""+isUserUploaded);
            // update UI
            if(isUserUploaded==false);

            else {
                goToNextActivity();
            }
        });
    }

    private String getImageLinkAsString() {
        if (downloadLink == null)
            return null;
        else
            return downloadLink.toString();
    }

    private void uploadUserProfilePic(){
        imageUploadModel.imageUpload(filePath);
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
        filePath = null;

        switch (requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                filePath = ImagePicker.getImageUri(getApplicationContext(), bitmap);
                if (filePath != null) {
                    uploadUserProfilePic();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToNextActivity(){
        Toast.makeText(this, "Registration Successful...",Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ExploreTabbedActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        finishAfterTransition();
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
        @Override
        protected void onResume () {
            super.onResume();
            FirebaseWriteHelper.getAuthToken();

        }

        @Override
        protected void onStart () {
            super.onStart();
            FirebaseWriteHelper.getAuthToken();
        }

}