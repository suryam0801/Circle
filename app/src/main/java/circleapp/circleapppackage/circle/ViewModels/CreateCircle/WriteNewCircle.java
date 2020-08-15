package circleapp.circleapppackage.circle.ViewModels.CreateCircle;

import java.util.HashMap;

import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class WriteNewCircle {
    public WriteNewCircle(){}
    private GlobalVariables globalVariables = new GlobalVariables();

    public void writeCircleToDb(Circle circle, User user, Subscriber subscriber){
        CircleRepository circleRepository = new CircleRepository();
        circleRepository.createUserMadeCircle(circle, subscriber);
        updateUserObject(user, circle);
        circleRepository.addUsersToCircle(circle,"admin");
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