package circleapp.circlepackage.circle.ViewModels.EditProfileViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.DataLayer.CirclePersonnelRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.EditProfile.EditProfile;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> imageprogress;
    private MutableLiveData<Boolean> nameprogress;
    private UserRepository userRepository = new UserRepository();
    private CirclePersonnelRepository circlePersonnelRepository = new CirclePersonnelRepository();
    private GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;
    private EditProfile editProfileClassTemp;
    public MutableLiveData<Boolean> editprofileimage(UserProfileChangeRequest profileUpdates, User user, EditProfile editProfileClassTemp) {
        imageprogress = new MutableLiveData<>();
        this.editProfileClassTemp = editProfileClassTemp;
        globalVariables.getAuthenticationToken().getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                userRepository.updateUser(globalVariables.getCurrentUser());
                imageprogress.setValue(true);
            }
        });
        circlePersonnelRepository.updateCirclePersonnelUserIfInvolved(user, new Subscriber(user,System.currentTimeMillis()));
//        Toast.makeText(editProfileClassTemp, "User Updated Successfully!!!!.....", Toast.LENGTH_SHORT).show();
        return imageprogress;
    }

    public MutableLiveData<Boolean> editprofilename(UserProfileChangeRequest profileUpdates, User user, EditProfile editProfileClassTemp){
        nameprogress = new MutableLiveData<>();
        globalVariables.getAuthenticationToken().getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                userRepository.updateUser(globalVariables.getCurrentUser());
                nameprogress.setValue(true);
            }
        });
        //update circle personnel
        circlePersonnelRepository.updateCirclePersonnelUserIfInvolved(user, new Subscriber(user,System.currentTimeMillis()));
//        Toast.makeText(editProfileClassTemp, "User Updated Successfully!!!!.....", Toast.LENGTH_SHORT).show();
        return nameprogress;
    }
}
