package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.Query;

import circleapp.circlepackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CommentsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsInitialLoadCommentsLiveData(String circleId, String broadcastId, int mCurrentPage, int TOTAL_ITEMS_TO_LOAD) {

        Query messageQuery = globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(messageQuery);
        return liveWorkBenchCircleData;
    }
    @NonNull
    public LiveData<String[]> getDataSnapsLoadMoreCommentsLiveData(String circleId, String broadcastId, String mLastKey, int limit) {
        Query messageQuery = globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).orderByKey().endAt(mLastKey).limitToLast(limit);
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(messageQuery);
        return liveWorkBenchCircleData;
    }

}
