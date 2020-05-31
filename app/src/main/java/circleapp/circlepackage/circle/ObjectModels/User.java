package circleapp.circlepackage.circle.ObjectModels;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.HashMap;

//import circleapp.circlepackage.circle.LocalDatabase.Converters;

//@Entity(tableName = "users")
public class User {

//    @PrimaryKey
    private String userId;

    private String firstName, lastName, contact, profileImageLink,  token_id, district, ward;

//    @TypeConverter({Converters.class})
    private HashMap<String, Boolean> locationTags;
    private HashMap<String, Boolean> interestTags;

    private int createdCircles, activeCircles, completedProjects;


    public User(){

    }

    public User(String firstName, String lastName, String contact,
                String profileImageLink, HashMap<String, Boolean> locationTags,
                HashMap<String, Boolean> interestTags, String userId, int createdCircles,
                int activeCircles, int completedProjects, String token_id, String ward, String district) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
        this.profileImageLink = profileImageLink;
        this.locationTags = locationTags;
        this.interestTags = interestTags;
        this.userId = userId;
        this.createdCircles = createdCircles;
        this.activeCircles = activeCircles;
        this.completedProjects = completedProjects;
        this.token_id = token_id;
        this.ward = ward;
        this.district = district;
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
                ", locationTags=" + locationTags +
                ", interestTags=" + interestTags +
                ", createdCircles=" + createdCircles +
                ", activeCircles=" + activeCircles +
                ", completedProjects=" + completedProjects +
                '}';
    }
}