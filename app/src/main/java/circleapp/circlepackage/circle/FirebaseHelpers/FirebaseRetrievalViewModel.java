package circleapp.circlepackage.circle.FirebaseHelpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.Arrays;

public class FirebaseRetrievalViewModel extends ViewModel {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");

    @NonNull
    public LiveData<String[]> getDataSnapsExploreCircleLiveData(String location) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild("circleDistrict").equalTo(location));
        return liveExploreCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsAllCircleLiveData() {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild("circleDistrict"));
        return liveExploreCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsWorkbenchCircleLiveData(String userId) {
        String childQueryPath = "membersList/" + userId;
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild(childQueryPath).equalTo(true));
        return liveWorkBenchCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsSharedLinkCircleLiveData(String circleId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_REF.child(circleId.trim()));
        return liveWorkBenchCircleData;
    }

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(BROADCASTS_REF.child(circleId));
        return liveExploreCircleData;
    }



    @NonNull
    public LiveData<String[]> getDataSnapsNotificationsLiveData(String userId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(NOTIFS_REF.child(userId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }

}