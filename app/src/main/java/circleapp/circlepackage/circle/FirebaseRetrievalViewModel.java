package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRetrievalViewModel extends ViewModel {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsExploreCircleLiveData(String location) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild("circleDistrict").equalTo(location));
        return liveExploreCircleData;
    }

    public LiveData<DataSnapshot> getDataSnapsWorkbenchCircleLiveData(String userId) {
        String childQueryPath = "membersList/" + userId;
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild(childQueryPath).equalTo(true));
        return liveWorkBenchCircleData;
    }
}