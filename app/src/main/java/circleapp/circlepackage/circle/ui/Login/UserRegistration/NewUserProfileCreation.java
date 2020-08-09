package circleapp.circlepackage.circle.ui.Login.UserRegistration;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.DataLayer.FBRepository;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.Model.LocalObjectModels.TempLocation;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Utils.UploadImages.ImageUpload;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.LocationsViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.UserRegistration.NewUserRegistration;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class NewUserProfileCreation extends AppCompatActivity implements View.OnKeyListener{

    private Uri filePath;
    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter
    private Uri downloadLink;
    private CircleImageView profilePic;
    private boolean locationExists;
    private String Name, contact;
    private EditText name;
    private Button register;
    private ImageButton avatar1, avatar2, avatar3, avatar4, avatar5, avatar6, avatar7, avatar8, avatarList[];
    private ImageView avatar1_bg, avatar2_bg, avatar3_bg, avatar4_bg, avatar5_bg, avatar6_bg, avatar7_bg, avatar8_bg, avatarBgList[];
    private String avatar, uid;
    private RelativeLayout setProfile;
    private String ward, district;
    private LoginUserObject loginUserObject;
    private ImageUpload imageUploadModel;
    private NewUserRegistration newUserRegistration;
    private ProgressDialog imageUploadProgressDialog;
    private TempLocation tempLocation;
    private GlobalVariables globalVariables = new GlobalVariables();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gather_user_details);
        //Getting the instance and references

        InitUIElements();
        setAvatarViews();
        //Initialize observables
        setImageUploadProgressObservable();
        setUserRegisteredObservable();
        //get temp user attributes from session
        setLoginUserObject();
        //check if current user location exists in db
        getLocationAlreadyExistsResult();
        setAvatarOnclickListeners();
        //listener for button to add the profilepic
        setProfile.setOnClickListener(v -> {
            Permissions.check(this,new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},null,null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    runPhotoUploadIntent();
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });

        // Listener for Register button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if all fields are entered
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
                    //Register user in FB Db
                    newUserRegistration.userRegister(uid, Name, district, ward, imageLink, avatar, contact, locationExists);
                }
            }
        });
    }
    private void InitUIElements(){
        name = findViewById(R.id.name);
        register = findViewById(R.id.registerButton);
        avatar = "";
        locationExists = false;
        profilePic = findViewById(R.id.profile_image);
        setProfile = findViewById(R.id.imagePreview);
        imageUploadProgressDialog = new ProgressDialog(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
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
        loginUserObject = globalVariables.getCurrentLoginUserObject();
        tempLocation = globalVariables.getCurrentTempLocation();
        ward = tempLocation.getWard();
        district = tempLocation.getDistrict();
        contact = loginUserObject.getCompletePhoneNumber();
        uid = loginUserObject.getUid();
    }

    public void getLocationAlreadyExistsResult() {
        LocationsViewModel viewModel = ViewModelProviders.of((FragmentActivity) this).get(LocationsViewModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsLocationsSingleValueLiveData(district);
        liveData.observe((LifecycleOwner) this, dataSnapshot -> {
            Log.d("Location",dataSnapshot.toString());
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
            avatar = "avatar1";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar1_bg, avatar1, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar2.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar2";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar2_bg, avatar2, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar3.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar3";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar3_bg, avatar3, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar4.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar4";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar4_bg, avatar4, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar5.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar5";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar5_bg, avatar5, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar6.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar6";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar6_bg, avatar6, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar7.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar7";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar7_bg, avatar7, avatarBgList, avatarList);
            downloadLink = null;
        });
        avatar8.setOnClickListener(v -> {
            //add code to unpress rest of the buttons
            avatar = "avatar8";
            HelperMethodsUI.setProfilePicMethod(NewUserProfileCreation.this, profilePic, avatar, avatar8_bg, avatar8, avatarBgList, avatarList);
            downloadLink = null;
        });
    }

    private void runPhotoUploadIntent(){
        ImagePicker imagePicker = new ImagePicker(getApplication());
        Intent chooseImageIntent = imagePicker.getPickImageIntent();
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    private void setImageUploadProgressObservable(){
        imageUploadModel = ViewModelProviders.of(this).get(ImageUpload.class);
        imageUploadModel.uploadImageWithProgress(filePath).observe(this, progress -> {
            // update UI
            if(progress==null);

            else if(progress[1].equals("-1")){
                imageUploadProgressDialog.dismiss();
                Toast.makeText(this, "Error uploading. Please try again", Toast.LENGTH_SHORT).show();
            }

            else if(!progress[1].equals("100")){
                imageUploadProgressDialog.setTitle("Uploading");
                imageUploadProgressDialog.setMessage("Uploaded " + progress[1] + "%...");
                imageUploadProgressDialog.show();
            }
            else if(progress[1].equals("100")){
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

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        filePath = null;

        switch (requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath = imagePicker.getImageUri(bitmap);
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
        FBRepository fbRepository = new FBRepository();
        fbRepository.signOutAuth();

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
            globalVariables.getAuthenticationToken();

        }

        @Override
        protected void onStart () {
            super.onStart();
            globalVariables.getAuthenticationToken();
        }

}