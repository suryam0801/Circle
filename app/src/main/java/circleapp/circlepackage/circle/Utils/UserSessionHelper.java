package circleapp.circlepackage.circle.Utils;

import android.app.Activity;
import android.content.Context;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class UserSessionHelper {
    public UserSessionHelper(){
    }
    public void saveUserToSession(Context context, User user){
        SessionStorage.saveUser((Activity) context, user);
    }
    public User getUserFromSession(Context context){
        return SessionStorage.getUser((Activity) context);
    }
}
