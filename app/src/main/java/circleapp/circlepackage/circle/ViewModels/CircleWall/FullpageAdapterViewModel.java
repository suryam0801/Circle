package circleapp.circlepackage.circle.ViewModels.CircleWall;

import android.content.Context;

import java.util.HashMap;

import circleapp.circlepackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circlepackage.circle.DataLayer.CircleRepository;
import circleapp.circlepackage.circle.DataLayer.CommentsRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Comment;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.ui.CircleWall.FullPageBroadcastCardAdapter;

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
    public void updateUserAfterReadingComments(Broadcast currentBroadcast, User user, String navFrom){

        HelperMethodsBL.updateUserFields(currentBroadcast, navFrom, user);
        HelperMethodsBL.initializeNewCommentsAlertTimestamp(currentBroadcast, user);
    }

    public void updateCommentNumbersPostCreate(Broadcast broadcast, long timetamp, Circle circle) {
        //updating broadCastTimeStamp after creating the comment
        CircleRepository circleRepository = new CircleRepository();
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        broadcastsRepository.updateBroadcast(broadcast,circle.getId());

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        circleRepository.updateCircle(circle);
    }

    public void makeCommentEntry(Context mContext, String commentMessage, Broadcast broadcast, User user, Circle circle) {
        CommentsRepository commentsRepository = new CommentsRepository();
        long currentCommentTimeStamp = System.currentTimeMillis();
        String commentId = commentsRepository.getCommentId(circle.getId(),broadcast.getId());
        Comment comment = new Comment(commentId,user.getName().trim(),commentMessage,user.getUserId(),user.getProfileImageLink(),currentCommentTimeStamp);

        commentsRepository.makeNewComment(comment, circle.getId(), broadcast.getId());
        SendNotification.sendCommentInfo(mContext,user.getUserId(), broadcast.getId(), circle.getName(), circle.getId(), user.getName(), broadcast.getListenersList(), circle.getBackgroundImageLink(), commentMessage,comment.getCommentorName());

        updateCommentNumbersPostCreate(broadcast, currentCommentTimeStamp, circle);
        HelperMethodsBL.updateUserFields(broadcast, "create", user);
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
