package circleapp.circlepackage.circle.ObjectModels;

import java.util.HashMap;

public class Broadcast {
    private String id, title, message, attachmentURI, creatorName, creatorID, creatorPhotoURI;
    private boolean pollExists, imageExists, adminVisibility;
    private long timeStamp, latestCommentTimestamp;
    private int numberOfComments;
    private Poll poll;
    HashMap<String, Boolean> listenersList;

    public boolean isImageExists() {
        return imageExists;
    }

    public void setImageExists(boolean imageExists) {
        this.imageExists = imageExists;
    }

    public HashMap<String, Boolean> getListenersList() {
        return listenersList;
    }

    public void setListenersList(HashMap<String, Boolean> listenersList) {
        this.listenersList = listenersList;
    }

    public Broadcast(String id, String title, String message, String attachmentURI, String creatorName, HashMap<String, Boolean> listenersList,
                     String creatorID, boolean pollExists, boolean imageExists, long timeStamp, Poll poll, String creatorPhotoURI,
                     long latestCommentTimestamp, int numberOfComments, boolean adminVisibility) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.attachmentURI = attachmentURI;
        this.listenersList = listenersList;
        this.creatorName = creatorName;
        this.creatorID = creatorID;
        this.pollExists = pollExists;
        this.imageExists = imageExists;
        this.creatorPhotoURI = creatorPhotoURI;
        this.timeStamp = timeStamp;
        this.poll = poll;
        this.latestCommentTimestamp = latestCommentTimestamp;
        this.numberOfComments = numberOfComments;
        this.adminVisibility = adminVisibility;
    }

    public Broadcast(){

    }
    public boolean isAdminVisibility() {
        return adminVisibility;
    }

    public void setAdminVisibility(boolean adminVisibility) {
        this.adminVisibility = adminVisibility;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Broadcast{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", attachmentURI='" + attachmentURI + '\'' +
                ", listenersList=" + listenersList  +
                ", creatorName='" + creatorName + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", creatorPhotoURI='" + creatorPhotoURI + '\'' +
                ", pollExists=" + pollExists +
                ", imageExists" + imageExists +
                ", timeStamp=" + timeStamp +
                ", latestCommentTimestamp=" + latestCommentTimestamp +
                ", numberOfComments=" + numberOfComments +
                ", poll=" + poll +
                ", adminVisibility=" + adminVisibility +
                '}';
    }
}
