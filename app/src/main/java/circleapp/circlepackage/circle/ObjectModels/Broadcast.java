package circleapp.circlepackage.circle.ObjectModels;

public class Broadcast {
    private String id, message, attachmentURI, creatorName, creatorID, creatorPhotoURI;
    private boolean pollExists;
    private long timeStamp, latestCommentTimestamp;
    private int numberOfComments;
    private Poll poll;

    public Broadcast(String id, String message, String attachmentURI, String creatorName,
                     String creatorID, boolean pollExists, long timeStamp, Poll poll, String creatorPhotoURI,
                     long latestCommentTimestamp, int numberOfComments) {
        this.id = id;
        this.message = message;
        this.attachmentURI = attachmentURI;
        this.creatorName = creatorName;
        this.creatorID = creatorID;
        this.pollExists = pollExists;
        this.creatorPhotoURI = creatorPhotoURI;
        this.timeStamp = timeStamp;
        this.poll = poll;
        this.latestCommentTimestamp = latestCommentTimestamp;
        this.numberOfComments = numberOfComments;
    }

    public Broadcast(){

    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttachmentURI() {
        return attachmentURI;
    }

    public void setAttachmentURI(String attachmentURI) {
        this.attachmentURI = attachmentURI;
    }

    public boolean isPollExists() {
        return pollExists;
    }

    public void setPollExists(boolean pollExists) {
        this.pollExists = pollExists;
    }

    public String getCreatorPhotoURI() {
        return creatorPhotoURI;
    }

    public void setCreatorPhotoURI(String creatorPhotoURI) {
        this.creatorPhotoURI = creatorPhotoURI;
    }

    public long getLatestCommentTimestamp() {
        return latestCommentTimestamp;
    }

    public void setLatestCommentTimestamp(long latestCommentTimestamp) {
        this.latestCommentTimestamp = latestCommentTimestamp;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    @Override
    public String toString() {
        return "Broadcast{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", attachmentURI='" + attachmentURI + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", creatorPhotoURI='" + creatorPhotoURI + '\'' +
                ", pollExists=" + pollExists +
                ", timeStamp=" + timeStamp +
                ", latestCommentTimestamp=" + latestCommentTimestamp +
                ", numberOfComments=" + numberOfComments +
                ", poll=" + poll +
                '}';
    }
}
