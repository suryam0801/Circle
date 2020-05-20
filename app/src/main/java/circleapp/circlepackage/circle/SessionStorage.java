package circleapp.circlepackage.circle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class SessionStorage {

    private static final String TAG = "SessionStorage";
    public static final String PREF_NAME= "Project";

    public static void saveCircle(Activity activity, Circle circle)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(circle);
        //Log.d(TAG, "saveCircle: "+string);
        editor.putString("1", "1");
        editor.putString("project", string);
        editor.apply();
    }

    public static Circle getCircle(Activity activity)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        String string = sharedPref.getString("project","1234");
        //Log.d(TAG, "getCircle: "+string);
        return new Gson().fromJson(string, Circle.class);
    }

    public static void saveUser(Activity activity, User user)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(user);
        editor.putString("tempUser", string);
        editor.apply();
    }

    public static User getUser(Activity activity)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempUser","1234");
        return new Gson().fromJson(string, User.class);
    }


/*
    public static void saveWorker(Activity activity, Worker worker)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(worker);
        editor.putString("memberTransfer", string);
        editor.apply();
    }

    public static Worker getWorker(Activity activity)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        String string = sharedPref.getString("memberTransfer","1234");
        return new Gson().fromJson(string, Worker.class);
    }
*/

/*
    public static void saveApplicant(Activity activity, Applicant applicant)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(applicant);
        editor.putString("tempApplicant", string);
        editor.apply();
    }

    public static Applicant getApplicant(Activity activity)
    {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME,Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempApplicant","1234");
        return new Gson().fromJson(string, Applicant.class);
    }
*/

}