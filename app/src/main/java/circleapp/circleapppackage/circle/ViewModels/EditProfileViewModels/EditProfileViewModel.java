package circleapp.circleapppackage.circle.ViewModels.EditProfileViewModels;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circleapppackage.circle.DataLayer.CirclePersonnelRepository;
import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> imageprogress;
    private MutableLiveData<Boolean> nameprogress;
    private UserRepository userRepository = new UserRepository();
    private CirclePersonnelRepository circlePersonnelRepository = new CirclePersonnelRepository();
    private GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;
    private FragmentActivity editProfileClassTemp;
    public MutableLiveData<Boolean> editprofileimage(UserProfileChangeRequest profileUpdates, User user, FragmentActivity editProfileClassTemp) {
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

    public MutableLiveData<Boolean> editprofilename(UserProfileChangeRequest profileUpdates, User user, FragmentActivity editProfileClassTemp){
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
