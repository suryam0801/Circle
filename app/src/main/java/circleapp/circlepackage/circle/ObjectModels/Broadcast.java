package circleapp.circlepackage.circle.ObjectModels;

public class Broadcast {
    public String id, message, attachmentURI, creatorName, creatorID;
    public boolean pollExists;
    public long timeStamp;
    public Poll poll;

    public Broadcast(String id, String message, String attachmentURI, String creatorName,
                     String creatorID, boolean pollExists, long timeStamp, Poll poll) {
        this.id = id;
        this.message = message;
        this.attachmentURI = attachmentURI;
        this.creatorName = creatorName;
        this.creatorID = creatorID;
        this.pollExists = pollExists;
        this.timeStamp = timeStamp;
        this.poll = poll;
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

    @Override
    public String toString() {
        return "Broadcast{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", attachmentURI='" + attachmentURI + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", pollExists=" + pollExists +
                ", timeStamp=" + timeStamp +
                ", poll=" + poll.toString() +
                '}';
    }
}
