package circleapp.circleapppackage.circle.DataLayer;

import android.util.Log;

import java.util.HashMap;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class BroadcastsRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts, User user) {

        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        UserRepository userRepository = new UserRepository();
        if (broadcast.isImageExists())
            storageReferenceRepository.removeBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcast.getId()).removeValue();
        FBTransactions noOfBroadcastsUpdate = new FBTransactions(globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfBroadcasts"));
        noOfBroadcastsUpdate.runTransactionOnIncrementalValues(-1);
        userRepository.updateUserObjectWhenDeletingBroadcast(user, broadcast);
    }

    public void writeBroadcast(Circle circle, Broadcast broadcast, int newCount) {
        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
        Log.d("passingbroad", broadcast.toString());
        if(circle.getNoOfCommentsPerBroadcast()!=null){
            if(circle.getNoOfCommentsPerBroadcast().get(broadcast.getId())==null)
                globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("noOfCommentsPerBroadcast").child(broadcast.getId()).setValue(0);
        }
        else
            globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("noOfCommentsPerBroadcast").child(broadcast.getId()).setValue(0);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circle.getId()).child(broadcast.getId()).setValue(broadcast);
        //globalVariables.getFBDatabase().getReference("/Users").child("noOfReadDiscussions").child(broadcast.getId()).setValue(0);
        storageReferenceRepository.addBroadcastImageReference(circle.getId(), broadcast.getId(), broadcast.getAttachmentURI());
        globalVariables.getFBDatabase().getReference("/Circles").child(circle.getId()).child("lastActivityTimeStamp").setValue(System.currentTimeMillis());
    }

    public void updateBroadcast(Broadcast broadcast, String circleId) {
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).setValue(broadcast);
        globalVariables.saveCurrentBroadcast(broadcast);
    }

    public String getBroadcastId(String circleId) {
        String broadcastId = globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).push().getKey();
        return broadcastId;
    }

    public void broadcastListenerList(int transaction, String userId, String circleId, String broadcastId) {
        if (transaction == 0)
            globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("listenersList").child(userId).setValue(true);
        else
            globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("listenersList").child(userId).removeValue();
    }

    public void updateLatestTimeStampInBroadcast(String broadcastId, long timestamp, String circleId){
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("latestCommentTimestamp").setValue(timestamp);
    }
}
