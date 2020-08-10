package circleapp.circlepackage.circle.DataLayer;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CirclePersonnelRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void applyOrJoin(Circle circle, User user, Subscriber subscriber) {
        if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
            globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            //adding userID to applicants list
            globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            SendNotification.sendApplication("new_applicant", user, circle, subscriber);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID(), subscriber.getToken_id(), subscriber.getName());
        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
            HashMap<String, Boolean> nowActive = new HashMap<>();
            if(user.getActiveCircles()!=null)
             nowActive = user.getActiveCircles();
            nowActive.put(circle.getId(), true);
            user.setActiveCircles(nowActive);
            UserRepository userRepository = new UserRepository();
            userRepository.updateUser(user);
        }
    }

    public void updateCirclePersonnelUserIfInvolved(User user, Subscriber subscriber){
        String key;
        if(user.getActiveCircles()!=null){
            for(Map.Entry<String, Boolean> entry : user.getActiveCircles().entrySet()) {
                key = entry.getKey();
                updateCirclePersonnel(user.getUserId(), key, subscriber);
            }
        }
    }

    public void updateCirclePersonnel(String  userId, String  circleId, Subscriber subscriber){
        globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child("members").child(userId).setValue(subscriber);
    }
}
