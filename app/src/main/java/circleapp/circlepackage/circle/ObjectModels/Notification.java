package circleapp.circlepackage.circle.ObjectModels;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String circleName, circleId, from,notify_to, state, date;
    private Long timestamp;

    public Notification () {

    }

    @Override
    public String toString() {
        return "Notification{" +
                "projectName='" + circleName + '\'' +
                ", projectId='" + circleId + '\'' +
                ", from='" + from + '\'' +
                ", notify_to='" + notify_to + '\'' +
                ", state='" + state + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    public Notification(String circleName, String circleId, String from,String notify_to, String state, Long timestamp, String date) {
        this.circleName = circleName;
        this.circleId = circleId;
        this.from = from;
        this.state = state;
        this.timestamp = timestamp;
        this.date = date;
        this.notify_to = notify_to;
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
    }

    private Notification(Parcel in) {
        circleName = in.readString();
        circleId = in.readString();
        from = in.readString();
        notify_to = in.readString();
        state = in.readString();
        timestamp = in.readLong();
        date = in.readString();
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
