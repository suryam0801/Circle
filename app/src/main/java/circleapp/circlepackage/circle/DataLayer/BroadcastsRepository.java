package circleapp.circlepackage.circle.DataLayer;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class BroadcastsRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void deleteBroadcast(String circleId, Broadcast broadcast, int noOfBroadcasts, User user) {

        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        UserRepository userRepository = new UserRepository();
        Log.d("wefkjn", "tempListening.toString()");
        if (broadcast.isImageExists())
            storageReferenceRepository.removeBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).removeValue();
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcast.getId()).removeValue();
        FBTransactions noOfBroadcastsUpdate = new FBTransactions(globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfBroadcasts"));
        noOfBroadcastsUpdate.runTransactionOnIncrementalValues(-1);
        userRepository.updateUserObjectWhenDeletingBroadcast(user, broadcast);
    }

    public void writeBroadcast(String circleId, Broadcast broadcast, int newCount) {
        StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
        globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfBroadcasts").setValue(newCount);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcast.getId()).setValue(broadcast);
        storageReferenceRepository.addBroadcastImageReference(circleId, broadcast.getId(), broadcast.getAttachmentURI());
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

    public String createPhotoBroadcast(String title, String photoUri, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {

        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, null, photoUri, creatorName, null, "AdminId", false, true, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments,true);
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public String createPollBroadcast(String text, String creatorName, int offsetTimeStamp, HashMap<String, Integer> pollOptions, String downloadUri, int noOfComments, String circleId) {
        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast;
        Poll poll = new Poll(text, pollOptions, null);
        if (downloadUri != null)
            broadcast = new Broadcast(id, null, null, downloadUri, creatorName, null, "AdminId", true, true, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        else
            broadcast = new Broadcast(id, null, null, null, creatorName, null, "AdminId", true, false, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments,true);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public String createMessageBroadcast(String title, String message, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        String id = HelperMethodsUI.uuidGet();
        Broadcast broadcast = new Broadcast(id, title, message, null, creatorName, null, "AdminId", false, false, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments, true);
        globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public void updateNoOfCommentsInBroadcast(String broadcastId, int counter, String circleId) {
        FBTransactions fbTransactions = new FBTransactions(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId).child("numberOfComments"));
        fbTransactions.runTransactionOnIncrementalValues(counter);
    }
}
