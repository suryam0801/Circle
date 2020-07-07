package circleapp.circlepackage.circle.FirebaseHelpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.Arrays;

public class FirebaseRetrievalViewModel extends ViewModel {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final DatabaseReference CIRCLES_REF = database.getReference("/Circles");
    private static final DatabaseReference NOTIFS_REF = database.getReference("/Notifications");
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");
    private static final DatabaseReference CIRCLES_PERSONEL_REF = database.getReference("/CirclePersonel");
    private static final DatabaseReference USERS_REF = database.getReference("/Users");
    private static final DatabaseReference COMMENTS_REF = database.getReference("BroadcastComments");
    private static final DatabaseReference LOCATIONS_REF = database.getReference("Locations");
    private static final DatabaseReference REPORT_ABUSE_REF = database.getReference("ReportAbuse");
    private static final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private static final DatabaseReference USER_FEEDBACK_REF = database.getReference("UserFeedback");

    @NonNull
    public LiveData<String[]> getDataSnapsExploreCircleLiveData(String location) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(CIRCLES_REF.orderByChild("circleDistrict").equalTo(location));
        return liveExploreCircleData;
    }

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
    public LiveData<String[]> getDataSnapsCommentsLiveData(String circleId, String broadcastId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(COMMENTS_REF.child(circleId).child(broadcastId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }


    @NonNull
    public LiveData<String[]> getDataSnapsCirclePersonelLiveData(String circleId, String membersOrApplicants) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_PERSONEL_REF.child(circleId).child(membersOrApplicants));
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

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsCircleValueCirlceLiveData(String circleId) {
        FirebaseSingleValueRead liveWorkBenchCircleData = new FirebaseSingleValueRead(CIRCLES_REF.child(circleId));
        return liveWorkBenchCircleData;
    }
    @NonNull
    public LiveData<DataSnapshot> getDataSnapsUserValueCirlceLiveData(String uid) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(USERS_REF.child(uid));
        return liveUserData;
    }

}