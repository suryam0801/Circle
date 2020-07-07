package circleapp.circlepackage.circle.ObjectModels;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String circleName,creatorId, circleId, from,notify_to, state, date,broadcastId,circleIcon,type, message;
    private Long timestamp;

    public Notification () {

    }

    @Override
    public String toString() {
        return "Notification{" +
                "projectName='" + circleName + '\'' +
                "creatorId='" + creatorId + '\'' +
                ", projectId='" + circleId + '\'' +
                ", from='" + from + '\'' +
                ", notify_to='" + notify_to + '\'' +
                ", state='" + state + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", date='" + date + '\'' +
                ", broadcastId='" + broadcastId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }


    public Notification(String circleName,String creatorId, String circleId, String from, String notify_to, String state, Long timestamp, String date, String broadcastId, String circleIcon, String type, String message) {
        this.circleName = circleName;
        this.circleId = circleId;
        this.creatorId = creatorId;
        this.from = from;
        this.state = state;
        this.timestamp = timestamp;
        this.date = date;
        this.notify_to = notify_to;
        this.broadcastId = broadcastId;
        this.circleIcon = circleIcon;
        this.type = type;
        this.message = message;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCircleIcon() {
        return circleIcon;
    }

    public void setCircleIcon(String circleIcon) {
        this.circleIcon = circleIcon;
    }

    public String getNotify_to() {
        return notify_to;
    }

    public void setNotify_to(String notify_to) {
        this.notify_to = notify_to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(circleName);
        parcel.writeString(circleId);
        parcel.writeString(from);
        parcel.writeString(notify_to);
        parcel.writeString(state);
        parcel.writeLong(timestamp);
        parcel.writeString(date);
        parcel.writeString(broadcastId);
    }

    private Notification(Parcel in) {
        circleName = in.readString();
        circleId = in.readString();
        from = in.readString();
        notify_to = in.readString();
        state = in.readString();
        timestamp = in.readLong();
        date = in.readString();
        broadcastId = in.readString();
    }

    public static final Parcelable.Creator<Notification> CREATOR
            = new Parcelable.Creator<Notification>() {
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}
