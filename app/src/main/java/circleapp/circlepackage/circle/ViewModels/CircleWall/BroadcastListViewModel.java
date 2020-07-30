package circleapp.circlepackage.circle.ViewModels.CircleWall;

import circleapp.circlepackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.User;

public class BroadcastListViewModel {
    public BroadcastListViewModel(){}
    private BroadcastsRepository broadcastsRepository = new BroadcastsRepository();
    public void updateListenerListOfBroadcast(int addOrRemove, User user, String circleId, String broadcastId){
        UserRepository userRepository = new UserRepository();
        broadcastsRepository.broadcastListenerList(addOrRemove, user.getUserId(), circleId, broadcastId);
        userRepository.updateUser(user);
    }
    public void updateBroadcastOnPollInteraction(Broadcast broadcast, String circleId){
        broadcastsRepository.updateBroadcast(broadcast, circleId);
    }
    public void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts, User user){
        broadcastsRepository.deleteBroadcast(circleId, broadcast, noOfBroadcasts, user);
    }
}
