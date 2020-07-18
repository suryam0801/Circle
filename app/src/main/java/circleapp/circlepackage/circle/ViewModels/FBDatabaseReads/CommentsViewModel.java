package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;

public class CommentsViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference COMMENTS_REF = database.getReference("BroadcastComments");

    @NonNull
    public LiveData<String[]> getDataSnapsCommentsLiveData(String circleId, String broadcastId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(COMMENTS_REF.child(circleId).child(broadcastId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }
}
