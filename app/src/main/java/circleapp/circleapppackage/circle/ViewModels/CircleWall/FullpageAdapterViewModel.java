package circleapp.circleapppackage.circle.ViewModels.CircleWall;

import android.content.Context;

import java.util.HashMap;

import circleapp.circleapppackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.DataLayer.CommentsRepository;
import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Helpers.SendNotification;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Comment;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageView.FullPageBroadcastCardAdapter;

public class FullpageAdapterViewModel {

    public FullpageAdapterViewModel(){}

    private BroadcastsRepository broadcastsRepository = new BroadcastsRepository();

    public void updateMutedList(User user, String circleId, String broadcastId, int transaction){
        UserRepository userRepository = new UserRepository();
        userRepository.updateUser(user);
        broadcastsRepository.broadcastListenerList(transaction, user.getUserId(), circleId, broadcastId);
    }
    public void updateBroadcastAfterPollAction(Broadcast broadcast, String circleId){
        broadcastsRepository.updateBroadcast(broadcast, circleId);
    }
    public void updateUserAfterReadingComments(Circle circle,Broadcast currentBroadcast, User user, String navFrom){

        HelperMethodsBL.initializeNewCommentsAlertTimestamp(currentBroadcast);
        HelperMethodsBL.updateUserFields(circle, currentBroadcast, navFrom);
    }

    public void updateCommentNumbersPostCreate(Broadcast broadcast, long timetamp, Circle circle) {
        //updating broadCastTimeStamp after creating the comment
        CircleRepository circleRepository = new CircleRepository();
        //updating latest timestamp in broadcasts
        broadcastsRepository.updateLatestTimeStampInBroadcast(broadcast.getId(), timetamp, circle.getId());

        //updating number of discussions in circle
        circleRepository.updateNoOfCommentsInBroadcast(circle,broadcast);

    }

    public void makeCommentEntry(Context mContext, String commentMessage, Broadcast broadcast, User user, Circle circle) {
        CommentsRepository commentsRepository = new CommentsRepository();
        long currentCommentTimeStamp = System.currentTimeMillis();
        String commentId = commentsRepository.getCommentId(circle.getId(),broadcast.getId());
        Comment comment = new Comment(commentId,user.getName().trim(),commentMessage,user.getUserId(),user.getProfileImageLink(),currentCommentTimeStamp,1);

        commentsRepository.makeNewComment(comment, circle.getId(), broadcast.getId());
        SendNotification.sendCommentInfo(mContext,user.getUserId(), broadcast.getId(), circle.getName(), circle.getId(), user.getName(), broadcast.getListenersList(), circle.getBackgroundImageLink(), commentMessage,comment.getCommentorName());

        //Circle and broadcast
        updateCommentNumbersPostCreate(broadcast, currentCommentTimeStamp, circle);
        updateUserAfterReadingComments(circle, broadcast, user, "create");

    }

    public void updatePollValues(FullPageBroadcastCardAdapter.ViewHolder viewHolder, Broadcast broadcast, User user, Poll poll, String option, Circle circle){
        HashMap<String, Integer> pollOptionsTemp = poll.getOptions();
        int currentSelectedVoteCount = poll.getOptions().get(option);

        if (viewHolder.getCurrentUserPollOption() == null) { //voting for first time
            ++currentSelectedVoteCount;
            pollOptionsTemp.put(option, currentSelectedVoteCount);
            viewHolder.setCurrentUserPollOption(option);
        } else if (!viewHolder.getCurrentUserPollOption().equals(option)) {
            int userPreviousVoteCount = poll.getOptions().get(viewHolder.getCurrentUserPollOption()); //repeated vote (regulates count)

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
        updateBroadcastAfterPollAction(broadcast, circle.getId());
    }

}
