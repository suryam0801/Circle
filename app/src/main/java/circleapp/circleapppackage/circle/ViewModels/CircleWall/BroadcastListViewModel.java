package circleapp.circleapppackage.circle.ViewModels.CircleWall;

import java.util.HashMap;

import circleapp.circleapppackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.BroadcastListAdapter;

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

    public void updatePollAnswer(String option, BroadcastListAdapter.ViewHolder viewHolder, Broadcast broadcast, Poll poll, Circle circle, User user) {
        HashMap<String, Integer> pollOptionsTemp = poll.getOptions();
        int currentSelectedVoteCount = poll.getOptions().get(option);

        if (viewHolder.getCurrentUserPollOption() == null) { //voting for first time
            ++currentSelectedVoteCount;
            pollOptionsTemp.put(option, currentSelectedVoteCount);
            viewHolder.setCurrentUserPollOption(option);
        } else if (!viewHolder.getCurrentUserPollOption().equals(option)) {
            int userPreviousVoteCount = poll.getOptions().get(viewHolder.getCurrentUserPollOption()); //repeated vote (regulates count)

            if (userPreviousVoteCount != 0)
                --userPreviousVoteCount;

            ++currentSelectedVoteCount;
            pollOptionsTemp.put(option, currentSelectedVoteCount);
            pollOptionsTemp.put(viewHolder.getCurrentUserPollOption(), userPreviousVoteCount);
            viewHolder.setCurrentUserPollOption(option);

        }

        HashMap<String, String> userResponseHashmap;
        if (poll.getUserResponse() != null) {
            userResponseHashmap = new HashMap<>(poll.getUserResponse());
            userResponseHashmap.put(user.getUserId(), viewHolder.getCurrentUserPollOption());
        } else {
            userResponseHashmap = new HashMap<>();
            userResponseHashmap.put(user.getUserId(), viewHolder.getCurrentUserPollOption());
        }

        poll.setOptions(pollOptionsTemp);
        poll.setUserResponse(userResponseHashmap);
        broadcast.setPoll(poll);
        updateBroadcastOnPollInteraction(broadcast,circle.getId());
    }
}
