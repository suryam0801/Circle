package circleapp.circlepackage.circle.ui.CircleWall;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.ViewModels.CircleWall.CircleWallViewModel;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.User;

import static android.Manifest.permission.CAMERA;

public class CreatePhotoBroadcastDialog {
    public boolean pollExists = false;
    public boolean imageExists = false;
    private static final int PICK_IMAGE_ID = 234;
    private Dialog createPhotoBroadcastPopup;
    private EditText setTitlePhoto;
    private TextView addPhotoText;
    private Button btnUploadPhotoBroadcast, cancelPhotoButton;
    private Activity activity;
    public ImageView addPhoto;
    private CircleWall circleWall;
    private Uri downloadLink;
    private GlobalVariables globalVariables;
    private CircleWallViewModel circleWallViewModel;
    public RelativeLayout photoUploadButtonView;
    private User user;
    private Circle circle;
    public void showCreatePhotoBroadcastDialog(Activity activity) {
        globalVariables = new GlobalVariables();
        this.activity = activity;
        InitUI();
        cancelPhotoButton.setOnClickListener(view -> createPhotoBroadcastPopup.dismiss());

        photoUploadButtonView.setOnClickListener(v -> {
            permissionCheck();
        });
        btnUploadPhotoBroadcast.setOnClickListener(view -> {
            this.downloadLink = globalVariables.getTempdownloadLink();
            Log.d("tagdownload",downloadLink.toString());
            if (this.downloadLink != null && !setTitlePhoto.getText().toString().isEmpty()) {
                this.imageExists = true;
                String title = setTitlePhoto.getText().toString();
                circleWallViewModel = ViewModelProviders.of((FragmentActivity) activity).get(CircleWallViewModel.class);
                circleWallViewModel.createPhotoBroadcast(title, this.downloadLink,circle,user,this.imageExists, activity).observe((LifecycleOwner) activity, state->{
                    if (state){
                        this.pollExists = false;
                        this.imageExists = false;
                        circleWall.updateUserCount(circle);
                        createPhotoBroadcastPopup.dismiss();
                    }
                    else {
                        createPhotoBroadcastPopup.dismiss();
                        Toast.makeText(activity,"Error while Creating broadcast",Toast.LENGTH_SHORT).show();
                    }
                });
            } else
                Toast.makeText(activity, "Fill out all fields::"+downloadLink, Toast.LENGTH_SHORT).show();

        });
        createPhotoBroadcastPopup.show();
    }

    private void permissionCheck() {
        Permissions.check(activity/*context*/,new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                ImagePicker imagePicker = new ImagePicker(activity.getApplication());
                Intent chooseImageIntent = imagePicker.getPickImageIntent();
                activity.startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
            }
        });
    }

    private void InitUI() {
        createPhotoBroadcastPopup = new Dialog(activity);
        createPhotoBroadcastPopup.setContentView(R.layout.photo_broadcast_create_popup); //set dialog view
        createPhotoBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPhotoBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        circleWall = new CircleWall();
        user = globalVariables.getCurrentUser();
        circle = globalVariables.getCurrentCircle();
        setTitlePhoto = createPhotoBroadcastPopup.findViewById(R.id.photoTitleEditText);
        setTitlePhoto.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        addPhoto = createPhotoBroadcastPopup.findViewById(R.id.photo_display_photo_add_broadcast);
        photoUploadButtonView = createPhotoBroadcastPopup.findViewById(R.id.photo_add_photo_view);
        addPhotoText = createPhotoBroadcastPopup.findViewById(R.id.photo_upload_photo);

        btnUploadPhotoBroadcast = createPhotoBroadcastPopup.findViewById(R.id.upload_photo_broadcast_btn);
        cancelPhotoButton = createPhotoBroadcastPopup.findViewById(R.id.create_photo_broadcast_cancel_btn);

    }
}