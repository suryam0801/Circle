package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;

public class MyCirclesViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsParticularCircleLiveData(String circleId) {
        FirebaseSingleValueRead liveparticularCircleData = new FirebaseSingleValueRead(CIRCLES_REF.child(circleId));
        return liveparticularCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsWorkbenchCircleLiveData(String userId) {
        String childQueryPath = "membersList/" + userId;
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild(childQueryPath).equalTo(true));
        return liveWorkBenchCircleData;
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsCircleSingleValueLiveData(String circleId) {
        FirebaseSingleValueRead liveWorkBenchCircleData = new FirebaseSingleValueRead(CIRCLES_REF.child(circleId));
        return liveWorkBenchCircleData;
    }
}
