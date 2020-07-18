package circleapp.circlepackage.circle.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Notification;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class SessionStorage {

    private static final String TAG = "SessionStorage";
    public static final String PREF_NAME = "Project";

    public static void saveLoginUserObject(Activity activity, LoginUserObject loginUserObject) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(loginUserObject);
        editor.putString("loginUserObject", string);
        editor.apply();
    }

    public static LoginUserObject getLoginUserObject(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("loginUserObject", "default");
        return new Gson().fromJson(string, LoginUserObject.class);
    }

    public static void saveCircle(Activity activity, Circle circle) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(circle);
        //Log.d(TAG, "saveCircle: "+string);
        editor.putString("1", "1");
        editor.putString("project", string);
        editor.apply();
    }

    public static Circle getCircle(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("project", "1234");
        //Log.d(TAG, "getCircle: "+string);
        return new Gson().fromJson(string, Circle.class);
    }

    public static void saveUser(Activity activity, User user) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(user);
        editor.putString("tempUser", string);
        editor.apply();
    }

    public static User getUser(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempUser", "1234");
        return new Gson().fromJson(string, User.class);
    }

    public static void saveBroadcast(Activity activity, Broadcast broadcast) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(broadcast);
        editor.putString("tempBroadcast", string);
        editor.apply();
    }

    public static Broadcast getBroadcast(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempBroadcast", "1234");
        return new Gson().fromJson(string, Broadcast.class);
    }

    public static void saveBroadcastList(Activity activity, List<Broadcast> broadcastList) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(broadcastList);
        editor.putString("tempBroadcastList", string);
        editor.apply();
    }

    public static List<Broadcast> getBroadcastList(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempBroadcastList", "1234");
        Type type = new TypeToken<List<Broadcast>>() {
        }.getType();
        return new Gson().fromJson(string, type);
    }


    public static void saveFilters(Activity activity, List<String> filterList) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String string = new Gson().toJson(filterList);
        editor.putString("tempFiltersList", string);
        editor.apply();
    }

    public static List<String> getFilters(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("tempFiltersList", "1234");
        if (string.equals("1234"))
            return null;

        Type type = new TypeToken<List<String>>() {
        }.getType();
        return new Gson().fromJson(string, type);
    }

    public static void saveCircleWallBgImage(Activity activity, String imageName) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("bgImageName", imageName);
        editor.apply();
    }

    public static String getCircleWallBgImage(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String string = sharedPref.getString("bgImageName", "1234");
        if (string.equals("1234"))
            return null;

        return string;
    }

    public static void tempIndexStore(Activity activity, int index) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("tempIndexStore", index);
        editor.apply();
    }

    public static int getTempIndexStore(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        int index = sharedPref.getInt("tempIndexStore", -1);
        if (index == -1)
            return 0;

        return index;
    }

}
