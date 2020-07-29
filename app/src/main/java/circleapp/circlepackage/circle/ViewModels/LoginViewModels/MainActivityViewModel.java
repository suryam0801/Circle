package circleapp.circlepackage.circle.ViewModels.LoginViewModels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
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

public class MainActivityViewModel extends AndroidViewModel {

    private GlobalVariables globalVariables = new GlobalVariables();
    Context context;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        context = application;
    }

    public void setPersistenceEnabled(){
        if (FirebaseApp.getApps(context)==null) {
            FirebaseApp.initializeApp(context);
        }
        FirebaseWriteHelper.setPersistenceEnabled(context, true);
    }

    public void saveUserToSession(User user){
        globalVariables.saveCurrentUser(user);
    }
}
