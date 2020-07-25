package circleapp.circlepackage.circle.ViewModels.CreateCircle;

import android.app.Activity;
import android.content.Context;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class WriteNewCircle {
    public WriteNewCircle(){}
    private GlobalVariables globalVariables = new GlobalVariables();
    public void writeCircleToDb(Circle circle, User user, Subscriber subscriber){
        FirebaseWriteHelper.createUserMadeCircle(circle, subscriber);
        updateUserObject(user, circle);
    }
    private void updateUserObject(User user, Circle circle){
        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        FirebaseWriteHelper.updateUser(user);
        globalVariables.saveCurrentCircle(circle);
    }
}