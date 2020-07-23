package circleapp.circlepackage.circle.ViewModels.CreateCircle;

import android.app.Activity;
import android.content.Context;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class WriteNewCircle {
    public static void writeCircleToDb(Context context, Circle circle, User user, Subscriber subscriber){
        FirebaseWriteHelper.createUserMadeCircle(circle, subscriber);
        updateUserObject(context, user, circle);
    }
    private static void updateUserObject(Context context, User user, Circle circle){
        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        FirebaseWriteHelper.updateUser(user, context);
        SessionStorage.saveCircle((Activity) context, circle);
    }
}