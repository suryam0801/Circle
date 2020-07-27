package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class MyCirclesViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsParticularCircleLiveData(String circleId) {
        FirebaseSingleValueRead liveparticularCircleData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Circles").child(circleId));
        return liveparticularCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsWorkbenchCircleLiveData(String userId) {
        String childQueryPath = "membersList/" + userId;
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Circles").orderByChild(childQueryPath).equalTo(true));
        return liveWorkBenchCircleData;
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsCircleSingleValueLiveData(String circleId) {
        FirebaseSingleValueRead liveWorkBenchCircleData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Circles").child(circleId));
        return liveWorkBenchCircleData;
    }
}
