package circleapp.circlepackage.circle.Model.ObjectModels;

import java.util.HashMap;

public class Circle {
    private String id, name, description, acceptanceType, visibility, creatorID, creatorName, circleDistrict, circleWard="", category, backgroundImageLink;
    private HashMap<String, Boolean> membersList, applicantsList;
    private long timestamp;
    private int noOfBroadcasts, noOfNewDiscussions;
    private boolean adminVisibility;

    public Circle() {
    }

    public Circle(String id, String name, String description, String acceptanceType, String visibility, String creatorID,
                  String creatorName, String category, String backgroundImageLink,
                  HashMap<String, Boolean> membersList, HashMap<String, Boolean> applicantsList,
                  String circleDistrict, String circleWard, long timestamp, int noOfBroadcasts, int noOfNewDiscussions, boolean adminVisibility) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backgroundImageLink = backgroundImageLink;
        this.acceptanceType = acceptanceType;
        this.visibility = visibility;
        this.creatorID = creatorID;
        this.creatorName = creatorName;
        this.category = category;
        this.membersList = membersList;
        this.applicantsList = applicantsList;
        this.circleDistrict = circleDistrict;
        this.circleWard = circleWard;
        this.timestamp = timestamp;
        this.noOfBroadcasts = noOfBroadcasts;
        this.noOfNewDiscussions = noOfNewDiscussions;
        this.adminVisibility = adminVisibility;
    }
    public boolean isAdminVisibility() {
        return adminVisibility;
    }

    public void setAdminVisibility(boolean adminVisibility) {
        this.adminVisibility = adminVisibility;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public HashMap<String, Boolean> getMembersList() {
        return membersList;
    }

    public void setMembersList(HashMap<String, Boolean> membersList) {
        this.membersList = membersList;
    }

    public HashMap<String, Boolean> getApplicantsList() {
        return applicantsList;
    }

    public void setApplicantsList(HashMap<String, Boolean> applicantsList) {
        this.applicantsList = applicantsList;
    }

    public String getCircleDistrict() {
        return circleDistrict;
    }

    public void setCircleDistrict(String circleDistrict) {
        this.circleDistrict = circleDistrict;
    }

    public String getCircleWard() {
        return circleWard;
    }

    public void setCircleWard(String circleWard) {
        this.circleWard = circleWard;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNoOfBroadcasts() {
        return noOfBroadcasts;
    }

    public void setNoOfBroadcasts(int noOfBroadcasts) {
        this.noOfBroadcasts = noOfBroadcasts;
    }

    public int getNoOfNewDiscussions() {
        return noOfNewDiscussions;
    }

    public void setNoOfNewDiscussions(int noOfNewDiscussions) {
        this.noOfNewDiscussions = noOfNewDiscussions;
    }

    public String getBackgroundImageLink() {
        return backgroundImageLink;
    }

    public void setBackgroundImageLink(String backgroundImageLink) {
        this.backgroundImageLink = backgroundImageLink;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", acceptanceType='" + acceptanceType + '\'' +
                ", visibility='" + visibility + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", circleDistrict='" + circleDistrict + '\'' +
                ", circleWard='" + circleWard + '\'' +
                ", category='" + category + '\'' +
                ", backgroundImageLink='" + backgroundImageLink + '\'' +
                ", membersList=" + membersList +
                ", applicantsList=" + applicantsList +
                ", timestamp=" + timestamp +
                ", noOfBroadcasts=" + noOfBroadcasts +
                ", noOfNewDiscussions=" + noOfNewDiscussions +
                ", adminVisibility=" + adminVisibility +
                '}';
    }
}
