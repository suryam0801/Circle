package circleapp.circlepackage.circle.ViewModels.LoginViewModels;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class UpdateUserStatus extends ViewModel {

    private GlobalVariables globalVariables = new GlobalVariables();

    private MutableLiveData<String> isUserExisting;
    public MutableLiveData<String> listenForUserUpdates(String  userStatus, Context context) {
        if (userStatus==null) {
            isUserExisting = new MutableLiveData<>();
        }
        else {
            setPersistenceEnabled(context);
            checkIfUserExists(context);
        }
        return isUserExisting;
    }
    public void checkIfUserExists(Context context){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(UserViewModel.class);

            LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(globalVariables.getAuthenticationToken().getCurrentUser().getUid());

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
        }
        FirebaseWriteHelper.setPersistenceEnabled(context, true);
    }
}
