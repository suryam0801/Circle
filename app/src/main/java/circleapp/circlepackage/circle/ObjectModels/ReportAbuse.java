package circleapp.circlepackage.circle.ObjectModels;

public class ReportAbuse {
    String reportAbuseID, circleID, creatorID, userID;

    public String getReportAbuseId() {
        return reportAbuseID;
    }

    public void setReportAbuseId(String reportAbuseId) {
        this.reportAbuseID = reportAbuseId;
    }

    public ReportAbuse(String reportAbuseId, String circleId, String creatorID, String userID){
        this.reportAbuseID = reportAbuseId;
        this.circleID = circleId;
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

    public String getCircleId() {
        return circleID;
    }

    public void setCircleId(String circleId) {
        this.circleID = circleId;
    }
    @Override
    public String toString() {
        return "ReportAbuse{" +
                "reportAbuseID='" + reportAbuseID + '\'' +
                ", circleID='" + circleID + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

}
