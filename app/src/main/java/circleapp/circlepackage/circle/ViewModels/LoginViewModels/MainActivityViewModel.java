package circleapp.circlepackage.circle.ViewModels.LoginViewModels;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.DataRepository.UserRepository;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class MainActivityViewModel extends ViewModel {

    private GlobalVariables globalVariables = new GlobalVariables();

    private MutableLiveData<String> isUserExisting = new MutableLiveData<>();;
    public MutableLiveData<String> getUserData(Context context) {
            setPersistenceEnabled(context);
            checkIfUserExists(context);
            return isUserExisting;
    }
    public void checkIfUserExists(Context context){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            UserRepository userRepository = new UserRepository();
            LiveData<DataSnapshot> liveData = userRepository.getDataSnapsUserValueLiveData(globalVariables.getAuthenticationToken().getCurrentUser().getUid());

            liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    isUserExisting.setValue("existing_user");
                    globalVariables.saveCurrentUser(user);
                } else {
                    isUserExisting.setValue("repeat_user");
                }
            });
        }
        else {
            isUserExisting.setValue("new_user");
        }
    }
    private void setPersistenceEnabled(Context context){
        if (FirebaseApp.getApps(context)==null) {
            FirebaseApp.initializeApp(context);
            FirebaseWriteHelper.setPersistenceEnabled(context, true);
        }
    }
}
