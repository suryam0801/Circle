package circleapp.circleapppackage.circle.DataLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class UserRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void updateUserObjectWhenDeletingBroadcast(User user, Broadcast broadcast) {
        //remove listening broadcast
        List<String> tempListening;
        if (user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId())) {
            tempListening = new ArrayList<>(user.getMutedBroadcasts());
            tempListening.remove(broadcast.getId());
            user.setMutedBroadcasts(tempListening);
            updateUser(user);
        }

        //remove no of read discussions
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() != null && user.getNoOfReadDiscussions().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.remove(broadcast.getId());
            user.setNoOfReadDiscussions(userNoReadComments);
            updateUser(user);
        }

        //remove new timestamp comments
        HashMap<String, Long> userCommentTimestamps;
        if (user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().containsKey(broadcast.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userCommentTimestamps = new HashMap<>(user.getNewTimeStampsComments());
            userCommentTimestamps.remove(broadcast.getId());
            user.setNewTimeStampsComments(userCommentTimestamps);
            updateUser(user);
        }
    }

    public void updateUserNewTimeStampComments(String userId, String broadcastId, long latestTimestamp) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("newTimeStampsComments").child(broadcastId).setValue(latestTimestamp);
    }

    public void updateUserNewReadComments(String userId, String broadcastId, long numberOfComments) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("noOfReadDiscussions").child(broadcastId).setValue(numberOfComments);
    }

    public void updateUserCount(String userId, String circleId, long noOfBroadcasts) {
        globalVariables.getFBDatabase().getReference("/Users").child(userId).child("notificationsAlert").child(circleId).setValue(noOfBroadcasts);
    }

    public void initializeNewCount(Circle c, User user) {
        if (user.getNotificationsAlert() != null && !user.getNotificationsAlert().containsKey(c.getId())) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user);
        } else if (user.getNotificationsAlert() == null) {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            updateUser(user);
        }
    }

    public void updateUser(User user) {
        globalVariables.saveCurrentUser(user);
        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).setValue(user);

    }

    public void updateUserReadDiscussions(User user, Broadcast broadcast){
        FBTransactions fbTransactions = new FBTransactions(globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("noOfReadDiscussions").child(broadcast.getId()));
        fbTransactions.runTransactionOnIncrementalValues(1);
    }

    public String getUserId() {
        String userId = globalVariables.getFBDatabase().getReference("/Users").child(globalVariables.getAuthenticationToken().getCurrentUser().getUid()).push().getKey();
        return userId;
    }

    public void updateUserNotifsIndicator(User user, HashMap<String, Integer> notifStore) {
        globalVariables.getFBDatabase().getReference("/Users").child(user.getUserId()).child("notificationsAlert").setValue(notifStore);
    }
}
