package circleapp.circlepackage.circle.ui.EditProfile;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class EdituserName {

    Dialog editUserNamedialogue, editUserProfiledialogue;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    public EditProfileViewModel editProfileViewModel;
    private GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;
    EditProfile EditProfileClassTemp;
    User user;

    public void edituserNamedialogue(EditProfile editProfile) {
        this.EditProfileClassTemp = editProfile;
        editUserNamedialogue = new Dialog(EditProfileClassTemp);
        editUserNamedialogue.setContentView(R.layout.user_name_edit_dialogue); //set dialog view
        final Button edit_name_finalize = editUserNamedialogue.findViewById(R.id.edit_name_Button);
        final EditText edit_name = editUserNamedialogue.findViewById(R.id.edit_name);
        edit_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        userNameProgressDialogue = new ProgressDialog(EditProfileClassTemp);
        imageUploadProgressDialog = new ProgressDialog(EditProfileClassTemp);
        editProfileViewModel = ViewModelProviders.of(EditProfileClassTemp).get(EditProfileViewModel.class);

        edit_name_finalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString().replaceAll("\\s+", " ");
                UpdateName(name);
            }
        });

        editUserNamedialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserNamedialogue.show();


    }
    private void UpdateName(String name){
        user = globalVariables.getCurrentUser();
        if (!TextUtils.isEmpty(name)) {
            userNameProgressDialogue.setTitle("Updating Name....");
            userNameProgressDialogue.show();
//                    String userId = FirebaseWriteHelper.getUserId();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            if (FirebaseWriteHelper.getUser() == null){
                userNameProgressDialogue.dismiss();
                editUserNamedialogue.dismiss();
                Toast.makeText(EditProfileClassTemp, "Error try Again!!!!",Toast.LENGTH_SHORT).show();
            }
            else {
                editProfileViewModel.editprofilename(profileUpdates,user,EditProfileClassTemp).observe(EditProfileClassTemp, state->{
                    if (state){
                        EditProfileClassTemp.userName.setText(FirebaseWriteHelper.getUser().getDisplayName());
                        userNameProgressDialogue.dismiss();
                        editUserNamedialogue.dismiss();
                        user.setName(name);
                        globalVariables.saveCurrentUser(user);
                    }
                });
            }

        } else {
            Toast.makeText(EditProfileClassTemp, "Please Enter Your Name...", Toast.LENGTH_SHORT).show();
        }
    }
}
