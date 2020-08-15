package circleapp.circleapppackage.circle.DataLayer;

import circleapp.circleapppackage.circle.Model.ObjectModels.Comment;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class CommentsRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void makeNewComment(Comment comment, String circleId, String broadcastId) {
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).child(comment.getId()).setValue(comment);
    }
    public String getCommentId(String circleId, String broadcastId){
        String commentId = globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).push().getKey();
        return commentId;
    }

    public void deleteComment(Comment comment, String circleId, String broadcastId){
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).child(comment.getId()).removeValue();
        FBTransactions fbTransactions = new FBTransactions(globalVariables.getFBDatabase().getReference("/Circles").child(circleId).child("noOfCommentsPerBroadcast").child(broadcastId));
        fbTransactions.runTransactionOnIncrementalValues(-1);
    }

}
