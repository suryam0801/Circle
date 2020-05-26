package circleapp.circlepackage.circle.ObjectModels;

import java.util.HashMap;
import java.util.List;

public class Circle {
    private String id, name, description, acceptanceType, creatorID, creatorName;
    private HashMap<String, Boolean> locationTags, interestTags, membersList;

    public Circle(){

    }

    public Circle(String id, String name, String description, String acceptanceType, String creatorID,
                  String creatorName, HashMap<String, Boolean> locationTags, HashMap<String, Boolean> interestTags,
                  HashMap<String, Boolean> membersList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.acceptanceType = acceptanceType;
        this.creatorID = creatorID;
        this.creatorName = creatorName;
        this.locationTags = locationTags;
        this.interestTags = interestTags;
        this.membersList = membersList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAcceptanceType() {
        return acceptanceType;
    }

    public void setAcceptanceType(String acceptanceType) {
        this.acceptanceType = acceptanceType;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
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

    public HashMap<String, Boolean> getMembersList() {
        return membersList;
    }

    public void setMembersList(HashMap<String, Boolean> membersList) {
        this.membersList = membersList;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "id='" + id + '\n' +
                ", name='" + name + '\n' +
                ", description='" + description + '\n' +
                ", acceptanceType='" + acceptanceType + '\n' +
                ", creatorID='" + creatorID + '\n' +
                ", creatorName='" + creatorName + '\n' +
                ", locationTags=" + locationTags + '\n' +
                ", interestTags=" + interestTags + '\n' +
                ", membersList=" + membersList +
                '}';
    }
}
