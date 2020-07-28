package circleapp.circlepackage.circle.ViewModels;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.DataRepository.UserRepository;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class UserViewModel extends ViewModel{
    private GlobalVariables globalVariables = new GlobalVariables();
    public MutableLiveData<User> userObject = new MutableLiveData<>();
    public MutableLiveData<User> getUserObject(Context context) {
        getUser(context);
        return userObject;
    }
    private void getUser(Context context){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            UserRepository userRepository = new UserRepository();
            LiveData<DataSnapshot> liveData = userRepository.getDataSnapsUserValueLiveData(globalVariables.getAuthenticationToken().getCurrentUser().getUid());
            liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    userObject.postValue(user);
                    globalVariables.saveCurrentUser(user);
                }
            });
        }
    }
}
