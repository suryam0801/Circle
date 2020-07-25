package circleapp.circlepackage.circle.ViewModels.EditProfileViewModels;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> imageprogress;
    private MutableLiveData<Boolean> nameprogress;
    public MutableLiveData<Boolean> editprofileimage(UserProfileChangeRequest profileUpdates, User user, Activity activity) {
        imageprogress = new MutableLiveData<>();
        FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseWriteHelper.updateUser(SessionStorage.getUser(activity), activity);
                imageprogress.setValue(true);
            }
        });
        return imageprogress;
    }

    public MutableLiveData<Boolean> editprofilename(UserProfileChangeRequest profileUpdates, User user, Activity activity){
        nameprogress = new MutableLiveData<>();
        FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseWriteHelper.updateUser(user, activity);
                Log.d("edit",FirebaseWriteHelper.getUser().getDisplayName()+"stored");
                nameprogress.setValue(true);
            }
        });
        return nameprogress;
    }
}
