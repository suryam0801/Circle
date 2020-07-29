package circleapp.circlepackage.circle.ViewModels.CircleWall;

import android.content.Context;

import circleapp.circlepackage.circle.DataLayer.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Comment;
import circleapp.circlepackage.circle.Model.ObjectModels.User;

public class FullpageAdapterViewModel {

    public FullpageAdapterViewModel(){}

    public void updateMutedList(User user, String circleId, String broadcastId, int transaction){
        FirebaseWriteHelper.updateUser(user);
        FirebaseWriteHelper.broadcastListenerList(transaction, user.getUserId(), circleId, broadcastId);
    }
    public void updateBroadcastAfterPollAction(Broadcast broadcast, String circleId){
        FirebaseWriteHelper.updateBroadcast(broadcast, circleId);
    }
    public void updateUserAfterReadingComments(Broadcast currentBroadcast, User user, String navFrom){

        HelperMethodsBL.updateUserFields(currentBroadcast, navFrom, user);
        HelperMethodsBL.initializeNewCommentsAlertTimestamp(currentBroadcast, user);
    }

    public void updateCommentNumbersPostCreate(Broadcast broadcast, long timetamp, Circle circle) {
        //updating broadCastTimeStamp after creating the comment
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        FirebaseWriteHelper.updateBroadcast(broadcast,circle.getId());

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        FirebaseWriteHelper.updateCircle(circle);
    }

    public void makeCommentEntry(Context mContext, String commentMessage, Broadcast broadcast, User user, Circle circle) {
        long currentCommentTimeStamp = System.currentTimeMillis();
        String commentId = FirebaseWriteHelper.getCommentId(circle.getId(),broadcast.getId());
        Comment comment = new Comment(commentId,user.getName().trim(),commentMessage,user.getUserId(),user.getProfileImageLink(),currentCommentTimeStamp);

        FirebaseWriteHelper.makeNewComment(comment, circle.getId(), broadcast.getId());
        SendNotification.sendCommentInfo(mContext,user.getUserId(), broadcast.getId(), circle.getName(), circle.getId(), user.getName(), broadcast.getListenersList(), circle.getBackgroundImageLink(), commentMessage,comment.getCommentorName());

        updateCommentNumbersPostCreate(broadcast, currentCommentTimeStamp, circle);
        HelperMethodsBL.updateUserFields(broadcast, "create", user);
    }

}
