package circleapp.circlepackage.circle.ObjectModels;

public class Comment {
    private String commentorName, comment, commentorId, commentorPicURL;
    private long timestamp;

    public Comment(){

    }

    public Comment(String commentorName, String comment, String commentorId, String commentorPicURL, long timestamp) {
        this.commentorName = commentorName;
        this.comment = comment;
        this.commentorId = commentorId;
        this.commentorPicURL = commentorPicURL;
        this.timestamp = timestamp;
    }

    public String getCommentorName() {
        return commentorName;
    }

    public void setCommentorName(String commentorName) {
        this.commentorName = commentorName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentorId() {
        return commentorId;
    }

    public void setCommentorId(String commentorId) {
        this.commentorId = commentorId;
    }

    public String getCommentorPicURL() {
        return commentorPicURL;
    }

    public void setCommentorPicURL(String commentorPicURL) {
        this.commentorPicURL = commentorPicURL;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
