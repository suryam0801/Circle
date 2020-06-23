package circleapp.circlepackage.circle.ObjectModels;

public class Feedback {
    String userId,userName,feedback,feedbackId;
    private long timestamp;

    public Feedback(){}

    public Feedback(String userId, String userName, String feedback, String feedbackId, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.feedback = feedback;
        this.feedbackId = feedbackId;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
