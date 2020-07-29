package circleapp.circlepackage.circle.ViewModels.CreateCircle;

import circleapp.circlepackage.circle.DataLayer.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.User;

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