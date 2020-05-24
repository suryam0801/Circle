package circleapp.circlepackage.circle.ObjectModels;

import java.util.HashMap;
import java.util.List;

public class User {

    private String firstName, lastName, contact, profileImageLink, userId, token_id;
    private HashMap<String, Boolean> locationTags, interestTags;
    private int createdProjects, workingProjects, completedProjects;

    public User(){

    }

    public User(String firstName, String lastName, String contact,
                String profileImageLink, HashMap<String, Boolean> locationTags,
                HashMap<String, Boolean> interestTags, String userId, int createdProjects,
                int workingProjects, int completedProjects, String token_id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.profileImageLink = profileImageLink;
        this.locationTags = locationTags;
        this.interestTags = interestTags;
        this.userId = userId;
        this.createdProjects = createdProjects;
        this.workingProjects = workingProjects;
        this.completedProjects = completedProjects;
        this.token_id = token_id;
    }

    public HashMap<String, Boolean> getLocationTags() {
        return locationTags;
    }

    public void setLocationTags(HashMap<String, Boolean> locationTags) {
        this.locationTags = locationTags;
    }

    public HashMap<String, Boolean> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(HashMap<String, Boolean> interestTags) {
        this.interestTags = interestTags;
    }

    public void setCreatedProjects(int createdProjects) {
        this.createdProjects = createdProjects;
    }

    public int getWorkingProjects() {
        return workingProjects;
    }

    public void setWorkingProjects(int workingProjects) {
        this.workingProjects = workingProjects;
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

    public int getCreatedProjects() {
        return createdProjects;
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


    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\n' +
                ", lastName='" + lastName + '\n' +
                ", contact='" + contact + '\n' +
                ", profileImageLink='" + profileImageLink + '\n' +
                ", locationTags=" + locationTags.toString() + '\n' +
                ", interestTags=" + interestTags.toString() + '\n' +
                ", userId='" + userId + '\n' +
                ", createdProjects=" + createdProjects + '\n' +
                ", workingProjects=" + workingProjects + '\n' +
                ", completedProjects=" + completedProjects + '\n' +
                ", token_id='" + token_id + '\n' +
                '}';
    }

}