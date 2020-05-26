package circleapp.circlepackage.circle.ObjectModels;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    protected Notification(Parcel in) {
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
