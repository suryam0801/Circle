package circleapp.circlepackage.circle.ObjectModels;

public class Broadcast {
    public String id, message, attachmentURI, pollID;
    public boolean pollExists;

    public Broadcast(String id, String message, String attachmentURI, String pollID, boolean pollExists) {
        this.id = id;
        this.message = message;
        this.attachmentURI = attachmentURI;
        this.pollID = pollID;
        this.pollExists = pollExists;
    }

    public Broadcast(){

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

    public String getPollID() {
        return pollID;
    }

    public void setPollID(String pollID) {
        this.pollID = pollID;
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
                ", pollID='" + pollID + '\'' +
                ", pollExists=" + pollExists +
                '}';
    }
}
