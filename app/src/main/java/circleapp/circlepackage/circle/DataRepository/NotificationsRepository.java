package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.Query;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class NotificationsRepository extends FirebaseQueryLiveData {
    private GlobalVariables globalVariables = new GlobalVariables();

    public NotificationsRepository(Query query) {
        super(query);
    }
    public NotificationsRepository(){
        super("/Notifications");
    }

    @NonNull
    public LiveData<String[]> getDataSnapsNotificationsLiveData(String userId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Notifications").child(userId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }

}
