package circleapp.circlepackage.circle.ViewModels.EditProfileViewModels;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.gson.Gson;

import circleapp.circlepackage.circle.DataLayer.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.ui.EditProfile.EditProfile;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> imageprogress;
    private MutableLiveData<Boolean> nameprogress;
    private MutableLiveData<Boolean> circlcePersonalprogress;
    GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<String[]> liveData;
    EditProfile editProfileClassTemp;
    public MutableLiveData<Boolean> editprofileimage(UserProfileChangeRequest profileUpdates, User user, EditProfile editProfileClassTemp) {
        imageprogress = new MutableLiveData<>();
        this.editProfileClassTemp = editProfileClassTemp;
        globalVariables.getAuthenticationToken().getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("gluser",globalVariables.getCurrentUser().toString());
                FirebaseWriteHelper.updateUser(globalVariables.getCurrentUser());
                imageprogress.setValue(true);
            }
        });
        MyCirclesViewModel viewModel = ViewModelProviders.of((FragmentActivity) editProfileClassTemp).get(MyCirclesViewModel.class);

        liveData = viewModel.getDataSnapsWorkbenchCircleLiveData(user.getUserId());

        liveData.observe((LifecycleOwner) editProfileClassTemp, returnArray -> {
            Circle circle = new Gson().fromJson(returnArray[0], Circle.class);
            Log.d("12345",circle.toString());
            Subscriber temp_subscriber = new Subscriber(globalVariables.getCurrentUser(),System.currentTimeMillis());
            FirebaseWriteHelper.updateCirclePersonnel(globalVariables.getCurrentUser(),circle,temp_subscriber);
        });
        Toast.makeText(editProfileClassTemp, "User Updated Successfully!!!!.....", Toast.LENGTH_SHORT).show();
        return imageprogress;
    }

    public MutableLiveData<Boolean> editprofilename(UserProfileChangeRequest profileUpdates, User user, EditProfile editProfileClassTemp){
        nameprogress = new MutableLiveData<>();
        globalVariables.getAuthenticationToken().getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseWriteHelper.updateUser(globalVariables.getCurrentUser());
                Log.d("edit",globalVariables.getAuthenticationToken().getCurrentUser().getDisplayName()+"stored");
                nameprogress.setValue(true);
            }
        });
        MyCirclesViewModel viewModel = ViewModelProviders.of((FragmentActivity) editProfileClassTemp).get(MyCirclesViewModel.class);

        liveData = viewModel.getDataSnapsWorkbenchCircleLiveData(user.getUserId());

        liveData.observe((LifecycleOwner) editProfileClassTemp, returnArray -> {
            Circle circle = new Gson().fromJson(returnArray[0], Circle.class);
            Log.d("12345",circle.toString());
            Subscriber temp_subscriber = new Subscriber(globalVariables.getCurrentUser(),System.currentTimeMillis());
            FirebaseWriteHelper.updateCirclePersonnel(globalVariables.getCurrentUser(),circle,temp_subscriber);
        });
        Toast.makeText(editProfileClassTemp, "User Updated Successfully!!!!.....", Toast.LENGTH_SHORT).show();
        return nameprogress;
    }
}
