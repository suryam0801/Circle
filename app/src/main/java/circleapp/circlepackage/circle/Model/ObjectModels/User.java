package circleapp.circlepackage.circle.Model.ObjectModels;

import java.util.HashMap;
import java.util.List;

public class User  {


    private String Name, contact, profileImageLink, userId, token_id, district, ward;
    private HashMap<String, Boolean> activeCircles;
    private HashMap<String, Integer> notificationsAlert;
    private HashMap<String, Integer> noOfReadDiscussions;
    private HashMap<String, Long> newTimeStampsComments;
    private List<String> mutedBroadcasts;
    private int createdCircles, completedProjects;
    private long version;


    public User(){

    }

    public User(String Name, String contact,
                String profileImageLink, String userId,
                int createdCircles, HashMap<String, Boolean> activeCircles, int completedProjects, String token_id,
                String ward, String district, HashMap<String, Integer> notificationsAlert,
                HashMap<String, Long> newDiscussionAlert, HashMap<String, Integer> noOfNewComments, List<String> mutedBroadcasts, long version) {
        this.Name = Name;
        this.contact = contact;
        this.profileImageLink = profileImageLink;
        this.userId = userId;
        this.createdCircles = createdCircles;
        this.activeCircles = activeCircles;
        this.completedProjects = completedProjects;
        this.token_id = token_id;
        this.ward = ward;
        this.district = district;
        this.notificationsAlert = notificationsAlert;
        this.newTimeStampsComments = newDiscussionAlert;
        this.noOfReadDiscussions = noOfNewComments;
        this.mutedBroadcasts = mutedBroadcasts;
        this.version = version;
    }

    public void setCreatedCircles(int createdCircles) {
        this.createdCircles = createdCircles;
    }

    public HashMap<String, Boolean> getActiveCircles() {
        return activeCircles;
    }

    public void setActiveCircles(HashMap<String, Boolean> activeCircles) {
        this.activeCircles = activeCircles;
    }

    public int getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public int getCreatedCircles() {
        return createdCircles;
    }

    public String getProfileImageLink() {
        return profileImageLink;
    }

    public void setProfileImageLink(String profileImageLink) {
        this.profileImageLink = profileImageLink;
    }
    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public HashMap<String, Integer> getNotificationsAlert() {
        return notificationsAlert;
    }

    public void setNotificationsAlert(HashMap<String, Integer> notificationsAlert) {
        this.notificationsAlert = notificationsAlert;
    }

    public HashMap<String, Long> getNewTimeStampsComments() {
        return newTimeStampsComments;
    }

    public void setNewTimeStampsComments(HashMap<String, Long> newTimeStampsComments) {
        this.newTimeStampsComments = newTimeStampsComments;
    }

    public HashMap<String, Integer> getNoOfReadDiscussions() {
        return noOfReadDiscussions;
    }

    public void setNoOfReadDiscussions(HashMap<String, Integer> noOfReadDiscussions) {
        this.noOfReadDiscussions = noOfReadDiscussions;
    }

    public List<String> getMutedBroadcasts() {
        return mutedBroadcasts;
    }

    public void setMutedBroadcasts(List<String> mutedBroadcasts) {
        this.mutedBroadcasts = mutedBroadcasts;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                ", contact='" + contact + '\'' +
                ", profileImageLink='" + profileImageLink + '\'' +
                ", userId='" + userId + '\'' +
                ", token_id='" + token_id + '\'' +
                ", district='" + district + '\'' +
                ", ward='" + ward + '\'' +
                ", notificationsAlert=" + notificationsAlert +
                ", noOfReadDiscussions=" + noOfReadDiscussions +
                ", newTimeStampsComments=" + newTimeStampsComments +
                ", listeningBroadcasts=" + mutedBroadcasts +
                ", createdCircles=" + createdCircles +
                ", activeCircles=" + activeCircles +
                ", completedProjects=" + completedProjects +
                ", version=" + version +
                '}';
    }
}