package circleapp.circlepackage.circle.ViewModels.CreateCircle;

import android.content.Context;

import java.util.HashMap;

import circleapp.circlepackage.circle.DataLayer.CircleRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class WriteNewCircle {
    public WriteNewCircle(){}
    private GlobalVariables globalVariables = new GlobalVariables();

    public void writeCircleToDb(Circle circle, User user, Subscriber subscriber, Context context){
        CircleRepository circleRepository = new CircleRepository();
        circleRepository.createUserMadeCircle(circle, subscriber);
        updateUserObject(user, circle);
        circleRepository.addUsersToCircle(circle, context);
    }

    private void updateUserObject(User user, Circle circle){
        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        HashMap<String, Boolean> activeCircleList = new HashMap<>();
        if(user.getActiveCircles()!=null)
            activeCircleList = user.getActiveCircles();
        activeCircleList.put(circle.getId(), true);
        user.setActiveCircles(activeCircleList);
        UserRepository userRepository = new UserRepository();
        userRepository.updateUser(user);
        globalVariables.saveCurrentCircle(circle);
    }
}