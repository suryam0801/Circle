package circleapp.circlepackage.circle.Helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsLogEvents {
    private static FirebaseAnalytics firebaseAnalytics;
    public AnalyticsLogEvents()  {
    }
    public void logEvents(Context context,String id, String name, String type){

        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        Bundle params = new Bundle();
        params.putString(name,id);
        firebaseAnalytics.logEvent(type, params);


    }
    public void setCurrentScreen(Activity context, String screenName, String screenClass){
        firebaseAnalytics.setCurrentScreen(context, screenName, screenClass);
    }
}
