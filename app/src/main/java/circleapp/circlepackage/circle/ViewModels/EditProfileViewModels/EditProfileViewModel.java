package circleapp.circlepackage.circle.ViewModels.EditProfileViewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> imageprogress;
    private MutableLiveData<Boolean> nameprogress;
    private MutableLiveData<Boolean> circlcePersonalprogress;
    GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;
    public MutableLiveData<Boolean> editprofileimage(UserProfileChangeRequest profileUpdates, User user) {
        imageprogress = new MutableLiveData<>();
        FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("gluser",globalVariables.getCurrentUser().toString());
                FirebaseWriteHelper.updateUser(globalVariables.getCurrentUser());
                imageprogress.setValue(true);
            }
        });
        return imageprogress;
    }
    public MutableLiveData<Boolean> updateCirclePersonal(Circle circle, Subscriber temp_subscriber){
        circlcePersonalprogress = new MutableLiveData<>();
        FirebaseWriteHelper.updateCirclePersonnel(globalVariables.getCurrentUser(),circle,temp_subscriber);
        return circlcePersonalprogress;
    }
    public MutableLiveData<Boolean> editprofilename(UserProfileChangeRequest profileUpdates, User user){
        nameprogress = new MutableLiveData<>();
        FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseWriteHelper.updateUser(globalVariables.getCurrentUser());
                Log.d("edit",FirebaseWriteHelper.getUser().getDisplayName()+"stored");
                nameprogress.setValue(true);
            }
        });
        return nameprogress;
    }
}
