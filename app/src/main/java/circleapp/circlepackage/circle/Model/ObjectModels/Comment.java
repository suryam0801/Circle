package circleapp.circlepackage.circle.Model.ObjectModels;

public class Comment {
    private String id, commentorName, comment, commentorId, commentorPicURL;
    private long timestamp;
    private long version;

    public Comment(){
    }

    public Comment(String id, String commentorName, String comment, String commentorId, String commentorPicURL, long timestamp, long version) {
        this.commentorName = commentorName;
        this.comment = comment;
        this.commentorId = commentorId;
        this.commentorPicURL = commentorPicURL;
        this.timestamp = timestamp;
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
