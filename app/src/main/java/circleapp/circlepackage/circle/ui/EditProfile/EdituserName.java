package circleapp.circlepackage.circle.ui.EditProfile;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class EdituserName {

    Dialog editUserNamedialogue, editUserProfiledialogue;
    private ProgressDialog userNameProgressDialogue, imageUploadProgressDialog;
    public EditProfileViewModel editProfileViewModel;

    public void edituserNamedialogue(Activity editProfile, User user, TextView userName) {
        editUserNamedialogue = new Dialog(editProfile);
        editUserNamedialogue.setContentView(R.layout.user_name_edit_dialogue); //set dialog view
        final Button edit_name_finalize = editUserNamedialogue.findViewById(R.id.edit_name_Button);
        final EditText edit_name = editUserNamedialogue.findViewById(R.id.edit_name);
        edit_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        userNameProgressDialogue = new ProgressDialog(editProfile);
        imageUploadProgressDialog = new ProgressDialog(editProfile);
        editProfileViewModel = ViewModelProviders.of((FragmentActivity) editProfile).get(EditProfileViewModel.class);

        edit_name_finalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit_name.getText().toString().replaceAll("\\s+", " ");
                ;
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
                        Toast.makeText(editProfile, "Error try Again!!!!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        editProfileViewModel.editprofilename(profileUpdates,user,editProfile).observe((LifecycleOwner) editProfile, state->{
                            if (state){
                                userName.setText(FirebaseWriteHelper.getUser().getDisplayName());
                                userNameProgressDialogue.dismiss();
                                editUserNamedialogue.dismiss();
                                user.setName(name);
                                SessionStorage.saveUser(editProfile,user);
                            }
                        });
                    }

                } else {
                    Toast.makeText(editProfile, "Please Enter Your Name...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editUserNamedialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserNamedialogue.show();


    }
}
