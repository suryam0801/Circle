package circleapp.circlepackage.circle.DataLayer;

import circleapp.circlepackage.circle.Model.ObjectModels.Comment;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CommentsRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void makeNewComment(Comment comment, String circleId, String broadcastId) {
        globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).push().setValue(comment);
    }
    public String getCommentId(String circleId, String broadcastId){
        String commentId = globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).push().getKey();
        return commentId;
    }

}
