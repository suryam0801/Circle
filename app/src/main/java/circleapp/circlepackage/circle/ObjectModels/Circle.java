package circleapp.circlepackage.circle.ObjectModels;

import java.util.List;

public class Circle {
    private String id, name, description, acceptanceType, creatorID, creatorName;
    private List<String> locationTags, interestTags;

    public Circle(){

    }

    public Circle(String id, String name, String description, String acceptanceType,
                  String creatorID, String creatorName, List<String> locationTags,
                  List<String> interestTags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.acceptanceType = acceptanceType;
        this.creatorID = creatorID;
        this.creatorName = creatorName;
        this.locationTags = locationTags;
        this.interestTags = interestTags;
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

    public List<String> getLocationTags() {
        return locationTags;
    }

    public void setLocationTags(List<String> locationTags) {
        this.locationTags = locationTags;
    }

    public List<String> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(List<String> interestTags) {
        this.interestTags = interestTags;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", acceptanceType='" + acceptanceType + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", locationTags=" + locationTags +
                ", interestTags=" + interestTags +
                '}';
    }
}
