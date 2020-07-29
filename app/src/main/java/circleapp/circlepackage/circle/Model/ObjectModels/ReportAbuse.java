package circleapp.circlepackage.circle.Model.ObjectModels;

public class ReportAbuse {
    String reportAbuseID ,circleID,broadcastID, commentID, creatorID, userID, reportType;

    public ReportAbuse() {
    }


    public ReportAbuse(String reportAbuseId, String circleID, String broadCastID , String commentID, String creatorID, String userID, String reportType){
        this.reportAbuseID = reportAbuseId;
        this.circleID = circleID;
        this.broadcastID = broadCastID;
        this.commentID = commentID;
        this.creatorID = creatorID;
        this.userID = userID;
        this.reportType = reportType;
    }

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

    public String getCircleID() { return circleID; }

    public void setCircleID(String circleID) { this.circleID = circleID; }

    public String getBroadcastID() { return broadcastID; }

    public void setBroadcastID(String broadcastID) { this.broadcastID = broadcastID; }

    public String getCommentID() { return commentID; }

    public void setCommentID(String commentID) { this.commentID = commentID; }

    public String getReportType() { return reportType; }

    public void setReportType(String reportType) { this.reportType = reportType; }

    @Override
    public String toString() {
        return "ReportAbuse{" +
                "reportAbuseID='" + reportAbuseID + '\'' +
                ", circleID='" + circleID + '\'' +
                ", broadcastID='" + broadcastID + '\'' +
                ", commentID='" + commentID + '\'' +
                ", creatorID='" + creatorID + '\'' +
                ", userID='" + userID + '\'' +
                '}';
    }

}
