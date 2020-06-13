package circleapp.circlepackage.circle.ObjectModels;

import java.util.HashMap;

public class User  {


    private String firstName, lastName, contact, profileImageLink, userId, token_id, district, ward;
    private HashMap<String, Boolean> interestTags;
    private HashMap<String, Integer> notificationsAlert;
    private int createdCircles, activeCircles, completedProjects;


    public User(){

    }

    public User(String firstName, String lastName, String contact,
                String profileImageLink, HashMap<String, Boolean> interestTags, String userId,
                int createdCircles, int activeCircles, int completedProjects, String token_id,
                String ward, String district, HashMap<String, Integer> notificationsAlert) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.profileImageLink = profileImageLink;
        this.interestTags = interestTags;
        this.userId = userId;
        this.createdCircles = createdCircles;
        this.activeCircles = activeCircles;
        this.completedProjects = completedProjects;
        this.token_id = token_id;
        this.ward = ward;
        this.district = district;
        this.notificationsAlert = notificationsAlert;
    }

    public HashMap<String, Boolean> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(HashMap<String, Boolean> interestTags) {
        this.interestTags = interestTags;
    }

    public void setCreatedCircles(int createdCircles) {
        this.createdCircles = createdCircles;
    }

    public int getActiveCircles() {
        return activeCircles;
    }

    public void setActiveCircles(int activeCircles) {
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
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", contact='" + contact + '\'' +
                ", profileImageLink='" + profileImageLink + '\'' +
                ", userId='" + userId + '\'' +
                ", token_id='" + token_id + '\'' +
                ", district='" + district + '\'' +
                ", ward='" + ward + '\'' +
                ", interestTags=" + interestTags +
                ", notificationsAlert=" + notificationsAlert +
                ", createdCircles=" + createdCircles +
                ", activeCircles=" + activeCircles +
                ", completedProjects=" + completedProjects +
                '}';
    }
}