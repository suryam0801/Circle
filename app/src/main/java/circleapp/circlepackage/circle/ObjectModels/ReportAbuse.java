package circleapp.circlepackage.circle.ObjectModels;

public class ReportAbuse {
    String reportAbuseID, contentType, contentID, creatorID, userID;

    public ReportAbuse() {
    }

    public ReportAbuse(String reportAbuseId, String contentType, String contentID, String creatorID, String userID){
        this.reportAbuseID = reportAbuseId;
        this.contentType = contentType;
        this.contentID = contentID;
        this.creatorID = creatorID;
        this.userID = userID;
    }
    public String getContentID() { return contentID; }

    public void setContentID(String contentID) { this.contentID = contentID; }
    public String getReportAbuseId() {
        return reportAbuseID;
    }

    public void setReportAbuseId(String reportAbuseId) {
        this.reportAbuseID = reportAbuseId;
    }
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "ReportAbuse{" +
                "reportAbuseID='" + reportAbuseID + '\'' +
                ", contentType='" + contentType + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

}
