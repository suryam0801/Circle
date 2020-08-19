package circleapp.circleapppackage.circle.ui.EditProfile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

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

public class EditProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    public CircleImageView profileImageView;
    private TextView userName;
    private TextView userNumber;
    private TextView createdCircles;
    private TextView workingCircles;
    private Button editProfPic;
    private Button logout;
    public Button finalizeChanges;
    private ImageButton back;
    private Uri filePath;
    public Uri downloadLink;
    private Context context;
    private AlertDialog.Builder confirmation;
    private static final int PICK_IMAGE_ID = 234;
    private ImageButton editName;
    private User user;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    private ImageUpload imageUploadModel;
    private EditProfileImage editUserProfileImage;
    private EdituserName edituserName;
    private GlobalVariables globalVariables = new GlobalVariables();

    public EditProfileFragment() {
    }

    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_profile, container, false);
        InitUIElements(view);
        defUIValues();
        editProfPic.setOnClickListener(v -> {
            editUserProfileImage.editProfile(getActivity(),profileImageView,finalizeChanges);
        });
        editName.setOnClickListener(v -> {
            edituserName.edituserNamedialogue(getActivity(),userName);
//            edituserNamedialogue();
        });
        logout.setOnClickListener(v -> {
            FBRepository fbRepository = new FBRepository();
            fbRepository.signOutAuth();
            startActivity(new Intent(getActivity(), EntryPage.class));
            getActivity().finish();
        });
        back.setOnClickListener(v -> {
            getActivity().finishAfterTransition();
            startActivity(new Intent(getActivity(), ExploreTabbedActivity.class));
        });

        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void InitUIElements(View view){
        userNameProgressDialogue = new ProgressDialog(getActivity());
        userNameProgressDialogue.setCancelable(false);
        userName = view.findViewById(R.id.viewProfile_name);
        userNumber = view.findViewById(R.id.viewProfile_email);
        createdCircles = view.findViewById(R.id.viewProfileCreatedCirclesCount);
        workingCircles = view.findViewById(R.id.viewProfileActiveCirclesCount);
        editProfPic = view.findViewById(R.id.profile_view_profilePicSetterImage);
        profileImageView = view.findViewById(R.id.profile_view_profile_image);
        finalizeChanges = view.findViewById(R.id.profile_finalize_changes);
        imageUploadProgressDialog = new ProgressDialog(getActivity());
        editName = view.findViewById(R.id.editName);
        logout = view.findViewById(R.id.profile_logout);
        back = view.findViewById(R.id.bck_view_edit_profile);
        userNameProgressDialogue = new ProgressDialog(getActivity());
        globalVariables.setEditView(view);
        editUserProfileImage = new EditProfileImage();
        edituserName  = new EdituserName();
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
        HelperMethodsUI.setUserProfileImage(user.getProfileImageLink(), getActivity(), profileImageView);
    }
    private void uploadUserProfilePic(){
        Log.d("File","uploadUserProfilePic called");
        imageUploadModel.imageUpload(filePath);
    }
    //code for upload the image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("File",String.valueOf(requestCode));
        switch(requestCode) {
            case PICK_IMAGE_ID:
                ImagePicker imagePicker = new ImagePicker(getActivity().getApplication());
                Bitmap bitmap = imagePicker.getImageFromResult(resultCode, data);
                filePath= imagePicker.getImageUri(bitmap);
                Log.d("File",filePath.toString());
                if(filePath !=null){
                    Log.d("File",filePath.toString());
                    uploadUserProfilePic();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onBackPressed() {

        int tempVisibility = finalizeChanges.getVisibility();
        if (finalizeChanges.getVisibility() == View.VISIBLE){
            alertDialog();
            AlertDialog alertDialog = confirmation.create();
            alertDialog.setTitle("Alert");
            alertDialog.show();
        }
        else {
            getActivity().finishAfterTransition();
            Intent intent = new Intent(getActivity(), ExploreTabbedActivity.class);
            startActivity(intent);
        }
    }
    public void alertDialog(){
        confirmation = new AlertDialog.Builder(getActivity());
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
                        Intent intent = new Intent(getActivity(), ExploreTabbedActivity.class);
                        startActivity(intent);
                    }
                });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void loadGlide(Uri downloadLink, Uri filePath, Context context){
        View view = globalVariables.getEditView();
        profileImageView = view.findViewById(R.id.profile_view_profile_image);
        finalizeChanges = view.findViewById(R.id.profile_finalize_changes);
        User user = globalVariables.getCurrentUser();
        Log.d("file","Context called::: "+ " "+imageUploadProgressDialog);
        if (context != null){
            Log.d("file",context.toString()+":::"+filePath.toString()+"::"+profileImageView);
            Glide.with(context).load(filePath).into(profileImageView);
            finalizeChanges.setVisibility(View.VISIBLE);
            user.setProfileImageLink(downloadLink.toString());
            globalVariables.saveCurrentUser(user);

        }
    }
}
