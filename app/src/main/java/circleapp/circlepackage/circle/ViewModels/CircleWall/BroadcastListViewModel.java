package circleapp.circlepackage.circle.ViewModels.CircleWall;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class BroadcastListViewModel {
    public BroadcastListViewModel(){}
    public void updateListenerListOfBroadcast(int addOrRemove, User user, String circleId, String broadcastId){
        FirebaseWriteHelper.broadcastListenerList(addOrRemove, user.getUserId(), circleId, broadcastId);
        FirebaseWriteHelper.updateUser(user);
    }
    public void updateBroadcastOnPollInteraction(Broadcast broadcast, String circleId){
        FirebaseWriteHelper.updateBroadcast(broadcast, circleId);
    }
    public void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts, User user){
        FirebaseWriteHelper.deleteBroadcast(circleId, broadcast, noOfBroadcasts, user);
    }
}
