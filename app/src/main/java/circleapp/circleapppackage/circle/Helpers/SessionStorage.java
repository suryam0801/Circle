package circleapp.circleapppackage.circle.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SessionStorage {

    private static final String TAG = "SessionStorage";
    public static final String PREF_NAME = "Project";

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

}
