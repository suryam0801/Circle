package circleapp.circleapppackage.circle.ViewModels.LoginViewModels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.FirebaseApp;

import circleapp.circleapppackage.circle.DataLayer.FBRepository;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

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
        FBRepository fbRepository = new FBRepository();
        fbRepository.setPersistenceEnabled(context, true);
    }

    public void saveUserToSession(User user){
        globalVariables.saveCurrentUser(user);
    }
}
