package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circleapppackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class NotificationsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsNotificationsLiveData(String userId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Notifications").child(userId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }

}
