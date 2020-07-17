package circleapp.circlepackage.circle.data.ViewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;

public class NotificationsViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");

    @NonNull
    public LiveData<String[]> getDataSnapsNotificationsLiveData(String userId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(NOTIFS_REF.child(userId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }

}
