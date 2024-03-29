package circleapp.circleapppackage.circle.Model.ObjectModels;

import java.util.HashMap;

public class Broadcast {
    private String id, title, message, attachmentURI, creatorName, creatorID, creatorPhotoURI;
    private boolean pollExists, imageExists,fileExists, adminVisibility;
    private long timeStamp, latestCommentTimestamp;
    private Poll poll;
    private HashMap<String, Boolean> listenersList;
    private long version;

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
                     String creatorID, boolean pollExists, boolean imageExists,boolean fileExists, long timeStamp, Poll poll, String creatorPhotoURI,
                     long latestCommentTimestamp, boolean adminVisibility, long version) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.attachmentURI = attachmentURI;
        this.listenersList = listenersList;
        this.creatorName = creatorName;
        this.creatorID = creatorID;
        this.pollExists = pollExists;
        this.imageExists = imageExists;
        this.fileExists = fileExists;
        this.creatorPhotoURI = creatorPhotoURI;
        this.timeStamp = timeStamp;
        this.poll = poll;
        this.latestCommentTimestamp = latestCommentTimestamp;
        this.adminVisibility = adminVisibility;
        this.version = version;
    }

    public Broadcast(){

    }
    public boolean isAdminVisibility() {
        return adminVisibility;
    }

    public void setAdminVisibility(boolean adminVisibility) {
        this.adminVisibility = adminVisibility;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFileExists() {
        return fileExists;
    }

    public void setFileExists(boolean fileExists) {
        this.fileExists = fileExists;
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
                ", fileExists" + fileExists +
                ", timeStamp=" + timeStamp +
                ", latestCommentTimestamp=" + latestCommentTimestamp +
                ", poll=" + poll +
                ", adminVisibility=" + adminVisibility +
                ", version=" + version +
                '}';
    }
}