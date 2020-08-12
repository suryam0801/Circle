package circleapp.circlepackage.circle.ui.EditProfile;

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
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.EditProfileViewModels.EditProfileViewModel;

public class EdituserName {

    private Dialog editUserNamedialogue;
    private ProgressDialog userNameProgressDialogue;
    public EditProfileViewModel editProfileViewModel;
    private GlobalVariables globalVariables = new GlobalVariables();
    private FragmentActivity EditProfileClassTemp;
    private User user;
    private TextView userName;

    public void edituserNamedialogue(FragmentActivity editProfile, TextView userName) {
        this.userName = userName;
        this.EditProfileClassTemp = editProfile;
        editUserNamedialogue = new Dialog(EditProfileClassTemp);
        editUserNamedialogue.setContentView(R.layout.user_name_edit_dialogue); //set dialog view
        final Button edit_name_finalize = editUserNamedialogue.findViewById(R.id.edit_name_Button);
        final EditText edit_name = editUserNamedialogue.findViewById(R.id.edit_name);
        edit_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        userNameProgressDialogue = new ProgressDialog(EditProfileClassTemp);
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
            if (globalVariables.getAuthenticationToken().getCurrentUser() == null){
                userNameProgressDialogue.dismiss();
                editUserNamedialogue.dismiss();
                Toast.makeText(EditProfileClassTemp, "Error try Again!!!!",Toast.LENGTH_SHORT).show();
            }
            else {
                user.setName(name);
                editProfileViewModel.editprofilename(profileUpdates,user,EditProfileClassTemp).observe(EditProfileClassTemp, state->{
                    if (state){
                        userName.setText(globalVariables.getAuthenticationToken().getCurrentUser().getDisplayName());
                        userNameProgressDialogue.dismiss();
                        editUserNamedialogue.dismiss();

                    }
                });
            }

        } else {
            Toast.makeText(EditProfileClassTemp, "Please Enter Your Name...", Toast.LENGTH_SHORT).show();
        }
    }
}
