package circleapp.circlepackage.circle;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import circleapp.circlepackage.circle.ObjectModels.User;

public class SessionStorage {
    private static final String TAG = "SessionStorage";
    public static final String PREF_NAME= "Project";

    public static void saveUser(Activity activity, User user)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(user);
        editor.putString("tempUser", string);
        editor.apply();
    }
}
