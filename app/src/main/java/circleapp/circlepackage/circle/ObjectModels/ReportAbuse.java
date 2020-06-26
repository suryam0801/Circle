package circleapp.circlepackage.circle.ObjectModels;

public class ReportAbuse {
    String reportAbuseID, contentType, creatorID, userID;

    public String getReportAbuseId() {
        return reportAbuseID;
    }

    public void setReportAbuseId(String reportAbuseId) {
        this.reportAbuseID = reportAbuseId;
    }

    public ReportAbuse(String reportAbuseId, String contentType, String creatorID, String userID){
        this.reportAbuseID = reportAbuseId;
        this.contentType = contentType;
        this.creatorID = creatorID;
        this.userID = userID;
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

    public String getReportAbuseID() {
        return reportAbuseID;
    }

    public void setReportAbuseID(String reportAbuseID) {
        this.reportAbuseID = reportAbuseID;
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
